package com.j.klee.core.mem;

import com.j.klee.expr.Expr;
import com.j.klee.expr.impl.AddressExpr;
import com.j.klee.expr.impl.SubExpr;
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
    public Expr address; // do we need this?
    public String addressName; // function_alloc_index
    public Expr size;
    public String name = "unnamed";

    public boolean isLocal;
    public boolean isGlobal;
    public boolean isFixed; // do we need this?
    public boolean isUserSpecified;

    public MemoryManager parent;

    public LLVMValueRef allocSite;

    public int allocationAlignment;

    public MemoryObject(LLVMValueRef inst, String addressName, Expr size, boolean isLocal, boolean isGlobal, int allocationAlignment) {
        this.allocSite = inst;
        this.addressName = addressName;
        this.size = size;
        this.isLocal = isLocal;
        this.isGlobal = isGlobal;
        this.allocationAlignment = allocationAlignment;

        this.address = new AddressExpr(addressName, this);
    }

    // TODO: forall?

    public void setName(String name) {
        this.name = name;
    }

    public Expr getOffsetExpr(Expr address) {
        return SubExpr.create(address, this.address);
    }

    public Expr read(Expr offset, Expr.Width type, boolean ignoreWrites) {
        // TODO: read offset
        // return this.address;
        return null;
    }
}

// TODO: do we need ObjectState?
