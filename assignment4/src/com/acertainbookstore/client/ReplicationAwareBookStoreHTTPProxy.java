package com.acertainbookstore.client;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.HashSet;
import java.util.List;
import java.util.Properties;
import java.util.Set;

import org.eclipse.jetty.client.HttpClient;
import org.eclipse.jetty.util.thread.QueuedThreadPool;

import com.acertainbookstore.business.Book;
import com.acertainbookstore.business.BookCopy;
import com.acertainbookstore.business.BookRating;
import com.acertainbookstore.interfaces.BookStore;
import com.acertainbookstore.interfaces.BookStoreSerializer;
import com.acertainbookstore.utils.BookStoreConstants;
import com.acertainbookstore.utils.BookStoreException;
import com.acertainbookstore.utils.BookStoreKryoSerializer;
import com.acertainbookstore.utils.BookStoreMessageTag;
import com.acertainbookstore.utils.BookStoreRequest;
import com.acertainbookstore.utils.BookStoreResponse;
import com.acertainbookstore.utils.BookStoreResult;
import com.acertainbookstore.utils.BookStoreUtility;
import com.acertainbookstore.utils.BookStoreXStreamSerializer;

/**
 * {@link ReplicationAwareBookStoreHTTPProxy} implements the client level
 * synchronous {@link CertainBookStore} API declared in the {@link BookStore}
 * class. It keeps retrying the API until a consistent reply is returned from
 * the replicas.
 */
public class ReplicationAwareBookStoreHTTPProxy implements BookStore {

	/** The serializer. */
	private static ThreadLocal<BookStoreSerializer> serializer;

	/** The client. */
	private HttpClient client;

	/** The slave addresses. */
	private Set<String> slaveAddresses;

	/** The master address. */
	private String masterAddress;

	/** The file path. */
	private String filePath = "./proxy.properties";

	/** The snapshot id. */
	private volatile long snapshotId = 0;

	/**
	 * Initializes a new {@link ReplicationAwareBookStoreHTTPProxy}.
	 *
	 * @throws Exception
	 *             the exception
	 */
	public ReplicationAwareBookStoreHTTPProxy() throws Exception {
		initializeReplicationAwareMappings();

		// Setup the type of serializer.
		if (BookStoreConstants.BINARY_SERIALIZATION) {
			serializer = ThreadLocal.withInitial(BookStoreKryoSerializer::new);
		} else {
			serializer = ThreadLocal.withInitial(BookStoreXStreamSerializer::new);
		}

		client = new HttpClient();

		// Max concurrent connections to every address.
		client.setMaxConnectionsPerDestination(BookStoreClientConstants.CLIENT_MAX_CONNECTION_ADDRESS);

		// Max number of threads.
		client.setExecutor(new QueuedThreadPool(BookStoreClientConstants.CLIENT_MAX_THREADSPOOL_THREADS));

		// Seconds timeout; if no server reply, the request expires.
		client.setConnectTimeout(BookStoreClientConstants.CLIENT_MAX_TIMEOUT_MILLISECS);

		client.start();
	}

	/**
	 * Gets the snapshot id.
	 *
	 * @return the snapshot id
	 */
	public long getSnapshotId() {
		return snapshotId;
	}

	/**
	 * Sets the snapshot id.
	 *
	 * @param snapShotId
	 *            the new snapshot id
	 */
	public void setSnapshotId(long snapShotId) {
		this.snapshotId = snapShotId;
	}

	/**
	 * Gets the replica address.
	 *
	 * @return the replica address
	 */
	public String getReplicaAddress() {
		throw new UnsupportedOperationException();
	}

	/**
	 * Gets the master server address.
	 *
	 * @return the master server address
	 */
	public String getMasterServerAddress() {
		return this.masterAddress;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.acertainbookstore.interfaces.BookStore#buyBooks(java.util.Set)
	 */
	public void buyBooks(Set<BookCopy> isbnSet) throws BookStoreException {
		String urlString = getMasterServerAddress() + "/" + BookStoreMessageTag.BUYBOOKS;
		BookStoreRequest bookStoreRequest = BookStoreRequest.newPostRequest(urlString, isbnSet);
		BookStoreResponse bookStoreResponse = BookStoreUtility.performHttpExchange(client, bookStoreRequest,
				serializer.get());
		BookStoreResult bookStoreResult = bookStoreResponse.getResult();
		this.setSnapshotId(bookStoreResult.getSnapshotId());
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.acertainbookstore.interfaces.BookStore#getBooks(java.util.Set)
	 */
	@SuppressWarnings("unchecked")
	public List<Book> getBooks(Set<Integer> isbnSet) throws BookStoreException {
		BookStoreResponse bookStoreResponse;
		BookStoreResult bookStoreResult;

		do {
			String urlString = getReplicaAddress() + "/" + BookStoreMessageTag.GETBOOKS;
			BookStoreRequest bookStoreRequest = BookStoreRequest.newPostRequest(urlString, isbnSet);
			bookStoreResponse = BookStoreUtility.performHttpExchange(client, bookStoreRequest, serializer.get());
			bookStoreResult = bookStoreResponse.getResult();
		} while (bookStoreResult.getSnapshotId() < this.getSnapshotId());

		this.setSnapshotId(bookStoreResult.getSnapshotId());
		return (List<Book>) bookStoreResult.getList();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.acertainbookstore.interfaces.BookStore#getEditorPicks(int)
	 */
	@SuppressWarnings("unchecked")
	public List<Book> getEditorPicks(int numBooks) throws BookStoreException {
		String urlEncodedNumBooks = null;

		try {
			urlEncodedNumBooks = URLEncoder.encode(Integer.toString(numBooks), "UTF-8");
		} catch (UnsupportedEncodingException ex) {
			throw new BookStoreException("unsupported encoding of numbooks", ex);
		}

		BookStoreResponse bookStoreResponse;
		BookStoreResult bookStoreResult;

		do {
			String urlString = getReplicaAddress() + "/" + BookStoreMessageTag.GETEDITORPICKS + "?"
					+ BookStoreConstants.BOOK_NUM_PARAM + "=" + urlEncodedNumBooks;
			BookStoreRequest bookStoreRequest = BookStoreRequest.newGetRequest(urlString);
			bookStoreResponse = BookStoreUtility.performHttpExchange(client, bookStoreRequest, serializer.get());
			bookStoreResult = bookStoreResponse.getResult();
		} while (bookStoreResult.getSnapshotId() < this.getSnapshotId());

		this.setSnapshotId(bookStoreResult.getSnapshotId());
		return (List<Book>) bookStoreResult.getList();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.acertainbookstore.interfaces.BookStore#rateBooks(java.util.Set)
	 */
	@Override
	public void rateBooks(Set<BookRating> bookRating) throws BookStoreException {
		throw new BookStoreException("Not implemented");
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.acertainbookstore.interfaces.BookStore#getTopRatedBooks(int)
	 */
	@Override
	public List<Book> getTopRatedBooks(int numBooks) throws BookStoreException {
		throw new BookStoreException("Not implemented");
	}

	/**
	 * Stops the proxy.
	 */
	public void stop() {
		try {
			client.stop();
		} catch (Exception ex) {
			System.err.println(ex.getStackTrace());
		}
	}

	/**
	 * Initialize replication aware mappings.
	 *
	 * @throws IOException
	 *             Signals that an I/O exception has occurred.
	 */
	private void initializeReplicationAwareMappings() throws IOException {
		final String httpProtocol = "http://";

		Properties props = new Properties();
		this.slaveAddresses = new HashSet<>();

		props.load(new FileInputStream(filePath));
		this.masterAddress = props.getProperty(BookStoreConstants.KEY_MASTER);

		if (!this.masterAddress.toLowerCase().startsWith(httpProtocol)) {
			this.masterAddress = httpProtocol + this.masterAddress;
		}

		String slaveAddressesInternal = props.getProperty(BookStoreConstants.KEY_SLAVE);

		for (String slave : slaveAddressesInternal.split(BookStoreConstants.SPLIT_SLAVE_REGEX)) {
			if (!slave.toLowerCase().startsWith(httpProtocol)) {
				slave = "http://" + slave;
			}

			this.slaveAddresses.add(slave);
		}
	}
}
