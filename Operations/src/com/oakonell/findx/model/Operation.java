package com.oakonell.findx.model;

import java.util.List;

public interface Operation {
	public enum OperationType {
		ADD, SUBTRACT, MULTIPLY, DIVIDE, SWAP, SQUARE, SQUARE_ROOT, WILD, FACTOR, DEFACTOR;
	}

	Equation apply(Equation equation);

	MoveResult applyMove(Equation equation, int moveNum,
			List<Operation> operations, Operation appliedOrNull);

	boolean isInverse(Operation op);

	Operation inverse();

	OperationType type();

	boolean canApply(Equation equation);

	void accept(OperationVisitor visitor);

	// Wild Cards are not built at the start
	boolean isBuilt();

	Operation afterUsed();

	boolean isAppliableWith(List<Operation> operations);

}
