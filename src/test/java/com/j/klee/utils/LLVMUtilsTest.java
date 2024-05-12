package com.j.klee.utils;

import com.j.klee.module.KFunction;
import com.j.klee.module.KInstruction;
import com.j.klee.module.KModule;
import org.bytedeco.javacpp.BytePointer;
import org.bytedeco.llvm.LLVM.*;
import org.bytedeco.llvm.global.LLVM;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import static org.bytedeco.llvm.global.LLVM.*;
import static org.junit.jupiter.api.Assertions.*;

class LLVMUtilsTest {
    public static final String LLVMTest = """
                    ; ModuleID = '.\\AddressOfLabels.c'
                    source_filename = ".\\AddressOfLabels.c"
                    target datalayout = "e-m:w-p270:32:32-p271:32:32-p272:64:64-i64:64-f80:128-n8:16:32:64-S128"
                    target triple = "x86_64-pc-windows-msvc19.39.33523"
                    
                    $sprintf = comdat any
                    
                    $vsprintf = comdat any
                    
                    $_snprintf = comdat any
                    
                    $_vsnprintf = comdat any
                    
                    $_vsprintf_l = comdat any
                    
                    $_vsnprintf_l = comdat any
                    
                    $__local_stdio_printf_options = comdat any
                    
                    $"??_C@_06PFCJHJPI@argc?51?$AA@" = comdat any
                    
                    $"??_C@_09BPNKFPHM@Label?5one?$AA@" = comdat any
                    
                    $"??_C@_09HBNKPPJH@Label?5two?$AA@" = comdat any
                    
                    $"??_C@_0M@FFHJLLC@Label?5three?$AA@" = comdat any
                    
                    @"??_C@_06PFCJHJPI@argc?51?$AA@" = linkonce_odr dso_local unnamed_addr constant [7 x i8] c"argc 1\00", comdat, align 1
                    @"??_C@_09BPNKFPHM@Label?5one?$AA@" = linkonce_odr dso_local unnamed_addr constant [10 x i8] c"Label one\00", comdat, align 1
                    @"??_C@_09HBNKPPJH@Label?5two?$AA@" = linkonce_odr dso_local unnamed_addr constant [10 x i8] c"Label two\00", comdat, align 1
                    @"??_C@_0M@FFHJLLC@Label?5three?$AA@" = linkonce_odr dso_local unnamed_addr constant [12 x i8] c"Label three\00", comdat, align 1
                    @__local_stdio_printf_options._OptionsStorage = internal global i64 0, align 8
                    
                    ; Function Attrs: noinline nounwind optnone uwtable
                    define dso_local i32 @main() #0 {
                      %1 = alloca i32, align 4
                      %2 = alloca i32, align 4
                      %3 = alloca ptr, align 8
                      %4 = alloca ptr, align 8
                      store i32 0, ptr %1, align 4
                      store i32 1, ptr %2, align 4
                      %5 = call i32 @puts(ptr noundef @"??_C@_06PFCJHJPI@argc?51?$AA@")
                      store ptr blockaddress(@main, %14), ptr %3, align 8
                      %6 = load i32, ptr %2, align 4
                      switch i32 %6, label %10 [
                        i32 1, label %7
                        i32 2, label %8
                      ]
                    
                    7:                                                ; preds = %0
                      br label %12
                    
                    8:                                                ; preds = %0
                      store ptr blockaddress(@main, %18), ptr %3, align 8
                      %9 = load ptr, ptr %3, align 8
                      br label %22
                    
                    10:                                               ; preds = %0
                      %11 = load ptr, ptr %3, align 8
                      br label %22
                    
                    12:                                               ; preds = %7
                      store ptr blockaddress(@main, %16), ptr %4, align 8
                      %13 = load ptr, ptr %4, align 8
                      br label %22
                    
                    14:                                               ; preds = %22
                      %15 = call i32 @puts(ptr noundef @"??_C@_09BPNKFPHM@Label?5one?$AA@")
                      store i32 1, ptr %1, align 4
                      br label %20
                    
                    16:                                               ; preds = %22
                      %17 = call i32 @puts(ptr noundef @"??_C@_09HBNKPPJH@Label?5two?$AA@")
                      store i32 0, ptr %1, align 4
                      br label %20
                    
                    18:                                               ; preds = %22
                      %19 = call i32 @puts(ptr noundef @"??_C@_0M@FFHJLLC@Label?5three?$AA@")
                      store i32 1, ptr %1, align 4
                      br label %20
                    
                    20:                                               ; preds = %18, %16, %14
                      %21 = load i32, ptr %1, align 4
                      ret i32 %21
                    
                    22:                                               ; preds = %12, %10, %8
                      %23 = phi ptr [ %9, %8 ], [ %11, %10 ], [ %13, %12 ]
                      indirectbr ptr %23, [label %14, label %18, label %16]
                    }
                    
                    declare dso_local i32 @puts(ptr noundef) #1
                    
                    ; Function Attrs: nocallback nofree nosync nounwind willreturn
                    declare void @llvm.va_start(ptr) #2
                    
                    ; Function Attrs: nocallback nofree nosync nounwind willreturn
                    declare void @llvm.va_end(ptr) #2
                    
                    declare dso_local i32 @__stdio_common_vsprintf(i64 noundef, ptr noundef, i64 noundef, ptr noundef, ptr noundef, ptr noundef) #1
                    
                    ; Function Attrs: noinline nounwind optnone uwtable
                    define linkonce_odr dso_local ptr @__local_stdio_printf_options() #0 comdat {
                      ret ptr @__local_stdio_printf_options._OptionsStorage
                    }
                    
                    attributes #0 = { noinline nounwind optnone uwtable "min-legal-vector-width"="0" "no-trapping-math"="true" "stack-protector-buffer-size"="8" "target-cpu"="x86-64" "target-features"="+cmov,+cx8,+fxsr,+mmx,+sse,+sse2,+x87" "tune-cpu"="generic" }
                    attributes #1 = { "no-trapping-math"="true" "stack-protector-buffer-size"="8" "target-cpu"="x86-64" "target-features"="+cmov,+cx8,+fxsr,+mmx,+sse,+sse2,+x87" "tune-cpu"="generic" }
                    attributes #2 = { nocallback nofree nosync nounwind willreturn }
                    
                    !llvm.module.flags = !{!0, !1, !2, !3}
                    !llvm.ident = !{!4}
                    
                    !0 = !{i32 1, !"wchar_size", i32 2}
                    !1 = !{i32 8, !"PIC Level", i32 2}
                    !2 = !{i32 7, !"uwtable", i32 2}
                    !3 = !{i32 1, !"MaxTLSAlign", i32 65536}
                    !4 = !{!"clang version 17.0.1"}
            """;

    private static final BytePointer error = new BytePointer();
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
        f = LLVMGetNamedFunction(module, "main");
        System.out.println("success");
    }

    @Test
    void findSwitchInstSuccessorByConstant() {
        KFunction kFunction = new KFunction(f, kModule);
        // switch instruction
        KInstruction instruction = kFunction.getInstructions()[9];
        LLVMBasicBlockRef bb1 = LLVMUtils.findSwitchInstSuccessorByConstant(instruction.getInst(), 1);
        assertEquals(bb1, LLVMGetInstructionParent(kFunction.getInstructions()[10].getInst()));
        LLVMBasicBlockRef bb2 = LLVMUtils.findSwitchInstSuccessorByConstant(instruction.getInst(), 2);
        assertEquals(bb2, LLVMGetInstructionParent(kFunction.getInstructions()[11].getInst()));
        LLVMBasicBlockRef bb3 = LLVM.LLVMGetSwitchDefaultDest(instruction.getInst());
        assertEquals(bb3, LLVMGetInstructionParent(kFunction.getInstructions()[14].getInst()));
    }
}