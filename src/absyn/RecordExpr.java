package absyn;

import symbol.Symbol;

public class RecordExpr extends Expr {
    public Symbol type;
    public FieldList fields;

    public RecordExpr(int pos, Symbol type, FieldList fields) {
        super(pos);
        this.type = type;
        this.fields = fields;
    }
}

