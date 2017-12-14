package com.acertainbookstore.interfaces;

import java.util.List;
import java.util.Set;
import java.util.concurrent.Future;

import com.acertainbookstore.business.ReplicationRequest;
import com.acertainbookstore.business.ReplicationResult;

/**
 * {@link Replicator} is used to replicate updates on master to slaves.
 */
public interface Replicator {

	/**
	 * Replicates the {@link ReplicationRequest} to the list of active slave
	 * servers concurrently and returns the a Future object containing the
	 * status of replication to the slave servers.
	 *
	 * @param request
	 *            the request
	 * @return the list
	 */
	public List<Future<ReplicationResult>> replicate(ReplicationRequest request);

	/**
	 * Is invoked to update the configuration of active servers in the
	 * {@link Replicator} without forcing the replicate method to explicitly
	 * block.
	 *
	 * @param faultySlaveServers
	 *            the faulty slave servers
	 */
	public void markServersFaulty(Set<String> faultySlaveServers);
}
