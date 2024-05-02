package com.j.klee.solver;

import com.j.klee.expr.Expr;
import io.ksmt.expr.KExpr;
import io.ksmt.sort.KBoolSort;

public interface Builder {
    KExpr<KBoolSort> construct(Expr e);
}
