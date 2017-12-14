package com.acertainbookstore.interfaces;

import java.util.Set;

import com.acertainbookstore.utils.BookStoreException;
import com.acertainbookstore.utils.BookStoreResult;

/**
 * {@link ReplicatedReadOnlyBookStore} declares a set of read only methods
 * conforming to the {@link BookStore} interface exposed by the bookstore to the
 * proxies. These methods need to be implemented by
 * {@link SlaveCertainBookStore}.
 */
public interface ReplicatedReadOnlyBookStore {

	/**
	 * Returns the list of books corresponding to the set of ISBNs.
	 *
	 * @param isbnList
	 *            the isbn list
	 * @return the books
	 * @throws BookStoreException
	 *             the book store exception
	 */
	public BookStoreResult getBooks(Set<Integer> isbnList) throws BookStoreException;

	/**
	 * Return a list of top rated numBooks books.
	 *
	 * @param numBooks
	 *            the num books
	 * @return the top rated books
	 * @throws BookStoreException
	 *             the book store exception
	 */
	public BookStoreResult getTopRatedBooks(int numBooks) throws BookStoreException;

	/**
	 * Returns the list of books containing numBooks editor picks.
	 *
	 * @param numBooks
	 *            the num books
	 * @return the editor picks
	 * @throws BookStoreException
	 *             the book store exception
	 */
	public BookStoreResult getEditorPicks(int numBooks) throws BookStoreException;
}
