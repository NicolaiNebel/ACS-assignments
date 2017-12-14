package com.acertainbookstore.business;

import java.util.Set;

import com.acertainbookstore.utils.BookStoreMessageTag;

/**
 * {@link ReplicationRequest} represents a replication request.
 */
public class ReplicationRequest {

	/** The data set. */
	private Set<?> dataSet;

	/** The message type. */
	private BookStoreMessageTag messageType;

	/**
	 * Instantiates a new replication request.
	 *
	 * @param dataSet
	 *            the data set
	 * @param messageType
	 *            the message type
	 */
	public ReplicationRequest(Set<?> dataSet, BookStoreMessageTag messageType) {
		this.setDataSet(dataSet);
		this.setMessageType(messageType);
	}

	/**
	 * Gets the data set.
	 *
	 * @return the data set
	 */
	public Set<?> getDataSet() {
		return dataSet;
	}

	/**
	 * Sets the data set.
	 *
	 * @param dataSet
	 *            the new data set
	 */
	public void setDataSet(Set<?> dataSet) {
		this.dataSet = dataSet;
	}

	/**
	 * Gets the message type.
	 *
	 * @return the message type
	 */
	public BookStoreMessageTag getMessageType() {
		return messageType;
	}

	/**
	 * Sets the message type.
	 *
	 * @param messageType
	 *            the new message type
	 */
	public void setMessageType(BookStoreMessageTag messageType) {
		this.messageType = messageType;
	}
}