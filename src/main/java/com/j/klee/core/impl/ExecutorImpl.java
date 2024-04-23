package com.j.klee.core.impl;

import com.j.klee.core.*;
import com.j.klee.module.KFunction;
import com.j.klee.module.KInstruction;
import com.j.klee.module.KModule;
import com.j.klee.utils.DataLayout;
import org.bytedeco.llvm.LLVM.LLVMModuleRef;
import org.bytedeco.llvm.LLVM.LLVMValueRef;

import java.util.ArrayList;
import java.util.List;
import java.util.SortedSet;
import java.util.TreeSet;

import static com.j.klee.expr.Expr.Width.getWidthFromInt;
import static com.j.klee.utils.LLVMUtils.getFunctionArgumentSize;

public class ExecutorImpl implements Executor {

    private KModule kModule;

    private SortedSet<ExecutionState> states = new TreeSet<>();

    private boolean haltExecution = false;

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
        System.out.println(ki.getInst());
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
