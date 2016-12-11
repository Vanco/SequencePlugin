package org.intellij.sequencer.diagram;

import com.google.gson.Gson;
import org.apache.log4j.Logger;
import org.intellij.sequencer.generator.ClassDescription;
import org.intellij.sequencer.generator.MethodDescription;

import java.io.IOException;
import java.io.PushbackReader;
import java.io.StringReader;
import java.util.*;

public class Parser {

    private static final Logger LOGGER = Logger.getLogger(Parser.class);

    private CallStack _callStack = new CallStack();
    private List _linkList = new ArrayList();
    private List _objList = new ArrayList();
    private int _currentHorizontalSeq = 0;
    private int _currentVerticalSeq = 0;

    public Parser() {
        ObjectInfo objectInfo = new ObjectInfo(ObjectInfo.ACTOR_NAME, Collections.EMPTY_LIST, _currentHorizontalSeq);
        _callStack.push(new CallInfo(objectInfo, "aMethod", _currentVerticalSeq));
    }

    public void parse(String sequenceStr) throws IOException {
        parse(new PushbackReader(new StringReader(sequenceStr)));
    }

    public void parse(PushbackReader reader) throws IOException {
        while(true) {
            skipWhitespace(reader);
            int c = reader.read();
            if(c == -1) {
                break;
            } else if(c == '(') {
                String methodName = readIdent(reader);
                addCall(methodName);
            } else if(c == ')') {
                addReturn();
            } else {
                LOGGER.error("Error '" + (char)c + "'");
            }
        }
        resolveBackCalls();
    }

    private void resolveBackCalls() {
        HashMap callsMap = new HashMap();
        for(Iterator iterator = _linkList.iterator(); iterator.hasNext();) {
            Link link = (Link)iterator.next();
            if(!(link instanceof Call))
                continue;
            callsMap.put(link.getMethodInfo().getNumbering(), link.getMethodInfo());
        }
        for(Iterator iterator = _linkList.iterator(); iterator.hasNext();) {
            Link link = (Link)iterator.next();
            Numbering numbering = link.getMethodInfo().getNumbering().getPreviousNumbering();
            if(numbering != null)
                link.setCallerMethodInfo((MethodInfo)callsMap.get(numbering));
        }
    }

    public List getLinks() {
        return _linkList;
    }

    public List getObjects() {
        return _objList;
    }

    private void addCall(String calledMethod) {
        Gson gson = new Gson();
        MethodDescription m = gson.fromJson(calledMethod, MethodDescription.class);
        ClassDescription c = m.getClassDescription();
        ObjectInfo objectInfo = new ObjectInfo(c.getClassName(), c.getAttributes(), _currentHorizontalSeq);
        int i = _objList.indexOf(objectInfo);
        if(i == -1) {
            ++_currentHorizontalSeq;
            _objList.add(objectInfo);
        } else {
            objectInfo = (ObjectInfo)_objList.get(i);
        }

        CallInfo callInfo = new CallInfo(objectInfo, m, _currentVerticalSeq);

        if(LOGGER.isDebugEnabled())
            LOGGER.debug("addCall(...) calling " + callInfo + " seq is " + _currentVerticalSeq);

        if(!_callStack.isEmpty()) {
            CallInfo currentInfo = _callStack.peek();
            callInfo.setNumbering();
            Call call = currentInfo.createCall(callInfo);
            call.setVerticalSeq(_currentVerticalSeq++);
            _linkList.add(call);
        }

        _callStack.push(callInfo);
    }

    private void addReturn() {
        CallInfo callInfo = _callStack.pop();

        MethodInfo methodInfo = new MethodInfo(callInfo.getObj(),
              callInfo.getNumbering(), callInfo.getAttributes(),
              callInfo.getMethod(), callInfo.getReturnType(),
              callInfo.getArgNames(), callInfo.getArgTypes(),
              callInfo.getStartingVerticalSeq(), _currentVerticalSeq);
        callInfo.getObj().addMethod(methodInfo);

        if(LOGGER.isDebugEnabled())
            LOGGER.debug("addReturn(...) returning from " + callInfo + " seq is " + _currentVerticalSeq);

        if(!_callStack.isEmpty()) {
            CallInfo currentInfo = _callStack.peek();
            currentInfo.getCall().setMethodInfo(methodInfo);
            CallReturn call = new CallReturn(callInfo.getObj(), currentInfo.getObj());
            call.setMethodInfo(methodInfo);
            _linkList.add(call);
            call.setVerticalSeq(_currentVerticalSeq++);
        }
    }

    private String readIdent(PushbackReader reader) throws IOException {

        skipWhitespace(reader);
        String result = readNonWhitespace(reader);
        skipWhitespace(reader);

        if(LOGGER.isDebugEnabled())
            LOGGER.debug("readIdent(...) returning " + result);

        return result;
    }

    private void skipWhitespace(PushbackReader reader) throws IOException {

        int c = -1;
        while(Character.isWhitespace((char)(c = reader.read()))) {
        }
        if(c != -1)
            reader.unread(c);
    }

    private String readNonWhitespace(PushbackReader r) throws IOException {
        int c = -1;
        StringBuffer sb = new StringBuffer();
        int deep = 0;
        boolean isGeneric = false;
        while((c = r.read()) != -1) {
            if(c == ')')
                break;
            else if (c == '\\') {
                int u = r.read();
                if (u != -1 && u == 'u') {
                    StringBuilder tmp = new StringBuilder();
                    tmp.append((char)c).append((char)u);
                    for (int j = 0; j < 4; j++) {
                        u = r.read();
                        tmp.append((char)u);
                    }
                    if (tmp.toString().equals("\\u003c")) {
                        deep ++;
                        isGeneric = true;
                        sb.append(tmp.toString());
                    } else if (tmp.toString().equals("\\u003e")) {
                        deep --;
                        if (deep == 0)
                            isGeneric = false;
                        sb.append(tmp.toString());
                    } else {
                        sb.append(tmp.toString());
                    }
                }
            }
            else if (c == '<') {
                deep ++;
                isGeneric = true;
                sb.append((char)c);
            }
            else if (c == '>') {
                deep --;
                if (deep == 0)
                    isGeneric = false;
                sb.append((char)c);
            }
            else if(Character.isWhitespace((char)c)) {
                if (isGeneric) {
                    sb.append((char)c);
                } else {
                    break;
                }
            }
            else
                sb.append((char)c);
        }
        if(c != -1)
            r.unread(c);
        return sb.toString();
    }

    private class CallStack {
        private Stack stack = new Stack();
        private CallInfo nPointerCallInfo;
        private int nPointerCounter;

        public void push(CallInfo callInfo) {
            stack.push(callInfo);
            nPointerCounter++;
            if(nPointerCounter > 1)
                nPointerCallInfo = callInfo;
        }

        public CallInfo pop() {
            CallInfo result = (CallInfo)stack.pop();
            nPointerCallInfo = result;
            return result;
        }

        public Numbering getNumbering() {
            if(nPointerCallInfo == null)
                return peek().getNumbering();
            return nPointerCallInfo.getNumbering();
        }

        public CallInfo peek() {
            return (CallInfo)stack.peek();
        }

        public int size() {
            return stack.size();
        }

        public boolean isEmpty() {
            return stack.isEmpty();
        }
    }

    private class CallInfo {
        private ObjectInfo _obj;
        private String _method;
        private List _argNames = new ArrayList();
        private List _argTypes = new ArrayList();
        private List _attributes = new ArrayList();
        private String _returnType;

        private Numbering _numbering;
        private Call _call;
        private int _startingSeq = -1;

        CallInfo(ObjectInfo obj, String method, int startingSeq) {
            _obj = obj;
            _method = method;
            _startingSeq = startingSeq;
        }

        CallInfo(ObjectInfo obj, MethodDescription m, int startingSeq) {
            _obj = obj;
            _method = m.getMethodName();
            _attributes.addAll(m.getAttributes());
            _argNames.addAll(m.getArgNames());
            _argTypes.addAll(m.getArgTypes());
            _returnType = m.getReturnType();
            _startingSeq = startingSeq;
        }

        void setNumbering() {
            int stackLevel = _callStack.size() - 1;
            Numbering numbering = _callStack.getNumbering();
            _numbering = new Numbering(numbering);
            if(_numbering.level() <= stackLevel)
                _numbering.addNewLevel();
            else
                _numbering.incrementLevel(stackLevel);
        }

        Call createCall(CallInfo to) {
            _call = new Call(_obj, to.getObj());
            return _call;
        }

        Call getCall() {
            return _call;
        }

        ObjectInfo getObj() {
            return _obj;
        }

        public List getAttributes() {
            return _attributes;
        }

        String getMethod() {
            return _method;
        }

        public String getReturnType() {
            return _returnType;
        }

        public List getArgNames() {
            return _argNames;
        }

        public List getArgTypes() {
            return _argTypes;
        }

        public Numbering getNumbering() {
            return _numbering;
        }

        int getStartingVerticalSeq() {
            return _startingSeq;
        }

        public String toString() {
            return "Calling " + _method + " on " + _obj;
        }
    }
}
