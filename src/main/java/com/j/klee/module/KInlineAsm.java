package com.j.klee.module;

import com.j.klee.utils.LLVMUtils;
import org.bytedeco.llvm.LLVM.LLVMTypeRef;
import org.bytedeco.llvm.LLVM.LLVMValueRef;

public class KInlineAsm implements KCallable {

    private static int globalId = 0;

    private final String name;
    private final LLVMValueRef inlineAsm;

    public KInlineAsm(LLVMValueRef inlineAsm) {
        this.inlineAsm = inlineAsm;
        this.name = "__asm__" + getFreshAsmId();
    }

    @Override
    public CallableKind getKind() {
        return CallableKind.InlineAsm;
    }

    @Override
    public String getName() {
        return this.name;
    }

    @Override
    public LLVMTypeRef getFunctionType() {
        return LLVMUtils.getFunctionType(inlineAsm);
    }

    @Override
    public LLVMValueRef getValue() {
        return inlineAsm;
    }

    private static int getFreshAsmId() {
        return globalId++;
    }
}
