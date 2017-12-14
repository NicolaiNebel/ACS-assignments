package com.acertainbookstore.business;

import java.util.*;
import java.util.concurrent.Callable;
import java.util.concurrent.Future;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import com.acertainbookstore.interfaces.Replication;
import com.acertainbookstore.interfaces.Replicator;
import com.acertainbookstore.server.ReplicationAwareServerHTTPProxy;

/**
 * {@link CertainBookStoreReplicator} is used to replicate updates to slaves
 * concurrently.
 */
public class CertainBookStoreReplicator implements Replicator {

	/** The replication clients. */
	private Map<String, Replication> replicationClients = null;

	/** The replicator thread pool. */
	private ExecutorService replicatorThreadPool = null;

	/**
	 * Instantiates a new certain book store replicator.
	 *
	 * @param maxReplicatorThreads
	 *            the max replicator threads
	 * @param slaveServers
	 *            the slave servers
	 */
	public CertainBookStoreReplicator(int maxReplicatorThreads, Set<String> slaveServers) {
		if (slaveServers == null) {
			return;
		}

		replicationClients = new HashMap<>();

		// Create the proxies for each destination slave.
		for (String aSlaveServer : slaveServers) {
			replicationClients.put(aSlaveServer, new ReplicationAwareServerHTTPProxy(aSlaveServer));
		}

		// Create the thread pool for concurrently invoking replicate RPCs.
		replicatorThreadPool = Executors.newFixedThreadPool(maxReplicatorThreads);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.acertainbookstore.interfaces.Replicator#replicate(com.
	 * acertainbookstore.business.ReplicationRequest)
	 */
	public List<Future<ReplicationResult>> replicate(ReplicationRequest request) {
		// Send a replicate request to all the
		// activeSlaveServers use CertainBookStoreReplicationTask to create a
		// Task, submit it to the thread pool and construct the "Future" results
		// to be returned
		List<Future<ReplicationResult>> ret = new ArrayList<>();
        for (Replication slaveReplication : replicationClients.values()) {
            Callable<ReplicationResult> c = new CertainBookStoreReplicationTask(slaveReplication, request);
			Future<ReplicationResult> future = replicatorThreadPool.submit(c);
			ret.add(future);
		}
		return ret;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.acertainbookstore.interfaces.Replicator#markServersFaulty(java.util.
	 * Set)
	 */
	public void markServersFaulty(Set<String> faultyServers) {
		if (faultyServers != null) {
			for (String aFaultyServer : faultyServers) {
				((ReplicationAwareServerHTTPProxy) replicationClients.get(aFaultyServer)).stop();
				replicationClients.remove(aFaultyServer);
			}
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Object#finalize()
	 */
	public void finalize() {
		// Shutdown the executor service, invoked when the object is out of
		// scope (garbage collected)
		replicatorThreadPool.shutdownNow();
	}
}
