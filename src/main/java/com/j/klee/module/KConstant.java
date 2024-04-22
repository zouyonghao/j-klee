package com.j.klee.module;

import org.bytedeco.llvm.LLVM.LLVMValueRef;

public class KConstant {
    public LLVMValueRef constant;
    public int id;
    public KInstruction kInstruction;

    public KConstant(LLVMValueRef constant, int id, KInstruction kInstruction) {
        this.constant = constant;
        this.id = id;
        this.kInstruction = kInstruction;
    }
}
