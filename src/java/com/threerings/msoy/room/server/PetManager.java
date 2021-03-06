//
// $Id$

package com.threerings.msoy.room.server;

import java.util.List;
import java.util.Map;
import java.util.Set;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.google.inject.Inject;
import com.google.inject.Injector;
import com.google.inject.Singleton;

import com.samskivert.util.Invoker;

import com.samskivert.jdbc.RepositoryUnit;

import com.threerings.presents.annotation.EventThread;
import com.threerings.presents.annotation.MainInvoker;
import com.threerings.presents.data.ClientObject;
import com.threerings.presents.server.InvocationException;
import com.threerings.presents.server.InvocationManager;

import com.threerings.crowd.chat.data.ChatCodes;
import com.threerings.crowd.chat.server.SpeakUtil;
import com.threerings.crowd.data.OccupantInfo;
import com.threerings.crowd.server.PlaceManager;
import com.threerings.crowd.server.PlaceRegistry;

import com.threerings.whirled.server.SceneRegistry;

import com.threerings.orth.room.client.PetService;
import com.threerings.orth.room.data.PetMarshaller;
import com.threerings.orth.room.server.PetProvider;

import com.threerings.msoy.data.MemberObject;
import com.threerings.msoy.data.MsoyCodes;
import com.threerings.msoy.item.data.all.MsoyItemType;
import com.threerings.msoy.item.data.all.Pet;
import com.threerings.msoy.item.server.persist.PetRecord;
import com.threerings.msoy.item.server.persist.PetRepository;
import com.threerings.msoy.peer.server.MsoyPeerManager;
import com.threerings.msoy.room.data.EntityMemories;
import com.threerings.msoy.room.data.PetCodes;
import com.threerings.msoy.room.data.PetInfo;
import com.threerings.msoy.room.data.PetObject;
import com.threerings.msoy.room.data.RoomObject;
import com.threerings.msoy.room.server.persist.MemoriesRecord;
import com.threerings.msoy.room.server.persist.MemoryRepository;
import com.threerings.msoy.server.MemberLocator;

import static com.threerings.msoy.Log.log;

/**
 * Takes care of loading, unloading and handling of Pets.
 */
@Singleton @EventThread
public class PetManager
    implements PetProvider
{
    @Inject public PetManager (InvocationManager invmgr, MemberLocator locator,
                               MsoyPeerManager peerMan)
    {
        // register our pet invocation services
        invmgr.registerProvider(this, PetMarshaller.class, MsoyCodes.WORLD_GROUP);

        // register a member forward participant that handles walked pets
        peerMan.memberFwdObs.add(new MsoyPeerManager.MemberForwardObserver() {
            public void memberWillBeSent (String nodeName, MemberObject memobj) {
                PetObject petobj = getPetObject(memobj.walkingId);
                if (petobj != null) {
                    // extract our memories from the room we're in
                    _sceneReg.leaveOccupiedScene(petobj);
                    // stuff our pet's data into a member object local attribute
                    PetLocal local = new PetLocal();
                    local.pet = petobj.pet;
                    local.memories = petobj.memories;
                    // TODO: what about the memories in the pet object now?
                    memobj.setLocal(PetLocal.class, local);
                    // the pet will shutdown later when the walking member is destroyed
                }
            }
        });

        // we want to hear when members logon so that we can unpickle forwarded pets
        locator.addObserver(new MemberLocator.Observer() {
            public void memberLoggedOn (MemberObject memobj) {
                PetLocal local = memobj.getLocal(PetLocal.class);
                if (local != null) {
                    memobj.setLocal(PetLocal.class, null);
                    createHandler(memobj, local.pet, local.memories, false);
                }
            }
            public void memberLoggedOff (MemberObject memobj) {
                // nada
            }
        });
    }

    /**
     * Returns the distributed object for the specified pet or null if the pet is not resolved.
     */
    public PetObject getPetObject (int petId)
    {
        PetHandler handler = _handlers.get(petId);
        return (handler == null) ? null : handler.getPetObject();
    }

    /**
     * Loads up all pets that are "let out" in the specified room. Any pets that live in this room
     * but are currently being walked will not be added to the room (they are already extant in the
     * world).
     */
    public void loadRoomPets (final RoomObject roomObj, final int sceneId)
    {
        _invoker.postUnit(new RepositoryUnit("loadRoomPets(" + sceneId + ")") {
            public void invokePersist () throws Exception {
                // load up our pets, collect their memory ids and convert them to runtime objs
                Set<Integer> mids = Sets.newHashSet();
                for (PetRecord petrec : _petRepo.loadItemsByLocation(sceneId)) {
                    _pets.add((Pet)petrec.toItem());
                    mids.add(petrec.itemId);
                }

                // next load up their memories
                if (mids.size() > 0) {
                    for (MemoriesRecord memrec : _memoryRepo.loadMemories(MsoyItemType.PET, mids)) {
                        _memories.put(memrec.itemId, memrec.toEntry());
                    }
                }
            }

            public void handleSuccess () {
                resolveRoomPets(sceneId, _pets, _memories);
            }
            public void handleFailure (Exception e) {
                log.warning("Failed to load pets [scene=" + sceneId + "].", e);
            }

            protected List<Pet> _pets = Lists.newArrayList();
            protected Map<Integer, EntityMemories> _memories = Maps.newHashMap();
        });
    }

    /**
     * Called when a room shuts down, unloads any pets that are "let out" in that room.
     */
    public void shutdownRoomPets (RoomObject roomObj)
    {
        for (OccupantInfo info : roomObj.occupantInfo) {
            if (info instanceof PetInfo) {
                PetHandler handler = _handlers.get(((PetInfo)info).getItemIdent().itemId);
                if (handler == null) {
                    log.warning("Failed to locate handler for pet in shutdown room " +
                                "[room=" + roomObj.getOid() + ", oinfo=" + info + "].");
                } else {
                    handler.shutdown(true);
                }
            }
        }
    }

    // from interface PetProvider
    public void callPet (ClientObject caller, final int petId,
                         final PetService.ConfirmListener listener)
        throws InvocationException
    {
        final MemberObject user = _locator.requireMember(caller);

        // if the owner is already walking a pet, they can't call another
        if (user.walkingId != 0) {
            throw new InvocationException(PetCodes.E_ALREADY_WALKING);
        }

        // check to see if the pet is already loaded
        PetHandler handler = _handlers.get(petId);
        if (handler != null) {
            handler.moveToOwner(user);
            listener.requestProcessed();
            return;
        }

        // resolve the pet, then move them to their owner
        _invoker.postUnit(new RepositoryUnit("callPet(" + petId + ")") {
            public void invokePersist () throws Exception {
                // load up the pet's record
                PetRecord petrec = _petRepo.loadItem(petId);
                if (petrec == null) {
                    throw new Exception("callPet() on non-existent pet");
                }
                if (petrec.ownerId != user.getMemberId()) {
                    throw new Exception("Pet handling by non-owner [who=" + user.who() +
                                        ", ownerId=" + petrec.ownerId + "]");
                }
                _pet = (Pet)petrec.toItem();

                // load up its memory
                MemoriesRecord memrec = _memoryRepo.loadMemory(MsoyItemType.PET, petId);
                if (memrec != null) {
                    _memory = memrec.toEntry();
                }
            }

            public void handleSuccess () {
                createHandler(user, _pet, _memory, true);
                listener.requestProcessed();
            }
            public void handleFailure (Exception e) {
                log.warning("Failed to load pet [pet=" + petId + "].", e);
                listener.requestFailed(PetCodes.E_INTERNAL_ERROR);
            }

            protected Pet _pet;
            protected EntityMemories _memory;
        });
    }

    // from interface PetProvider
    public void orderPet (ClientObject caller, int petId, int order,
                          PetService.ConfirmListener listener)
        throws InvocationException
    {
        final MemberObject user = _locator.requireMember(caller);
        PetHandler handler = _handlers.get(petId);
        if (handler == null) {
            log.warning("orderPet() on non-resolved pet [who=" + user.who() +
                        ", pet=" + petId + "].");
            throw new InvocationException(PetCodes.E_INTERNAL_ERROR);
        }

        // pass the buck on to the handler (if it does not throw an invocation exception, then
        // we'll assume the order was processed)
        handler.orderPet(user, order);
        listener.requestProcessed();
    }

    // from interface PetProvider
    public void sendChat (ClientObject caller, int bodyOid, int sceneId, String message,
                          PetService.ConfirmListener listener)
        throws InvocationException
    {
        final MemberObject user = _locator.requireMember(caller);

        // get the manager of the room where we're chatting
        PlaceManager pmgr = _placeReg.getPlaceManager(user.getPlaceOid());
        if (!(pmgr instanceof RoomManager)) {
            log.warning("sendChat() on invalid location [location=" + user.location + "]");
            throw new InvocationException(PetCodes.E_INTERNAL_ERROR);
        }

        // are we in the right scene?
        RoomManager mgr = (RoomManager) pmgr;
        if (mgr.getScene().getId() != sceneId) {
            log.warning("sendChat() requested during a room change; chat will be ignored.");

        } else {
            // get occupant info, make sure the given oid is a pet in the room!
            OccupantInfo info = mgr.getPlaceObject().occupantInfo.get(bodyOid);
            if (!(info instanceof PetInfo)) {
                log.warning("sendChat() on invalid occupant [bodyOid=" + bodyOid +
                            ", loc=" + user.location);
                throw new InvocationException(PetCodes.E_INTERNAL_ERROR);
            }

            // check if the user actually owns the pet
            PetInfo petInfo = (PetInfo)info;
            if (!mgr.ensureEntityControl(user, petInfo.getItemIdent(), "PetManager.sendChat")) {
                log.warning("sendChat() requested by non-owner [who=" + user.who() +
                            ", pet=" + petInfo + "].");
                throw new InvocationException(PetCodes.E_INTERNAL_ERROR);
            }

            // it's in the room, let's chat
            SpeakUtil.sendSpeak(
                mgr.getPlaceObject(), petInfo.username, null, message, ChatCodes.DEFAULT_MODE);
        }

        listener.requestProcessed();
    }

    /**
     * Called after {@link #loadRoomPets} has loaded our persistent pet data to finish the job of
     * pet resolution.
     */
    protected void resolveRoomPets (int sceneId, List<Pet> pets,
                                    Map<Integer, EntityMemories> memories)
    {
        for (Pet pet : pets) {
            // if this pet is already resolved (is wandering around with its owner), skip it (TODO:
            // this becomes more complicated when our pet can wander to other servers)
            if (_handlers.containsKey(pet.itemId)) {
                continue;
            }

            // create and initialize a handler for this pet and start them in this room
            PetHandler handler = _injector.getInstance(PetHandler.class);
            handler.init(pet, memories.get(pet.itemId));
            handler.enterRoom(sceneId);
        }
    }

    /**
     * Finishes the resolution of a pet initiated by {@link #callPet}.
     */
    protected void createHandler (
        MemberObject owner, Pet pet, EntityMemories memories, boolean moveToOwner)
    {
        // instead of doing a bunch of complicated prevention to avoid multiply resolving pets,
        // we'll just get this far and abandon ship; it's not going to happen that often
        if (_handlers.containsKey(pet.itemId)) {
            log.info("createHandler() ignoring repeat resolution. [pet=" + pet.itemId + "].");
            return;
        }

        // create and initialize a handler for this pet
        PetHandler handler = _injector.getInstance(PetHandler.class);
        handler.init(pet, memories);

        try {
            if (moveToOwner) {
                // we're being resolved for the first time, move to our owner
                handler.moveToOwner(owner);
            } else {
                // we're being forwarded from another server, just wire up our listeners
                handler.reinitFollowing(owner);
            }
        } catch (InvocationException ie) {
            log.warning("Resolved pet rejects owner? [pet=" + handler + ", error=" + ie + "].");
            // wow, this can only mean that the owner is not our owner... this shouldn't happen but
            // let's try doing the right thing.
            handler.shutdown(false);
        }
    }

    protected void mapHandler (int itemId, PetHandler handler)
    {
        _handlers.put(itemId, handler);
    }

    protected void clearHandler (int itemId)
    {
        _handlers.remove(itemId);
    }

    /** Maintains a mapping of all pet handlers by item id. */
    protected Map<Integer, PetHandler> _handlers = Maps.newHashMap();

    @Inject protected @MainInvoker Invoker _invoker;
    @Inject protected Injector _injector;
    @Inject protected MemoryRepository _memoryRepo;
    @Inject protected MemberLocator _locator;
    @Inject protected PetRepository _petRepo;
    @Inject protected PlaceRegistry _placeReg;
    @Inject protected SceneRegistry _sceneReg;
}
