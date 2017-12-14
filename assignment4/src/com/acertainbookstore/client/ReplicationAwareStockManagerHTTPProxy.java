package com.acertainbookstore.client;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.HashSet;
import java.util.List;
import java.util.Properties;
import java.util.Set;

import org.eclipse.jetty.client.HttpClient;
import org.eclipse.jetty.util.thread.QueuedThreadPool;

import com.acertainbookstore.business.BookCopy;
import com.acertainbookstore.business.BookEditorPick;
import com.acertainbookstore.business.StockBook;
import com.acertainbookstore.interfaces.BookStoreSerializer;
import com.acertainbookstore.interfaces.StockManager;
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
 * {@link ReplicationAwareStockManagerHTTPProxy} implements the client level
 * synchronous {@link CertainBookStore} API declared in the {@link BookStore}
 * class. It keeps retrying the API until a consistent reply is returned from
 * the replicas.
 */
public class ReplicationAwareStockManagerHTTPProxy implements StockManager {

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
	private long snapshotId = 0;

	/**
	 * Initialize the client object.
	 *
	 * @throws Exception
	 *             the exception
	 */
	public ReplicationAwareStockManagerHTTPProxy() throws Exception {
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
	 * @param snapshotId
	 *            the new snapshot id
	 */
	public void setSnapshotId(long snapshotId) {
		this.snapshotId = snapshotId;
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
		return masterAddress;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.acertainbookstore.interfaces.StockManager#addBooks(java.util.Set)
	 */
	public void addBooks(Set<StockBook> bookSet) throws BookStoreException {
		String urlString = getMasterServerAddress() + "/" + BookStoreMessageTag.ADDBOOKS;
		BookStoreRequest bookStoreRequest = BookStoreRequest.newPostRequest(urlString, bookSet);
		BookStoreResponse bookStoreResponse = BookStoreUtility.performHttpExchange(client, bookStoreRequest,
				serializer.get());
		BookStoreResult bookStoreResult = bookStoreResponse.getResult();
		this.setSnapshotId(bookStoreResult.getSnapshotId());
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.acertainbookstore.interfaces.StockManager#addCopies(java.util.Set)
	 */
	public void addCopies(Set<BookCopy> bookCopiesSet) throws BookStoreException {
		String urlString = getMasterServerAddress() + "/" + BookStoreMessageTag.ADDCOPIES;
		BookStoreRequest bookStoreRequest = BookStoreRequest.newPostRequest(urlString, bookCopiesSet);
		BookStoreResponse bookStoreResponse = BookStoreUtility.performHttpExchange(client, bookStoreRequest,
				serializer.get());
		BookStoreResult bookStoreResult = bookStoreResponse.getResult();
		this.setSnapshotId(bookStoreResult.getSnapshotId());
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.acertainbookstore.interfaces.StockManager#getBooks()
	 */
	@SuppressWarnings("unchecked")
	public List<StockBook> getBooks() throws BookStoreException {
		BookStoreResponse bookStoreResponse;
		BookStoreResult bookStoreResult;

		do {
			String urlString = getReplicaAddress() + "/" + BookStoreMessageTag.LISTBOOKS;
			BookStoreRequest bookStoreRequest = BookStoreRequest.newGetRequest(urlString);
			bookStoreResponse = BookStoreUtility.performHttpExchange(client, bookStoreRequest, serializer.get());
			bookStoreResult = bookStoreResponse.getResult();
		} while (bookStoreResult.getSnapshotId() < this.getSnapshotId());

		this.setSnapshotId(bookStoreResult.getSnapshotId());
		return (List<StockBook>) bookStoreResult.getList();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.acertainbookstore.interfaces.StockManager#updateEditorPicks(java.util
	 * .Set)
	 */
	public void updateEditorPicks(Set<BookEditorPick> editorPicksValues) throws BookStoreException {
		String urlString = getMasterServerAddress() + "/" + BookStoreMessageTag.UPDATEEDITORPICKS + "?";
		BookStoreRequest bookStoreRequest = BookStoreRequest.newPostRequest(urlString, editorPicksValues);
		BookStoreResponse bookStoreResponse = BookStoreUtility.performHttpExchange(client, bookStoreRequest,
				serializer.get());
		BookStoreResult bookStoreResult = bookStoreResponse.getResult();
		this.setSnapshotId(bookStoreResult.getSnapshotId());
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.acertainbookstore.interfaces.StockManager#removeAllBooks()
	 */
	public void removeAllBooks() throws BookStoreException {
		String urlString = getMasterServerAddress() + "/" + BookStoreMessageTag.REMOVEALLBOOKS;

		// Creating zero-length buffer for POST request body, because we don't
		// need to send any data; this request is just a signal to remove all
		// books.
		BookStoreRequest bookStoreRequest = BookStoreRequest.newPostRequest(urlString, "");
		BookStoreResponse bookStoreResponse = BookStoreUtility.performHttpExchange(client, bookStoreRequest,
				serializer.get());
		BookStoreResult bookStoreResult = bookStoreResponse.getResult();
		this.setSnapshotId(bookStoreResult.getSnapshotId());
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.acertainbookstore.interfaces.StockManager#removeBooks(java.util.Set)
	 */
	public void removeBooks(Set<Integer> isbnSet) throws BookStoreException {
		String urlString = getMasterServerAddress() + "/" + BookStoreMessageTag.REMOVEBOOKS;
		BookStoreRequest bookStoreRequest = BookStoreRequest.newPostRequest(urlString, isbnSet);
		BookStoreResponse bookStoreResponse = BookStoreUtility.performHttpExchange(client, bookStoreRequest,
				serializer.get());
		BookStoreResult bookStoreResult = bookStoreResponse.getResult();
		this.setSnapshotId(bookStoreResult.getSnapshotId());
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.acertainbookstore.interfaces.StockManager#getBooksByISBN(java.util.
	 * Set)
	 */
	@SuppressWarnings("unchecked")
	public List<StockBook> getBooksByISBN(Set<Integer> isbns) throws BookStoreException {
		BookStoreResponse bookStoreResponse;
		BookStoreResult bookStoreResult;

		do {
			String urlString = getReplicaAddress() + "/" + BookStoreMessageTag.GETSTOCKBOOKSBYISBN;
			BookStoreRequest bookStoreRequest = BookStoreRequest.newPostRequest(urlString, isbns);
			bookStoreResponse = BookStoreUtility.performHttpExchange(client, bookStoreRequest, serializer.get());
			bookStoreResult = bookStoreResponse.getResult();
		} while (bookStoreResult.getSnapshotId() < this.getSnapshotId());

		this.setSnapshotId(bookStoreResult.getSnapshotId());
		return (List<StockBook>) bookStoreResult.getList();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.acertainbookstore.interfaces.StockManager#getBooksInDemand()
	 */
	@Override
	public List<StockBook> getBooksInDemand() throws BookStoreException {
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
		final String stock = "/stock";

		Properties props = new Properties();
		slaveAddresses = new HashSet<>();

		props.load(new FileInputStream(filePath));
		this.masterAddress = props.getProperty(BookStoreConstants.KEY_MASTER);

		if (!this.masterAddress.toLowerCase().startsWith(httpProtocol)) {
			this.masterAddress = httpProtocol + this.masterAddress;
		}

		if (!this.masterAddress.endsWith(stock)) {
			this.masterAddress = this.masterAddress + stock;
		}

		String slaveAddressesInternal = props.getProperty(BookStoreConstants.KEY_SLAVE);

		for (String slave : slaveAddressesInternal.split(BookStoreConstants.SPLIT_SLAVE_REGEX)) {
			if (!slave.toLowerCase().startsWith(httpProtocol)) {
				slave = httpProtocol + slave;
			}

			if (!slave.endsWith(stock)) {
				slave = slave + stock;
			}

			this.slaveAddresses.add(slave);
		}
	}
}
