package com.acertainbookstore.utils;

/**
 * {@link BookStoreResponse} is the data structure that encapsulates a HTTP
 * response from the bookstore server to the client. The data structure contains
 * error messages from the server if an error occurred.
 */
public class BookStoreResponse {

	/** The exception. */
	private BookStoreException exception;

	/** The result. */
	private BookStoreResult result;

	/**
	 * Instantiates a new {@link BookStoreResponse}.
	 *
	 * @param exception
	 *            the exception
	 * @param list
	 *            the list
	 */
	public BookStoreResponse(BookStoreException exception, BookStoreResult result) {
		this.setException(exception);
		this.setResult(result);
	}

	/**
	 * Instantiates a new book store response.
	 */
	public BookStoreResponse() {
		this.setException(null);
		this.setResult(null);
	}

	/**
	 * Gets the result.
	 *
	 * @return the result
	 */
	public BookStoreResult getResult() {
		return result;
	}

	/**
	 * Sets the result.
	 *
	 * @param result
	 *            the new result
	 */
	public void setResult(BookStoreResult result) {
		this.result = result;
	}

	/**
	 * Gets the exception.
	 *
	 * @return the exception
	 */
	public BookStoreException getException() {
		return exception;
	}

	/**
	 * Sets the exception.
	 *
	 * @param exception
	 *            the new exception
	 */
	public void setException(BookStoreException exception) {
		this.exception = exception;
	}
}
