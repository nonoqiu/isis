
package org.nakedobjects.distribution.duplex;

import org.nakedobjects.container.configuration.ComponentException;
import org.nakedobjects.distribution.ObjectUpdateMessage;
import org.nakedobjects.distribution.RequestContext;
import org.nakedobjects.object.UpdateNotifier;

import org.apache.log4j.Logger;


public class ServerEndPoint implements ConnectionFromClient {
	private static final Logger LOG = Logger.getLogger(ServerEndPoint.class);
    private ServerListener listener;


    public void broadcast(ObjectUpdateMessage msg) {
    	LOG.debug("Broadcasting update " + msg);
        listener.update(msg);
    }

    public UpdateNotifier getNotifier() {
        return null;
    }

    public void init(RequestContext server) throws ComponentException {
            listener = new ServerListener(server);
            listener.start();
    }

    public void shutdown() {
    	listener.shutdown();
    }
    
    public String toString() {
		return "Duplex connection " + listener;
	}
}


/*
Naked Objects - a framework that exposes behaviourally complete
business objects directly to the user.
Copyright (C) 2000 - 2003  Naked Objects Group Ltd

This program is free software; you can redistribute it and/or modify
it under the terms of the GNU General Public License as published by
the Free Software Foundation; either version 2 of the License, or
(at your option) any later version.

This program is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
GNU General Public License for more details.

You should have received a copy of the GNU General Public License
along with this program; if not, write to the Free Software
Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA

The authors can be contacted via www.nakedobjects.org (the
registered address of Naked Objects Group is Kingsway House, 123 Goldworth
Road, Woking GU21 1NR, UK).
*/