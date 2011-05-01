package absyn;

public class StringExpr extends Expr {
    public String value;
    public StringExpr(int pos, String v) {
        super(pos);
        value = v;
    }
}

