package com.j.klee.core;

import com.j.klee.expr.Expr;

public class Context {
    static boolean initialized = false;
    static Context currentContext = null;

    boolean isLittleEndian;
    Expr.Width pointerWidth;

    public Context(boolean isLittleEndian, Expr.Width pointerWidth) {
        this.isLittleEndian = isLittleEndian;
        this.pointerWidth = pointerWidth;
    }

    public static void initialize(boolean isLittleEndian, Expr.Width pointerWidth) {
        currentContext = new Context(isLittleEndian, pointerWidth);
        initialized = true;
    }

    public static Context get() {
        return currentContext;
    }

    public Expr.Width getPointerWidth() {
        return pointerWidth;
    }
}
