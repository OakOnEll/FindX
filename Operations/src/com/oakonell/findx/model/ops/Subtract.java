package com.oakonell.findx.model.ops;

import javax.annotation.concurrent.Immutable;

import org.apache.commons.math3.fraction.Fraction;

import com.oakonell.findx.model.Expression;
import com.oakonell.findx.model.Operation;
import com.oakonell.findx.model.OperationVisitor;

@Immutable
public class Subtract extends AbstractOperation {
	private final Expression expr;

	public Subtract(Expression exp) {
		expr = exp;
	}

	public Expression getExpression() {
		return expr;
	}

	@Override
	public Expression apply(Expression expression) {
		Fraction x2Coeff = OptimizedFractionUtils.subtract(
				expression.getX2Coefficient(), expr.getX2Coefficient());
		Fraction xCoeff = OptimizedFractionUtils.subtract(
				expression.getXCoefficient(), expr.getXCoefficient());
		Fraction constant = OptimizedFractionUtils.subtract(
				expression.getConstant(), expr.getConstant());
		return new Expression(x2Coeff, xCoeff, constant);
	}

	@Override
	public String toString() {
		return "Subtract " + expr.toString();
	}

	@Override
	public boolean isInverse(Operation op) {
		if (op instanceof Add) {
			Add other = (Add) op;
			return other.getExpression().equals(expr);
		} else if (op instanceof Subtract) {
			Subtract other = (Subtract) op;
			Expression otherExpr = other.getExpression();
			return otherExpr.getXCoefficient().equals(
					expr.getXCoefficient().negate())
					&& otherExpr.getConstant().equals(
							expr.getConstant().negate());
		}
		return false;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((expr == null) ? 0 : expr.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (obj == null) {
			return false;
		}
		if (getClass() != obj.getClass()) {
			return false;
		}
		Subtract other = (Subtract) obj;
		if (expr == null) {
			if (other.expr != null) {
				return false;
			}
		} else if (!expr.equals(other.expr)) {
			return false;
		}
		return true;
	}

	@Override
	public OperationType type() {
		return OperationType.SUBTRACT;
	}

	@Override
	public Operation inverse() {
		return new Add(expr);
	}

	@Override
	public void accept(OperationVisitor visitor) {
		visitor.visitSubtract(this);
	}

}
