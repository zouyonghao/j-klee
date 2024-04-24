package com.j.klee.expr.impl;

import com.j.klee.expr.Expr;

// TODO: implement ConstantExpr
public class ConstantExpr extends Expr {
    public ConstantExpr(int byteSize, Width width) {

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
        return null;
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
        return false;
    }

    public static Expr createPointer(int byteSize, Width width) {
        return alloc(byteSize, width);
    }

    private static Expr alloc(int byteSize, Width width) {
        return new ConstantExpr(byteSize, width);
    }
}
