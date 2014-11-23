package com.oakonell.findx.model;

import java.util.List;

import org.apache.commons.math3.fraction.Fraction;

import com.oakonell.findx.model.Level.LevelSolution;

public interface ILevel {

	public abstract LevelSolution getLevelSolution();

	public abstract List<Fraction> getSolutions();

	public abstract Stage getStage();

	public abstract String getMultilineDescription();

	public abstract boolean isUnlocked();

	public abstract void possibilyUpdateRating(int moves, int undosUsed);

	public abstract int calculateRating(int numMoves, int undosUsed);

	public abstract int getRating();

	public abstract int getMinMoves();

	public abstract List<Operation> getOperations();

	public abstract Equation getEquation();

	public abstract String getName();

	public abstract ILevel getPreviousLevel();

	public abstract ILevel getNextLevel();

	public abstract String getId();

}