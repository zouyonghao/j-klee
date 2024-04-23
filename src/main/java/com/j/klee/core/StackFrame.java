package com.j.klee.core;

import com.j.klee.module.KFunction;
import com.j.klee.module.KInstruction;

import java.util.Iterator;

public class StackFrame {
    public Iterator<KInstruction> caller;
    public KFunction kf;

    public StackFrame(Iterator<KInstruction> caller, KFunction kFunction) {
        this.caller = caller;
        this.kf = kFunction;
    }
    // TODO: call path node
    // public CallPathNode callPathNode;
    // TODO: stack frame
}
