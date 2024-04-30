package com.j.klee.expr.impl;

import com.j.klee.expr.Expr;

public abstract class CmpExpr extends BinaryExpr {

    public CmpExpr(Expr left, Expr right) {
        super(left, right);
    }

    @Override
    public Width getWidth() {
        return Width.Bool;
    }

    public static class EqExpr extends CmpExpr {
        public EqExpr(Expr left, Expr right) {
            super(left, right);
        }

        public static Expr create(Expr left, Expr right) {
            if (left == right) {
                return new ConstantExpr(1, Width.Bool);
            } else {
                return new EqExpr(left, right);
            }
        }

        @Override
        public Kind getKind() {
            return Kind.Eq;
        }

        @Override
        public Expr rebuild(Expr[] kids) {
            return create(kids[0], kids[1]);
        }
    }
}

