package com.j.klee.utils;

import org.bytedeco.javacpp.BytePointer;
import org.bytedeco.llvm.LLVM.*;
import org.bytedeco.llvm.global.LLVM;

import java.util.HashMap;
import java.util.Map;

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

    public static int getAllocIndex(LLVMValueRef allocInst) {
        if (LLVMGetInstructionOpcode(allocInst) != LLVMAlloca) {
            throw new IllegalArgumentException("Provided instruction is not an alloca.");
        }

        // Get the parent basic block of the alloca instruction
        LLVMBasicBlockRef bb = LLVMGetInstructionParent(allocInst);
        if (bb == null) {
            throw new IllegalArgumentException("Alloca instruction has no parent basic block.");
        }

        // Iterate through all instructions in the basic block to find the index
        LLVMValueRef instr;
        int index = 0;
        for (instr = LLVMGetFirstInstruction(bb); instr != null; instr = LLVMGetNextInstruction(instr)) {
            if (instr.address() == allocInst.address()) {
                return index;
            }
            index++;
        }

        throw new IllegalArgumentException("Instruction not found (shouldn't happen unless the input is invalid)");
    }

    public static LLVMValueRef getFunctionFromInst(LLVMValueRef inst) {
        return LLVMGetBasicBlockParent(LLVMGetInstructionParent(inst));
    }

    public static LLVMValueRef getAPIntFromLong(long v, int width) {
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

    public static String getStringFromLLVMValue(LLVMValueRef globalVar) {
        if (null == LLVMIsAGlobalVariable(globalVar)) {
            throw new IllegalArgumentException("LLVM String value should be global.");
        }

        // Get the initializer of the global variable
        LLVMValueRef initializer = LLVMGetInitializer(globalVar);
        if (null == initializer || null == LLVMIsAConstantDataSequential(initializer)) {
            throw new IllegalArgumentException("Error: Global variable does not have a proper initializer.");
        }

        // Check if the initializer is a constant array
        if (null != LLVMIsAConstantArray(initializer) || null != LLVMIsAConstantDataArray(initializer)) {
            long length = LLVMGetArrayLength2(LLVMTypeOf(initializer));
            StringBuilder result = new StringBuilder();
            for (long i = 0; i < length; i++) {
                LLVMValueRef elem = LLVMGetAggregateElement(initializer, (int) i);
                char character = (char) LLVMConstIntGetZExtValue(elem);
                if (character != 0) {
                    result.append(character);
                } else {
                    break;
                }
            }
            return result.toString();
        }

        throw new IllegalArgumentException("initializer is not a constant array");
    }

    public static final Map<Long, LLVMValueRef> ADDRESS_LLVM_VALUE_MAP = new HashMap<>();

    public static void registerAddress(long address, LLVMValueRef value) {
        ADDRESS_LLVM_VALUE_MAP.put(address, value);
    }

    public static LLVMValueRef getValueFromAddress(long address) {
        return ADDRESS_LLVM_VALUE_MAP.get(address);
    }

    // For switch instruction
    public static LLVMBasicBlockRef findSwitchInstSuccessorByConstant(LLVMValueRef inst, long longValue) {
        assert (LLVMIsASwitchInst(inst) != null);
        // switch i32 %6, label %10 [
        //         i32 1, label %7
        //         i32 2, label %8
        // ]
        // Num of operands = 6
        // %6 (cond), %10 (default), 1, %7, 2, %8
        int numOperands = LLVMGetNumOperands(inst);
        assert (numOperands > 3);
        for (int i = 2; i < numOperands; i += 2) {
            long caseValue = LLVMConstIntGetZExtValue(LLVMGetOperand(inst, i));
            if (caseValue == longValue) {
                return LLVMValueAsBasicBlock(LLVMGetOperand(inst, i + 1));
            }
        }
        throw new IllegalArgumentException("Error: invalid switch constant");
    }
}
