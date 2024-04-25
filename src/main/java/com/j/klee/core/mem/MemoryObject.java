package com.j.klee.core.mem;

import com.j.klee.expr.Expr;
import org.bytedeco.llvm.LLVM.LLVMValueRef;

public class MemoryObject {
    private static int counter = 1; /// do we need this?

    public int id;
    /**
     * Symbolic Memory Addresses:
     * Each variable (including arrays and dynamically allocated memory) gets
     * a unique symbolic address that does not overlap with others unless
     * explicitly stated in the code through pointer manipulations.
     */
    public Expr address;
    public String addressName; // function_alloc_index
    public Expr size;
    public String name = "unnamed";

    public boolean isLocal;
    public boolean isGlobal;
    public boolean isFixed; // do we need this?
    public boolean isUserSpecified;

    public MemoryManager parent;

    public LLVMValueRef allocSite;

    // TODO: forall?

    public void setName(String name) {
        this.name = name;
    }
}

// TODO: do we need ObjectState?
