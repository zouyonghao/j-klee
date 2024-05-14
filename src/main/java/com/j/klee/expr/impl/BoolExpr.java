package com.j.klee.expr.impl;

import com.j.klee.expr.Expr;

public class BoolExpr extends Expr {

    public boolean value;

    public BoolExpr(boolean v) {
        value = v;
    }

    @Override
    protected int compareContents(Expr other) {
        return 0;
    }

    @Override
    public Kind getKind() {
        return Kind.Bool;
    }

    @Override
    public Width getWidth() {
        return Width.Bool;
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
        System.out.print(value ? "true" : "false");
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
        return value;
    }
}
