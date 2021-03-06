//
// $Id$

package client.adminz;

import com.google.gwt.core.client.GWT;

import com.threerings.msoy.admin.gwt.AdminService;
import com.threerings.msoy.admin.gwt.AdminServiceAsync;

import client.ui.DataPanel;

/**
 * A panel that displays some data after loading.
 */
public abstract class AdminDataPanel<T> extends DataPanel<T>
{
    protected AdminDataPanel (String styleName)
    {
        super(styleName);
    }

    protected static final AdminMessages _msgs = GWT.create(AdminMessages.class);
    protected static final AdminServiceAsync _adminsvc = GWT.create(AdminService.class);
}
