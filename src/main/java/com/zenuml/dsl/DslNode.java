package com.zenuml.dsl;

import java.util.ArrayList;
import java.util.List;

public abstract class DslNode {
    protected DslNode parent;
    List<DslNode> children;

    public DslNode() {
        children=new ArrayList<>();
    }


    protected boolean isRoot() {
        return parent == null;
    }

    protected void printIndent(StringBuffer output) {
        for (int i = 0; i < getLevel(); i++) {
            output.append("  ");
        }
    }

    public final DslNode addChild(DslNode node) {
        children.add(node);
        node.parent=this;
        return node;
    }

    abstract void toDsl(StringBuffer output);


    protected void printChindren(StringBuffer output) {
        children.forEach(dslNode -> dslNode.toDsl(output));
    }

    protected int getLevel() {
        int level=0;
        DslNode node=this;
        while(node.parent!=null){
            level++;
            node=node.parent;
        }
        return level;
    }
}
