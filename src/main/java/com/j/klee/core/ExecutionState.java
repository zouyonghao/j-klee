package com.j.klee.core;

import com.j.klee.module.KFunction;
import com.j.klee.module.KInstruction;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

public class ExecutionState implements Comparable<ExecutionState> {
    public Iterator<KInstruction> pc;
    public Iterator<KInstruction> prevPC;
    public List<StackFrame> stack = new ArrayList<>();

    public int id;

    private static int nextID = 1;

    public ExecutionState(KFunction kFunction) {
        pc = Arrays.stream(kFunction.getInstructions()).iterator();
        prevPC = pc;
        stack.add(new StackFrame(null, kFunction));
        setID();
    }

    private void setID() {
        id = nextID++;
    }

    @Override
    public int compareTo(ExecutionState o) {
        return id - o.id; // TODO: is this correct?
    }
}
