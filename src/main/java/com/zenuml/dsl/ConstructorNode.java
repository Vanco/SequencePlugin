package com.zenuml.dsl;

public class ConstructorNode extends DslNode {
    String var;
    String constructor;

    public ConstructorNode(String var, String constructor) {
        this.var = var;
        this.constructor = constructor;
    }

    @Override
    void toDsl(StringBuffer output) {
        printIndent(output);
        if (hasChildren()) {
            output.append(String.format("%s = new %s{\n", var, constructor));
            printChindren(output);
            printWithIndent(output, "}\n");
            return;
        }
        output.append(String.format("%s = new %s;\n", var, constructor));
    }

    private void printWithIndent(StringBuffer output, String content) {
        printIndent(output);
        output.append(content);
    }

}
