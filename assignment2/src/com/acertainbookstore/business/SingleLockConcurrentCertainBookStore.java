package com.acertainbookstore.business;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Random;
import java.util.Set;
import java.util.concurrent.*;
import java.util.concurrent.locks.*;

import com.acertainbookstore.interfaces.BookStore;
import com.acertainbookstore.interfaces.StockManager;
import com.acertainbookstore.utils.BookStoreConstants;
import com.acertainbookstore.utils.BookStoreException;
import com.acertainbookstore.utils.BookStoreUtility;

/** {@link SingleLockConcurrentCertainBookStore} implements the {@link BookStore} and
 * {@link StockManager} functionalities.
 * 
 * @see BookStore
 * @see StockManager
 */
public class SingleLockConcurrentCertainBookStore implements BookStore, StockManager {

	/** The mapping of books from ISBN to {@link BookStoreBook}. */
	private Map<Integer, BookStoreBook> bookMap = null;

	/**
	 * Instantiates a new {@link SingleLockConcurrentCertainBookStore}.
	 */
	public SingleLockConcurrentCertainBookStore() {
		// Constructors are not synchronized
		bookMap = new HashMap<>();
	}

	//Global lock for the bookstore state
	private ReadWriteLock readWriteLock = new ReentrantReadWriteLock();

	private void takeReadLock() { readWriteLock.readLock().lock(); }
	private void releaseReadLock() { readWriteLock.readLock().unlock(); }

	private void takeWriteLock() { readWriteLock.writeLock().lock(); }
	private void releaseWriteLock() {readWriteLock.writeLock().unlock(); }
	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.acertainbookstore.interfaces.StockManager#addBooks(java.util.Set)
	 */
	public void addBooks(Set<StockBook> bookSet) throws BookStoreException {
		takeWriteLock();
		try {
			if (bookSet == null) {
				throw new BookStoreException(BookStoreConstants.NULL_INPUT);
			}

			// Check if all are there
			for (StockBook book : bookSet) {
				int isbn = book.getISBN();
				String bookTitle = book.getTitle();
				String bookAuthor = book.getAuthor();
				int noCopies = book.getNumCopies();
				float bookPrice = book.getPrice();

				if (BookStoreUtility.isInvalidISBN(isbn)) {
					throw new BookStoreException(BookStoreConstants.BOOK + book.toString() + BookStoreConstants.INVALID);
				}

				if (BookStoreUtility.isEmpty(bookTitle)) {
					throw new BookStoreException(BookStoreConstants.BOOK + book.toString() + BookStoreConstants.INVALID);
				}

				if (BookStoreUtility.isEmpty(bookAuthor)) {
					throw new BookStoreException(BookStoreConstants.BOOK + book.toString() + BookStoreConstants.INVALID);
				}

				if (BookStoreUtility.isInvalidNoCopies(noCopies)) {
					throw new BookStoreException(BookStoreConstants.BOOK + book.toString() + BookStoreConstants.INVALID);
				}

				if (bookPrice < 0.0) {
					throw new BookStoreException(BookStoreConstants.BOOK + book.toString() + BookStoreConstants.INVALID);
				}

				if (bookMap.containsKey(isbn)) {
					throw new BookStoreException(BookStoreConstants.ISBN + isbn + BookStoreConstants.DUPLICATED);
				}
			}

			for (StockBook book : bookSet) {
				int isbn = book.getISBN();
				bookMap.put(isbn, new BookStoreBook(book));
			}
		} finally {
			releaseWriteLock();
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.acertainbookstore.interfaces.StockManager#addCopies(java.util.Set)
	 */
	public void addCopies(Set<BookCopy> bookCopiesSet) throws BookStoreException {
		int isbn;
		int numCopies;

		takeWriteLock();
		try {
			if (bookCopiesSet == null) {
				throw new BookStoreException(BookStoreConstants.NULL_INPUT);
			}

			for (BookCopy bookCopy : bookCopiesSet) {
				isbn = bookCopy.getISBN();
				numCopies = bookCopy.getNumCopies();

				if (BookStoreUtility.isInvalidISBN(isbn)) {
					throw new BookStoreException(BookStoreConstants.ISBN + isbn + BookStoreConstants.INVALID);
				}

				if (!bookMap.containsKey(isbn)) {
					throw new BookStoreException(BookStoreConstants.ISBN + isbn + BookStoreConstants.NOT_AVAILABLE);
				}

				if (BookStoreUtility.isInvalidNoCopies(numCopies)) {
					throw new BookStoreException(BookStoreConstants.NUM_COPIES + numCopies + BookStoreConstants.INVALID);
				}
			}

			// Update the number of copies
			BookStoreBook book;
			for (BookCopy bookCopy : bookCopiesSet) {
				isbn = bookCopy.getISBN();
				numCopies = bookCopy.getNumCopies();
				book = bookMap.get(isbn);
				book.addCopies(numCopies);
			}
		} finally {
			releaseWriteLock();
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.acertainbookstore.interfaces.StockManager#updateEditorPicks(java.util
	 * .Set)
	 */

	public void updateEditorPicks(Set<BookEditorPick> editorPicks) throws BookStoreException {
		takeWriteLock();
		try {
			// Check that all ISBNs that we add/remove are there first.
			if (editorPicks == null) {
				throw new BookStoreException(BookStoreConstants.NULL_INPUT);
			}

			int isbnValue;

			for (BookEditorPick editorPickArg : editorPicks) {
				isbnValue = editorPickArg.getISBN();

				if (BookStoreUtility.isInvalidISBN(isbnValue)) {
					throw new BookStoreException(BookStoreConstants.ISBN + isbnValue + BookStoreConstants.INVALID);
				}

				if (!bookMap.containsKey(isbnValue)) {
					throw new BookStoreException(BookStoreConstants.ISBN + isbnValue + BookStoreConstants.NOT_AVAILABLE);
				}
			}

			for (BookEditorPick editorPickArg : editorPicks) {
				bookMap.get(editorPickArg.getISBN()).setEditorPick(editorPickArg.isEditorPick());
			}
		} finally {
			releaseWriteLock();
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.acertainbookstore.interfaces.BookStore#buyBooks(java.util.Set)
	 */
	public void buyBooks(Set<BookCopy> bookCopiesToBuy) throws BookStoreException {
		if (bookCopiesToBuy == null) {
			throw new BookStoreException(BookStoreConstants.NULL_INPUT);
		}

		int isbn;
		BookStoreBook book;
		Boolean saleMiss = false;

		Map<Integer, Integer> salesMisses = new HashMap<>();

		takeWriteLock();
		try {
			// Check that all ISBNs that we buy are there first.
			for (BookCopy bookCopyToBuy : bookCopiesToBuy) {
				isbn = bookCopyToBuy.getISBN();

				if (bookCopyToBuy.getNumCopies() < 0) {
					throw new BookStoreException(
							BookStoreConstants.NUM_COPIES + bookCopyToBuy.getNumCopies() + BookStoreConstants.INVALID);
				}

				if (BookStoreUtility.isInvalidISBN(isbn)) {
					throw new BookStoreException(BookStoreConstants.ISBN + isbn + BookStoreConstants.INVALID);
				}

				if (!bookMap.containsKey(isbn)) {
					throw new BookStoreException(BookStoreConstants.ISBN + isbn + BookStoreConstants.NOT_AVAILABLE);
				}

				book = bookMap.get(isbn);

				if (!book.areCopiesInStore(bookCopyToBuy.getNumCopies())) {
					// If we cannot sell the copies of the book, it is a miss.
					salesMisses.put(isbn, bookCopyToBuy.getNumCopies() - book.getNumCopies());
					saleMiss = true;
				}
			}

			// We throw exception now since we want to see how many books in the
			// order incurred misses which is used by books in demand
			if (saleMiss) {
				for (Map.Entry<Integer, Integer> saleMissEntry : salesMisses.entrySet()) {
					book = bookMap.get(saleMissEntry.getKey());
					book.addSaleMiss(saleMissEntry.getValue());
				}
				throw new BookStoreException(BookStoreConstants.BOOK + BookStoreConstants.NOT_AVAILABLE);
			}
			// Then make the purchase.
			for (BookCopy bookCopyToBuy : bookCopiesToBuy) {
				book = bookMap.get(bookCopyToBuy.getISBN());
				book.buyCopies(bookCopyToBuy.getNumCopies());
			}
		} finally {
			releaseWriteLock();
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.acertainbookstore.interfaces.StockManager#getBooksByISBN(java.util.
	 * Set)
	 */
	public List<StockBook> getBooksByISBN(Set<Integer> isbnSet) throws BookStoreException {
		if (isbnSet == null) {
			throw new BookStoreException(BookStoreConstants.NULL_INPUT);
		}

		takeReadLock();
		try {
			for (Integer ISBN : isbnSet) {
				if (BookStoreUtility.isInvalidISBN(ISBN)) {
					throw new BookStoreException(BookStoreConstants.ISBN + ISBN + BookStoreConstants.INVALID);
				}

				if (!bookMap.containsKey(ISBN)) {
					throw new BookStoreException(BookStoreConstants.ISBN + ISBN + BookStoreConstants.NOT_AVAILABLE);
				}
			}

			List<StockBook> listBooks = new ArrayList<>();

			for (Integer isbn : isbnSet) {
				listBooks.add(bookMap.get(isbn).immutableStockBook());
			}

			return listBooks;
		} finally {
			releaseReadLock();
		}
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see com.acertainbookstore.interfaces.StockManager#getBooks()
	 */
	public List<StockBook> getBooks() {
		takeReadLock();

		try {
			List<StockBook> listBooks = new ArrayList<>();
			Collection<BookStoreBook> bookMapValues = bookMap.values();

			for (BookStoreBook book : bookMapValues) {
				listBooks.add(book.immutableStockBook());
			}

			return listBooks;
		} finally {
			releaseReadLock();
		}
	}


	/*
	 * (non-Javadoc)
	 * 
	 * @see com.acertainbookstore.interfaces.BookStore#getBooks(java.util.Set)
	 */
	public List<Book> getBooks(Set<Integer> isbnSet) throws BookStoreException {
	 	takeReadLock();

	 	try {
			if (isbnSet == null) {
				throw new BookStoreException(BookStoreConstants.NULL_INPUT);
			}

			// Check that all ISBNs that we rate are there to start with.
			for (Integer ISBN : isbnSet) {
				if (BookStoreUtility.isInvalidISBN(ISBN)) {
					throw new BookStoreException(BookStoreConstants.ISBN + ISBN + BookStoreConstants.INVALID);
				}

				if (!bookMap.containsKey(ISBN)) {
					throw new BookStoreException(BookStoreConstants.ISBN + ISBN + BookStoreConstants.NOT_AVAILABLE);
				}
			}

			List<Book> listBooks = new ArrayList<>();

			for (Integer isbn : isbnSet) {
				listBooks.add(bookMap.get(isbn).immutableBook());
			}

			return listBooks;
		} finally {
	 		releaseReadLock();
		}
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see com.acertainbookstore.interfaces.BookStore#getEditorPicks(int)
	 */

	public List<Book> getEditorPicks(int numBooks) throws BookStoreException {
		takeReadLock();
		try {
			if (numBooks < 0) {
				throw new BookStoreException("numBooks = " + numBooks + ", but it must be positive");
			}

			List<BookStoreBook> listAllEditorPicks = new ArrayList<>();
			List<Book> listEditorPicks = new ArrayList<>();
			Iterator<Entry<Integer, BookStoreBook>> it = bookMap.entrySet().iterator();
			BookStoreBook book;

			// Get all books that are editor picks.
			while (it.hasNext()) {
				Entry<Integer, BookStoreBook> pair = it.next();
				book = pair.getValue();

				if (book.isEditorPick()) {
					listAllEditorPicks.add(book);
				}
			}

			// Find numBooks random indices of books that will be picked.
			Random rand = new Random();
			Set<Integer> tobePicked = new HashSet<>();
			int rangePicks = listAllEditorPicks.size();

			if (rangePicks <= numBooks) {

				// We need to add all books.
				for (int i = 0; i < listAllEditorPicks.size(); i++) {
					tobePicked.add(i);
				}
			} else {

				// We need to pick randomly the books that need to be returned.
				int randNum;

				while (tobePicked.size() < numBooks) {
					randNum = rand.nextInt(rangePicks);
					tobePicked.add(randNum);
				}
			}

			// Get the numBooks random books.
			for (Integer index : tobePicked) {
				book = listAllEditorPicks.get(index);
				listEditorPicks.add(book.immutableBook());
			}

			return listEditorPicks;
		} finally {
			releaseReadLock();
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.acertainbookstore.interfaces.BookStore#getTopRatedBooks(int)
	 */
	@Override
	public List<Book> getTopRatedBooks(int numBooks) throws BookStoreException {
		throw new BookStoreException();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.acertainbookstore.interfaces.StockManager#getBooksInDemand()
	 */
	@Override
	public List<StockBook> getBooksInDemand() throws BookStoreException {
		throw new BookStoreException();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.acertainbookstore.interfaces.BookStore#rateBooks(java.util.Set)
	 */
	@Override
	public void rateBooks(Set<BookRating> bookRating) throws BookStoreException {
		throw new BookStoreException();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.acertainbookstore.interfaces.StockManager#removeAllBooks()
	 */
	public void removeAllBooks() throws BookStoreException {
		takeWriteLock();
		try {
			bookMap.clear();
		} finally {
			releaseWriteLock();
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.acertainbookstore.interfaces.StockManager#removeBooks(java.util.Set)
	 */
	public void removeBooks(Set<Integer> isbnSet) throws BookStoreException {
	    takeWriteLock();
	    try {
			if (isbnSet == null) {
				throw new BookStoreException(BookStoreConstants.NULL_INPUT);
			}

			for (Integer ISBN : isbnSet) {
				if (BookStoreUtility.isInvalidISBN(ISBN)) {
					throw new BookStoreException(BookStoreConstants.ISBN + ISBN + BookStoreConstants.INVALID);
				}

				if (!bookMap.containsKey(ISBN)) {
					throw new BookStoreException(BookStoreConstants.ISBN + ISBN + BookStoreConstants.NOT_AVAILABLE);
				}
			}

			for (int isbn : isbnSet) {
				bookMap.remove(isbn);
			}
		} finally {
	    	releaseWriteLock();
		}
	}
}
