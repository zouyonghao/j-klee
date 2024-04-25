package com.j.klee.core.impl;

import com.j.klee.core.*;
import com.j.klee.core.mem.MemoryManager;
import com.j.klee.core.mem.MemoryObject;
import com.j.klee.expr.Expr;
import com.j.klee.module.KFunction;
import com.j.klee.module.KInstruction;
import com.j.klee.module.KModule;
import com.j.klee.utils.DataLayout;
import org.bytedeco.llvm.LLVM.LLVMModuleRef;
import org.bytedeco.llvm.LLVM.LLVMTypeRef;
import org.bytedeco.llvm.LLVM.LLVMValueRef;

import java.util.ArrayList;
import java.util.List;
import java.util.SortedSet;
import java.util.TreeSet;

import static com.j.klee.expr.Expr.Width.getWidthFromInt;
import static com.j.klee.utils.LLVMUtils.getFunctionArgumentSize;
import static org.bytedeco.llvm.global.LLVM.*;

public class ExecutorImpl implements Executor {

    private KModule kModule;

    private SortedSet<ExecutionState> states = new TreeSet<>();

    private boolean haltExecution = false;

    private MemoryManager memoryManager = new MemoryManager();

    @Override
    public void runFunctionAsMain(LLVMValueRef f, int argc, char[][] argv, char[][] envp) {
        // TODO: argvMO
        // TODO: arguments

        KFunction kf = kModule.getFunctionMap().get(f);
        assert (kf != null);
        assert (getFunctionArgumentSize(f) == 0);

        ExecutionState state = new ExecutionState(kModule.getFunctionMap().get(f));

        // TODO: bind arguments
        // TODO: initialize global variables

        // TODO: process tree
        run(state);
    }

    @Override
    public void run(ExecutionState initialState) {
        bindModuleConstants();

        // TODO: timers
        states.add(initialState);

        // TODO: using seed

        // TODO: searcher

        while (!states.isEmpty() && !haltExecution) {
            ExecutionState state = states.first();
            state.prevPC = state.pc;
            // TODO: step instruction
            KInstruction ki = state.pc.next();

            assert (ki == state.pc.getKInst());

            executeInstruction(state, ki);

            // TODO: update states
            updateStates(state);

            // TODO: dump states
            // TODO: check memory usage
        }
    }

    private void updateStates(ExecutionState state) {
        if (!state.pc.hasNext()) {
            states.remove(state);
        }
    }

    private void executeInstruction(ExecutionState state, KInstruction ki) {
        LLVMValueRef inst = ki.getInst();
        switch (LLVMGetInstructionOpcode(inst)) {
            case LLVMRet -> {
                System.out.println("currently unsupported inst: ret");
            }
            case LLVMBr -> {
                System.out.println("currently unsupported inst: br");
            }
            case LLVMIndirectBr -> {
                System.out.println("currently unsupported inst: indirect");
            }
            case LLVMSwitch -> {
                System.out.println("currently unsupported inst: switch");
            }
            case LLVMUnreachable -> {
                System.out.println("currently unsupported inst: unreachable");
            }
            case LLVMInvoke -> {
                System.out.println("currently unsupported inst: invoke");
            }
            case LLVMCall -> {
                System.out.println("currently unsupported inst: call");
            }
            case LLVMPHI -> {
                System.out.println("currently unsupported inst: phi");
            }
            case LLVMSelect -> {
                System.out.println("currently unsupported inst: select");
            }
            case LLVMVAArg -> {
                System.out.println("currently unsupported inst: vaarg");
            }
            case LLVMAdd -> {
                System.out.println("currently unsupported inst: add");
            }
            case LLVMSub -> {
                System.out.println("currently unsupported inst: sub");
            }
            case LLVMMul -> {
                System.out.println("currently unsupported inst: mul");
            }
            case LLVMUDiv -> {
                System.out.println("currently unsupported inst: udiv");
            }
            case LLVMSDiv -> {
                System.out.println("currently unsupported inst: sdiv");
            }
            case LLVMURem -> {
                System.out.println("currently unsupported inst: urem");
            }
            case LLVMSRem -> {
                System.out.println("currently unsupported inst: srem");
            }
            case LLVMAnd -> {
                System.out.println("currently unsupported inst: and");
            }
            case LLVMOr -> {
                System.out.println("currently unsupported inst: or");
            }
            case LLVMXor -> {
                System.out.println("currently unsupported inst: xor");
            }
            case LLVMShl -> {
                System.out.println("currently unsupported inst: shl");
            }
            case LLVMLShr -> {
                System.out.println("currently unsupported inst: lshr");
            }
            case LLVMAShr -> {
                System.out.println("currently unsupported inst: ashr");
            }
            case LLVMICmp -> {
                System.out.println("currently unsupported inst: icmp");
            }
            case LLVMAlloca -> {
                LLVMTypeRef allocType = LLVMGetAllocatedType(inst);
                int byteSize = this.kModule.getTargetData().LLVMStoreSizeOfType(allocType);
                Expr size = Expr.createPointer(byteSize);
                // TODO: array allocation
                executeAlloc(state, size, true, ki);
            }
            case LLVMLoad -> {
                System.out.println("currently unsupported inst: load");
            }
            case LLVMStore -> {
                System.out.println("currently unsupported inst: store");
            }
            case LLVMGetElementPtr -> {
                System.out.println("currently unsupported inst: getElementPtr");
            }
            case LLVMTrunc -> {
                System.out.println("currently unsupported inst: trunc");
            }
            case LLVMZExt -> {
                System.out.println("currently unsupported inst: zext");
            }
            case LLVMSExt -> {
                System.out.println("currently unsupported inst: sext");
            }
            case LLVMIntToPtr -> {
                System.out.println("currently unsupported inst: intToPtr");
            }
            case LLVMPtrToInt -> {
                System.out.println("currently unsupported inst: ptrToInt");
            }
            case LLVMBitCast -> {
                System.out.println("currently unsupported inst: bitCast");
            }
            case LLVMFNeg, LLVMFAdd, LLVMFSub, LLVMFMul, LLVMFDiv, LLVMFRem, LLVMFPTrunc, LLVMFPExt, LLVMFPToUI,
                    LLVMFPToSI, LLVMUIToFP, LLVMSIToFP, LLVMFCmp -> {
                System.out.println("currently unsupported inst: floating point");
            }
            case LLVMInsertValue -> {
                System.out.println("currently unsupported inst: insert value");
            }
            case LLVMExtractValue -> {
                System.out.println("currently unsupported inst: extract value");
            }
            case LLVMFence -> {
                System.out.println("currently unsupported inst: fence");
            }
            case LLVMInsertElement -> {
                System.out.println("currently unsupported inst: insert element");
            }
            case LLVMExtractElement -> {
                System.out.println("currently unsupported inst: extract element");
            }
            case LLVMShuffleVector -> {
                System.out.println("currently unsupported inst: shuffle vector");
            }
            case LLVMResume -> {
                System.out.println("currently unsupported inst: resume");
            }
            case LLVMLandingPad -> {
                System.out.println("currently unsupported inst: landing pad");
            }
            case LLVMAtomicRMW -> {
                System.out.println("currently unsupported inst: rmw");
            }
            case LLVMAtomicCmpXchg -> {
                System.out.println("currently unsupported inst: xchg");
            }
            default -> {
                System.out.println("currently unsupported inst: " + LLVMGetInstructionOpcode(inst));
            }
        }
    }

    public void executeAlloc(ExecutionState state, Expr size, boolean isLocal, KInstruction ki) {
        executeAlloc(state, size, isLocal, ki, false, null, 0);
    }

    public void executeAlloc(ExecutionState state, Expr size, boolean isLocal, KInstruction ki,
                             boolean zeroMemory, Object reAllocFrom, int allocationAlignment) {
        // TODO: toUnique size
        // TODO: optimize size
        MemoryObject mo = memoryManager.allocate(size, isLocal, state.prevPC.getInst(), allocationAlignment);
    }

    private void bindModuleConstants() {
        for (KFunction kf : kModule.getFunctionMap().values()) {
            for (int i = 0; i < kf.getNumInstructions(); i++) {
                bindInstructionConstants(kf.getInstructions()[i]);
            }
        }
    }

    private void bindInstructionConstants(KInstruction instruction) {
        // TODO: GetElementPtrInst, InsertValueInst, ExtractValueInst
    }

    @Override
    public LLVMModuleRef setModule(List<LLVMModuleRef> modules, ModuleOptions moduleOptions) {
        assert (kModule == null);
        kModule = new KModule();
        // TODO: link with KLEE intrinsics library before running any optimizations
        // TODO: 1) link the modules together
        kModule.link(modules, moduleOptions);

        // TODO: 2) apply different instrumentation
        // TODO: 3) optimise and prepare for KLEE

        // preserve functions
        List<String> preservedFunctions = new ArrayList<>();
        SpecialFunctionHandler specialFunctionHandler = new SpecialFunctionHandlerImpl(this);
        specialFunctionHandler.prepare(preservedFunctions);

        preservedFunctions.add(moduleOptions.entryPoint);
        preservedFunctions.add("memset");
        preservedFunctions.add("memcpy");
        preservedFunctions.add("memcmp");
        preservedFunctions.add("memmove");

        kModule.optimiseAndPrepare(moduleOptions, preservedFunctions);
        kModule.checkModule();

        // TODO: 4) Manifest the module
        kModule.manifest(
                // interpreter,
                // statsTracker
        );

        specialFunctionHandler.bind();

        // TODO: stats tracker

        // TODO: initialize the context

        DataLayout dataLayout = kModule.getTargetData();
        Context.initialize(dataLayout.isLittleEndian(), getWidthFromInt(dataLayout.getPointerSizeInBits()));

        return kModule.getModuleRef();
    }

    @Override
    public KModule getKModule() {
        return kModule;
    }
}
