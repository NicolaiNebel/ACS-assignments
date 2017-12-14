package com.acertainbookstore.business;

import com.acertainbookstore.interfaces.ReplicatedReadOnlyBookStore;
import com.acertainbookstore.interfaces.ReplicatedReadOnlyStockManager;
import com.acertainbookstore.interfaces.Replication;
import com.acertainbookstore.utils.BookStoreException;

/**
 * {@link SlaveCertainBookStore} is a wrapper over the CertainBookStore class
 * and supports the ReplicatedReadOnlyBookStore and
 * ReplicatedReadOnlyStockManager interfaces.
 * 
 * This class must also handle replication requests sent by the master.
 */
public class SlaveCertainBookStore extends ReadOnlyCertainBookStore
		implements ReplicatedReadOnlyBookStore, ReplicatedReadOnlyStockManager, Replication {

	/**
	 * Instantiates a new slave certain book store.
	 */
	public SlaveCertainBookStore() {
		bookStore = new CertainBookStore();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.acertainbookstore.interfaces.Replication#replicate(com.
	 * acertainbookstore.business.ReplicationRequest)
	 */
	@Override
	public synchronized ReplicationResult replicate(ReplicationRequest req) throws BookStoreException {
		throw new BookStoreException("This method needs to be implemented.");
	}
}