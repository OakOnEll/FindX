package com.oakonell.findx.model.ops;

import java.util.List;

import com.oakonell.findx.model.Equation;
import com.oakonell.findx.model.MoveResult;
import com.oakonell.findx.model.Operation;
import com.oakonell.findx.model.OperationVisitor;

public class WildCard implements Operation {
	private Operation actual;
	private boolean isBuilt;

	public Operation getActual() {
		return actual;
	}

	public void setActual(Operation actual) {
		this.actual = actual;
	}

	@Override
	public Equation apply(Equation equation) {
		if (actual == null) {
			throw new IllegalStateException(
					"Actual operation for wildcard has not been assigned");
		}
		return actual.apply(equation);
	}

	@Override
	public MoveResult applyMove(Equation equation, int moveNum, List<Operation> operations) {
		if (actual == null) {
			throw new IllegalStateException(
					"Actual operation for wildcard has not been assigned");
		}
		return actual.applyMove(equation, moveNum, operations);
	}

	@Override
	public boolean isInverse(Operation op) {
		if (actual == null) {
			return false;
		}
		return actual.isInverse(op);
	}

	@Override
	public Operation inverse() {
		if (actual == null) {
			return null;
		}
		return actual.inverse();
	}

	@Override
	public OperationType type() {
		return OperationType.WILD;
	}

	@Override
	public boolean canApply(Equation equation) {
		if (actual == null) {
			return true;
		}
		return actual.canApply(equation);
	}

	@Override
	public void accept(OperationVisitor visitor) {
		visitor.visitWild(this);
	}

	@Override
	public String toString() {
		if (actual == null || !isBuilt) {
			return "?";
		}
		return "?- " + actual.toString();
	}

	public boolean isBuilt() {
		return isBuilt;
	}

	public void setIsBuilt(boolean isBuilt) {
		this.isBuilt = isBuilt;
	}

	@Override
	public Operation afterUsed() {
		return actual;
	}

}
