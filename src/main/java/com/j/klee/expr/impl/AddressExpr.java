package com.j.klee.expr.impl;

import com.j.klee.core.mem.MemoryObject;
import com.j.klee.expr.Expr;

public class AddressExpr extends Expr {

    public String addressName;

    public MemoryObject mo;

    public AddressExpr(String addressName, MemoryObject mo) {
        this.addressName = addressName;
        this.mo = mo;
    }

    @Override
    protected int compareContents(Expr other) {
        return 0;
    }

    @Override
    public Kind getKind() {
        return null;
    }

    @Override
    public Width getWidth() {
        return null;
    }

    @Override
    public int getNumKinds() {
        return 0;
    }

    @Override
    public Expr getKid(int i) {
        return null;
    }

    @Override
    public void print() {

    }

    @Override
    public long computeHash() {
        return 0;
    }

    @Override
    public Expr rebuild(Expr[] kids) {
        return null;
    }
}
