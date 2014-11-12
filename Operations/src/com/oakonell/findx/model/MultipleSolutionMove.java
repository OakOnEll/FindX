package com.oakonell.findx.model;

public class MultipleSolutionMove implements IMoveWithOperation {
	private final Operation operation;
	private final String equation;
	private final Equation startEquation;
	private int moveNum;

	public MultipleSolutionMove(Equation startEquation, Operation operation, String equation,
			int moveNum) {
		this.startEquation = startEquation;
		this.operation = operation;
		this.equation = equation;
		this.moveNum = moveNum;
	}

	@Override
	public String getEndEquationString() {
		return equation;
	}

	@Override
	public String getDescriptiontext() {
		return operation.toString();
	}

	@Override
	public boolean isSolved() {
		return false;
	}

	@Override
	public String getMoveNumText() {
		return moveNum + "";
	}

	public void incrementMoveNum() {
		moveNum++;
	}

	public void decrementMoveNum() {
		moveNum--;
	}

	public Operation getOperation() {
		return operation;
	}

	public String toString() {
		return "move " + moveNum + ": " + "   ---    " + operation.toString()
				+ "   ->   " + getEndEquationString();
	}

	@Override
	public Equation getStartEquation() {
		return startEquation;
	}
}
