package intermediate;

import java.util.*;
import util.*;

public class IntermediateCodeList implements Iterable<IntermediateCode> {
    private SimpleLinkedList<IntermediateCode> codes = new SimpleLinkedList<IntermediateCode>();

    public void add(IntermediateCode i) {
        add(i.label, i.tac, i.comment);
    }

    public void add(Label label, ThreeAddressCode tac, String comment) {
        codes.add(new IntermediateCode(label, tac, comment));
    }

    public void addFirst(Label label, ThreeAddressCode tac, String comment) {
        codes.addFirst(new IntermediateCode(label, tac, comment));
    }

    public void add(ThreeAddressCode tac, String comment) {
        add(null, tac, comment);
    }

    public void add(Label label, ThreeAddressCode tac) {
        add(label, tac, null);
    }

    public void add(Label label) {
        add(label, null, null);
    }

    public void add(Label label, String comment) {
        add(label, null, comment);
    }

    public void add(ThreeAddressCode tac) {
        add(null, tac, null);
    }

    public void addFirst(ThreeAddressCode tac, String comment) {
        addFirst(null, tac, comment);
    }

    public void addFirst(Label label, ThreeAddressCode tac) {
        addFirst(label, tac, null);
    }

    public void addFirst(Label label) {
        addFirst(label, null, null);
    }

    public void addFirst(Label label, String comment) {
        addFirst(label, null, comment);
    }

    public void addFirst(ThreeAddressCode tac) {
        addFirst(null, tac, null);
    }

    public void addAll(IntermediateCodeList list) {
        codes.addAll(list.codes);
    }

    public void addAllClone(IntermediateCodeList list) {
        codes.addAll(new SimpleLinkedList<IntermediateCode>(list.codes));
    }

    public Iterator<IntermediateCode> iterator() {
        return codes.iterator();
    }
}

