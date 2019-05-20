package com.zenuml.dsl;

public class LoopNode extends DslNode {
    private String condition;

    public LoopNode(String condition) {
        super();
        this.condition = condition;
    }

    @Override
    public void toDsl(StringBuffer output) {
        printIndent(output);
        output.append("while(");
        output.append(condition);
        output.append("){\n");
        printChindren(output);
        printIndent(output);
        output.append("}\n");
    }
    
}
