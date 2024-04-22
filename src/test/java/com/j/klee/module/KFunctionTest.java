package com.j.klee.module;

import org.bytedeco.javacpp.BytePointer;
import org.bytedeco.llvm.LLVM.LLVMContextRef;
import org.bytedeco.llvm.LLVM.LLVMMemoryBufferRef;
import org.bytedeco.llvm.LLVM.LLVMModuleRef;
import org.bytedeco.llvm.LLVM.LLVMValueRef;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import static org.bytedeco.llvm.global.LLVM.*;

class KFunctionTest {

    private static final BytePointer error = new BytePointer();

    public static final String LLVMTest = """
            define i32 @test(i32 %a, i32 %b) {
            entry:
              %0 = add i32 %a, %b
              ret i32 %0
            }
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

    @Test
    void getOperandNum() {
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