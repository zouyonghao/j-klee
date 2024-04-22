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
import static org.bytedeco.llvm.global.LLVM.*;

public class KFunction implements KCallable {

    private LLVMValueRef function;

    private int numArgs;

    /**
     * register numbers in this function
     * %1 = add i32 %a, %b  ; %1 is a virtual register
     * holding the result of adding %a and %b
     */
    private int numRegisters;

    private int numInstructions;

    private KInstruction[] instructions;

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

        instructions = new KInstruction[numInstructions];
        Map<LLVMValueRef, Integer> registerMap = new HashMap<>();

        // The first arg_size() registers are reserved for formals.
        // so the register number is not equal to the register in
        // the file, but with an offset, numArgs.
        numRegisters = numArgs;
        LLVMBasicBlockRef tmpBB = LLVMGetFirstBasicBlock(f);
        while (tmpBB != null) {
            LLVMValueRef inst = LLVMGetFirstInstruction(tmpBB);
            while (inst != null) {
                registerMap.put(inst, numRegisters++);
                inst = LLVMGetNextInstruction(inst);
            }
            tmpBB = LLVMGetNextBasicBlock(tmpBB);
        }

        int i = 0;
        tmpBB = LLVMGetFirstBasicBlock(f);
        while (tmpBB != null) {
            LLVMValueRef inst = LLVMGetFirstInstruction(tmpBB);
            while (inst != null) {
                KInstruction ki = switch (LLVMGetInstructionOpcode(inst)) {
                    case LLVMGetElementPtr, LLVMInsertValue, LLVMExtractValue -> new KGEPInstruction();
                    default -> new KInstruction();
                };
                ki.setInst(inst);
                ki.setDest(registerMap.get(inst));
                if (LLVMIsACallInst(inst) != null || LLVMIsAInvokeInst(inst) != null) {
                    LLVMValueRef functionCalled = LLVMGetCalledValue(inst);
                    int numArgs = getFunctionArgumentSize(functionCalled);
                    ki.setOperands(new ArrayList<>(numArgs + 1));
                    // 0 - the call function instruction
                    ki.getOperands().set(0, getOperandNum(functionCalled, registerMap, kModule, ki));
                    for (int j = 0; j < numArgs; j++) {
                        LLVMValueRef v = LLVMGetParam(functionCalled, i);
                        ki.getOperands().set(i + 1, getOperandNum(v, registerMap, kModule, ki));
                    }
                }

                instructions[i] = ki;
                i++;

                inst = LLVMGetNextInstruction(inst);
            }
            tmpBB = LLVMGetNextBasicBlock(tmpBB);
        }
    }

    int getOperandNum(LLVMValueRef value, Map<LLVMValueRef, Integer> registerMap, KModule kModule, KInstruction kInstruction) {
        if (LLVMIsAInstruction(value) != null) {
            return registerMap.get(value);
        } else if (LLVMIsAArgument(value) != null) {
            LLVMValueRef function = LLVMGetParamParent(value);
            int paramCount = LLVMCountParams(function);
            for (int i = 0; i < paramCount; i++) {
                if (LLVMGetParam(function, i) == value) {
                    return i;
                }
            }
            System.out.println("getOperandNum failed for argument value, line: " + LLVMGetDebugLocLine(value));
            System.exit(-1);
        } else if (LLVMIsABasicBlock(value) != null || LLVMIsAInlineAsm(value) != null || LLVMIsAValueAsMetadata(value) != null) {
            return -1;
        } else {
            assert (LLVMIsAConstant(value) != null);
            LLVMValueRef constant = LLVMIsAConstant(value);
            return -(kModule.getConstantID(value, kInstruction) + 2);
        }
        return 0;
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

