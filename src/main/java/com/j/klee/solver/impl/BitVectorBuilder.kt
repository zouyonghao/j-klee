package com.j.klee.solver.impl

import com.j.klee.core.mem.UpdateList
import com.j.klee.expr.Expr
import com.j.klee.expr.Expr.Width
import com.j.klee.expr.impl.CmpExpr.EqExpr
import com.j.klee.expr.impl.ConstantExpr
import com.j.klee.expr.impl.ReadExpr
import com.j.klee.solver.Builder
import io.ksmt.KContext
import io.ksmt.expr.KExpr
import io.ksmt.sort.KBoolSort

/**
 * We use Kotlin for this file because we need many functions in
 * ksmt which are only available in Kotlin.
 */
class BitVectorBuilder(private val ctx: KContext) : Builder {

    class WidthReference {
        var width: Width? = null
    }

    override fun construct(e: Expr): KExpr<KBoolSort> {
        val width = WidthReference()
        val result: KExpr<*>? = constructActual(e, width)
        return result as KExpr<KBoolSort>
    }

    private fun constructActual(e: Expr, _width: WidthReference?): KExpr<*>? {
        var width = _width
        if (width == null) {
            width = WidthReference()
        }
        when (e) {
            is ConstantExpr -> {
                // if (width.width == Expr.Width.Bool) {
                //     return constantExpr.isTrue() ? ctx.mkTrue() : ctx.mkFalse();
                // }
                // TODO: int32?
                if (width.width?.width!! <= Expr.Width.Int64.width) {
                    return ctx.mkBv(e.zExtValue);
                }
                // TODO: larger constant
            }

            is ReadExpr -> {
                width.width = e.updates.root.range;
                return readExpr(getArrayForUpdate(e.updates.root, e.updates.head), constructActual(e.index, null));
            }

            is EqExpr -> {
                println("construct eq");
                e.print();
                println(e.getWidth());
            }

            else -> throw IllegalStateException("Unexpected value: $e");
        }
        return ctx.mkTrue();
    }

    private fun readExpr(arrayForUpdate: KExpr<*>?, kExpr: KExpr<*>?): KExpr<*>? {
        return null;
    }

    private fun getArrayForUpdate(root: UpdateList.Array, un: UpdateList.UpdateNode?): KExpr<*>? {
        if (un == null) {
            return getInitialArray(root);
        } else {
            // TODO: array hash
            return writeExpr(
                getArrayForUpdate(root, un.next),
                constructActual(un.index, null),
                constructActual(un.value, null)
            );
        }
    }

    private fun writeExpr(arrayForUpdate: KExpr<*>?, kExpr: KExpr<*>?, kExpr1: KExpr<*>?): KExpr<*>? {
        return null;
    }

    private val arrayExprMap: MutableMap<String, KExpr<*>?> = HashMap();

    private fun getInitialArray(root: UpdateList.Array): KExpr<*> {
        // TODO: array hash
        if (arrayExprMap.containsKey(root.name)) {
            return arrayExprMap.get(root.name)!!
        } else {
            // Z3SortHandle domainSort = getArgumentSort(indexWidth);
            // Z3SortHandle rangeSort = getArgumentSort(valueWidth);
            // Z3SortHandle t = getArraySort(domainSort, rangeSort);
            // Z3_symbol s = Z3_mk_string_symbol(ctx, const_cast<char *>(name));
            // return Z3ASTHandle(Z3_mk_const(ctx, s, t), ctx);
            val domainSort = ctx.mkBvSort(root.domain.width.toUInt())
            val rangeSort = ctx.mkBvSort(root.range.width.toUInt())
            val t = ctx.mkArraySort(domainSort, rangeSort)
            val arrayExpr = ctx.mkConst(root.name, t)
            arrayExprMap[root.name] = arrayExpr
            return arrayExpr
            // TODO: constant array?
        }
    }
}