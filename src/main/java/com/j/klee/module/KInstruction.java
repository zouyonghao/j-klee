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
    private int[] operands;

    // TODO: Destination register index
    private int dest;

    public String getSourceLocation() {
        return "";
    }

    public LLVMValueRef getInst() {
        return inst;
    }

    public void setInst(LLVMValueRef inst) {
        this.inst = inst;
    }

    public void setDest(Integer integer) {
        this.dest = integer.intValue();
    }

    public void setOperands(int[] operands) {
        this.operands = operands;
    }

    public int[] getOperands() {
        return this.operands;
    }

    public InstructionInfo getInfo() {
        return this.info;
    }

    public void setInfo(InstructionInfo info) {
        this.info = info;
    }

    public int getDest() {
        return dest;
    }
}

