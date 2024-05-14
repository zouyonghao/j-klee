package com.j.klee.solver.impl;

import com.j.klee.core.mem.UpdateList;
import com.j.klee.expr.Expr;
import com.j.klee.solver.Builder;
import com.j.klee.solver.Query;
import com.j.klee.solver.Solver.Validity;
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

    public Validity evaluate(Query query) {
        Validity result;
        boolean isValid = computeTruth(query);
        if (isValid) {
            result = Validity.True;
        } else {
            isValid = computeTruth(query.notExpr());
            if (isValid) {
                result = Validity.False;
            } else {
                result = Validity.Unknown;
            }
        }

        return result;
    }

    public boolean mustBeTrue(Query query) {
        return computeTruth(query);
    }

    private boolean computeTruth(Query query) {
        boolean hasSolution;
        hasSolution = internalRunSolver(query, null, null);
        return !hasSolution;
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

            solver.assertExpr(ctx.not(construct(ctx, query.expr)));
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
