package com.j.klee.core.mem;

import com.j.klee.expr.Expr;
import org.bytedeco.llvm.LLVM.LLVMValueRef;

public class MemoryManager {
    public MemoryObject allocate(Expr size, boolean isLocal, LLVMValueRef inst, int allocationAlignment) {
        return new MemoryObject();
    }
}
