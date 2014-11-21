package com.oakonell.findx.model;

public class FormattedMove extends Move {

	private String description;

	public FormattedMove(Equation start, Operation op, String description,
			int moveNum) {
		super(start, op, moveNum);
		this.description = description;
	}

	@Override
	public String getEndEquationString() {
		return description;
	}
	@Override
	public Equation getEndEquation() {
		return getStartEquation();
	}

}
