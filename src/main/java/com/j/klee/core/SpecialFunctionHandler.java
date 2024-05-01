package com.j.klee.core;

import com.j.klee.expr.Expr;
import com.j.klee.module.KInstruction;
import org.bytedeco.llvm.LLVM.LLVMValueRef;

import java.util.List;

public interface SpecialFunctionHandler {
    void prepare(List<String> preservedFunction);

    void bind();

    boolean handle(ExecutionState state, LLVMValueRef f, KInstruction target, List<Expr> arguments);
}
