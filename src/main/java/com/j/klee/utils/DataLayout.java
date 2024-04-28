package com.j.klee.utils;

import org.bytedeco.llvm.LLVM.LLVMModuleRef;
import org.bytedeco.llvm.LLVM.LLVMTargetDataRef;
import org.bytedeco.llvm.LLVM.LLVMTypeRef;
import org.bytedeco.llvm.global.LLVM;

import static org.bytedeco.llvm.global.LLVM.LLVMGetModuleDataLayout;
import static org.bytedeco.llvm.global.LLVM.LLVMPointerSize;

public class DataLayout {

    private final LLVMModuleRef module;

    private final LLVMTargetDataRef dataLayout;

    public DataLayout(LLVMModuleRef module) {
        this.module = module;
        this.dataLayout = LLVMGetModuleDataLayout(module);
    }

    public LLVMModuleRef getModule() {
        return module;
    }

    public boolean isLittleEndian() {
        return false;
    }

    public int getPointerSizeInBits() {
        return LLVMPointerSize(dataLayout) * 8;
    }

    public int getTypeSizeInBits(LLVMTypeRef type) {
        if (LLVM.LLVMTypeIsSized(type) != 1) {
            LLVM.LLVMDumpType(type);
            throw new IllegalStateException("Type is not sized!");
        }
        switch (LLVM.LLVMGetTypeKind(type)) {
            case LLVM.LLVMLabelTypeKind -> {
                return 0;
            }
            case LLVM.LLVMPointerTypeKind -> {
                return LLVM.LLVMPointerSizeForAS(dataLayout, LLVM.LLVMGetPointerAddressSpace(type));
            }
            case LLVM.LLVMIntegerTypeKind -> {
                return LLVM.LLVMGetIntTypeWidth(type);
            }
        }
        LLVM.LLVMDumpType(type);
        throw new IllegalStateException("Unsupported type!");
    }

    public int LLVMStoreSizeOfType(LLVMTypeRef type) {
        return Math.toIntExact((LLVM.LLVMStoreSizeOfType(dataLayout, type) + 7) / 8);
    }
}
