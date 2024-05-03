package com.j.klee.expr.impl;

import com.j.klee.expr.Expr;

public class ZExtExpr extends Expr {

    public Expr src;
    public Width width;

    public ZExtExpr(Expr e, Width w) {
        this.src = e;
        this.width = w;
    }

    public static Expr create(Expr e, Width w) {
        Width kBits = e.getWidth();
        if (w == kBits) {
            return e;
        } else if (w.getWidth() < kBits.getWidth()) {
            return ExtractExpr.create(e, 0, w);
            // // TODO: do we need to deal with ConstantExpr?
            // } else if (e instanceof ConstantExpr c) {
            //     return c.zExt(w);
        } else {
            return ZExtExpr.alloc(e, w);
        }
    }

    private static Expr alloc(Expr e, Width w) {
        return new ZExtExpr(e, w);
    }

    @Override
    protected int compareContents(Expr other) {
        return 0;
    }

    @Override
    public Kind getKind() {
        return Kind.ZExt;
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
        return i == 0 ? src : null;
    }

    @Override
    public void print() {
        System.out.print("(ZExt ");
        src.print();
        System.out.print(")");
    }

    @Override
    public long computeHash() {
        return 0;
    }

    @Override
    public Expr rebuild(Expr[] kids) {
        return create(kids[0], width);
    }
}
