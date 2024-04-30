package com.j.klee.core;

public class BranchType {
    public static final int NONE = 0;
    public static final int ConditionalBranch = 1;
    public static final int IndirectBranch = 2;
    public static final int Switch = 3;
    public static final int Call = 4;
    public static final int MemOp = 5;
    public static final int ResolvePointer = 6;
    public static final int Alloc = 7;
    public static final int ReAlloc = 8;
    public static final int Free = 9;
    public static final int GetVal = 10;
    public static final int TPotComputeLambda = 11;
    public static final int END = TPotComputeLambda + 1;
}
