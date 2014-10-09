package com.oakonell.findx.model;


public interface Operation {
    public enum OperationType {
        ADD, SUBTRACT, MULTIPLY, DIVIDE, SWAP, SQUARE, SQUARE_ROOT;
    }

    Equation apply(Equation equation);

    boolean isInverse(Operation op);

    Operation inverse();

    OperationType type();

    boolean canApply(Equation equation);
    
    void accept(OperationVisitor visitor);

}
