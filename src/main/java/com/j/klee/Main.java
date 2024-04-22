package com.j.klee;

import com.j.klee.core.Executor;
import com.j.klee.core.ModuleOptions;
import org.bytedeco.javacpp.BytePointer;
import org.bytedeco.llvm.LLVM.*;

import java.nio.IntBuffer;
import java.util.ArrayList;
import java.util.List;

import static org.bytedeco.llvm.global.LLVM.*;

public class Main {
    private static final BytePointer error = new BytePointer();

    public static void main(String[] args) {

        if (args.length != 1) {
            System.out.println("Usage: java Main <filename>");
            return;
        }

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

        Executor e = Executor.create();
        ModuleOptions moduleOptions = new ModuleOptions();
        e.setModule(modules, moduleOptions);

        e.runFunctionAsMain(mainFunction, 0, null, null);

        // System.out.println("Reading functions...");
        // // Iterate over all functions in the module
        // LLVMValueRef func = LLVMGetFirstFunction(module);
        // while (func != null && !func.isNull()) {
        //     System.out.println("Reading function: " + LLVMGetValueName(func).getString());
        //     // Iterate over all basic blocks in the function
        //     LLVMBasicBlockRef basicBlock = LLVMGetFirstBasicBlock(func);
        //     while (basicBlock != null && !basicBlock.isNull()) {
        //         // Iterate over all instructions in the basic block
        //         LLVMValueRef inst = LLVMGetFirstInstruction(basicBlock);
        //         while (inst != null && !inst.isNull()) {
        //             // Process the instruction
        //             BytePointer instStr = LLVMPrintValueToString(inst);
        //             System.out.println("Reading instruction from line: " + LLVMGetDebugLocLine(inst));
        //             System.out.println("Inst type: " + LLVMPrintTypeToString(LLVMTypeOf(inst)).getString());
        //             LLVMDisposeMessage(instStr);
        //
        //             inst = LLVMGetNextInstruction(inst);
        //         }
        //         basicBlock = LLVMGetNextBasicBlock(basicBlock);
        //     }
        //     func = LLVMGetNextFunction(func);
        // }

        // Clean up
        LLVMDisposeModule(module);
        // LLVMDisposeMemoryBuffer(memBuf); // this will cause crash
        LLVMContextDispose(context);
    }
}