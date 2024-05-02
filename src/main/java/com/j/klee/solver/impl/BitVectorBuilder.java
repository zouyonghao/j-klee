package com.j.klee.solver.impl;

import com.j.klee.expr.Expr;
import com.j.klee.expr.impl.CmpExpr.EqExpr;
import com.j.klee.expr.impl.ConstantExpr;
import com.j.klee.solver.Builder;
import io.ksmt.KContext;
import io.ksmt.expr.KExpr;
import io.ksmt.sort.KBoolSort;


public class BitVectorBuilder implements Builder {
    private final KContext ctx;

    public static class WidthReference {
        public Expr.Width width;
    }

    public BitVectorBuilder(KContext ctx) {
        this.ctx = ctx;
    }

    @Override
    public KExpr<KBoolSort> construct(Expr e) {
        // TODO: cache
        WidthReference width = new WidthReference();
        KExpr<?> result = constructActual(e, width);
        // TODO: is there an elegant way to check the generic type?
        return (KExpr<KBoolSort>) result;
    }

    private KExpr<?> constructActual(Expr e, WidthReference width) {
        switch (e) {
            case ConstantExpr constantExpr -> {
                if (width.width == Expr.Width.Bool) {
                    return constantExpr.isTrue() ? ctx.mkTrue() : ctx.mkFalse();
                }
                // TODO: int32?
                if (width.width.getWidth() <= Expr.Width.Int64.getWidth()) {
                    return ctx.mkBv(constantExpr.getZExtValue());
                }
            }
            case EqExpr eqExpr -> {
                System.out.println("construct eq");
                eqExpr.print();
                System.out.println(eqExpr.getWidth());
            }
            default -> throw new IllegalStateException("Unexpected value: " + e);
        }
        return ctx.mkTrue();
    }
}
