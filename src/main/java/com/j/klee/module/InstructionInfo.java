package com.j.klee.module;

public class InstructionInfo {
    private long id;
    private final long line;
    private final long column;
    private final long assemblyLine;
    private final String file;

    InstructionInfo(long id, long line, long column, long assemblyLine, String file) {
        this.id = id;
        this.line = line;
        this.column = column;
        this.assemblyLine = assemblyLine;
        this.file = file;
    }

    public long getId() {
        return id;
    }

    public long getLine() {
        return line;
    }

    public long getColumn() {
        return column;
    }

    public long getAssemblyLine() {
        return assemblyLine;
    }

    public String getFile() {
        return file;
    }

    public void setId(int id) {
        this.id = id;
    }
}
