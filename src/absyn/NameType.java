package absyn;

import symbol.Symbol;

public class NameType extends Type {
    public Symbol name;

    public NameType(int pos, Symbol name) {
        super(pos);
        this.name = name;
    }
}

