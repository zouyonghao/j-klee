package com.j.klee.core.impl;

import com.j.klee.expr.impl.ConstantExpr;
import com.j.klee.module.KInstruction;
import com.j.klee.utils.LLVMUtils;
import org.bytedeco.javacpp.BytePointer;
import org.bytedeco.llvm.LLVM.LLVMBasicBlockRef;
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
        if (LLVMIsAFunction(constant) != null) {
            boolean isDeclaration = LLVMUtils.isDeclaration(constant);
            System.out.println("undefined reference to function: " + LLVMUtils.getFunctionName(constant) + (isDeclaration ? ", it's a function declaration." : ""));
            return null;
        }
        if (LLVMIsConstantString(constant) == LLVMUtils.True) {
            // TODO
            System.out.println("not support constant string for now");
            return null;
        }
        if (LLVMIsGlobalConstant(constant) == LLVMUtils.True) {
            // TODO
            System.out.println("not support global constant for now");
            LLVMUtils.registerAddress(constant.address(), constant);
            return ConstantExpr.createPointer(constant.address());
        }
        if (LLVMIsAMDNode(constant) != null) {
            // TODO
            System.out.println("not support MD node for now");
            return null;
        }
        if (LLVM.LLVMIsABlockAddress(constant) != null) {
            System.out.println("a block address ptr...");
            LLVMBasicBlockRef bb = LLVM.LLVMValueAsBasicBlock(LLVM.LLVMGetOperand(constant, 1));
            assert (bb != null);
            System.out.println(bb.getPointer().address());
            LLVMUtils.registerAddress(bb.getPointer().address(), LLVM.LLVMGetOperand(constant, 1));
            return ConstantExpr.createPointer(bb.getPointer().address());
        }
        System.out.println("not supported constant: " + LLVMPrintTypeToString(LLVMTypeOf(constant)).getString());
        try (BytePointer bp = LLVMPrintValueToString(constant)) {
            System.out.println(bp.getString());
        }
        return null;
    }
}
