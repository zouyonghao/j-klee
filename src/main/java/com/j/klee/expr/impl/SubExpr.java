package com.j.klee.expr.impl;

import com.j.klee.expr.Expr;

public class SubExpr extends Expr {

    public Expr left;
    public Expr right;

    public SubExpr(Expr left, Expr right) {
        this.left = left;
        this.right = right;
    }

    @Override
    protected int compareContents(Expr other) {
        return 0;
    }

    @Override
    public Kind getKind() {
        return Kind.Sub;
    }

    @Override
    public Width getWidth() {
        return left.getWidth();
    }

    @Override
    public int getNumKinds() {
        return 2;
    }

    @Override
    public Expr getKid(int i) {
        if (i == 0) {
            return left;
        } else if (i == 1) {
            return right;
        }
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

    public static SubExpr create(Expr left, Expr right) {
        if (left.getWidth() != right.getWidth()) {
            throw new IllegalStateException("Left and right must have same width");
        }
        return new SubExpr(left, right);
    }
}
