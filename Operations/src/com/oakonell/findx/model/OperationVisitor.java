package com.oakonell.findx.model;

import com.oakonell.findx.model.ops.Add;
import com.oakonell.findx.model.ops.Divide;
import com.oakonell.findx.model.ops.Multiply;
import com.oakonell.findx.model.ops.Subtract;
import com.oakonell.findx.model.ops.Swap;

public interface OperationVisitor {
    void visitAdd(Add add);

    void visitSubtract(Subtract sub);

    void visitMultiply(Multiply multiply);

    void visitDivide(Divide divide);

    void visitSwap(Swap swap);
}
