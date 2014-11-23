package com.oakonell.findx.custom.model;

import com.oakonell.findx.model.ILevel;

public interface ICustomLevel extends ILevel {

	boolean savedToServer();

	String getServerId();

	int getSequence();

	String getAuthor();

	boolean isImported();

	long getDbId();

	void setServerId(String id);

}
