package com.zenuml.dsl;

public class SequenceDiagram {
    DslNode root;
    DslNode current;
    public SequenceDiagram(DslNode root) {
        this.root=root;
        this.current=root;
    }

    public void addSub(FunctionNode functionNode) {
        current.addChild(functionNode);
        current=functionNode;
    }

    public String toDsl() {
        StringBuffer stringBuffer=new StringBuffer();
        root.toDsl(stringBuffer);
        return stringBuffer.toString();
    }

    public void end() {
        current = current.parent;
    }
}
