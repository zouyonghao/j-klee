package com.j.klee.core.impl;

import com.j.klee.core.Executor;
import org.bytedeco.llvm.LLVM.LLVMValueRef;

public class ExecutorImpl implements Executor {
    @Override
    public void runFunctionAsMain(LLVMValueRef f, int argc, char[][] argv, char[][] envp) {

    }
}
