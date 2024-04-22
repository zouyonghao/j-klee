package com.j.klee.module;

import org.bytedeco.llvm.LLVM.LLVMValueRef;

import java.util.ArrayList;
import java.util.List;

public class KInstruction {
    private LLVMValueRef inst;
    private InstructionInfo info;

    // TODO:
    // Value numbers for each operand. -1 is an invalid value,
    // otherwise negative numbers are indices (negated and offset
    // by 2) into the module constant table and positive numbers
    // are register indices.
    private List<Integer> operands;

    // TODO: Destination register index
    private int dest;

    public String getSourceLocation() {
        return "";
    }

    public void setInst(LLVMValueRef inst) {
        this.inst = inst;
    }

    public void setDest(Integer integer) {
        this.dest = integer.intValue();
    }

    public void setOperands(ArrayList<Integer> operands) {
        this.operands = operands;
    }

    public List<Integer> getOperands() {
        return this.operands;
    }
}

