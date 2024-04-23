package com.j.klee;

import com.j.klee.core.Executor;
import com.j.klee.core.ModuleOptions;
import org.bytedeco.javacpp.BytePointer;
import org.bytedeco.llvm.LLVM.LLVMContextRef;
import org.bytedeco.llvm.LLVM.LLVMMemoryBufferRef;
import org.bytedeco.llvm.LLVM.LLVMModuleRef;
import org.bytedeco.llvm.LLVM.LLVMValueRef;

import java.nio.IntBuffer;
import java.util.ArrayList;
import java.util.List;

import static org.bytedeco.llvm.global.LLVM.*;

public class Main {
    private static final BytePointer error = new BytePointer();

    public static void main(String[] args) {

        // TODO: parse args

        if (args.length != 1) {
            System.out.println("Usage: java Main <filename>");
            return;
        }

        // TODO: watch dog?

        String filename = args[0];

        // Initialize LLVM
        // LLVMInitializeCore(LLVMGetGlobalPassRegistry());

        IntBuffer major = IntBuffer.allocate(1);
        IntBuffer minor = IntBuffer.allocate(1);
        IntBuffer version = IntBuffer.allocate(1);
        LLVMGetVersion(major, minor, version);

        System.out.printf("LLVM version: %d.%d.%d%n", major.get(0), minor.get(0), version.get(0));

        // Create a context
        LLVMContextRef context = LLVMContextCreate();

        // TODO: load archive
        // Create a memory buffer from the IR file
        LLVMMemoryBufferRef memBuf = new LLVMMemoryBufferRef();
        if (LLVMCreateMemoryBufferWithContentsOfFile(new BytePointer(filename), memBuf, error) != 0) {
            System.out.println("Error reading file: " + error.getString());
            LLVMDisposeMessage(error);
            return;
        }

        System.out.println("Parse the IR in the memory buffer...");
        LLVMModuleRef module = new LLVMModuleRef();
        if (LLVMParseIRInContext(context, memBuf, module, error) != 0) {
            System.out.println("Error parsing IR: " + error.getString());
            LLVMDisposeMessage(error);
            return;
        }

        // TODO: support multiple modules
        List<LLVMModuleRef> modules = new ArrayList<>();
        modules.add(module);

        // TODO: detect architecture

        // TODO: support entry point
        System.out.println("Checking main function existence...");
        LLVMValueRef mainFunction = null;
        for (LLVMModuleRef m : modules) {
            mainFunction = LLVMGetNamedFunction(m, "main");
            if (mainFunction != null) {
                break;
            }
        }

        if (mainFunction == null) {
            System.out.println("main function not found");
            return;
        }

        // TODO: support POSIX runtime

        // TODO: UBSan can we?

        // TODO: cxx can we?

        // TODO: libc

        // TODO: link libraries

        // TODO: env

        // TODO: seed, record and replay

        Executor e = Executor.create();
        ModuleOptions moduleOptions = new ModuleOptions();

        try (LLVMModuleRef finalModule = e.setModule(modules, moduleOptions)) {
            mainFunction = LLVMGetNamedFunction(finalModule, "main");

            // TODO: externals and globals check for final module
        }

        // TODO: parse args
        e.runFunctionAsMain(mainFunction, 0, null, null);

        // Clean up
        LLVMDisposeModule(module);
        // LLVMDisposeMemoryBuffer(memBuf); // this will cause crash
        LLVMContextDispose(context);
    }
}