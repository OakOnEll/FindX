package com.oakonell.findx.custom.model;

import com.oakonell.findx.model.Level;
import com.oakonell.findx.model.Stage;
import com.oakonell.utils.StringUtils;

public class CustomLevel extends Level implements ICustomLevel{
	private long dbId;
	private int sequence;
	private boolean isImported;
	private String author;
	private String serverId;

	public CustomLevel(CustomLevelBuilder builder, Stage stage) {

		// long dbId, int sequence, Stage stage, String id, String name,
		// Equation equation, List<Operation> operations,
		// int maxMoves, int minMoves) {
		super(stage, builder.getTitle(), builder.getCurrentStartEquation(),
				builder.getOperations(), builder.getLevelSolution());
		dbId = builder.getId();
		sequence = builder.getSequence();
		isImported = builder.isImported();
		author = builder.getAuthor();
		serverId = builder.getServerId();
	}

	protected String getRatingTableId() {
		return dbId + "";
	}
	
	public long getDbId() {
		return dbId;
	}

	public int getSequence() {
		return sequence;
	}

	public boolean isImported() {
		return isImported;
	}

	public String getAuthor() {
		return author;
	}

	public boolean savedToServer() {
		return !StringUtils.isEmpty(serverId);
	}

	public String getServerId() {
		return serverId;
	}

	public void setServerId(String id) {
		this.serverId = id;
	}
	
	@Override
	public boolean isUnlocked() {
		return true;
	}
}
