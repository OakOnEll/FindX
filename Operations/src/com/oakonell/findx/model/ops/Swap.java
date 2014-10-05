package com.oakonell.findx.model.ops;

import javax.annotation.concurrent.Immutable;

import com.oakonell.findx.model.Equation;
import com.oakonell.findx.model.Operation;
import com.oakonell.findx.model.OperationVisitor;

@Immutable
public class Swap implements Operation {

	@Override
	public Equation apply(Equation eq) {
		return new Equation(eq.getRhs(), eq.getLhs());
	}

	@Override
	public String toString() {
		return "Swap";
	}

	@Override
	public boolean isInverse(Operation op) {
		return op instanceof Swap;
	}

	@Override
	public boolean equals(Object o) {
		return o instanceof Swap;
	}

	@Override
	public int hashCode() {
		return 17;
	}

	@Override
	public OperationType type() {
		return OperationType.SWAP;
	}

	@Override
	public Operation inverse() {
		return this;
	}

	@Override
	public void accept(OperationVisitor visitor) {
		visitor.visitSwap(this);
	}

}
