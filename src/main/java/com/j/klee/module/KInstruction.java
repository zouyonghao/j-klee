package com.j.klee.module;

import org.bytedeco.llvm.LLVM.LLVMValueRef;

public class KInstruction {
    private LLVMValueRef inst;
    private InstructionInfo info;

    // TODO:
    // Value numbers for each operand. -1 is an invalid value,
    // otherwise negative numbers are indices (negated and offset
    // by 2) into the module constant table and positive numbers
    // are register indices.
    private int operands;

    // TODO: Destination register index
    private long dest;

    public String getSourceLocation() {
        return "";
    }

    public boolean operandsValid() {
        return operands != -1;
    }
}

