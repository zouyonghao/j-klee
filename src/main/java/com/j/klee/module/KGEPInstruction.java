package com.j.klee.module;

import java.util.Map;

public class KGEPInstruction extends KInstruction {

    // TODO:
    // indices - The list of variable sized adjustments to add to the pointer
    // operand to execute the instruction. The first element is the operand
    // index into the GetElementPtr instruction, and the second element is the
    // element size to multiple that index by.
    Map<Integer, Long> indices;

    // TODO:
    // offset - A constant offset to add to the pointer operand to execute the instruction.
    long offset;

}
