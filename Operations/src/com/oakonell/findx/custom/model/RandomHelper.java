package com.oakonell.findx.custom.model;

import java.util.Random;

import org.apache.commons.math3.fraction.Fraction;

import com.oakonell.findx.model.Expression;
import com.oakonell.findx.model.Operation;

public class RandomHelper {
    private Random rand = new Random();

    public Fraction randFraction() {
        // TODO come up with good random distribution ranges
        return new Fraction(rand.nextInt(25) - 13);
    }

    public void addRandomMoves(CustomLevelBuilder builder, int numMoves) {
        // loop over the number of random moves
        int numOps = builder.getOperations().size();
        if (numOps == 0 || numMoves == 0) {
            return;
        }
        for (int i = 0; i < numMoves; i++) {
            int opIndex = rand.nextInt(numOps);
            Operation operation = builder.getOperations().get(opIndex);
            builder.apply(operation);
        }
    }

    public int nextInt(int length) {
        return rand.nextInt(length);
    }

    public Fraction randSmallFraction() {
        // TODO come up with good random distribution ranges
        return new Fraction(rand.nextInt(8) - 4);
    }

    public Expression randomExpression() {
        // this can produce difficult expressions...
        return new Expression(randSmallFraction(), randFraction());
    }

}
