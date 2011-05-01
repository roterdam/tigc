package absyn;

public class TypeFields extends Absyn {
    public TypeField head;
    public TypeFields next;

    public TypeFields(int pos, TypeField head, TypeFields next) {
        super(pos);
        this.head = head;
        this.next = next;
    }
}

