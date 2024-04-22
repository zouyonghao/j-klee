package com.j.klee.core.impl;

import com.j.klee.core.Executor;
import com.j.klee.core.SpecialFunctionHandler;
import org.bytedeco.llvm.LLVM.LLVMValueRef;

import java.util.List;
import java.util.Map;

import static com.j.klee.utils.LLVMUtils.*;
import static org.bytedeco.llvm.global.LLVM.LLVMGetNamedFunction;

public class SpecialFunctionHandlerImpl implements SpecialFunctionHandler {

    private static final Map<String, FunctionHandler> handlerInfo = Map.ofEntries(
            Map.entry("abort", new AbortFunctionHandler())
    );

    Executor executor;

    public SpecialFunctionHandlerImpl(ExecutorImpl executor) {
        this.executor = executor;
    }

    @Override
    public void prepare(List<String> preservedFunction) {
        for (Map.Entry<String, FunctionHandler> entry : handlerInfo.entrySet()) {
            LLVMValueRef f = LLVMGetNamedFunction(executor.getKModule().getModuleRef(), entry.getKey());
            if (f != null && (!entry.getValue().doNotOverride() || isDeclaration(f))) {
                preservedFunction.add(entry.getKey());

                if (entry.getValue().doesNotReturn()) {
                    addNoReturnFunctionAttribute(f);
                }

                if (!isDeclaration(f)) {
                    deleteFunctionBody(f);
                }
            }
        }
    }

    @Override
    public void bind() {

    }
}

interface FunctionHandler {
    void handle();

    boolean doesNotReturn();

    boolean hasReturnValue();


    boolean doNotOverride(); /// Intrinsic should not be used if already defined
}

class AbortFunctionHandler implements FunctionHandler {
    @Override
    public void handle() {

    }

    @Override
    public boolean doesNotReturn() {
        return true;
    }

    @Override
    public boolean hasReturnValue() {
        return false;
    }


    @Override
    public boolean doNotOverride() {
        return false;
    }

}
