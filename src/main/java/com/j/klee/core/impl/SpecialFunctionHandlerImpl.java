package com.j.klee.core.impl;

import com.j.klee.core.Executor;
import com.j.klee.core.SpecialFunctionHandler;
import org.bytedeco.llvm.LLVM.LLVMValueRef;

import java.util.AbstractMap;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.j.klee.utils.LLVMUtils.*;
import static org.bytedeco.llvm.global.LLVM.LLVMGetNamedFunction;

public class SpecialFunctionHandlerImpl implements SpecialFunctionHandler {

    private static final Map<String, FunctionHandler> handlerInfo = Map.ofEntries(
            Map.entry("abort", new AbortFunctionHandler())
    );

    Executor executor;

    Map<LLVMValueRef, Map.Entry<FunctionHandler, Boolean>> handlers = new HashMap<>();

    public SpecialFunctionHandlerImpl(ExecutorImpl executor) {
        this.executor = executor;
    }

    @Override
    public void prepare(List<String> preservedFunction) {
        for (Map.Entry<String, FunctionHandler> hi : handlerInfo.entrySet()) {
            LLVMValueRef f = LLVMGetNamedFunction(executor.getKModule().getModuleRef(), hi.getKey());
            if (f != null && (!hi.getValue().doNotOverride() || isDeclaration(f))) {
                preservedFunction.add(hi.getKey());

                if (hi.getValue().doesNotReturn()) {
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
        for (Map.Entry<String, FunctionHandler> hi : handlerInfo.entrySet()) {
            LLVMValueRef f = LLVMGetNamedFunction(executor.getKModule().getModuleRef(), hi.getKey());
            if (f != null && (!hi.getValue().doNotOverride() || isDeclaration(f))) {
                handlers.put(f, new AbstractMap.SimpleEntry<>(hi.getValue(), hi.getValue().hasReturnValue()));
            }
        }
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
