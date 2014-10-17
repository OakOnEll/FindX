package com.oakonell.findx.custom;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.math3.exception.MathParseException;
import org.apache.commons.math3.fraction.Fraction;

import android.app.Dialog;
import android.content.Context;
import android.text.Editable;
import android.text.Html;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.Spinner;
import android.widget.TextView;

import com.oakonell.findx.R;
import com.oakonell.findx.custom.model.RandomHelper;
import com.oakonell.findx.custom.widget.FractionEditText;
import com.oakonell.findx.custom.widget.FractionEditText.OnFractionChanged;
import com.oakonell.findx.model.Expression;
import com.oakonell.findx.model.Operation;
import com.oakonell.findx.model.Operation.OperationType;
import com.oakonell.findx.model.OperationVisitor;
import com.oakonell.findx.model.ops.Add;
import com.oakonell.findx.model.ops.Divide;
import com.oakonell.findx.model.ops.Multiply;
import com.oakonell.findx.model.ops.Square;
import com.oakonell.findx.model.ops.SquareRoot;
import com.oakonell.findx.model.ops.Subtract;
import com.oakonell.findx.model.ops.Swap;
import com.oakonell.findx.model.ops.WildCard;

public class OperationBuilderDialog {

	public interface OperationBuiltContinuation {
		void operationBuilt(Operation op);
	}

	private RandomHelper randomHelper;

	public OperationBuilderDialog(RandomHelper randomHelper) {
		this.randomHelper = randomHelper;
	}

	public void buildExpression(Context context, Operation existingOperation,
			final OperationBuiltContinuation continuation) {
		final Dialog dialog = new Dialog(context);

		dialog.setContentView(R.layout.operation_builder);
		dialog.setTitle(R.string.edit_operation);

		final CheckBox isWildCheck = (CheckBox) dialog
				.findViewById(R.id.is_wild);
		final Spinner operationSpinner = (Spinner) dialog
				.findViewById(R.id.operation_spinner);
		final ArrayAdapter<CharSequence> adapter = new ArrayAdapter<CharSequence>(
				context, android.R.layout.simple_spinner_item);
		adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		operationSpinner.setAdapter(adapter);

		final List<OperationType> opTypes = new ArrayList<Operation.OperationType>();
		for (OperationType each : Operation.OperationType.values()) {
			if (each == OperationType.WILD)
				continue;
			if (each == OperationType.SQUARE)
				continue;
			opTypes.add(each);
			// TODO translate the op type enums
			adapter.add(each.toString());
		}

		final FractionEditText x2CoeffText = (FractionEditText) dialog
				.findViewById(R.id.x2_coeff);
		final FractionEditText xCoeffText = (FractionEditText) dialog
				.findViewById(R.id.x_coeff);
		final TextView xLabel = (TextView) dialog.findViewById(R.id.x_label);
		final TextView x2Label = (TextView) dialog.findViewById(R.id.x2_label);
		x2Label.setText(Html.fromHtml("x<sup><small>2</small></sup> + "));

		final FractionEditText constantText = (FractionEditText) dialog
				.findViewById(R.id.constant);

		if (existingOperation == null) {
			operationSpinner.setSelection(0);
		} else {
			operationSpinner.setSelection(opTypes.indexOf(existingOperation
					.type()));
			adapter.notifyDataSetChanged();
			OperationVisitor setter = new OperationVisitor() {
				@Override
				public void visitWild(WildCard wild) {
					isWildCheck.setChecked(true);
					operationSpinner.setSelection(opTypes.indexOf(wild
							.getActual().type()));
					if (wild.isBuilt()) {
						wild.getActual().accept(this);
					}
				}

				@Override
				public void visitSwap(Swap swap) {
				}

				@Override
				public void visitSquare(Square square) {
				}

				@Override
				public void visitSquareRoot(SquareRoot squareRoot) {
				}

				@Override
				public void visitSubtract(Subtract sub) {
					x2CoeffText.setFraction(sub.getExpression()
							.getX2Coefficient());
					xCoeffText.setFraction(sub.getExpression()
							.getXCoefficient());
					constantText.setFraction(sub.getExpression().getConstant());
				}

				@Override
				public void visitAdd(Add add) {
					x2CoeffText.setFraction(add.getExpression()
							.getX2Coefficient());
					xCoeffText.setFraction(add.getExpression()
							.getXCoefficient());
					constantText.setFraction(add.getExpression().getConstant());
				}

				@Override
				public void visitMultiply(Multiply multiply) {
					constantText.setFraction(multiply.getFactor());
				}

				@Override
				public void visitDivide(Divide divide) {
					constantText.setFraction(divide.getFactor());
				}

			};
			existingOperation.accept(setter);
		}

		final Button okButton = (Button) dialog.findViewById(R.id.ok);

		OnFractionChanged onFractionChanged = new OnFractionChanged() {

			@Override
			public void fractionParseError(FractionEditText view, Editable value) {
				okButton.setEnabled(false);
			}

			@Override
			public void fractionChanged(FractionEditText view, Fraction value) {
				int itemIndex = operationSpinner.getSelectedItemPosition();
				OperationType opType = opTypes.get(itemIndex);

				switch (opType) {
				case ADD:
					// fall through
				case SUBTRACT: {
					checkAdditionInputsValidity(x2CoeffText, xCoeffText,
							constantText, okButton);
				}
					break;
				case MULTIPLY:
					// fall through
				case DIVIDE: {
					checkMultiplicationInputsValidity(constantText, okButton);
				}
					break;
				case SQUARE:
					break;
				case SQUARE_ROOT:
					break;
				case SWAP:
					break;
				default:
					throw new RuntimeException("Unsupported operation type "
							+ opType);
				}
			}
		};

		x2CoeffText.setOnFractionChanged(onFractionChanged);
		xCoeffText.setOnFractionChanged(onFractionChanged);
		constantText.setOnFractionChanged(onFractionChanged);

		operationSpinner
				.setOnItemSelectedListener(new OnItemSelectedListener() {
					@Override
					public void onItemSelected(AdapterView<?> parent,
							View view, int pos, long rowId) {
						int itemIndex = operationSpinner
								.getSelectedItemPosition();
						OperationType opType = opTypes.get(itemIndex);

						handleOpType(opType, x2CoeffText, xCoeffText, x2Label,
								xLabel, constantText, okButton);
					}

					@Override
					public void onNothingSelected(AdapterView<?> arg0) {
					}
				});

		Button randomize = (Button) dialog.findViewById(R.id.random_op);
		if (randomHelper == null) {
			randomize.setVisibility(View.GONE);
			isWildCheck.setVisibility(View.GONE);
		}
		randomize.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View view) {
				int opTypeIndex = randomHelper.nextInt(opTypes.size());
				OperationType operationType = opTypes.get(opTypeIndex);
				switch (operationType) {
				case ADD:
					// fall through
				case SUBTRACT:
					// ignore x^2 coefficient for randoms
					Expression expr = randomHelper.randomExpression();
					xCoeffText.setFraction(expr.getXCoefficient());
					constantText.setFraction(expr.getConstant());
					break;
				case DIVIDE:
					// fall through
				case MULTIPLY:
					Fraction randSmallFraction = randomHelper
							.randSmallFraction();
					if (randSmallFraction.compareTo(Fraction.ZERO) == 0) {
						randSmallFraction = Fraction.ONE;
					}
					constantText.setFraction(randSmallFraction);
					break;
				case SQUARE:
					break;
				case SQUARE_ROOT:
					break;
				case SWAP:
					break;
				default:
					throw new RuntimeException("Unhandled operation type "
							+ operationType);
				}

				operationSpinner.setSelection(opTypes.indexOf(operationType));
				adapter.notifyDataSetChanged();
			}
		});

		okButton.setEnabled(false);
		okButton.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				int itemIndex = operationSpinner.getSelectedItemPosition();
				OperationType opType = opTypes.get(itemIndex);

				Operation operation = null;
				// exception from getFraction is handled by checking validity
				// first.
				switch (opType) {
				case ADD: {
					if (!checkAdditionInputsValidity(x2CoeffText, xCoeffText,
							constantText, okButton)) {
						return;
					}
					Fraction x2Coeff = x2CoeffText.getFraction();
					Fraction xCoeff = xCoeffText.getFraction();
					Fraction constant = constantText.getFraction();

					operation = new Add(new Expression(x2Coeff, xCoeff,
							constant));
				}
					break;
				case SUBTRACT: {
					if (!checkAdditionInputsValidity(x2CoeffText, xCoeffText,
							constantText, okButton)) {
						return;
					}
					Fraction x2Coeff = x2CoeffText.getFraction();
					Fraction xCoeff = xCoeffText.getFraction();
					Fraction constant = constantText.getFraction();

					operation = new Subtract(new Expression(x2Coeff, xCoeff,
							constant));
				}
					break;
				case MULTIPLY: {
					if (!checkMultiplicationInputsValidity(constantText,
							okButton)) {
						return;
					}
					Fraction constant = constantText.getFraction();

					operation = new Multiply(constant);
				}
					break;
				case DIVIDE: {
					if (!checkMultiplicationInputsValidity(constantText,
							okButton)) {
						return;
					}

					Fraction constant = constantText.getFraction();
					operation = new Divide(constant);
				}
					break;
				case SQUARE:
					operation = new Square();
					break;
				case SQUARE_ROOT:
					operation = new SquareRoot();
					break;
				case SWAP:
					operation = new Swap();
					break;
				default:
					throw new RuntimeException("Unsupported operation type "
							+ opType);
				}

				if (isWildCheck.isChecked()) {
					WildCard wildOperation = new WildCard();
					wildOperation.setActual(operation);
					wildOperation.setIsBuilt(true);
					operation = wildOperation;
				}

				continuation.operationBuilt(operation);
				dialog.dismiss();
			}
		});

		Button cancelButton = (Button) dialog.findViewById(R.id.cancel);
		cancelButton.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				dialog.cancel();
			}
		});

		dialog.show();

	}

	private boolean checkMultiplicationInputsValidity(
			final FractionEditText constantText, final Button okButton) {
		boolean inputsValid = constantText.isValid();
		boolean hasAValue = constantText.getEditableText().length() > 0;
		inputsValid = inputsValid && hasAValue;
		if (inputsValid) {
			try {
				Fraction fraction = constantText.getFraction();
				if (fraction != null && fraction.compareTo(Fraction.ZERO) == 0) {
					constantText.setError(constantText.getContext().getString(
							R.string.cannot_be_zero));
					inputsValid = false;
				}
			} catch (MathParseException e) {
				inputsValid = false;
			}
		}
		okButton.setEnabled(inputsValid);
		return inputsValid;
	}

	private boolean checkAdditionInputsValidity(
			final FractionEditText x2CoeffText,
			final FractionEditText xCoeffText,
			final FractionEditText constantText, final Button okButton) {
		boolean inputsValid = xCoeffText.isValid() && constantText.isValid()
				&& x2CoeffText.isValid();
		inputsValid = inputsValid
				&& (x2CoeffText.getEditableText().length() > 0
						|| xCoeffText.getEditableText().length() > 0 || constantText
						.getEditableText().length() > 0);
		okButton.setEnabled(inputsValid);
		return inputsValid;
	}

	private void handleOpType(OperationType opType,
			final FractionEditText x2CoeffText,
			final FractionEditText xCoeffText, final TextView x2Label,
			final TextView xLabel, final FractionEditText constantText,
			final Button okButton) {
		switch (opType) {
		case ADD:
			// fall through
		case SUBTRACT: {
			x2CoeffText.setVisibility(View.VISIBLE);
			x2Label.setVisibility(View.VISIBLE);
			xCoeffText.setVisibility(View.VISIBLE);
			xLabel.setVisibility(View.VISIBLE);
			constantText.setVisibility(View.VISIBLE);

			checkAdditionInputsValidity(x2CoeffText, xCoeffText, constantText,
					okButton);

		}
			break;
		case MULTIPLY:
			// fall through
		case DIVIDE: {
			x2CoeffText.setVisibility(View.GONE);
			x2Label.setVisibility(View.GONE);
			xCoeffText.setVisibility(View.GONE);
			xLabel.setVisibility(View.GONE);
			constantText.setVisibility(View.VISIBLE);

			checkMultiplicationInputsValidity(constantText, okButton);
		}
			break;
		case SQUARE:
			// fallthrough
		case SQUARE_ROOT:
			// fallthrough
		case SWAP:
			x2CoeffText.setVisibility(View.GONE);
			x2Label.setVisibility(View.GONE);
			xCoeffText.setVisibility(View.GONE);
			xLabel.setVisibility(View.GONE);
			constantText.setVisibility(View.GONE);

			okButton.setEnabled(true);
			break;
		default:
			throw new RuntimeException("Unsupported operation type " + opType);
		}
	}

}
