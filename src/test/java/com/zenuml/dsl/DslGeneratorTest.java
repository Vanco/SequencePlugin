package com.zenuml.dsl;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

import java.io.IOException;
import java.io.InputStream;

import static org.junit.Assert.assertEquals;
@RunWith(JUnit4.class)
public class DslGeneratorTest {

    private String readUmlFile(String file) {
        String resourceName=file + ".zenuml";
        InputStream inputStream = getClass().getClassLoader().getResourceAsStream(resourceName);
        try {
            byte[] data=new byte[inputStream.available()];
            inputStream.read(data);
            return new String(data);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    @Test
    public void test_empty() {
        DslNode root = new FunctionNode("RootClass", "function(a1,a2)");
        checkDslResult(root,"file1");
    }

    @Test
    public void test_one_function_child() {
        DslNode root = new FunctionNode("RootClass", "function(a1,a2)");
        root.addChild(new FunctionNode("RootClass", "function2(a1,a2)"));
        checkDslResult(root,"file2");
    }

    private void checkDslResult(DslNode root, String file) {
        StringBuffer dsl = new StringBuffer();
        root.toDsl(dsl);
        checkDslResult(file, dsl.toString());
    }

    private void checkDslResult(String file, String dsl) {
        assertEquals(readUmlFile(file), dsl);
    }

    @Test
    public void test_has_condition_child() {
        DslNode root = new FunctionNode("RootClass", "function(a1,a2)");
        DslNode function1 = new FunctionNode("Class1","function()");
        ConditionNode condition = (ConditionNode)root.addChild(new ConditionNode("condition"));
        ElseNode elseNode = new ElseNode();
        elseNode.addChild(new FunctionNode("Class2","function()"));
        condition.addChild(function1);
        condition.addElse(elseNode);
        checkDslResult(root,"file3");
    }

    @Test
    public void test_function_has_return(){
        DslNode root = new FunctionNode("RootClass", "function(a1,a2)");
        root.addChild(new FunctionNode("RootClass", "function2(a1,a2)","result"));
        checkDslResult(root,"file4");
    }
    @Test
    public void test_loop(){
        DslNode root = new FunctionNode("RootClass", "function(a1,a2)");
        root.addChild(new LoopNode("condition"))
                .addChild(new FunctionNode("class2", "function()"));
        checkDslResult(root,"file_loop");
    }

    @Test
    public void test_else_if(){
        DslNode root = new FunctionNode("RootClass", "function(a1,a2)");
        ConditionNode conditionNode = new ConditionNode("condition");
        root.addChild(conditionNode);
        conditionNode.addChild(new FunctionNode("Class1","function()"));

        ElseNode elseNode = new ElseNode("condition2");
        elseNode.addChild(new FunctionNode("Class2","function()"));
        ElseNode elseNodeEnd = new ElseNode();
        elseNodeEnd.addChild(new FunctionNode("Class3","function()"));
        conditionNode.addElse(elseNode);
        conditionNode.addElse(elseNodeEnd);

        checkDslResult(root,"file_elseif");

    }

    @Test
    public void test_new(){
        DslNode root = new FunctionNode("RootClass", "function(a1,a2)");
        root.addChild(new ConstructorNode("class4","Class4(a1,a2)"));
        checkDslResult(root,"file_new");
    }

    @Test
    public void test_new_has_child(){
        DslNode root = new FunctionNode("RootClass", "function(a1,a2)");
        DslNode newNode = root.addChild(new ConstructorNode("class4", "Class4(a1,a2)"));
        newNode.addChild(new FunctionNode("Class2","function()"));
        checkDslResult(root,"file_new_has_child");
    }


    @Test
    public void test_sequence() {
        DslNode root=new FunctionNode("RootClass","function(a1,a2)");
        SequenceDiagram sequenceDiagram=new SequenceDiagram();
        sequenceDiagram.addSub(root);
        sequenceDiagram.addSub(new FunctionNode("class1","function1()"));
        sequenceDiagram.addSub(new FunctionNode("class2","function2()"));
        sequenceDiagram.end();
        sequenceDiagram.addSub(new FunctionNode("class3","function3()"));
        checkDslResult("file_sequence",sequenceDiagram.toDsl());
    }
}
