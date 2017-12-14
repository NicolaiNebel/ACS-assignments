package com.acertainbookstore.business;

import java.util.Set;

import com.acertainbookstore.interfaces.ReplicatedReadOnlyBookStore;
import com.acertainbookstore.interfaces.ReplicatedReadOnlyStockManager;
import com.acertainbookstore.utils.BookStoreException;
import com.acertainbookstore.utils.BookStoreResult;

/**
 * {@link ReadOnlyCertainBookStore} defines a read only bookstore.
 */
public class ReadOnlyCertainBookStore implements ReplicatedReadOnlyBookStore, ReplicatedReadOnlyStockManager {

	/** The book store. */
	protected CertainBookStore bookStore = null;

	/** The snapshot id. */
	protected long snapshotId = 0;

	/**
	 * Instantiates a new read only certain book store.
	 */
	public ReadOnlyCertainBookStore() {
		bookStore = new CertainBookStore();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.acertainbookstore.interfaces.ReplicatedReadOnlyStockManager#getBooks(
	 * )
	 */
	public synchronized BookStoreResult getBooks() throws BookStoreException {
		return new BookStoreResult(bookStore.getBooks(), snapshotId);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.acertainbookstore.interfaces.ReplicatedReadOnlyStockManager#
	 * getBooksInDemand()
	 */
	public synchronized BookStoreResult getBooksInDemand() throws BookStoreException {
		throw new BookStoreException("Not implemented");
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.acertainbookstore.interfaces.ReplicatedReadOnlyBookStore#getBooks(
	 * java.util.Set)
	 */
	public synchronized BookStoreResult getBooks(Set<Integer> isbnList) throws BookStoreException {
		return new BookStoreResult(bookStore.getBooks(isbnList), snapshotId);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.acertainbookstore.interfaces.ReplicatedReadOnlyBookStore#
	 * getTopRatedBooks(int)
	 */
	public synchronized BookStoreResult getTopRatedBooks(int numBooks) throws BookStoreException {
		throw new BookStoreException("Not implemented");
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.acertainbookstore.interfaces.ReplicatedReadOnlyBookStore#
	 * getEditorPicks(int)
	 */
	public synchronized BookStoreResult getEditorPicks(int numBooks) throws BookStoreException {
		return new BookStoreResult(bookStore.getEditorPicks(numBooks), snapshotId);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.acertainbookstore.interfaces.ReplicatedReadOnlyStockManager#
	 * getBooksByISBN(java.util.Set)
	 */
	public synchronized BookStoreResult getBooksByISBN(Set<Integer> isbns) throws BookStoreException {
		return new BookStoreResult(bookStore.getBooksByISBN(isbns), snapshotId);
	}
}
