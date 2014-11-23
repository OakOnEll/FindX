package com.oakonell.findx.model;

import javax.annotation.concurrent.Immutable;

@Immutable
public class Move implements IMoveWithOperation {
	private final Equation start;
	private final Operation op;
	private int moveNum;
	private final Equation endEquation;

	public Move(Equation start, Operation op, int moveNum) {
		this.start = start;
		this.op = op;
		this.moveNum = moveNum;
		if (op == null) {
			endEquation = start;
		} else {
			endEquation = op.apply(start);
		}
	}

	public Equation getStartEquation() {
		return start;
	}

	public Operation getOperation() {
		return op;
	}

	public Equation getEndEquation() {
		return endEquation;
	}

	@Override
	public String toString() {
		if (op == null) {
			return "move " + moveNum + ": " + start.toString();
		}
		return "move " + moveNum + ": " + start.toString() + "   ---    "
				+ op.toString() + "   ->   " + getEndEquation().toString();
	}

	@Override
	public String getDescriptiontext() {
		return (op == null) ? "" : op.toString();
	}

	@Override
	public String getEndEquationString() {
		if (isSolved()) {
			return getEndEquation().toString()
					+ " <font color=\"#32cd32\"><big><bold>\u2713</bold></big></font>";// check
																						// mark
		}
		return getEndEquation().toString();

	}

	@Override
	public boolean isSolved() {
		return getEndEquation().isSolved();
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

}
