package com.oakonell.findx.model;

public interface IMove {

	Equation getStartEquation();
	
	String getEndEquationString();

	String getDescriptiontext();

	boolean isSolved();

	String getMoveNumText();

	void incrementMoveNum();
	void decrementMoveNum();
}
