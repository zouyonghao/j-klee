package com.j.klee.core;

import java.util.List;

public interface SpecialFunctionHandler {
    void prepare(List<String> preservedFunction);

    void bind();
}
