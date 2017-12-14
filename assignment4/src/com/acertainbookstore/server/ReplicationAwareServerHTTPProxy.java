package com.acertainbookstore.server;

import com.acertainbookstore.business.ReplicationRequest;
import com.acertainbookstore.business.ReplicationResult;
import com.acertainbookstore.interfaces.Replication;
import com.acertainbookstore.utils.BookStoreException;

/**
 * {@link ReplicationAwareServerHTTPProxy} implements the client side code for
 * replicate RPC, invoked by the master bookstore to propagate updates to
 * slaves, there is one proxy for each destination slave server.
 */
public class ReplicationAwareServerHTTPProxy implements Replication {

	/**
	 * Instantiates a new replication aware server HTTP proxy.
	 *
	 * @param destinationServerAddress
	 *            the destination server address
	 */
	public ReplicationAwareServerHTTPProxy(String destinationServerAddress) {
		throw new UnsupportedOperationException();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.acertainbookstore.interfaces.Replication#replicate(com.
	 * acertainbookstore.business.ReplicationRequest)
	 */
	@Override
	public ReplicationResult replicate(ReplicationRequest req) throws BookStoreException {
		throw new BookStoreException("This method needs to be implemented.");
	}

	/**
	 * Stop.
	 */
	public void stop() {
		// Shutdown the client
	}
}
