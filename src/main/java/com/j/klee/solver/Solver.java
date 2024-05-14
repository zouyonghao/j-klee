package com.j.klee.solver;

import com.j.klee.expr.Expr;
import com.j.klee.expr.impl.BoolExpr;
import com.j.klee.expr.impl.BoolNotExpr;
import com.j.klee.expr.impl.ConstantExpr;
import com.j.klee.solver.impl.SolverImpl;

import java.util.List;

public class Solver {

    SolverImpl solver = new SolverImpl();

    public Validity evaluate(List<Expr> constraints, Expr condition) {
        if (condition instanceof ConstantExpr e) {
            return e.isTrue() ? Validity.True : Validity.False;
        }
        if (condition instanceof BoolExpr be) {
            return be.isTrue() ? Validity.True : Validity.False;
        }
        return solver.evaluate(new Query(constraints, condition));
    }

    public boolean mayBeTrue(List<Expr> constraints, Expr expr) {
        return !mustBeFalse(constraints, expr);
    }

    public boolean mustBeFalse(List<Expr> constraints, Expr expr) {
        return mustBeTrue(constraints, new BoolNotExpr(expr));
    }

    public boolean mustBeTrue(List<Expr> constraints, Expr expr) {
        if (expr instanceof ConstantExpr || expr instanceof BoolExpr) {
            return expr.isTrue();
        }

        return solver.mustBeTrue(new Query(constraints, expr));
    }

    public enum Validity {
        True, False, Unknown
    }
}
