package com.j.klee;

import com.j.klee.core.Executor;
import com.j.klee.core.ModuleOptions;
import com.j.klee.utils.LLVMUtils;
import org.apache.commons.cli.*;
import org.bytedeco.javacpp.BytePointer;
import org.bytedeco.llvm.LLVM.LLVMContextRef;
import org.bytedeco.llvm.LLVM.LLVMMemoryBufferRef;
import org.bytedeco.llvm.LLVM.LLVMModuleRef;
import org.bytedeco.llvm.LLVM.LLVMValueRef;
import org.bytedeco.llvm.global.LLVM;

import java.nio.IntBuffer;
import java.util.ArrayList;
import java.util.List;

import static org.bytedeco.llvm.global.LLVM.*;

public class Main {
    private static final BytePointer error = new BytePointer();

    private static String entryFunctionName = "main";
    private static boolean listFunctions = false;

    public static void main(String[] args) {

        // TODO: parse args
        String[] remainingArgs = parseArgs(args);

        if (remainingArgs.length != 1) {
            System.out.println("Usage: java Main <filename>");
            return;
        }

        // TODO: watch dog?

        String filename = remainingArgs[0];

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

        // iterator all functions
        if (listFunctions) {
            for (LLVMModuleRef m : modules) {
                LLVMValueRef f = LLVM.LLVMGetFirstFunction(m);
                while (f != null) {
                    var funcName = LLVMUtils.getFunctionName(f);
                    System.out.println(funcName);
                    f = LLVM.LLVMGetNextFunction(f);
                }
            }
        }
        // TODO: support entry point
        System.out.println("Checking main function existence...");
        LLVMValueRef entryFunction = null;
        for (LLVMModuleRef m : modules) {
            entryFunction = LLVMGetNamedFunction(m, entryFunctionName);
            if (entryFunction != null) {
                break;
            }
        }

        if (entryFunction == null) {
            System.err.println("entry function " + entryFunctionName + " function not found");
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
            entryFunction = LLVMGetNamedFunction(finalModule, entryFunctionName);

            // TODO: externals and globals check for final module
        }

        e.initializeSolver();

        // TODO: parse args
        e.runFunctionAsMain(entryFunction, 0, null, null);

        // Clean up
        LLVMDisposeModule(module);
        // LLVMDisposeMemoryBuffer(memBuf); // this will cause crash
        LLVMContextDispose(context);
    }

    private static String[] parseArgs(String[] args) {
        Options options = new Options();
        options.addOption(new Option("e", "entry", false, "entry point"));
        options.addOption(new Option("lf", "list-functions", false, "list all functions in the module"));

        CommandLineParser parser = new DefaultParser();
        HelpFormatter formatter = new HelpFormatter();
        CommandLine cmd;

        try {
            // Parse the command line arguments
            cmd = parser.parse(options, args);

            // Check if option "a" is present and print its value
            if (cmd.hasOption("entry")) {
                entryFunctionName = cmd.getOptionValue("entry");
            }

            if (cmd.hasOption("lf")) {
                listFunctions = true;
            }

            return cmd.getArgs();
        } catch (ParseException e) {
            System.out.println("Error: " + e.getMessage());
            formatter.printHelp("ParseOptionsExample", options);
            System.exit(1);
            return null;
        }
    }
}