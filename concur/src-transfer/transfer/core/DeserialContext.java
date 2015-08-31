package transfer.core;

import transfer.utils.IntegerMap;

import java.lang.reflect.Type;

/**
 * 解码上下文
 * Created by Jake on 2015/5/9.
 */
public class DeserialContext {

    private IntegerMap referenceMap;

    private ParseStackTrace stackTrace;

    public IntegerMap getReferenceMap() {
        return referenceMap;
    }

    /**
     * 输出解析堆栈
     * (当出现异常时,可以定位解析异常的位置)
     */
    public void printStackTrace() {
        System.out.println(getStackTrace());
    }


    public ParseStackTrace getStackTrace() {
        return stackTrace;
    }

    public ParseStackTrace nextStackTrace(String name, Type type) {
        ParseStackTrace next;
        if (stackTrace == null) {
            next = new ParseStackTrace(name, type);
        } else {
            next = stackTrace.next(name, type);
        }
        this.stackTrace = next;
        return next;
    }

    public ParseStackTrace nextStackTrace(Type type) {
        ParseStackTrace next;
        if (stackTrace == null) {
            next = new ParseStackTrace(type);
        } else {
            next = stackTrace.next(null, type);
        }
        this.stackTrace = next;
        return next;
    }


    public ParseStackTrace next(ParseStackTrace parent, String name, Type type) {
        ParseStackTrace next = parent.next(name, type);
        this.stackTrace = next;
        return next;
    }

    public ParseStackTrace next(ParseStackTrace parent, Type type) {
        ParseStackTrace next = parent.next(null, type);
        this.stackTrace = next;
        return next;
    }

    public ParseStackTrace next(ParseStackTrace parent, String name) {
        ParseStackTrace next = parent.next(name, null);
        this.stackTrace = next;
        return next;
    }

    public ParseStackTrace nextIndex(ParseStackTrace parent, int index) {
        parent.setIndex(index);
        this.stackTrace = parent;
        return parent;
    }

}
