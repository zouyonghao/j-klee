package com.j.klee.core;

import com.j.klee.core.impl.ExecutorImpl;
import org.bytedeco.llvm.LLVM.LLVMValueRef;

public interface Executor {
    static Executor create() {
        return new ExecutorImpl();
    }

    /**
     * TODO: can we have a IR for a symbolic executor, so we decouple with LLVM
     *
     * @param f    LLVM function
     * @param argc argc
     * @param argv argv
     * @param envp environment
     */
    void runFunctionAsMain(LLVMValueRef f, int argc, char[][] argv, char[][] envp);


}
