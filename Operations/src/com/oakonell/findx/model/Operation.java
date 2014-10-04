package com.oakonell.findx.model;

import com.parse.ParseObject;

public interface Operation {
    public enum OperationType {
        ADD, SUBTRACT, MULTIPLY, DIVIDE, SWAP;
    }

    Equation apply(Equation equation);

    boolean isInverse(Operation op);

    Operation inverse();

    OperationType type();

    void accept(OperationVisitor visitor);

	void addToParseObject(ParseObject parseOp);
}
