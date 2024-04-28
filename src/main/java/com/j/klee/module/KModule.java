package com.j.klee.module;

import com.j.klee.core.ModuleOptions;
import com.j.klee.utils.DataLayout;
import com.j.klee.utils.LLVMUtils;
import org.bytedeco.llvm.LLVM.LLVMModuleRef;
import org.bytedeco.llvm.LLVM.LLVMValueRef;
import org.bytedeco.llvm.global.LLVM;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class KModule {

    private LLVMModuleRef module;

    // shadow versions of LLVM structures
    private List<KFunction> functions = new ArrayList<>(); // TODO: do we need this?
    private Map<LLVMValueRef, KFunction> functionMap = new HashMap<>();

    private InstructionInfoTable instructionInfoTable;

    public List<LLVMValueRef> constants = new ArrayList<>();
    private Map<LLVMValueRef, KConstant> constantMap = new HashMap<>();

    public Cell[] constantTable;

    private DataLayout targetData;

    public boolean link(List<LLVMModuleRef> modules, ModuleOptions moduleOptions) {
        module = modules.get(0);
        targetData = new DataLayout(module);
        return true;
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
            for (int i = 0; i < kf.getNumInstructions(); i++) {
                KInstruction ki = kf.getInstructions()[i];
                ki.setInfo(instructionInfoTable.getInstructionInfo(ki.getInst()));
            }

            functionMap.put(f, kf);
            functions.add(kf);

            f = LLVM.LLVMGetNextFunction(f);
        }

        // TODO: escaping functions
        // TODO: add declarations to escaping functions
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

    public DataLayout getTargetData() {
        return targetData;
    }

    public Map<LLVMValueRef, KFunction> getFunctionMap() {
        return functionMap;
    }
}
