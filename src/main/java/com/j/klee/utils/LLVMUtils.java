package com.j.klee.utils;

import com.j.klee.core.ExecutionState;
import com.j.klee.module.KInstruction;
import org.bytedeco.javacpp.BytePointer;
import org.bytedeco.llvm.LLVM.*;
import org.bytedeco.llvm.global.LLVM;

import java.util.List;

import static org.bytedeco.llvm.global.LLVM.*;

public class LLVMUtils {
    public static final int False = 0;
    public static final int True = 1;

    public static void addNoReturnFunctionAttribute(LLVMValueRef f) {
        LLVMAttributeRef noReturnAttr = LLVMCreateEnumAttribute(LLVMGetModuleContext(LLVMGetGlobalParent(f)), LLVMGetEnumAttributeKindForName("noreturn", 8), 0);
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

    public static int getAllocaIndex(LLVMValueRef allocaInst) {
        if (LLVMGetInstructionOpcode(allocaInst) != LLVMAlloca) {
            throw new IllegalArgumentException("Provided instruction is not an alloca.");
        }

        // Get the parent basic block of the alloca instruction
        LLVMBasicBlockRef bb = LLVMGetInstructionParent(allocaInst);
        if (bb == null) {
            throw new IllegalArgumentException("Alloca instruction has no parent basic block.");
        }

        // Iterate through all instructions in the basic block to find the index
        LLVMValueRef instr;
        int index = 0;
        for (instr = LLVMGetFirstInstruction(bb); instr != null; instr = LLVMGetNextInstruction(instr)) {
            if (instr.address() == allocaInst.address()) {
                return index;
            }
            index++;
        }

        throw new IllegalArgumentException("Instruction not found (shouldn't happen unless the input is invalid)");
    }

    public static LLVMValueRef getFunctionFromInst(LLVMValueRef inst) {
        return LLVMGetBasicBlockParent(LLVMGetInstructionParent(inst));
    }

    public static LLVMValueRef getAPIntFromInt(int v, int width) {
        LLVMContextRef context = LLVMContextCreate();
        LLVMTypeRef intType = LLVMIntTypeInContext(context, width);
        return LLVMConstInt(intType, v, 0);
    }

    public static boolean isUnconditional(LLVMValueRef inst) {
        assert (LLVMGetInstructionOpcode(inst) == LLVMBr);
        return LLVMGetNumOperands(inst) == 1;
    }

    public static int getBasicBlockIndex(LLVMValueRef inst, LLVMBasicBlockRef src) {
        assert (LLVM.LLVMGetInstructionOpcode(inst) == LLVMPHI);
        for (int i = 0; i < LLVM.LLVMGetNumOperands(inst); i++) {
            if (LLVM.LLVMGetIncomingBlock(inst, i).address() == src.address()) {
                return i;
            }
        }
        return -1;
    }

    public static void dumpValue(LLVMValueRef value) {
        try (BytePointer bp = LLVM.LLVMPrintValueToString(value)) {
            System.out.println();
            System.out.println(bp.getString());
        }
    }

    public static boolean isUseEmpty(LLVMValueRef value) {
        return LLVM.LLVMGetFirstUse(value) == null;
    }

    /**
     * A class for intrinsic IDs
     *
     * @see com.j.klee.core.impl.ExecutorImpl##executeCall
     */
    public static final class Intrinsic {
        public static final int NotIntrinsic = 0;
        public static final int FAbs = 130;
    }
}
