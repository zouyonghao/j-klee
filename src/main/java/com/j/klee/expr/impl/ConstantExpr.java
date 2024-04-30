package com.j.klee.expr.impl;

import com.j.klee.core.Context;
import com.j.klee.expr.Expr;
import com.j.klee.utils.LLVMUtils;
import org.bytedeco.llvm.LLVM.LLVMContextRef;
import org.bytedeco.llvm.LLVM.LLVMValueRef;
import org.bytedeco.llvm.global.LLVM;

import static org.bytedeco.llvm.global.LLVM.LLVMContextCreate;

// TODO: implement ConstantExpr
public class ConstantExpr extends Expr {

    LLVMValueRef constIntValue;

    public ConstantExpr(int value, Width width) {
        constIntValue = LLVMUtils.getAPIntFromInt(value, width.getWidth());
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

    public boolean isTrue() {
        return getWidth() == Width.Bool; // TODO: value.getBoolValue() == true
    }

    @Override
    public boolean isZero() {
        // TODO: getAPValue().isMinValue()
        return LLVM.LLVMConstIntGetZExtValue(constIntValue) == 0;
    }

    public static ConstantExpr create(int idx, Width width) {
        return alloc(idx, width);
    }

    public static Expr createPointer(int byteSize, Width width) {
        return alloc(byteSize, width);
    }

    public static ConstantExpr alloc(int value, Width width) {
        return new ConstantExpr(value, width);
    }

    public Expr extract(int off, Width w) {
        throw new IllegalStateException("Not implemented");
    }
}
