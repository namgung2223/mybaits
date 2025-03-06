package com.kovi.kovinewinterface.vo.libUpdate;

import java.io.Serializable;
import java.util.StringJoiner;

public class DefaultVO implements Serializable {

	private static final long serialVersionUID = -858838578081269359L;

	private int start;
	private int length;
	private String orderColumn;
	private String orderDir;
	private String adminId;
	private String adminRights;

	// Getter & Setter
	public String getAdminRights() {
		return adminRights;
	}

	public void setAdminRights(String adminRights) {
		this.adminRights = adminRights;
	}

	public String getAdminId() {
		return adminId;
	}

	public void setAdminId(String adminId) {
		this.adminId = adminId;
	}

	public int getStart() {
		return start;
	}

	public void setStart(int start) {
		this.start = start;
	}

	public int getLength() {
		return length;
	}

	public void setLength(int length) {
		this.length = length;
	}

	public String getOrderColumn() {
		return orderColumn;
	}

	public void setOrderColumn(String orderColumn) {
		this.orderColumn = orderColumn;
	}

	public String getOrderDir() {
		return orderDir;
	}

	public void setOrderDir(String orderDir) {
		this.orderDir = orderDir;
	}

	@Override
	public String toString() {
		return new StringJoiner(", ", DefaultVO.class.getSimpleName() + "{", "}")
				.add("start=" + start)
				.add("length=" + length)
				.add("orderColumn='" + orderColumn + "'")
				.add("orderDir='" + orderDir + "'")
				.add("adminId='" + adminId + "'")
				.add("adminRights='" + adminRights + "'")
				.toString();
	}
}
