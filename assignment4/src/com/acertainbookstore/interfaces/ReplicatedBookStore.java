package com.acertainbookstore.interfaces;

import java.util.Set;

import com.acertainbookstore.business.BookCopy;
import com.acertainbookstore.business.BookRating;
import com.acertainbookstore.utils.BookStoreException;
import com.acertainbookstore.utils.BookStoreResult;

/**
 * {@link ReplicatedBookStore} declares a set of methods conforming to the
 * {@link BookStore} interface exposed by the bookstore to the proxies. These
 * methods need to be implemented by {@link MasterCertainBookStore}.
 */
public interface ReplicatedBookStore extends ReplicatedReadOnlyBookStore {

	/**
	 * Buy the sets of books specified.
	 *
	 * @param booksToBuy
	 *            the books to buy
	 * @return the book store result
	 * @throws BookStoreException
	 *             the book store exception
	 */
	public BookStoreResult buyBooks(Set<BookCopy> booksToBuy) throws BookStoreException;

	/**
	 * Applies the BookRatings in the set, i.e. rates each book with their
	 * respective rating.
	 *
	 * @param bookRating
	 *            the book rating
	 * @return the book store result
	 * @throws BookStoreException
	 *             the book store exception
	 */
	public BookStoreResult rateBooks(Set<BookRating> bookRating) throws BookStoreException;
}
