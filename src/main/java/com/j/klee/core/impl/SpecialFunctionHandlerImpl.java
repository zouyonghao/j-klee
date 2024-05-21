package com.j.klee.core.impl;

import com.j.klee.core.ExecutionState;
import com.j.klee.core.Executor;
import com.j.klee.core.SpecialFunctionHandler;
import com.j.klee.expr.Expr;
import com.j.klee.expr.impl.ConstantExpr;
import com.j.klee.module.KInstruction;
import com.j.klee.utils.LLVMUtils;
import org.bytedeco.llvm.LLVM.LLVMValueRef;
import picocli.CommandLine;

import java.util.AbstractMap;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.j.klee.utils.LLVMUtils.*;
import static org.bytedeco.llvm.global.LLVM.LLVMGetNamedFunction;

public class SpecialFunctionHandlerImpl implements SpecialFunctionHandler {

    Executor executor;

    private final Map<String, FunctionHandler> handlerInfo = Map.ofEntries(Map.entry("__assert_fail", new AssertFailHandler()), Map.entry("klee_silent_exit", new SilentExitHandler()), Map.entry("abort", new AbortFunctionHandler()), Map.entry("puts", new PutsFunctionHandler()));

    private final Map<LLVMValueRef, Map.Entry<FunctionHandler, Boolean>> handlers = new HashMap<>();

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

    @Override
    public boolean handle(ExecutionState state, LLVMValueRef f, KInstruction target, List<Expr> arguments) {
        Map.Entry<FunctionHandler, Boolean> handlerEntry = null;
        for (Map.Entry<LLVMValueRef, Map.Entry<FunctionHandler, Boolean>> hi : handlers.entrySet()) {
            if (hi.getKey().address() == f.address()) {
                handlerEntry = hi.getValue();
                break;
            }
        }
        if (handlerEntry != null) {
            FunctionHandler handler = handlerEntry.getKey();
            boolean hasReturnValue = handlerEntry.getValue();
            if (!hasReturnValue && !LLVMUtils.isUseEmpty(target.getInst())) {
                System.err.println("expected return value from void special function " + target.getInst());
                executor.terminateState(state);
            } else {
                handler.handle(state, target, arguments);
            }
            return true;
        }
        return false;
    }

    interface FunctionHandler {
        void handle(ExecutionState state, KInstruction target, List<Expr> arguments);

        boolean doesNotReturn();

        boolean hasReturnValue();

        boolean doNotOverride(); /// Intrinsic should not be used if already defined
    }

    class AbortFunctionHandler implements FunctionHandler {

        @Override
        public void handle(ExecutionState state, KInstruction target, List<Expr> arguments) {
            System.err.println("terminating state for calling abort, id" + state.id);
            executor.terminateState(state);
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

    class AssertFailHandler implements FunctionHandler {

        @Override
        public void handle(ExecutionState state, KInstruction target, List<Expr> arguments) {
            System.err.println("terminating state for calling assert_fail, id" + state.id);
            executor.terminateState(state);
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

    class SilentExitHandler implements FunctionHandler {
        @Override
        public void handle(ExecutionState state, KInstruction target, List<Expr> arguments) {
            executor.terminateState(state);
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

    static class PutsFunctionHandler implements FunctionHandler {
        @Override
        public void handle(ExecutionState state, KInstruction target, List<Expr> arguments) {
            System.out.println("calling puts...");
            LLVMValueRef argument = LLVMUtils.getValueFromAddress(((ConstantExpr) arguments.getFirst()).getZExtValue());
            assert (argument != null);
            String argString = LLVMUtils.getStringFromLLVMValue(argument);
            System.out.println(CommandLine.Help.Ansi.AUTO.string("@|bold,green " + argString + "|@"));
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
}

