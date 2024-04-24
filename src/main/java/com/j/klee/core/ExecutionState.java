package com.j.klee.core;

import com.j.klee.module.KFunction;
import com.j.klee.module.KInstruction;
import org.bytedeco.llvm.LLVM.LLVMValueRef;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

public class ExecutionState implements Comparable<ExecutionState> {
    public KInstIterator pc;
    public KInstIterator prevPC;
    public List<StackFrame> stack = new ArrayList<>();

    public int id;

    private static int nextID = 1;

    public ExecutionState(KFunction kFunction) {
        pc = new KInstIterator(Arrays.stream(kFunction.getInstructions()).iterator());
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

    public static class KInstIterator implements Iterator<KInstruction> {

        public final Iterator<KInstruction> baseIterator;

        private KInstruction current;

        public KInstIterator(Iterator<KInstruction> baseIterator) {
            this.baseIterator = baseIterator;
        }

        @Override
        public boolean hasNext() {
            return baseIterator.hasNext();
        }

        @Override
        public KInstruction next() {
            current = baseIterator.next();
            return current;
        }

        public KInstruction getKInst() {
            return current;
        }

        public LLVMValueRef getInst() {
            return current.getInst();
        }
    }
}
