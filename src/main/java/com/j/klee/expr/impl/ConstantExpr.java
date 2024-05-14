package com.j.klee.expr.impl;

import com.j.klee.expr.Expr;
import com.j.klee.utils.LLVMUtils;
import org.bytedeco.llvm.LLVM.LLVMValueRef;
import org.bytedeco.llvm.global.LLVM;

public class ConstantExpr extends Expr {

    LLVMValueRef constIntValue;

    public ConstantExpr(long value, Width width) {
        constIntValue = LLVMUtils.getAPIntFromLong(value, width.getWidth());
    }

    public ConstantExpr(LLVMValueRef constIntValue) {
        this.constIntValue = constIntValue;
    }

    @Override
    protected int compareContents(Expr other) {
        return 0;
    }

    @Override
    public Kind getKind() {
        return null;
    }

    @Override
    public Width getWidth() {
        return Width.getWidthFromInt(LLVM.LLVMGetIntTypeWidth(LLVM.LLVMTypeOf(constIntValue)));
    }

    @Override
    public int getNumKinds() {
        return 0;
    }

    @Override
    public Expr getKid(int i) {
        return null;
    }

    @Override
    public void print() {
        System.out.print("(Constant " + LLVM.LLVMConstIntGetZExtValue(constIntValue) + ")");
    }

    @Override
    public long computeHash() {
        return 0;
    }

    @Override
    public Expr rebuild(Expr[] kids) {
        return null;
    }

    // TODO: should we use this or BoolExpr
    public boolean isTrue() {
        return getWidth() == Width.Bool && getZExtValue() == 1;
    }

    @Override
    public boolean isZero() {
        // TODO: getAPValue().isMinValue()
        return LLVM.LLVMConstIntGetZExtValue(constIntValue) == 0;
    }

    public static ConstantExpr create(int v, Width width) {
        return alloc(v, width);
    }

    public static ConstantExpr createPointer(long value, Width width) {
        return alloc(value, width);
    }

    public static ConstantExpr alloc(long value, Width width) {
        return new ConstantExpr(value, width);
    }

    public Expr extract(int off, Width w) {
        throw new IllegalStateException("Not implemented");
    }

    public long getZExtValue() {
        return LLVM.LLVMConstIntGetZExtValue(constIntValue);
    }
}
