package com.acertainbookstore.business;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.HashSet;
import java.util.List;
import java.util.Properties;
import java.util.Set;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

import com.acertainbookstore.interfaces.ReplicatedBookStore;
import com.acertainbookstore.interfaces.ReplicatedStockManager;
import com.acertainbookstore.interfaces.Replicator;
import com.acertainbookstore.utils.BookStoreConstants;
import com.acertainbookstore.utils.BookStoreException;
import com.acertainbookstore.utils.BookStoreMessageTag;
import com.acertainbookstore.utils.BookStoreResult;

/**
 * {@link MasterCertainBookStore} is a wrapper over the {@link CertainBookStore}
 * class and supports the {@link ReplicatedBookStore} and
 * {@link ReplicatedStockManager} interfaces. This class also contains a
 * {@link Replicator} which replicates updates to slaves.
 */
public class MasterCertainBookStore extends ReadOnlyCertainBookStore
		implements ReplicatedBookStore, ReplicatedStockManager {

	/** The replicator. */
	private Replicator replicator = null;

	/** The file path. */
	private String filePath = "./server.properties";

	/**
	 * Instantiates a new master certain book store.
	 *
	 * @throws BookStoreException
	 *             the book store exception
	 */
	public MasterCertainBookStore() throws BookStoreException {
		Set<String> slaveServers = initializeSlaveMapping();
		bookStore = new CertainBookStore();

		// The thread pool size is equal to number of slaves for which
		// concurrent requests need to be sent.
		replicator = new CertainBookStoreReplicator(slaveServers.size(), slaveServers);
	}

	/**
	 * Initialize slave mapping.
	 *
	 * @return the sets the
	 * @throws BookStoreException
	 *             the book store exception
	 */
	private Set<String> initializeSlaveMapping() throws BookStoreException {
		Properties props = new Properties();
		Set<String> slaveServers = new HashSet<>();

		try {
			props.load(new FileInputStream(filePath));
		} catch (IOException ex) {
			throw new BookStoreException(ex);
		}

		String slaveAddresses = props.getProperty(BookStoreConstants.KEY_SLAVE);

		for (String slave : slaveAddresses.split(BookStoreConstants.SPLIT_SLAVE_REGEX)) {
			if (!slave.toLowerCase().startsWith("http://")) {
				slave = "http://" + slave;
			}

			if (!slave.endsWith("/")) {
				slave = slave + "/";
			}

			slaveServers.add(slave);
		}

		return slaveServers;
	}

	/**
	 * Wait for slave updates.
	 *
	 * @param replicatedSlaveFutures
	 *            the replicated slave futures
	 */
	private void waitForSlaveUpdates(List<Future<ReplicationResult>> replicatedSlaveFutures) {
		Set<String> faultySlaveServers = new HashSet<>();

		for (Future<ReplicationResult> slaveServer : replicatedSlaveFutures) {
			while (true) {

				// We want a non-cancellable get() so do it in a loop until
				// successful to ignore interrupted exceptions.
				try {

					// block until the future result is available.
					ReplicationResult result = slaveServer.get();

					if (!result.isReplicationSuccessful()) {
						faultySlaveServers.add(result.getServerAddress());
					}

					break;
				} catch (InterruptedException e) {

					// Current thread was interrupted, there is no terminate
					// semantics so just ignore it and retry.
				} catch (ExecutionException e) {

					// Some exception happened in the replicator thread - this
					// should not happen, crash the process -> fail stop.
					e.printStackTrace();
					System.exit(-1);
				}
			}
		}

		if (!faultySlaveServers.isEmpty()) {
			replicator.markServersFaulty(faultySlaveServers);
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.acertainbookstore.interfaces.ReplicatedStockManager#addBooks(java.
	 * util.Set)
	 */
	public synchronized BookStoreResult addBooks(Set<StockBook> bookSet) throws BookStoreException {
		ReplicationRequest request = new ReplicationRequest(bookSet, BookStoreMessageTag.ADDBOOKS);
		List<Future<ReplicationResult>> replicatedSlaveFutures = replicator.replicate(request);

		// If this fails it will throw an exception.
		bookStore.addBooks(bookSet);

		snapshotId++;
		waitForSlaveUpdates(replicatedSlaveFutures);
		return new BookStoreResult(null, snapshotId);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.acertainbookstore.interfaces.ReplicatedStockManager#addCopies(java.
	 * util.Set)
	 */
	public synchronized BookStoreResult addCopies(Set<BookCopy> bookCopiesSet) throws BookStoreException {
		ReplicationRequest request = new ReplicationRequest(bookCopiesSet, BookStoreMessageTag.ADDCOPIES);
		List<Future<ReplicationResult>> replicatedSlaveFutures = replicator.replicate(request);

		// If this fails it will throw an exception.
		bookStore.addCopies(bookCopiesSet);

		snapshotId++;
		waitForSlaveUpdates(replicatedSlaveFutures);
		return new BookStoreResult(null, snapshotId);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.acertainbookstore.interfaces.ReplicatedStockManager#updateEditorPicks
	 * (java.util.Set)
	 */
	public synchronized BookStoreResult updateEditorPicks(Set<BookEditorPick> editorPicks) throws BookStoreException {
		ReplicationRequest request = new ReplicationRequest(editorPicks, BookStoreMessageTag.UPDATEEDITORPICKS);
		List<Future<ReplicationResult>> replicatedSlaveFutures = replicator.replicate(request);

		// If this fails it will throw an exception.
		bookStore.updateEditorPicks(editorPicks);

		snapshotId++;
		waitForSlaveUpdates(replicatedSlaveFutures);
		return new BookStoreResult(null, snapshotId);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.acertainbookstore.interfaces.ReplicatedBookStore#buyBooks(java.util.
	 * Set)
	 */
	public synchronized BookStoreResult buyBooks(Set<BookCopy> booksToBuy) throws BookStoreException {
		ReplicationRequest request = new ReplicationRequest(booksToBuy, BookStoreMessageTag.BUYBOOKS);
		List<Future<ReplicationResult>> replicatedSlaveFutures = replicator.replicate(request);

		// If this fails it will throw an exception.
		bookStore.buyBooks(booksToBuy);

		snapshotId++;
		waitForSlaveUpdates(replicatedSlaveFutures);
		return new BookStoreResult(null, snapshotId);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.acertainbookstore.interfaces.ReplicatedBookStore#rateBooks(java.util.
	 * Set)
	 */
	public synchronized BookStoreResult rateBooks(Set<BookRating> bookRating) throws BookStoreException {
		throw new BookStoreException("Not implemented");
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.acertainbookstore.interfaces.ReplicatedStockManager#removeAllBooks()
	 */
	public synchronized BookStoreResult removeAllBooks() throws BookStoreException {
		ReplicationRequest request = new ReplicationRequest(null, BookStoreMessageTag.REMOVEALLBOOKS);
		List<Future<ReplicationResult>> replicatedSlaveFutures = replicator.replicate(request);

		// If this fails it will throw an exception.
		bookStore.removeAllBooks();

		snapshotId++;
		waitForSlaveUpdates(replicatedSlaveFutures);
		return new BookStoreResult(null, snapshotId);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.acertainbookstore.interfaces.ReplicatedStockManager#removeBooks(java.
	 * util.Set)
	 */
	public synchronized BookStoreResult removeBooks(Set<Integer> isbnSet) throws BookStoreException {
		ReplicationRequest request = new ReplicationRequest(isbnSet, BookStoreMessageTag.REMOVEBOOKS);
		List<Future<ReplicationResult>> replicatedSlaveFutures = replicator.replicate(request);

		// If this fails it will throw an exception.
		bookStore.removeBooks(isbnSet);

		snapshotId++;
		waitForSlaveUpdates(replicatedSlaveFutures);
		return new BookStoreResult(null, snapshotId);
	}
}
