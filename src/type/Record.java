package type;

import symbol.Symbol;

public class Record extends Type {
    public Symbol field;
    public Type type;
    public Record next;

    public Record(Symbol field, Type type, Record next) {
        this.field = field;
        this.type = type;
        this.next = next;
    }

    public boolean fits(Type t) {
        return this == t.actual();
    }

    public boolean isEmpty() {
        return false;
    }

    public java.lang.String toString() {
        return "RECORD";
    }

    public Type findField(Symbol fieldName) {
        if (field == fieldName)
            return type;
        else if (next != null)
            return next.findField(fieldName);
        else
            return null;
    }

    public int fieldIndex(Symbol fieldName) {
        if (field == fieldName)
            return 0;
        else if (next != null)
            return 1 + next.fieldIndex(fieldName);
        else
            return -1;
    }

    public int length() {
        if (next == null)
            return 1;
        else
            return 1 + next.length();
    }
}

