package com.j.klee.module;

import com.j.klee.utils.LLVMUtils;
import org.bytedeco.llvm.LLVM.LLVMBasicBlockRef;
import org.bytedeco.llvm.LLVM.LLVMTypeRef;
import org.bytedeco.llvm.LLVM.LLVMValueRef;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.j.klee.utils.LLVMUtils.getFunctionArgumentSize;
import static com.j.klee.utils.LLVMUtils.getFunctionName;
import static org.bytedeco.llvm.global.LLVM.LLVMGetFirstBasicBlock;
import static org.bytedeco.llvm.global.LLVM.LLVMGetNextBasicBlock;

public class KFunction implements KCallable {

    private LLVMValueRef function;

    private int numArgs, numRegisters;

    private int numInstructions;

    private List<KInstruction> instructions;

    private Map<LLVMBasicBlockRef, Integer> basicBlockEntry;

    // TODO: track coverage
    private boolean trackCoverage;

    public KFunction(LLVMValueRef f, KModule kModule) {
        function = f;
        numArgs = getFunctionArgumentSize(f);
        numInstructions = 0;
        trackCoverage = true;

        basicBlockEntry = new HashMap<>();

        LLVMBasicBlockRef basicBlock = LLVMGetFirstBasicBlock(f);
        // Assign unique instruction IDs to each basic block
        while (basicBlock != null) {
            basicBlockEntry.put(basicBlock, numInstructions);
            numInstructions += LLVMUtils.getBasicBlockInstNum(basicBlock);
            basicBlock = LLVMGetNextBasicBlock(basicBlock);
        }

        instructions = new ArrayList<>();
        Map<LLVMValueRef, Integer> registerMap = new HashMap<>();
        // TODO
    }

    @Override
    public CallableKind getKind() {
        return CallableKind.Function;
    }

    @Override
    public String getName() {
        return getFunctionName(function);
    }

    @Override
    public LLVMTypeRef getFunctionType() {
        return LLVMUtils.getFunctionType(function);
    }

    @Override
    public LLVMValueRef getValue() {
        return function;
    }
}

