package com.j.klee.core;

import com.j.klee.core.impl.ExecutorImpl;
import com.j.klee.expr.Expr;
import com.j.klee.module.KInstruction;
import com.j.klee.module.KModule;
import org.bytedeco.llvm.LLVM.LLVMModuleRef;
import org.bytedeco.llvm.LLVM.LLVMValueRef;

import java.util.List;

public interface Executor {
    static Executor create() {
        return new ExecutorImpl();
    }

    /**
     * TODO: can we have a IR for the symbolic executor, so we are decoupled from LLVM?
     *
     * @param f    LLVM function
     * @param argc argc
     * @param argv argv
     * @param envp environment
     */
    void runFunctionAsMain(LLVMValueRef f, int argc, char[][] argv, char[][] envp);

    LLVMModuleRef setModule(List<LLVMModuleRef> modules, ModuleOptions moduleOptions);

    KModule getKModule();

    void run(ExecutionState initialState);

    void executeAlloc(ExecutionState state, Expr size, boolean b, KInstruction ki);

    void executeAlloc(ExecutionState state, Expr size, boolean isLocal, KInstruction ki,
                      boolean zeroMemory, Object reAllocFrom, int allocationAlignment);

    void terminateState(ExecutionState state);

    void initializeSolver();
}
