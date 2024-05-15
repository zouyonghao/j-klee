package com.j.klee.core.mem;

import com.j.klee.expr.Expr;
import com.j.klee.utils.LLVMUtils;
import org.bytedeco.llvm.LLVM.LLVMValueRef;

public class MemoryManager {
    public MemoryObject allocate(Expr size, boolean isLocal, boolean isGlobal, LLVMValueRef inst, int allocationAlignment) {
        LLVMValueRef f = LLVMUtils.getFunctionFromInst(inst);
        // TODO: recursive function?
        String addressName = LLVMUtils.getFunctionName(f) + "-" + LLVMUtils.getAllocIndex(inst);
        return new MemoryObject(inst, addressName, size, isLocal, isGlobal, allocationAlignment);
    }
}
