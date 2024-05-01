package com.j.klee.core.mem;

import com.j.klee.core.ExecutionState;
import com.j.klee.core.impl.ExecutorImpl;
import com.j.klee.expr.Expr;

import java.util.ArrayList;
import java.util.List;

public class Heap {
    List<MemoryObject> objects;

    public void addObject(MemoryObject object) {
        objects.add(object);
    }

    public ExecutorImpl.MemOpResult resolveOneExact(ExecutionState state, Expr address) {
        ExecutorImpl.MemOpResult result = new ExecutorImpl.MemOpResult();
        result.resultType = ExecutorImpl.MemOpResultType.MemOpError;
        for (MemoryObject object : objects) {
            if (object.address.equals(address)) {
                result.mo = object;
                System.out.println(result.mo.addressName);
                result.resultType = ExecutorImpl.MemOpResultType.MemOpSuccess;
                return result;
            }
        }
        return result;
    }

    public Heap() {
        objects = new ArrayList<>();
    }

    public Heap(Heap other) {
        objects = new ArrayList<>(other.objects);
    }
}
