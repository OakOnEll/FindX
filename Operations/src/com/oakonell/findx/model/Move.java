package com.oakonell.findx.model;

import javax.annotation.concurrent.Immutable;

@Immutable
public class Move {
    private final Equation start;
    private final Operation op;

    public Move(Equation start, Operation op) {
        this.start = start;
        this.op = op;
    }

    public Equation getStartEquation() {
        return start;
    }

    public Operation getOperation() {
        return op;
    }

    public Equation getEndEquation() {
        if (op == null) {
            return start;
        }
        return op.apply(start);
    }

    @Override
    public String toString() {
        if (op == null) {
            return "move: " + start.toString();
        }
        return "move: " + start.toString() + "   ---    " + op.toString() + "   ->   " + getEndEquation().toString();
    }

}
