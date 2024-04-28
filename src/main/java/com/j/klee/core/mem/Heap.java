package com.j.klee.core.mem;

import java.util.ArrayList;
import java.util.List;

public class Heap {
    List<MemoryObject> objects = new ArrayList<MemoryObject>();

    public void addObject(MemoryObject object) {
        objects.add(object);
    }
}
