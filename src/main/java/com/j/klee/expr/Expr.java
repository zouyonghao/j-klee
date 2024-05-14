package com.j.klee.expr;

import com.j.klee.core.Context;
import com.j.klee.expr.impl.BoolExpr;
import com.j.klee.expr.impl.CmpExpr.EqExpr;
import com.j.klee.expr.impl.ConstantExpr;

public abstract class Expr {
    // static int count; TODO: do we need this?
    static final int MAGIC_HASH_CONSTANT = 39;

    // Int64 can indicate i64, double or <2 * i32> in different cases.
    public enum Width {
        InvalidWidth(0), Bool(1), Int8(8), Int16(16), Int32(32), Int64(64), Fl80(80), Int128(128), Int256(256), Int512(512), MaxWidth(512), //
        /*
         * Temp width...
         */
        Temp24(24), Temp40(40), Temp48(48), Temp56(56), Temp72(72);

        private final int width;

        Width(int width) {
            this.width = width;
        }

        public int getWidth() {
            return width;
        }

        public static Width getWidthFromInt(int value) {
            for (Width width : Width.values()) {
                if (value == width.getWidth()) {
                    return width;
                }
            }
            throw new IllegalArgumentException("No width available with value " + value);
        }
    }

    public enum Kind {
        InvalidKind, Constant, NotOptimized, Read, Select, Concat, Extract,

        // casting
        ZExt, SExt,

        // bit
        BitNot,

        // binary
        Add, Sub, Mul, UDiv, SDiv, URem, SRem,

        // bit
        BitAnd, BitOr, Xor, Shl, LShr, AShr,

        // bool
        Bool, BoolNot, BoolAnd, BoolOr,

        // compare
        Eq, Ne, Ult, Ule, Slt, Sle, Sgt, Sge;

        public static final Kind LastKind = Sge;
        public static final Kind CastKindFirst = ZExt;
        public static final Kind CastKindLast = SExt;
        public static final Kind BinaryKindFirst = Add;
        public static final Kind BinaryKindLast = Sge;
        public static final Kind CmpKindFirst = Eq;
        public static final Kind CmpKindLast = Sge;
    }

    protected long hashValue;

    protected abstract int compareContents(Expr other);

    public Expr() {
    }

    public abstract Kind getKind();

    public abstract Width getWidth();

    public abstract int getNumKinds();

    public abstract Expr getKid(int i);

    public abstract void print();

    public void dump() {
        print();
    }

    public long hash() {
        return hashValue;
    }

    public abstract long computeHash();

    int compare(Expr other) {
        // TODO: compare Expr
        return 0;
    }

    public abstract Expr rebuild(Expr[] kids);

    public boolean isZero() {
        return false;
    }

    public boolean isTrue() {
        throw new IllegalStateException("not a bool or constant expression!");
    }

    public boolean isFalse() {
        return false;
    }

    static void printKind(Kind kind) {
        System.out.println(kind.toString());
    }

    static void printWidth(Width width) {
        System.out.println(width.toString());
    }

    static int getMinBytesForWidth(Width width) {
        return (width.width + 7) / 8;
    }

    // TODO: Kind utilities

    public static ConstantExpr createPointer(long value) {
        return ConstantExpr.createPointer(value, Context.get().getPointerWidth());
    }

    public static Expr createIsZero(Expr condition) {
        return EqExpr.create(condition, ConstantExpr.create(0, condition.getWidth()));
    }
    // TODO: Utilities
    // TODO: more...

}
