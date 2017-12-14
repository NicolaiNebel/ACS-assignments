package com.acertainbookstore.business;

import java.util.concurrent.Callable;

import com.acertainbookstore.interfaces.Replication;
import com.acertainbookstore.utils.BookStoreException;

/**
 * {@link CertainBookStoreReplicationTask} performs replication to a slave
 * server. It returns the result of the replication on completion using
 * {@link ReplicationResult}.
 */
public class CertainBookStoreReplicationTask implements Callable<ReplicationResult> {

	/**
	 * Instantiates a new certain book store replication task.
	 *
	 * @param replicationClient
	 *            the replication client
	 * @param request
	 *            the request
	 */

	private Replication replication = null;
	private ReplicationRequest request = null;

	public CertainBookStoreReplicationTask(Replication replicationClient, ReplicationRequest request) {
		this.replication = replicationClient;
		this.request = request;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.util.concurrent.Callable#call()
	 */
	@Override
	public ReplicationResult call() throws BookStoreException {
        return replication.replicate(request);
	}
}
