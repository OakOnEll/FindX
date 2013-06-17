package com.oakonell.findx.custom.widget;

import org.apache.commons.math3.exception.MathArithmeticException;
import org.apache.commons.math3.exception.MathParseException;
import org.apache.commons.math3.fraction.Fraction;
import org.apache.commons.math3.fraction.FractionFormat;

import android.content.Context;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.AttributeSet;
import android.widget.EditText;

/**
 * EditText Extension to be used in order to create forms in android.
 * 
 * borrowed from Andrea Baccega <me@andreabaccega.com>
 * (https://github.com/vekexasia/android-form-edittext)
 * 
 * alternative?
 * https://rxdroid.googlecode.com/svn-history/r51/trunk/src/at/caspase
 * /rxdroid/EditFraction.java
 */
public class FractionEditText extends EditText {

    public interface OnFractionChanged {
        void fractionChanged(FractionEditText view, Fraction value);

        void fractionParseError(FractionEditText view, Editable value);
    }

    private OnFractionChanged onFractionChanged;

    // private static class FractionInputFilter implements InputFilter {
    //
    // private static final Pattern mPattern =
    // Pattern.compile("[-+]?[0-9]+(/[0-9]+)?");
    //
    // @Override
    // public CharSequence filter(CharSequence source, int start, int end,
    // Spanned dest, int dstart, int dend) {
    // Matcher matcher = mPattern.matcher(source);
    // if (!matcher.matches()) {
    // return "";
    // }
    // return null;
    // }
    //
    // }

    /**
     * This should be used with {@link #addTextChangedListener(TextWatcher)}. It
     * fixes the non-hiding error popup behaviour.
     */
    private TextWatcher errorPopupRemoverTextWatcher = new TextWatcher() {
        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) {
            if (s != null && s.length() > 0 && getError() != null) {
                setError(null);
            }
        }

        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {
        }

        @Override
        public void afterTextChanged(Editable s) {
        }
    };

    public FractionEditText(Context context) {
        super(context);
        configure();
    }

    private void configure() {
        addTextChangedListener(errorPopupRemoverTextWatcher);

        addTextChangedListener(new TextWatcher() {
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void afterTextChanged(Editable s) {
                if (onFractionChanged != null) {
                    try {
                        Fraction fraction = getFraction();
                        onFractionChanged.fractionChanged(FractionEditText.this, fraction);
                    } catch (MathParseException e) {
                        onFractionChanged.fractionParseError(FractionEditText.this, getText());
                    }
                }
            }
        });

        // setFilters(new InputFilter[] { new FractionInputFilter() });
    }

    public FractionEditText(Context context, AttributeSet attrs) {
        super(context, attrs);
        configure();
    }

    public FractionEditText(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        configure();
    }

    public void setFraction(Fraction f) {
        setText(f.toString());
    }

    public Fraction getFraction() {
        FractionFormat format = new FractionFormat();
        String string = getEditableText().toString();
        if (TextUtils.isEmpty(string)) {
            return null;
        }

        try {
            Fraction fraction = format.parse(string);
            return fraction;
        } catch (MathParseException e) {
            setError("Invalid Fraction value '" + string + "'");
            throw e;
        } catch (MathArithmeticException e) {
            setError("Invalid Fraction value '" + string + "'");
            throw new MathParseException(string, 0);
        }
    }

    public boolean isValid() {
        FractionFormat format = new FractionFormat();
        String string = getEditableText().toString();
        if (TextUtils.isEmpty(string)) {
            return true;
        }

        try {
            format.parse(string);
            return true;
        } catch (MathParseException e) {
            setError("Invalid Fraction value '" + string + "'");
            return false;
        } catch (MathArithmeticException e) {
            setError("Invalid Fraction value '" + string + "'");
            return false;
        }
    }

    public void setOnFractionChanged(OnFractionChanged listener) {
        onFractionChanged = listener;
    }
}
