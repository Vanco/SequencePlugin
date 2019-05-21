package com.zenuml.dsl;

public class FunctionNode extends DslNode {
    private final String className;
    private final String functionName;
    private final String result;

    public FunctionNode(String className, String functionName) {
        this(className,functionName,null);
    }

    public FunctionNode(String className, String functionName, String result) {
        super();
        this.className=className;
        this.functionName=functionName;
        this.result=result;
    }

    @Override
    public void toDsl(StringBuffer output) {
        printIndent(output);
        if(result!=null){
            output.append(result);
            output.append("=");
        }
        output.append(className);
        output.append(".");
        output.append(functionName);
        if (children.size() == 0 && !isRoot()) {
            output.append(";\n");
        } else {
            output.append("{\n");
            printChindren(output);
            printIndent(output);
            output.append("}");
        }
    }

}
