package absyn;

public class DeclList extends Absyn {
    public Decl decl;
    public DeclList next;

    public DeclList(int pos, Decl decl, DeclList next) {
        super(pos);
        this.decl = decl;
        this.next = next;
    }
}

