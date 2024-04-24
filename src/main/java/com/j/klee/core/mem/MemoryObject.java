package com.j.klee.core.mem;

import com.j.klee.expr.Expr;
import org.bytedeco.llvm.LLVM.LLVMValueRef;

public class MemoryObject {
    private static int counter = 1; /// do we need this?

    public int id;
    public Expr address;
    public Expr size;
    public String name;

    public boolean isLocal;
    public boolean isGlobal;
    public boolean isFixed; // do we need this?
    public boolean isUserSpecified;

    public MemoryManager parent;

    LLVMValueRef allocSite;

    // TODO: forall?

    public void setName(String name) {
        this.name = name;
    }

}

// TODO: do we need ObjectState?
