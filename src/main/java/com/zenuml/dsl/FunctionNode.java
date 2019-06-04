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
        printResult(output);
        printFunctionCall(output);
        if (!hasChildren() && !isRoot()) {
            output.append(";\n");
        } else {
            output.append("{\n");
            printChindren(output);
            printIndent(output);
            output.append("}\n");
        }
    }

    private void printFunctionCall(StringBuffer output) {
        output.append(className);
        output.append(".");
        output.append(functionName);
    }

    private void printResult(StringBuffer output) {
        if(result!=null){
            output.append(result);
            output.append("=");
        }
    }

}
