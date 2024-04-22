package com.j.klee.module;

import com.j.klee.core.ModuleOptions;
import com.j.klee.utils.LLVMUtils;
import org.bytedeco.llvm.LLVM.LLVMBasicBlockRef;
import org.bytedeco.llvm.LLVM.LLVMModuleRef;
import org.bytedeco.llvm.LLVM.LLVMValueRef;
import org.bytedeco.llvm.global.LLVM;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static com.j.klee.utils.LLVMUtils.getFunctionName;
import static org.bytedeco.llvm.global.LLVM.*;

public class KModule {

    LLVMModuleRef module;

    InstructionInfoTable instructionInfoTable;

    List<LLVMValueRef> constants;
    Map<LLVMValueRef, KConstant> constantMap;

    public void link(List<LLVMModuleRef> modules, ModuleOptions moduleOptions) {
        module = modules.get(0);
    }

    public LLVMModuleRef getModuleRef() {
        return module;
    }

    public void optimiseAndPrepare(ModuleOptions moduleOptions, List<String> preservedFunctions) {
        // TODO
    }


    public void checkModule() {
        // TODO
    }

    public void manifest() {
        // TODO: output source
        // TODO: output final bc

        // build shadow structures
        instructionInfoTable = new InstructionInfoTable(module);

        List<LLVMValueRef> declarations = new ArrayList<>();

        LLVMValueRef f = LLVM.LLVMGetFirstFunction(module);
        while (f != null) {
            if (LLVMUtils.isDeclaration(f)) {
                declarations.add(f);
            }

            KFunction kf = new KFunction(f, this);

            f = LLVM.LLVMGetNextFunction(f);
        }
    }

    public int getConstantID(LLVMValueRef value, KInstruction kInstruction) {
        if (constantMap.get(value) != null) {
            return constantMap.get(value).id;
        }

        int id = constants.size();
        KConstant kConstant = new KConstant(value, id, kInstruction);
        constantMap.put(value, kConstant);
        constants.add(value);
        return id;
    }
}
