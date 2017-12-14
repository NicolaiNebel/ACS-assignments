package com.acertainbookstore.interfaces;

import com.acertainbookstore.business.ReplicationRequest;
import com.acertainbookstore.business.ReplicationResult;
import com.acertainbookstore.utils.BookStoreException;

/**
 * {@link Replication} defines the methods that can be invoked by the master
 * bookstore on the slave bookstores.
 */
public interface Replication {
	
	/**
	 * Replicate.
	 *
	 * @param req the req
	 * @return the replication result
	 * @throws BookStoreException the book store exception
	 */
	ReplicationResult replicate(ReplicationRequest req) throws BookStoreException;
}
