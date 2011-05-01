package absyn;

public class IntExpr extends Expr {
    public Integer value;
    public IntExpr(int pos, Integer v) {
        super(pos);
        value = v;
    }
}

