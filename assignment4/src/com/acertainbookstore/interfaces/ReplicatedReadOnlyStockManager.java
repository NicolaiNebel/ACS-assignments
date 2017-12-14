package com.acertainbookstore.interfaces;

import java.util.Set;
import com.acertainbookstore.utils.BookStoreException;
import com.acertainbookstore.utils.BookStoreResult;

/**
 * {@link ReplicatedReadOnlyStockManager} declares a set of read only methods
 * conforming to {@link StockManager} interface exposed by the bookstore to the
 * proxies. These methods need to be implemented by
 * {@link SlaveCertainBookStore}.
 */
public interface ReplicatedReadOnlyStockManager {

	/**
	 * Returns the list of books in the bookstore.
	 *
	 * @return the books
	 * @throws BookStoreException
	 *             the book store exception
	 */
	public BookStoreResult getBooks() throws BookStoreException;

	/**
	 * Returns the books matching the set of ISBNs given, is different to
	 * getBooks in the BookStore interface because of the return type of the
	 * books.
	 *
	 * @param isbns
	 *            the isbns
	 * @return the books by ISBN
	 * @throws BookStoreException
	 *             the book store exception
	 */
	public BookStoreResult getBooksByISBN(Set<Integer> isbns) throws BookStoreException;

	/**
	 * Returns the list of books which has sale miss.
	 *
	 * @return the books in demand
	 * @throws BookStoreException
	 *             the book store exception
	 */
	public BookStoreResult getBooksInDemand() throws BookStoreException;
}
