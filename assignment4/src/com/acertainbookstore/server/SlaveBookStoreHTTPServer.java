package com.acertainbookstore.server;

import org.eclipse.jetty.util.thread.QueuedThreadPool;

import com.acertainbookstore.business.SlaveCertainBookStore;
import com.acertainbookstore.utils.BookStoreConstants;

/**
 * Starts the {@link SlaveBookStoreHTTPServer}.
 */
public class SlaveBookStoreHTTPServer {

	/** The Constant defaultListenOnPort. */
	private static final int DEFAULT_PORT = 8081;
	private static final int MIN_THREADPOOL_SIZE = 10;
	private static final int MAX_THREADPOOL_SIZE = 100;

	/**
	 * Prevents the instantiation of a new {@link BookStoreHTTPServer}.
	 */
	private SlaveBookStoreHTTPServer() {
		// Prevent instances from being created.
	}

	/**
	 * @param args
	 *            Not being used now
	 */
	public static void main(String[] args) {
		SlaveCertainBookStore bookStore = new SlaveCertainBookStore();
		int listenOnPort = DEFAULT_PORT;

		SlaveBookStoreHTTPMessageHandler handler = new SlaveBookStoreHTTPMessageHandler(bookStore);
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
