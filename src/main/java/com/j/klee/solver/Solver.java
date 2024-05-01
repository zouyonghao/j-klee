package com.j.klee.solver;

import com.j.klee.expr.Expr;

import java.util.List;

public class Solver {
    public SolverEvaluateResult evaluate(List<Expr> constraints, Expr condition) {
        return new SolverEvaluateResult(true, Validity.Unknown);
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
