package com.j.klee.core;

import com.j.klee.core.mem.Heap;
import com.j.klee.expr.Expr;
import com.j.klee.module.KFunction;
import com.j.klee.module.KInstruction;
import org.bytedeco.llvm.LLVM.LLVMValueRef;

import java.util.ArrayList;
import java.util.List;

public class ExecutionState implements Comparable<ExecutionState> {
    private static int nextID = 1;

    public KInstIterator pc;
    public KInstIterator prevPC;
    public List<StackFrame> stack;
    public int id;
    public Heap heap;
    public int incomingBBIndex;
    public List<Expr> constraints;

    public ExecutionState(KFunction kFunction) {
        pc = new KInstIterator(kFunction.getInstructions());
        prevPC = new KInstIterator(pc);
        stack = new ArrayList<>();
        stack.add(new StackFrame(null, kFunction));
        setID();
        heap = new Heap();
        constraints = new ArrayList<>();
    }

    public ExecutionState(ExecutionState executionState) {
        this.pc = new KInstIterator(executionState.pc);
        this.prevPC = new KInstIterator(executionState.prevPC);
        this.stack = new ArrayList<>(executionState.stack); // TODO: should we use deep copy here?
        this.id = executionState.id;
        this.heap = new Heap(executionState.heap);
        this.incomingBBIndex = executionState.incomingBBIndex;
        this.constraints = new ArrayList<>(executionState.constraints);
    }

    private void setID() {
        id = nextID++;
    }

    @Override
    public int compareTo(ExecutionState o) {
        return id - o.id; // TODO: is this correct?
    }

    public ExecutionState branch() {
        ExecutionState newState = new ExecutionState(this);
        newState.setID();
        return newState;
    }

    public void addConstraint(Expr condition) {
        // TODO: constraint manager
        // TODO: optimize constraint
        // TODO: rewrite constraint
        constraints.add(condition);
    }

    public static class KInstIterator {

        public KInstruction[] kInstructions;

        public int index;

        private KInstruction current;

        public KInstIterator(KInstruction[] kInstructions) {
            this.kInstructions = kInstructions;
            this.index = 0;
            this.current = kInstructions[index];
        }

        public KInstIterator(KInstIterator other) {
            this.kInstructions = other.kInstructions;
            this.index = other.index;
            this.current = other.current;
        }

        public boolean hasNext() {
            return index < kInstructions.length;
        }

        public void next() {
            index++;
            current = kInstructions[index];
        }

        public KInstruction getKInst() {
            return current;
        }

        public LLVMValueRef getInst() {
            return current != null ? current.getInst() : null;
        }

        public void setTo(int index) {
            this.index = index;
            current = kInstructions[index];
        }
    }
}
