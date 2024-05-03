package com.j.klee.solver;

import com.j.klee.expr.Expr;
import com.j.klee.expr.impl.BoolNotExpr;
import com.j.klee.expr.impl.ConstantExpr;

import java.util.List;

public class Query {
    public List<Expr> constraints;
    public Expr expr;

    public Query(List<Expr> constraints, Expr expr) {
        this.constraints = constraints;
        this.expr = expr;
    }

    public Query withExpr(Expr expr) {
        return new Query(constraints, expr);
    }

    public Query withFalse() {
        return new Query(constraints, ConstantExpr.create(0, Expr.Width.Bool));
    }

    public Query negateExpr() {
        return withExpr(Expr.createIsZero(expr));
    }

    public Query notExpr() {
        return withExpr(BoolNotExpr.create(expr));
    }

    // TODO: dump
}
