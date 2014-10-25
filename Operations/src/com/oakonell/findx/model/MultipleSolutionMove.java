package com.oakonell.findx.model;

public class MultipleSolutionMove implements IMove {
	private final String description;
	private final String equation;
	private final int moveNum;

	public MultipleSolutionMove(String descriptiontext, String equation,
			int moveNum) {
		this.description = descriptiontext;
		this.equation = equation;
		this.moveNum = moveNum;
	}

	@Override
	public String getEndEquationString() {
		return equation;
	}

	@Override
	public String getDescriptiontext() {
		return description;
	}

	@Override
	public boolean isSolved() {
		return false;
	}

	@Override
	public String getMoveNumText() {
		return moveNum + "";
	}

}
