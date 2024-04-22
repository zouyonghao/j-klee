package com.j.klee.module;

import org.bytedeco.llvm.LLVM.LLVMBasicBlockRef;
import org.bytedeco.llvm.LLVM.LLVMModuleRef;
import org.bytedeco.llvm.LLVM.LLVMValueRef;
import org.bytedeco.llvm.global.LLVM;

import java.util.HashMap;
import java.util.Map;

import static com.j.klee.utils.LLVMUtils.getFunctionName;
import static org.bytedeco.llvm.global.LLVM.*;

public class InstructionInfoTable {

    private final Map<LLVMValueRef, InstructionInfo> infos;
    private final Map<LLVMValueRef, FunctionInfo> functionInfos;

    // TODO: internedStrings
    // private List<String> internedStrings;

    public InstructionInfoTable(LLVMModuleRef module) {
        // TODO: generate all debug instruction information
        infos = new HashMap<>();
        functionInfos = new HashMap<>();
        // internedStrings = new ArrayList<>();

        LLVMValueRef f = LLVM.LLVMGetFirstFunction(module);
        while (f != null) {
            // TODO: get function info
            FunctionInfo functionInfo = new FunctionInfo(0, LLVMGetDebugLocLine(f), 0, "", getFunctionName(f));
            functionInfos.put(f, functionInfo);

            LLVMBasicBlockRef currentBB = LLVMGetFirstBasicBlock(f);
            while (currentBB != null) {
                LLVMValueRef inst = LLVMGetFirstInstruction(currentBB);
                while (inst != null) {
                    // TODO: get instruction info
                    InstructionInfo instructionInfo = new InstructionInfo(0, LLVMGetDebugLocLine(inst), LLVMGetDebugLocColumn(inst), 0, "");
                    infos.put(inst, instructionInfo);
                    inst = LLVMGetNextInstruction(inst);
                }
                currentBB = LLVMGetNextBasicBlock(currentBB);
            }

            f = LLVM.LLVMGetNextFunction(f);
        }

        int idCounter = 0;
        for (InstructionInfo instructionInfo : infos.values()) {
            instructionInfo.setId(idCounter++);
        }
        for (FunctionInfo functionInfo : functionInfos.values()) {
            functionInfo.setId(idCounter++);
        }
    }

    public int getMaxID() {
        return infos.size() + functionInfos.size();
    }

    public InstructionInfo getInstructionInfo(LLVMValueRef inst) {
        return infos.get(inst);
    }

    public FunctionInfo getFunctionInfo(LLVMValueRef function) {
        return functionInfos.get(function);
    }
}
