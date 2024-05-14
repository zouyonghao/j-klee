package com.j.klee.solver.impl

import com.j.klee.core.mem.UpdateList
import com.j.klee.expr.Expr
import com.j.klee.expr.Expr.Width
import com.j.klee.expr.impl.*
import com.j.klee.expr.impl.BinaryExpr.AddExpr
import com.j.klee.expr.impl.BinaryExpr.SubExpr
import com.j.klee.expr.impl.BoolBinaryExpr.BoolAndExpr
import com.j.klee.expr.impl.CmpExpr.EqExpr
import com.j.klee.solver.Builder
import io.ksmt.KContext
import io.ksmt.expr.KExpr
import io.ksmt.sort.KArraySort
import io.ksmt.sort.KBoolSort
import io.ksmt.sort.KBvSort
import io.ksmt.sort.KSort
import io.ksmt.utils.BvUtils.bvZero

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
        val result: KExpr<*> = constructActual(e, width)
        return result as KExpr<KBoolSort>
    }

    // TODO: there are so many casts for the type system...
    private fun constructActual(e: Expr, widthRef: WidthReference): KExpr<*> {
        // println("\nConstructing expr:")
        // e.print()
        var result: KExpr<*>?
        when (e) {
            is ConstantExpr -> {
                widthRef.width = e.width
                result = ctx.mkBv(e.zExtValue, widthRef.width!!.width.toUInt());
                // TODO: larger constant?
            }

            is ReadExpr -> {
                widthRef.width = e.updates.root.range;
                result = readExpr(
                    getArrayForUpdate(e.updates.root, e.updates.head),
                    constructActual(e.index, /* not used */ WidthReference())
                )
            }

            is EqExpr -> {
                val left = constructActual(e.left, widthRef)
                val right = constructActual(e.right, widthRef)
                widthRef.width = Width.Bool
                result = ctx.mkEqNoSimplify(left as KExpr<KSort>, right as KExpr<KSort>)
            }

            is ZExtExpr -> {
                val srcWidth = WidthReference()
                val src = constructActual(e.src, srcWidth)
                widthRef.width = e.getWidth()
                // TODO: width is bool
                // TODO: width > srcWidth -> invalid width
                result = ctx.mkBvConcatExpr(
                    ctx.bvZero((widthRef.width!!.width - srcWidth.width!!.width).toUInt()), src as KExpr<KBvSort>
                )
            }

            is ExtractExpr -> {
                val src = constructActual(e.expr, widthRef)
                // TODO: bool extract
                widthRef.width = e.width
                result = ctx.mkBvExtractExpr(e.offset + widthRef.width!!.width - 1, e.offset, src as KExpr<KBvSort>)
            }

            is AddExpr -> {
                val left = constructActual(e.left, widthRef)
                val right = constructActual(e.right, widthRef)
                result = ctx.mkBvAddExpr(left as KExpr<KBvSort>, right as KExpr<KBvSort>)
            }

            is SubExpr -> {
                val left = constructActual(e.left, widthRef)
                val right = constructActual(e.right, widthRef)
                result = ctx.mkBvSubExpr(left as KExpr<KBvSort>, right as KExpr<KBvSort>)
            }

            is AddressExpr -> {
                result = addressExpr(e)
                widthRef.width = e.width
            }

            is BoolNotExpr -> {
                result = ctx.mkNot(construct(e.src))
                widthRef.width = Width.Bool
            }

            is ConcatExpr -> {
                val numKids = e.numKinds
                result = constructActual(e.getKid(numKids - 1), WidthReference())
                for (j in numKids - 2 downTo 0) {
                    result = ctx.mkBvConcatExpr(
                        constructActual(e.getKid(j), WidthReference()) as KExpr<KBvSort>,
                        result as KExpr<KBvSort>
                    )
                }
                widthRef.width = e.width
            }

            is BoolAndExpr -> {
                val left = constructActual(e.left, widthRef)
                val right = constructActual(e.right, widthRef)
                result = ctx.mkAnd(left as KExpr<KBoolSort>, right as KExpr<KBoolSort>)
            }

            is BoolExpr -> {
                result = if (e.isTrue) ctx.mkTrue() else ctx.mkFalse()
            }

            else -> throw IllegalStateException("Unexpected value: $e");
        }
       // println("\nresult is: ")
       // val sb = StringBuilder()
       // result!!.print(sb)
       // println(sb)
        return result!!
    }

    private val addressExprMap: MutableMap<String, KExpr<*>> = HashMap();
    private fun addressExpr(e: AddressExpr): KExpr<*> {
        val addressName = "address-" + e.addressName
        if (addressExprMap.containsKey(addressName)) {
            return addressExprMap[addressName]!!
        } else {
            val t = ctx.mkBvSort(e.width.width.toUInt())
            val addressBV = ctx.mkConst(addressName, t)
            addressExprMap[addressName] = addressBV
            return addressBV
        }
    }

    private fun readExpr(arrayForUpdate: KExpr<*>, index: KExpr<*>): KExpr<*> {
        return ctx.mkArraySelect(arrayForUpdate as KExpr<KArraySort<KSort, KSort>>, index as KExpr<KSort>)
    }

    private fun getArrayForUpdate(
        root: UpdateList.Array, un: UpdateList.UpdateNode?
    ): KExpr<*> {
        if (un == null) {
            return getInitialArray(root);
        } else {
            // TODO: array hash
            return writeExpr(
                getArrayForUpdate(root, un.next),
                constructActual(un.index, /* not used */ WidthReference()),
                constructActual(un.value, /* not used */ WidthReference())
            );
        }
    }

    private fun writeExpr(
        arrayForUpdate: KExpr<*>, index: KExpr<*>, value: KExpr<*>
    ): KExpr<*> {
        return ctx.mkArrayStore(
            arrayForUpdate as KExpr<KArraySort<KSort, KSort>>, index as KExpr<KSort>, value as KExpr<KSort>
        );
    }

    private val arrayExprMap: MutableMap<String, KExpr<*>?> = HashMap();

    private fun getInitialArray(root: UpdateList.Array): KExpr<*> {
        // TODO: array hash
        if (arrayExprMap.containsKey(root.name)) {
            return arrayExprMap[root.name]!!
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