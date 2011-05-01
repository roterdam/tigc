package absyn;

import symbol.Symbol;

public class FieldList extends Absyn {
    public Symbol name;
    public Expr value;
    public FieldList next;

    public FieldList(int pos, Symbol name, Expr value, FieldList next) {
        super(pos);
        this.name = name;
        this.value = value;
        this.next = next;
    }
}

