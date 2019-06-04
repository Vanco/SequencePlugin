package com.zenuml.dsl;

public class SequenceDiagram {
    DslNode root;
    DslNode current;

    public SequenceDiagram() {
    }

    public void addSub(DslNode node) {
        if(root==null){
            this.root=node;
            this.current=node;
            return;
        }
        current.addChild(node);
        current=node;
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
