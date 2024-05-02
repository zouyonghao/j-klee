// package com.j.klee.solver.impl;
//
// import com.j.klee.core.mem.UpdateList;
// import com.j.klee.expr.Expr;
// import com.j.klee.expr.impl.CmpExpr.EqExpr;
// import com.j.klee.expr.impl.ConstantExpr;
// import com.j.klee.expr.impl.ReadExpr;
// import com.j.klee.solver.Builder;
// import io.ksmt.KContext;
// import io.ksmt.expr.KExpr;
// import io.ksmt.sort.KBoolSort;
//
// import java.util.HashMap;
// import java.util.Map;
//
// import static com.microsoft.z3.Native.mkBvSort;
//
//
// public class BitVectorBuilder implements Builder {
//     private final KContext ctx;
//
//     public
//
//     public BitVectorBuilder(KContext ctx) {
//         this.ctx = ctx;
//     }
//
//     @Override
//     public KExpr<KBoolSort> construct(Expr e) {
//         // TODO: cache
//         WidthReference width = new WidthReference();
//         KExpr<?> result = constructActual(e, width);
//         // TODO: is there an elegant way to check the generic type?
//         return (KExpr<KBoolSort>) result;
//     }
//
//     private KExpr<?> constructActual(Expr e, WidthReference width) {
//         if (width == null) {
//             width = new WidthReference();
//         }
//         switch (e) {
//             case ConstantExpr constantExpr -> {
//                 // if (width.width == Expr.Width.Bool) {
//                 //     return constantExpr.isTrue() ? ctx.mkTrue() : ctx.mkFalse();
//                 // }
//                 // TODO: int32?
//                 if (width.width.getWidth() <= Expr.Width.Int64.getWidth()) {
//                     return ctx.mkBv(constantExpr.getZExtValue());
//                 }
//                 // TODO: larger constant
//             }
//             case ReadExpr re -> {
//                 width.width = re.updates.root.range;
//                 return readExpr(getArrayForUpdate(re.updates.root, re.updates.head), constructActual(re.index, null));
//             }
//             case EqExpr eqExpr -> {
//                 System.out.println("construct eq");
//                 eqExpr.print();
//                 System.out.println(eqExpr.getWidth());
//             }
//             default -> throw new IllegalStateException("Unexpected value: " + e);
//         }
//         return ctx.mkTrue();
//     }
//
//     private KExpr<?> readExpr(KExpr<?> arrayForUpdate, KExpr<?> kExpr) {
//         return null;
//     }
//
//     private KExpr<?> getArrayForUpdate(UpdateList.Array root, UpdateList.UpdateNode un) {
//         if (un == null) {
//             return getInitialArray(root);
//         } else {
//             // TODO: array hash
//             return writeExpr(getArrayForUpdate(root, un.next), constructActual(un.index, null), constructActual(un.value, null));
//         }
//     }
//
//     private KExpr<?> writeExpr(KExpr<?> arrayForUpdate, KExpr<?> kExpr, KExpr<?> kExpr1) {
//         return null;
//     }
//
//     private final Map<String, KExpr<?>> arrayExprMap = new HashMap<>();
//
//     private KExpr<?> getInitialArray(UpdateList.Array root) {
//         // TODO: array hash
//         if (arrayExprMap.containsKey(root.name)) {
//             return arrayExprMap.get(root.name);
//         } else {
//             // Z3SortHandle domainSort = getArgumentSort(indexWidth);
//             // Z3SortHandle rangeSort = getArgumentSort(valueWidth);
//             // Z3SortHandle t = getArraySort(domainSort, rangeSort);
//             // Z3_symbol s = Z3_mk_string_symbol(ctx, const_cast<char *>(name));
//             // return Z3ASTHandle(Z3_mk_const(ctx, s, t), ctx);
//             // ctx.mkUninterpretedSort()
//             return null;
//
//             // TODO: constant array?
//         }
//     }
// }
