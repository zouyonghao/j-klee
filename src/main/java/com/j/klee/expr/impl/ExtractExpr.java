package com.j.klee.expr.impl;

import com.j.klee.expr.Expr;

public class ExtractExpr extends Expr {

    public Expr expr;
    int offset;
    Width width;

    public ExtractExpr(Expr e, int o, Width w) {
        this.expr = e;
        this.offset = o;
        this.width = w;
    }

    public static Expr alloc(Expr e, int o, Width w) {
        return new ExtractExpr(e, o, w);
    }

    public static Expr create(Expr expr, int off, Width w) {
        Width kw = expr.getWidth();
        if (w == kw) {
            return expr;
            // // TODO: do we need to deal with ConstantExpr?
            // }
            // else if (expr instanceof ConstantExpr c) {
            //     return c.extract(off, w);
        } else {
            return new ExtractExpr(expr, off, w);
        }
    }

    @Override
    protected int compareContents(Expr other) {
        return 0;
    }

    @Override
    public Kind getKind() {
        return Kind.Extract;
    }

    @Override
    public Width getWidth() {
        return width;
    }

    @Override
    public int getNumKinds() {
        return 1;
    }

    @Override
    public Expr getKid(int i) {
        return expr;
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
}
