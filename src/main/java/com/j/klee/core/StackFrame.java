package com.j.klee.core;

import com.j.klee.core.mem.MemoryObject;
import com.j.klee.module.Cell;
import com.j.klee.module.KFunction;
import com.j.klee.module.KInstruction;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class StackFrame {
    public Iterator<KInstruction> caller;
    public KFunction kf;
    public List<MemoryObject> allocas;
    public Cell[] locals;

    public StackFrame(Iterator<KInstruction> caller, KFunction kFunction) {
        this.caller = caller;
        this.kf = kFunction;
        this.allocas = new ArrayList<>();
        this.locals = new Cell[kf.getNumRegisters()];
        for (int i = 0; i < locals.length; i++) {
            locals[i] = new Cell();
        }
    }
    // TODO: call path node
    // public CallPathNode callPathNode;
    // TODO: stack frame
}
