package com.acertainbookstore.server;

import com.acertainbookstore.business.ReplicationRequest;
import com.acertainbookstore.business.ReplicationResult;
import com.acertainbookstore.interfaces.BookStoreSerializer;
import com.acertainbookstore.interfaces.Replication;
import com.acertainbookstore.utils.*;
import org.eclipse.jetty.client.HttpClient;

/**
 * {@link ReplicationAwareServerHTTPProxy} implements the client side code for
 * replicate RPC, invoked by the master bookstore to propagate updates to
 * slaves, there is one proxy for each destination slave server.
 */
public class ReplicationAwareServerHTTPProxy implements Replication {

	private String slaveAddress = null;
	private HttpClient client = null;
	private static ThreadLocal<BookStoreSerializer> serializer;

	/**
	 * Instantiates a new replication aware server HTTP proxy.
	 *
	 * @param destinationServerAddress
	 *            the destination server address
	 */
	public ReplicationAwareServerHTTPProxy(String destinationServerAddress) {
		slaveAddress = destinationServerAddress;

		// Setup the type of serializer.
		if (BookStoreConstants.BINARY_SERIALIZATION) {
			serializer = ThreadLocal.withInitial(BookStoreKryoSerializer::new);
		} else {
			serializer = ThreadLocal.withInitial(BookStoreXStreamSerializer::new);
		}

		client = new HttpClient();
	}


	/*
	 * (non-Javadoc)
	 * 
	 * @see com.acertainbookstore.interfaces.Replication#replicate(com.
	 * acertainbookstore.business.ReplicationRequest)
	 */
	@Override
	public ReplicationResult replicate(ReplicationRequest req) throws BookStoreException {
		String urlString = slaveAddress + "/" + BookStoreMessageTag.REPLICATE;
		BookStoreRequest bookStoreRequest = BookStoreRequest.newPostRequest(urlString, req);
		BookStoreResponse bookStoreResponse = BookStoreUtility.performHttpExchange(client, bookStoreRequest,
				serializer.get());
		BookStoreResult bookStoreResult = bookStoreResponse.getResult();

		ReplicationResult replicationResult = (ReplicationResult) bookStoreResult.getList().get(0);

		replicationResult.setServerAddress(slaveAddress);
		return replicationResult;
	}

	/**
	 * Stop.
	 */
	public void stop() {
		// Shutdown the client
	}
}
