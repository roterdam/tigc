package absyn;

public class AssignmentExpr extends Expr {
    public LValue lvalue;
    public Expr e;

    public AssignmentExpr(int pos, LValue lvalue, Expr e) {
        super(pos);
        this.lvalue = lvalue;
        this.e = e;
    }
}
