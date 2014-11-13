package com.oakonell.findx.model;

public class MoveResult {
	private IMove move;
	private IMove secondary1Move;
	private IMove secondary2Move;

	public MoveResult(Move move) {
		this.move = move;
	}

	public MoveResult(IMove imove, IMove secondary1Move, IMove secondary2Move) {
		this.move = imove;
		this.secondary1Move = secondary1Move;
		this.secondary2Move = secondary2Move;
	}

	public IMove getPrimaryMove() {
		return move;
	}

	public boolean hasMultiple() {
		return secondary1Move != null;
	}

	public IMove getSecondary1() {
		return secondary1Move;
	}

	public IMove getSecondary2() {
		return secondary2Move;
	}

	public boolean isSolved() {
		if (hasMultiple())
			return false;
		if (!(move instanceof Move))
			return false;
		return ((Move) move).getEndEquation().isSolved();
	}

	public Equation getPrimaryEndEquation() {
		if (!(move instanceof Move))
			return null;
		return ((Move) move).getEndEquation();
	}

}
