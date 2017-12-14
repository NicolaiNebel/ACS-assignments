package com.acertainbookstore.server;

import org.eclipse.jetty.util.thread.QueuedThreadPool;

import com.acertainbookstore.business.MasterCertainBookStore;
import com.acertainbookstore.utils.BookStoreConstants;
import com.acertainbookstore.utils.BookStoreException;

/**
 * Starts the {@link MasterBookStoreHTTPServer}.
 */
public class MasterBookStoreHTTPServer {

	/** The Constant defaultListenOnPort. */
	private static final int DEFAULT_PORT = 8081;

	/** The Constant MIN_THREADPOOL_SIZE. */
	private static final int MIN_THREADPOOL_SIZE = 10;

	/** The Constant MAX_THREADPOOL_SIZE. */
	private static final int MAX_THREADPOOL_SIZE = 100;

	/**
	 * Prevents the instantiation of a new {@link MasterBookStoreHTTPServer}.
	 */
	private MasterBookStoreHTTPServer() {
		// Prevent instances from being created.
	}

	/**
	 * The main method.
	 *
	 * @param args
	 *            the arguments
	 * @throws BookStoreException
	 *             the book store exception
	 */
	public static void main(String[] args) throws BookStoreException {
		MasterCertainBookStore bookStore = new MasterCertainBookStore();
		int listenOnPort = DEFAULT_PORT;

		MasterBookStoreHTTPMessageHandler handler = new MasterBookStoreHTTPMessageHandler(bookStore);
		String serverPortString = System.getProperty(BookStoreConstants.PROPERTY_KEY_SERVER_PORT);

		if (serverPortString != null) {
			try {
				listenOnPort = Integer.parseInt(serverPortString);
			} catch (NumberFormatException ex) {
				System.err.println(ex);
			}
		}

		QueuedThreadPool threadpool = new QueuedThreadPool(MAX_THREADPOOL_SIZE, MIN_THREADPOOL_SIZE);
		BookStoreHTTPServerUtility.createServer(listenOnPort, handler, threadpool);
	}
}
