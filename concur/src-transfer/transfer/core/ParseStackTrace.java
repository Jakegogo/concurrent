package transfer.core;

import java.lang.reflect.Type;

/**
 * 解析跟踪栈
 * Created by Jake on 2015/5/10.
 */
public class ParseStackTrace {

    private String name;

    private Type type;

    private int index = -1;

    private ParseStackTrace parent;

    ParseStackTrace(){

    }

    public ParseStackTrace(Type type){
        this.type = type;
    }

    public ParseStackTrace(String name, Type type){
        this.name = name;
        this.type = type;
    }

    public ParseStackTrace next(String name, Type type) {
        ParseStackTrace next = new ParseStackTrace();
        next.name = name;
        next.type = type;
        next.parent = this;
        return next;
    }


    public void setName(String name) {
        this.name = name;
    }

    public void setType(Type type) {
        this.type = type;
    }

    public void setIndex(int index) {
        this.index = index;
    }

    @Override
    public String toString() {
        boolean delimeter = false;

        StringBuilder stackStr = new StringBuilder()
                .append("\n")
                .append("at {");
                delimeter = notNullAppend(delimeter, stackStr, name, "name='" + name + '\'');
                delimeter = notNullAppend(delimeter, stackStr, type, "type=" + type);
                delimeter = ge0Append(delimeter, stackStr, index, "index=" + index);
                stackStr.append('}');

        if (this.parent != null) {
            stackStr.append(this.getParent().toString());
        }
        return stackStr.toString();
    }

    private boolean ge0Append(boolean delimeter, StringBuilder stackStr, int num, String str) {
        if (num >= 0) {
            if (delimeter) {
                stackStr.append(", ");
            }
            stackStr.append(str);
            delimeter = true;
        }
        return delimeter;
    }

    private boolean notNullAppend(boolean delimeter, StringBuilder stackStr, Object obj, String str) {
        if (obj != null) {
            if (delimeter) {
                stackStr.append(", ");
            }
            stackStr.append(str);
            delimeter = true;
        }
        return delimeter;
    }

    public String getName() {
        return name;
    }

    public Type getType() {
        return type;
    }

    public int getIndex() {
        return index;
    }

    public ParseStackTrace getParent() {
        return parent;
    }



}
