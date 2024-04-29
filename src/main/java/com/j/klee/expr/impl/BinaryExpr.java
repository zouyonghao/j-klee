package com.j.klee.expr.impl;

import com.j.klee.expr.Expr;

public abstract class BinaryExpr extends Expr {

    public Expr left;
    public Expr right;

    public BinaryExpr(Expr left, Expr right) {
        this.left = left;
        this.right = right;
    }

    @Override
    protected int compareContents(Expr other) {
        return 0;
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

    public static class SubExpr extends BinaryExpr {

        public SubExpr(Expr left, Expr right) {
            super(left, right);
        }

        @Override
        public Kind getKind() {
            return Kind.Sub;
        }

        @Override
        public Expr rebuild(Expr[] kids) {
            return create(kids[0], kids[1]);
        }

        public static Expr create(Expr left, Expr right) {
            if (left.getWidth() != right.getWidth()) {
                throw new IllegalStateException("Left and right must have same width");
            }
            return new SubExpr(left, right);
        }
    }

    public static class AddExpr extends BinaryExpr {
        public AddExpr(Expr left, Expr right) {
            super(left, right);
        }

        @Override
        public Kind getKind() {
            return Kind.Add;
        }

        @Override
        public Expr rebuild(Expr[] kids) {
            return create(kids[0], kids[1]);
        }

        public static Expr create(Expr left, Expr right) {
            if (left.getWidth() != right.getWidth()) {
                throw new IllegalStateException("Left and right must have same width");
            }
            return new AddExpr(left, right);
        }
    }
}
