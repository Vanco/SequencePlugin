package com.zenuml.dsl;

public class ElseNode extends DslNode {
    private final String condition;

    public ElseNode() {
        this(null);
    }

    public ElseNode(String condition) {
        super();
        this.condition =condition;
    }

    @Override
    public void toDsl(StringBuffer output) {
        printIndent(output);
        if(condition ==null) {
            output.append("else{\n");
        }else{
            output.append("else if("+condition+"){\n");
        }
        printChindren(output);
        printIndent(output);
        output.append("}\n");

    }


}
