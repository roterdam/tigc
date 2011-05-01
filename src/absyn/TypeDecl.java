package absyn;

import symbol.Symbol;

public class TypeDecl extends Decl {
    public Symbol name;
    public Type type;

    public TypeDecl(int pos, Symbol name, Type type) {
        super(pos);
        this.name = name;
        this.type = type;
    }
}

