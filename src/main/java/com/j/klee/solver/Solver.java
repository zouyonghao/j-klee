package com.j.klee.solver;

import com.j.klee.expr.Expr;
import com.j.klee.expr.impl.ConstantExpr;
import com.j.klee.solver.impl.SolverImpl;

import java.util.List;

public class Solver {

    SolverImpl solver = new SolverImpl();

    public SolverEvaluateResult evaluate(List<Expr> constraints, Expr condition) {
        if (condition instanceof ConstantExpr e) {
            return new SolverEvaluateResult(true, e.isTrue() ? Validity.True : Validity.False);
        }
        return solver.evaluate(new Query(constraints, condition));
    }

    public enum Validity {
        True, False, Unknown
    }

    public static class SolverEvaluateResult {
        public boolean solved;
        public Validity validity;

        public SolverEvaluateResult(boolean solved, Validity validity) {
            this.solved = solved;
            this.validity = validity;
        }
    }
}
