package com.j.klee.module;

import org.bytedeco.llvm.LLVM.LLVMTypeRef;
import org.bytedeco.llvm.LLVM.LLVMValueRef;

public interface KCallable {

    CallableKind getKind();

    String getName();

    LLVMTypeRef getFunctionType();

    // TODO: what does this value mean?
    LLVMValueRef getValue();

}
