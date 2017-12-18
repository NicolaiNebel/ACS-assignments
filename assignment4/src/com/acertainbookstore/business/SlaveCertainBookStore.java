package com.acertainbookstore.business;

import com.acertainbookstore.interfaces.ReplicatedReadOnlyBookStore;
import com.acertainbookstore.interfaces.ReplicatedReadOnlyStockManager;
import com.acertainbookstore.interfaces.Replication;
import com.acertainbookstore.utils.BookStoreException;

import java.util.Set;

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
	    if (req.getMessageType() == null) {
	    	throw new BookStoreException("Slave received null message tag");
		}

		switch (req.getMessageType()) {
			case ADDBOOKS:
                bookStore.addBooks((Set<StockBook>) req.getDataSet());
                break;
			case REMOVEALLBOOKS:
				bookStore.removeAllBooks();
				break;
			case ADDCOPIES:
				bookStore.addCopies((Set<BookCopy>) req.getDataSet());
				break;
			case BUYBOOKS:
				bookStore.buyBooks((Set<BookCopy>) req.getDataSet());
				break;
			case UPDATEEDITORPICKS:
				bookStore.updateEditorPicks((Set<BookEditorPick>) req.getDataSet());
				break;
			case REMOVEBOOKS:
				bookStore.removeBooks((Set<Integer>) req.getDataSet());
				break;
			default:
				throw new BookStoreException("Invalid message tag for replicate request " + req.getMessageType());
		}

        //Update the snapshotId, return a successful ReplicationResult with no address yet!
		snapshotId += 1;
		return new ReplicationResult("",true);
	}
}