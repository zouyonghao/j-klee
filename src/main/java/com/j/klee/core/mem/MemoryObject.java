package com.j.klee.core.mem;

import com.j.klee.expr.Expr;
import com.j.klee.expr.impl.*;
import com.j.klee.expr.impl.BinaryExpr.AddExpr;
import com.j.klee.expr.impl.BinaryExpr.SubExpr;
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
    public UpdateList updates;

    public MemoryObject(LLVMValueRef inst, String addressName, Expr size, boolean isLocal, boolean isGlobal, int allocationAlignment) {
        this.allocSite = inst;
        this.addressName = addressName;
        this.size = size;
        this.isLocal = isLocal;
        this.isGlobal = isGlobal;
        this.allocationAlignment = allocationAlignment;

        this.address = new AddressExpr(addressName, this);
        updates = new UpdateList(new UpdateList.Array(addressName, size), null);
    }

    // TODO: forall?

    public void setName(String name) {
        this.name = name;
    }

    public Expr getOffsetExpr(Expr address) {
        return SubExpr.create(address, this.address);
    }

    public Expr read(Expr offset, Expr.Width width, boolean ignoreWrites) {
        offset = ZExtExpr.create(offset, Expr.Width.Int32);
        int numBytes = width.getWidth() / 8;
        Expr res = null;
        for (int i = 0; i < numBytes; i++) {
            // TODO: little endian, big endian
            Expr readByte = read8(AddExpr.create(offset, ConstantExpr.create(i, Expr.Width.Int32)), ignoreWrites);
            res = i == 0 ? readByte : ConcatExpr.create(readByte, res);
        }
        return res;
    }

    private Expr read8(Expr offset, boolean ignoreWrites) {
        return ReadExpr.create(getUpdates(ignoreWrites), offset);
    }

    private UpdateList getUpdates(boolean ignoreWrites) {
        if (updates.root == null) {
            throw new IllegalStateException("root is null");
        }

        if (!ignoreWrites) {
            return updates;
        }

        return new UpdateList(updates.root, null);
    }

    public void write(Expr offset, Expr value) {
        offset = ZExtExpr.create(offset, Expr.Width.Int32);
        int numBytes = value.getWidth().getWidth() / 8;
        for (int i = 0; i < numBytes; i++) {
            // TODO: little endian, big endian
            write8(AddExpr.create(offset, ConstantExpr.create(i, Expr.Width.Int32)),
                    ExtractExpr.create(value, 8 * i, Expr.Width.Int8));
        }
    }

    private void write8(Expr offset, Expr value) {
        assert (offset.getWidth() == Expr.Width.Int32);
        updates.extend(offset, value);
    }
}

// TODO: do we need ObjectState?
