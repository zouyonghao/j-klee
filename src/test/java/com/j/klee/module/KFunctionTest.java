package com.j.klee.module;

import org.bytedeco.javacpp.BytePointer;
import org.bytedeco.llvm.LLVM.LLVMContextRef;
import org.bytedeco.llvm.LLVM.LLVMMemoryBufferRef;
import org.bytedeco.llvm.LLVM.LLVMModuleRef;
import org.bytedeco.llvm.LLVM.LLVMValueRef;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import static org.bytedeco.llvm.global.LLVM.*;

class KFunctionTest {

    private static final BytePointer error = new BytePointer();

    public static final String LLVMTest = """
            @.str = private unnamed_addr constant [2 x i8] c"x\\00", align 1
            define i32 @test(i32 %a, i32 %b) {
            entry:
              %0 = add i32 %a, %b
              %1 = add i32 %a, 10
              %2 = alloca i8, align 1
              call void @klee_make_symbolic(i8* noundef %2, i64 noundef 1, i8* noundef getelementptr inbounds ([2 x i8], [2 x i8]* @.str, i64 0, i64 0))
              ret i32 %0
            }
            declare void @klee_make_symbolic(i8* noundef, i64 noundef, i8* noundef) #2
            """;

    public static final LLVMContextRef context = LLVMContextCreate();
    public static final LLVMModuleRef module = new LLVMModuleRef();
    public static LLVMMemoryBufferRef memBuf = null;
    private static KModule kModule;
    private static LLVMValueRef f;

    @BeforeAll
    static void setUpBeforeClass() {

        System.out.print("setting memory buffer...");
        memBuf = LLVMCreateMemoryBufferWithMemoryRangeCopy(LLVMTest, LLVMTest.length(), "kFunctionTest");
        System.out.println("success");

        System.out.print("parsing IR...");
        if (LLVMParseIRInContext(context, memBuf, module, error) != 0) {
            System.out.println("Error parsing IR: " + error.getString());
            LLVMDisposeMessage(error);
            return;
        }
        System.out.println("success");

        kModule = new KModule();

        System.out.print("parsing function test...");
        f = LLVMGetNamedFunction(module, "test");
        System.out.println("success");
    }

    @AfterAll
    public static void tearDownAfterClass() {
        System.out.print("tearing down...");
        // LLVMDisposeMessage(error);
        // LLVMDisposeModule(module);
        // LLVMDisposeMemoryBuffer(memBuf);
        // LLVMContextDispose(context);
    }

    @Test
    void getOperandNum() {
        KFunction kFunction = new KFunction(f, kModule);
        for (KInstruction ki : kFunction.getInstructions()) {
            for (int operands : ki.getOperands()) {
                System.out.println(operands);
            }
        }
    }

    @Test
    void getKind() {
        KFunction kFunction = new KFunction(f, kModule);
        assert (kFunction.getKind() == CallableKind.Function);
    }

    @Test
    void getName() {
    }

    @Test
    void getFunctionType() {
    }

    @Test
    void getValue() {
    }
}