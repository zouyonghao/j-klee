package com.j.klee.expr.impl;

import com.j.klee.expr.Expr;

public class ConcatExpr extends Expr {

    public Width width;
    public Expr left, right;

    public ConcatExpr(Expr left, Expr right) {
        this.left = left;
        this.right = right;
        this.width = Width.getWidthFromInt(left.getWidth().getWidth() + right.getWidth().getWidth());
    }

    @Override
    protected int compareContents(Expr other) {
        return 0;
    }

    @Override
    public Kind getKind() {
        return Kind.Concat;
    }

    @Override
    public Width getWidth() {
        return width;
    }

    @Override
    public int getNumKinds() {
        return 2;
    }

    @Override
    public Expr getKid(int i) {
        return (i == 0) ? left : ((i == 1) ? right : null);
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
        return create(kids[0], kids[1]);
    }

    public static ConcatExpr create(Expr left, Expr right) {
        // TODO: optimize 0, merge contiguous extracts
        return alloc(left, right);
    }

    private static ConcatExpr alloc(Expr left, Expr right) {
        return new ConcatExpr(left, right);
    }
}
