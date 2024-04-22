package com.j.klee.module;

public class FunctionInfo {
    private long id;
    private long line;
    private long assemblyLine;
    private String file;
    private final String functionName;

    public FunctionInfo(long id, long line, long assemblyLine, String file, String functionName) {
        this.id = id;
        this.line = line;
        this.assemblyLine = assemblyLine;
        this.file = file;
        this.functionName = functionName;
    }

    public FunctionInfo(FunctionInfo another) {
        this(another.id, another.line, another.assemblyLine, another.file, another.functionName);
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public long getLine() {
        return line;
    }

    public void setLine(long line) {
        this.line = line;
    }

    public long getAssemblyLine() {
        return assemblyLine;
    }

    public void setAssemblyLine(long assemblyLine) {
        this.assemblyLine = assemblyLine;
    }

    public String getFile() {
        return file;
    }

    public void setFile(String file) {
        this.file = file;
    }

    public String getFunctionName() {
        return functionName;
    }
}
