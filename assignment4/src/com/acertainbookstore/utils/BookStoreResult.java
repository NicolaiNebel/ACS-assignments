package com.acertainbookstore.utils;

import java.util.List;

/**
 * Data structure to represent a result from the bookstore
 */
public class BookStoreResult {
	private List<?> list;
	private long snapshotId;

	public BookStoreResult(List<?> list, long snapshotId) {
		this.setList(list);
		this.setSnapshotId(snapshotId);
	}

	public List<?> getList() {
		return list;
	}

	public void setList(List<?> list) {
		this.list = list;
	}

	public long getSnapshotId() {
		return snapshotId;
	}

	public void setSnapshotId(long snapshotId) {
		this.snapshotId = snapshotId;
	}
}
