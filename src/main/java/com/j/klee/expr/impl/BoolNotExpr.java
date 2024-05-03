package com.j.klee.expr.impl;

import com.j.klee.expr.Expr;

public class BoolNotExpr extends Expr {
    public Expr src;

    public BoolNotExpr(Expr e) {
        assert (e.getWidth() == Width.Bool);
        this.src = e;
    }

    @Override
    protected int compareContents(Expr other) {
        return 0;
    }

    @Override
    public Kind getKind() {
        return Kind.BoolNot;
    }

    @Override
    public Width getWidth() {
        return Width.Bool;
    }

    @Override
    public int getNumKinds() {
        return 1;
    }

    @Override
    public Expr getKid(int i) {
        return i == 0 ? src : null;
    }

    @Override
    public void print() {
        System.out.print("(Not ");
        src.print();
        System.out.print(")");
    }

    @Override
    public long computeHash() {
        return 0;
    }

    @Override
    public Expr rebuild(Expr[] kids) {
        return create(kids[0]);
    }

    public static Expr create(Expr e) {
        return new BoolNotExpr(e);
    }
}
