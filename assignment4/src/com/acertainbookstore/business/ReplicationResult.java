package com.acertainbookstore.business;

/**
 * {@link ReplicationResult} represents the result of a replication.
 */
public class ReplicationResult {

	/** The server where the replication request was sent. */
	private String serverAddress;

	/** The replication successful. */
	private boolean replicationSuccessful;

	/**
	 * Instantiates a new replication result.
	 *
	 * @param serverAddress
	 *            the server address
	 * @param replicationSuccessful
	 *            the replication successful
	 */
	public ReplicationResult(String serverAddress, boolean replicationSuccessful) {
		this.setServerAddress(serverAddress);
		this.setReplicationSuccessful(replicationSuccessful);
	}

	/**
	 * Gets the server address.
	 *
	 * @return the server address
	 */
	public String getServerAddress() {
		return serverAddress;
	}

	/**
	 * Sets the server address.
	 *
	 * @param serverAddress
	 *            the new server address
	 */
	public void setServerAddress(String serverAddress) {
		this.serverAddress = serverAddress;
	}

	/**
	 * Checks if is replication successful.
	 *
	 * @return true, if is replication successful
	 */
	public boolean isReplicationSuccessful() {
		return replicationSuccessful;
	}

	/**
	 * Sets the replication successful.
	 *
	 * @param replicationSuccessful
	 *            the new replication successful
	 */
	public void setReplicationSuccessful(boolean replicationSuccessful) {
		this.replicationSuccessful = replicationSuccessful;
	}
}
