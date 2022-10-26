package vanstudio.sequence.generator;

import com.google.gson.Gson;
import vanstudio.sequence.openapi.model.ClassDescription;
import vanstudio.sequence.openapi.model.MethodDescription;
import org.junit.Test;

import java.awt.image.DataBufferInt;
import java.util.ArrayList;

import static org.junit.Assert.*;

public class MethodDescriptionTest {

    @Test
    public void toJson() {
        ArrayList<String> attr = new ArrayList<>();
        attr.add("public");

        ArrayList<String> argName = new ArrayList<>();
        argName.add("classDescription");
        argName.add("attributes");
        argName.add("methodName");
        argName.add("returnType");
        argName.add("argNames");
        argName.add("argTypes");
        argName.add("offset");

        ArrayList<String> argType = new ArrayList<>();
        argType.add("org.intellij.sequencer.model.ClassDescription");
        argType.add("java.util.List<java.lang.String>");
        argType.add("java.lang.String");
        argType.add("java.lang.String");
        argType.add("java.util.List<? extend java.lang.String>");
        argType.add("java.util.List<java.lang.String>");
        argType.add("int");

        MethodDescription m = MethodDescription.createMethodDescription(ClassDescription.ANONYMOUS_CLASS,
                attr,
                "createMethodDescription",
                "org.intellij.sequencer.model.MethodDescription",
                argName,
                argType,
                0
        );

        String json = m.toJson();
        Gson gson = new Gson();
        MethodDescription method = gson.fromJson(json, MethodDescription.class);

        assertEquals(m, method);
    }

    @Test(expected = NegativeArraySizeException.class)
    public void maxSize() {
        int size = -1233295232;
        DataBufferInt bufferInt = new DataBufferInt(size);
    }
}
