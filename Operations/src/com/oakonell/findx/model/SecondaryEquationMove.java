package com.oakonell.findx.model;

public class SecondaryEquationMove implements IMove {
	private Equation equation;
	private int num;

	public SecondaryEquationMove(Equation equationInWaiting, int num) {
		this.equation = equationInWaiting;
		this.num = num;
	}

	@Override
	public String getEndEquationString() {
		return equation.toString();
	}

	@Override
	public String getDescriptiontext() {
		return "Solution " + num;
	}

	@Override
	public boolean isSolved() {
		return false;
	}

	@Override
	public String getMoveNumText() {
		return "";
	}

	public Equation getStartEquation() {
		return equation;
	}

}
