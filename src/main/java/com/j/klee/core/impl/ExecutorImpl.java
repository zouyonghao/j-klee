package com.j.klee.core.impl;

import com.j.klee.core.Context;
import com.j.klee.core.Executor;
import com.j.klee.core.ModuleOptions;
import com.j.klee.core.SpecialFunctionHandler;
import com.j.klee.module.KModule;
import com.j.klee.utils.DataLayout;
import org.bytedeco.llvm.LLVM.LLVMModuleRef;
import org.bytedeco.llvm.LLVM.LLVMValueRef;

import java.util.ArrayList;
import java.util.List;

import static com.j.klee.expr.Expr.Width.getWidthFromInt;

public class ExecutorImpl implements Executor {

    private KModule kModule;

    @Override
    public void runFunctionAsMain(LLVMValueRef f, int argc, char[][] argv, char[][] envp) {
        // TODO: argvMO
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
