package com.j.klee.utils;

import org.bytedeco.llvm.LLVM.LLVMModuleRef;

public class DataLayout {

    private final LLVMModuleRef module;

    public DataLayout(LLVMModuleRef module) {
        this.module = module;
    }

    public LLVMModuleRef getModule() {
        return module;
    }

    public boolean isLittleEndian() {
        return false;
    }

    public int getPointerSizeInBits() {
        return 64;
    }
}
