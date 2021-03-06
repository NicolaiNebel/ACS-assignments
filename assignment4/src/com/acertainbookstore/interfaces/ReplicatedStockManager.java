package com.acertainbookstore.interfaces;

import java.util.Set;

import com.acertainbookstore.business.BookCopy;
import com.acertainbookstore.business.BookEditorPick;
import com.acertainbookstore.business.StockBook;
import com.acertainbookstore.utils.BookStoreException;
import com.acertainbookstore.utils.BookStoreResult;

/**
 * {@link ReplicatedStockManager} declares a set of methods conforming to the
 * {@link StockManager} interface exposed by the bookstore to the proxies. These
 * methods need to be implemented by MasterCertainBookStore.
 */
public interface ReplicatedStockManager extends ReplicatedReadOnlyStockManager {

	/**
	 * Adds the books in bookSet to the stock.
	 *
	 * @param bookSet
	 *            the book set
	 * @return the book store result
	 * @throws BookStoreException
	 *             the book store exception
	 */
	public BookStoreResult addBooks(Set<StockBook> bookSet) throws BookStoreException;

	/**
	 * Add copies of the existing book to the bookstore.
	 *
	 * @param bookCopiesSet
	 *            the book copies set
	 * @return the book store result
	 * @throws BookStoreException
	 *             the book store exception
	 */
	public BookStoreResult addCopies(Set<BookCopy> bookCopiesSet) throws BookStoreException;

	/**
	 * Books are marked/unmarked as an editor pick.
	 *
	 * @param editorPicks
	 *            the editor picks
	 * @return the book store result
	 * @throws BookStoreException
	 *             the book store exception
	 */
	public BookStoreResult updateEditorPicks(Set<BookEditorPick> editorPicks) throws BookStoreException;

	/**
	 * Clean up the bookstore - remove all the books and the associated data.
	 *
	 * @return the book store result
	 * @throws BookStoreException
	 *             the book store exception
	 */
	public BookStoreResult removeAllBooks() throws BookStoreException;

	/**
	 * Clean up the bookstore selectively for the list of isbns provided.
	 *
	 * @param isbnSet
	 *            the isbn set
	 * @return the book store result
	 * @throws BookStoreException
	 *             the book store exception
	 */
	public BookStoreResult removeBooks(Set<Integer> isbnSet) throws BookStoreException;

}
