package com.j.klee.core.impl;

import com.j.klee.expr.impl.ConstantExpr;
import com.j.klee.module.KInstruction;
import org.bytedeco.llvm.LLVM.LLVMValueRef;
import org.bytedeco.llvm.global.LLVM;

import static org.bytedeco.llvm.global.LLVM.*;

public class ExecutorUtil {
    public static ConstantExpr evalConstant(LLVMValueRef constant) {
        return evalConstant(constant, null);
    }

    public static ConstantExpr evalConstant(LLVMValueRef constant, KInstruction ki) {
        // TODO: evalConstant
        if (LLVMIsAConstantInt(constant) != null) {
            System.out.println("LLVM constant int value: " + LLVM.LLVMConstIntGetZExtValue(constant));
            return new ConstantExpr(constant);
        }
        System.out.println("not supported constant: " + LLVMPrintTypeToString(LLVMTypeOf(constant)).getString());
        return null;
    }
}
