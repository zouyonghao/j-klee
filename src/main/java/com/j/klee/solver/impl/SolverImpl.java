package com.j.klee.solver.impl;

import com.j.klee.core.mem.UpdateList;
import com.j.klee.expr.Expr;
import com.j.klee.solver.Builder;
import com.j.klee.solver.Query;
import com.j.klee.solver.Solver;
import io.ksmt.KContext;
import io.ksmt.expr.KExpr;
import io.ksmt.solver.KSolver;
import io.ksmt.solver.KSolverStatus;
import io.ksmt.solver.z3.KZ3Solver;
import io.ksmt.sort.KBoolSort;
import kotlin.time.DurationUnit;

import java.util.List;

import static kotlin.time.DurationKt.toDuration;

public class SolverImpl {

    private Builder builder;

    public static class ComputeTruthResult {
        public boolean solved;
        public boolean isValid;
    }

    public Solver.SolverEvaluateResult evaluate(Query query) {
        Solver.SolverEvaluateResult result = new Solver.SolverEvaluateResult(false, Solver.Validity.Unknown);
        ComputeTruthResult computeTruthResult = computeTruth(query);
        result.solved = computeTruthResult.solved;
        if (!result.solved) {
            return result;
        }
        if (computeTruthResult.isValid) {
            result.validity = Solver.Validity.True;
        } else {
            computeTruthResult = computeTruth(query.negateExpr());
            result.solved = computeTruthResult.solved;
            if (!result.solved) {
                return result;
            } else {
                if (computeTruthResult.isValid) {
                    result.validity = Solver.Validity.False;
                } else {
                    result.validity = Solver.Validity.Unknown;
                }
            }
        }

        return result;
    }

    private ComputeTruthResult computeTruth(Query query) {
        ComputeTruthResult result = new ComputeTruthResult();
        result.solved = true;
        boolean hasSolution;
        try {
            hasSolution = internalRunSolver(query, null, null);
            result.isValid = !hasSolution;
        } catch (Exception e) {
            System.out.println("get exception: " + e);
            result.solved = false;
        }
        return result;
    }

    /**
     * @param query
     * @param objects
     * @param values
     * @return true when the query is SAT
     */
    private boolean internalRunSolver(Query query, UpdateList.Array[] objects, List<String> values) {
        try (final KContext ctx = new KContext(); final KSolver<?> solver = new KZ3Solver(ctx)) {
            builder = new BitVectorBuilder(ctx);
            for (Expr constraint : query.constraints) {
                solver.assertExpr(construct(ctx, constraint));
            }

            // solver.assertExpr(ctx.not(construct(ctx, query.expr)));
            long solverTimeout = toDuration(1, DurationUnit.SECONDS);
            final KSolverStatus status = solver.check(solverTimeout);
            if (status == KSolverStatus.SAT) {
                return true;
            } else if (status == KSolverStatus.UNSAT) {
                return false;
            } else {
                throw new IllegalStateException("query not solved!");
            }
        }
    }

    private KExpr<KBoolSort> construct(KContext ctx, Expr constraint) {
        return builder.construct(constraint);
    }
}
