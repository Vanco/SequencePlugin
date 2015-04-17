package org.intellij.sequencer.diagram;

import org.apache.log4j.Logger;

import java.io.IOException;
import java.io.PushbackReader;
import java.io.StringReader;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Parser {
//    private final static Pattern METHOD_PATTERN =
//          Pattern.compile("(?:\\|(\\w+))*\\|@([a-zA-Z_0-9.]+)\\[(?:(\\w+)=([a-zA-Z_0-9.]+(?:\\[\\])?),?)*\\]:([a-zA-Z_0-9.]+(?:\\[\\])?)");
//    private final static Pattern CLASS_PATTERN =
//          Pattern.compile("(?:\\|(\\w+))*\\|@([a-zA-Z_0-9.]+)");
//    private final static Pattern ATTRIBUTE_PATTERN =
//          Pattern.compile("\\|(\\w+)");
//    private static final Pattern NAME_PATTERN =
//          Pattern.compile("\\|@([a-zA-Z_0-9.]+)");
//    private static final Pattern ARG_PATTERN =
//          Pattern.compile("(?:(\\w+)=([a-zA-Z_0-9.]+(?:\\[\\])?))");
//    private static final Pattern RETURN_TYPE =
//          Pattern.compile(":([a-zA-Z_0-9.]+(?:\\[\\])?)");

    private final static Pattern METHOD_PATTERN =
            Pattern.compile("(?:\\|(\\w+))*\\|@([a-zA-Z_0-9.]+)\\[(?:(\\w+)=([a-zA-Z_0-9.]+(?:<(([a-zA-Z_0-9.]+),?)*>)?(?:\\[\\])?),?)*\\]:([a-zA-Z_0-9.]+(?:<(([a-zA-Z_0-9.]+),?)*>)?(?:\\[\\])?)");
    private final static Pattern CLASS_PATTERN =
            Pattern.compile("(?:\\|(\\w+))*\\|@([a-zA-Z_0-9.]+)");
    private final static Pattern ATTRIBUTE_PATTERN =
            Pattern.compile("\\|(\\w+)");
    private static final Pattern NAME_PATTERN =
            Pattern.compile("\\|@([a-zA-Z_0-9.]+)");
    private static final Pattern ARG_PATTERN =
            Pattern.compile("(?:(\\w+)=([a-zA-Z_0-9.]+(?:<(([a-zA-Z_0-9.]+),?)*>)?(?:\\[\\])?))");
    private static final Pattern RETURN_TYPE =
            Pattern.compile(":([a-zA-Z_0-9.]+(?:<(([a-zA-Z_0-9.]+),?)*>)?(?:\\[\\])?)");

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
                String objName = readIdent(reader);
                String methodName = readIdent(reader);
                addCall(objName, methodName);
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

    private void addCall(String calledObject, String calledMethod) {
        Matcher matcher = CLASS_PATTERN.matcher(calledObject);
        String className = calledObject;
        List attributes = new ArrayList();
        if(matcher.matches()) {
            Matcher nameMatcher = NAME_PATTERN.matcher(calledObject);
            if(nameMatcher.find())
                className = nameMatcher.group(1);
            else
                className = "NotParsed";
            Matcher attrMatcher = ATTRIBUTE_PATTERN.matcher(calledObject);
            while(attrMatcher.find())
                attributes.add(attrMatcher.group(1));
        }
        ObjectInfo objectInfo = new ObjectInfo(className, attributes, _currentHorizontalSeq);
        int i = _objList.indexOf(objectInfo);
        if(i == -1) {
            ++_currentHorizontalSeq;
            _objList.add(objectInfo);
        } else {
            objectInfo = (ObjectInfo)_objList.get(i);
        }

        CallInfo callInfo = new CallInfo(objectInfo, calledMethod, _currentVerticalSeq);

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
            Matcher wholeExprMatcher = METHOD_PATTERN.matcher(method);
            if(method == null || method.length() == 0 || !wholeExprMatcher.matches()) {
                _method = method;
            } else {
                Matcher attrMatcher = ATTRIBUTE_PATTERN.matcher(method);
                while(attrMatcher.find())
                    _attributes.add(attrMatcher.group(1));
                Matcher methodNameMatcher = NAME_PATTERN.matcher(method);
                if(methodNameMatcher.find())
                    _method = methodNameMatcher.group(1);
                else
                    _method = "NotParsed";
                Matcher argMatcher = ARG_PATTERN.matcher(method);
                while(argMatcher.find()) {
                    _argNames.add(argMatcher.group(1));
                    _argTypes.add(argMatcher.group(2));
                }
                Matcher returnTypeMatcher = RETURN_TYPE.matcher(method);
                if(returnTypeMatcher.find())
                    _returnType = returnTypeMatcher.group(1);
            }
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
