package com.j.klee.expr.impl;

import com.j.klee.core.mem.UpdateList;
import com.j.klee.expr.Expr;

public class ReadExpr extends Expr {

    public UpdateList updates;
    Expr index;

    public ReadExpr(UpdateList ul, Expr index) {
        this.updates = ul;
        this.index = index;
    }

    @Override
    protected int compareContents(Expr other) {
        // TODO
        return 0;
    }

    @Override
    public Kind getKind() {
        return Kind.Read;
    }

    @Override
    public Width getWidth() {
        return updates.root.range;
    }

    @Override
    public int getNumKinds() {
        return 1;
    }

    @Override
    public Expr getKid(int i) {
        return i == 0 ? index : null;
    }

    @Override
    public void print() {
        System.out.print("(Read ");
        index.print();
        if (updates.root != null) {
            System.out.print(" " + updates.root.name);
        }
        UpdateList.UpdateNode un = updates.head;
        while (un != null) {
            System.out.println();
            un.value.print();
            un = un.next;
        }
        System.out.print(")");
    }

    @Override
    public long computeHash() {
        return 0;
    }

    @Override
    public Expr rebuild(Expr[] kids) {
        return null;
    }

    public static ReadExpr create(UpdateList ul, Expr index) {
        // TODO: find a potential written value for a index
        return ReadExpr.alloc(ul, index);
    }

    public static ReadExpr alloc(UpdateList ul, Expr index) {
        return new ReadExpr(ul, index);
    }
}
