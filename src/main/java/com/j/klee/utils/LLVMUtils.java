package com.j.klee.utils;

import org.bytedeco.llvm.LLVM.LLVMAttributeRef;
import org.bytedeco.llvm.LLVM.LLVMBasicBlockRef;
import org.bytedeco.llvm.LLVM.LLVMTypeRef;
import org.bytedeco.llvm.LLVM.LLVMValueRef;

import static org.bytedeco.llvm.global.LLVM.*;

public class LLVMUtils {

    public static void addNoReturnFunctionAttribute(LLVMValueRef f) {
        LLVMAttributeRef noReturnAttr = LLVMCreateEnumAttribute(LLVMGetModuleContext(LLVMGetGlobalParent(f)),
                LLVMGetEnumAttributeKindForName("noreturn", 8), 0);
        LLVMAddAttributeAtIndex(f, LLVMAttributeFunctionIndex, noReturnAttr);
    }

    public static boolean isDeclaration(LLVMValueRef f) {
        return LLVMIsDeclaration(f) == 1;
    }

    public static void deleteFunctionBody(LLVMValueRef f) {
        assert (!isDeclaration(f));
        LLVMBasicBlockRef currentBB = LLVMGetFirstBasicBlock(f);
        while (currentBB != null) {
            LLVMBasicBlockRef nextBB = LLVMGetNextBasicBlock(currentBB);
            LLVMValueRef inst = LLVMGetFirstInstruction(currentBB);
            while (inst != null) {
                LLVMValueRef nextInst = LLVMGetNextInstruction(inst);
                LLVMInstructionEraseFromParent(inst);
                inst = nextInst;
            }

            LLVMDeleteBasicBlock(currentBB);
            currentBB = nextBB;
        }

        LLVMSetLinkage(f, LLVMExternalLinkage);
    }

    public static String getFunctionName(LLVMValueRef f) {
        return LLVMGetValueName(f).getString();
    }

    public static LLVMTypeRef getFunctionType(LLVMValueRef f) {
        return LLVMTypeOf(f);
    }

    public static int getFunctionArgumentSize(LLVMValueRef f) {
        assert (LLVMIsAFunction(f) != null);
        int size = 0;
        LLVMValueRef arg = LLVMGetFirstParam(f);
        while (arg != null) {
            size++;
            arg = LLVMGetNextParam(arg);
        }
        return size;
    }

    public static int getBasicBlockInstNum(LLVMBasicBlockRef basicBlock) {
        int num = 0;
        LLVMValueRef inst = LLVMGetFirstInstruction(basicBlock);
        while (inst != null) {
            num++;
            inst = LLVMGetNextInstruction(inst);
        }
        return num;
    }
}
