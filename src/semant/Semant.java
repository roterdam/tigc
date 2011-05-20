package semant;

import symbol.*;
import error.ErrorMsg;
import absyn.*;
import utils.SimpleLinkedList;

public class Semant {
    private Table<Entry> vt;
    private Table<type.Type> tt;
    private ErrorMsg e;

    private Symbol sym(String s) {
        return Symbol.symbol(s);
    }

    private void initTypes() {
        tt.put(sym("int"), new type.Int());
        tt.put(sym("string"), new type.String());
    }

    private void initFunctions() {
        // function print(s : string)
        vt.put(sym("print"), new FuncEntry(
                    new type.Record(sym("s"), new type.String(), null),
                    new type.Void()));

        // function printi(i : int)
        vt.put(sym("printi"), new FuncEntry(
                    new type.Record(sym("i"), new type.Int(), null),
                    new type.Void()));

        // function flush()
        vt.put(sym("flush"), new FuncEntry(
                    null, new type.Void()));

        // function getchar() : string
        vt.put(sym("getchar"), new FuncEntry(
                    null, new type.String()));

        // function ord(s: string) : int
        vt.put(sym("ord"), new FuncEntry(
                    new type.Record(sym("s"), new type.String(), null),
                    new type.Int()));

        // function chr(i: int) : string
        vt.put(sym("chr"), new FuncEntry(
                    new type.Record(sym("i"), new type.Int(), null),
                    new type.String()));

        // function size(s: string) : int
        vt.put(sym("size"), new FuncEntry(
                    new type.Record(sym("s"), new type.String(), null),
                    new type.Int()));

        // function substring(s : string, first: int, n: int) : string
        vt.put(sym("substring"), new FuncEntry(
                    new type.Record(sym("s"), new type.String(),
                        new type.Record(sym("first"), new type.Int(),
                            new type.Record(sym("n"), new type.Int(), null)
                            )
                        ), new type.String()));
        
        // function concat(s1: string, s2: string) : string
        vt.put(sym("concat"), new FuncEntry(
                    new type.Record(sym("s1"), new type.String(),
                        new type.Record(sym("s2"), new type.String(), null)
                        ), new type.String()));

        // function not(i: int): int
        vt.put(sym("not"), new FuncEntry(
                    new type.Record(sym("i"), new type.Int(), null),
                    new type.Int()));

        // function exit(i: int)
        vt.put(sym("exit"), new FuncEntry(
                    new type.Record(sym("i"), new type.Int(), null),
                    new type.Void()));
    }

    public Semant(ErrorMsg em) {
        e = em;

        tt = new Table<type.Type>();
        vt = new Table<Entry>();
        initTypes();
        initFunctions();
    }

    public void translate(absyn.Expr expr) {
        transExpr(expr, false);
    }

    private void checkType(type.Type left, type.Type right, int pos) {
        if (!right.fits(left))
            e.report(left.toString() + " needed, but " + right.toString() + " given", pos);
    }

    private SimpleLinkedList<Symbol> merge(SimpleLinkedList<Symbol> x, SimpleLinkedList<Symbol> y) {
        return SimpleLinkedList.merge(x, y);
    }

    private TranslateResult transExpr(absyn.Expr expr, boolean breakable) {
        if (expr instanceof ArrayExpr)
            return transExpr((ArrayExpr) expr, breakable);
        else if (expr instanceof AssignmentExpr)
            return transExpr((AssignmentExpr) expr, breakable);
        else if (expr instanceof BreakExpr)
            return transExpr((BreakExpr) expr, breakable);
        else if (expr instanceof CallExpr)
            return transExpr((CallExpr) expr, breakable);
        else if (expr instanceof ForExpr)
            return transExpr((ForExpr) expr, breakable);
        else if (expr instanceof IfExpr)
            return transExpr((IfExpr) expr, breakable);
        else if (expr instanceof IntExpr)
            return transExpr((IntExpr) expr, breakable);
        else if (expr instanceof LetExpr)
            return transExpr((LetExpr) expr, breakable);
        else if (expr instanceof LValueExpr)
            return transExpr((LValueExpr) expr, breakable);
        else if (expr instanceof NegationExpr)
            return transExpr((NegationExpr) expr, breakable);
        else if (expr instanceof NilExpr)
            return transExpr((NilExpr) expr, breakable);
        else if (expr instanceof OpExpr)
            return transExpr((OpExpr) expr, breakable);
        else if (expr instanceof RecordExpr)
            return transExpr((RecordExpr) expr, breakable);
        else if (expr instanceof SeqExpr)
            return transExpr((SeqExpr) expr, breakable);
        else if (expr instanceof StringExpr)
            return transExpr((StringExpr) expr, breakable);
        else if (expr instanceof WhileExpr)
            return transExpr((WhileExpr) expr, breakable);
        else
            return new TranslateResult(null, new type.Int());
    }

    private TranslateResult transExpr(ArrayExpr expr, boolean breakable) {
        type.Type t = tt.get(expr.type), ta = t.actual();
        if (t == null) {
            e.report("Undefined type: " + expr.type.toString() + "; int array assumed.", expr.pos);
            return new TranslateResult(null, new type.Array(new type.Int()));
        } else if (!(ta instanceof type.Array)) {
            e.report(t.toString() + " is not an array type; int array assumed.");
            return new TranslateResult(null, new type.Array(new type.Int()));
        } else {
            TranslateResult size = transExpr(expr.size, breakable);
            checkType(new type.Int(), size.type, expr.size.pos);
            TranslateResult init = transExpr(expr.init, breakable);
            checkType(((type.Array)ta).base, init.type, expr.init.pos);

            return new TranslateResult(null, t, merge(size.foreigns, init.foreigns));
        }
    }

    private TranslateResult transExpr(AssignmentExpr expr, boolean breakable) {
        TranslateResult l = transLValue(expr.lvalue, breakable, true);
        TranslateResult r = transExpr(expr.e, breakable);
        checkType(l.type, r.type, expr.pos);
        return new TranslateResult(null, new type.Void(), merge(l.foreigns, r.foreigns));
    }

    private TranslateResult transExpr(BreakExpr expr, boolean breakable) {
        if (!breakable)
            e.report("Invalid break", expr.pos);
        return new TranslateResult(null, new type.Void());
    }

    private TranslateResult transExpr(CallExpr expr, boolean breakable) {
        Entry e = vt.get(expr.func);
        if (e == null) {
            this.e.report("Undefined function " + expr.func.toString() + "; assumed return VOID", expr.pos);
            return new TranslateResult(null, new type.Void());
        }
        if (e instanceof VarEntry) {
            this.e.report(expr.func.toString() + " is not a function; assumed return VOID", expr.pos);
            return new TranslateResult(null, new type.Void());
        }

        FuncEntry func = (FuncEntry)e;

        type.Record p = func.params;
        ExprList q = expr.args;

        SimpleLinkedList<Symbol> foreigns = new SimpleLinkedList<Symbol>();
        while (p != null && !p.isEmpty() && q != null) {
            TranslateResult tq = transExpr(q.expr, breakable);
            checkType(p.type, tq.type, q.expr.pos);
            foreigns = merge(foreigns, tq.foreigns);

            p = p.next;
            q = q.next;
        }

        if ((p != null && !p.isEmpty()) || q != null)
            this.e.report("Function param number mismatch", expr.pos);
        
        return new TranslateResult(null, func.result, foreigns);
    }

    private TranslateResult transExpr(ForExpr expr, boolean breakable) {
        TranslateResult br = transExpr(expr.begin, breakable),
                        er = transExpr(expr.end, breakable);
        checkType(new type.Int(), br.type, expr.begin.pos);
        checkType(new type.Int(), er.type, expr.end.pos);
        vt.beginScope();
        vt.put(expr.var, new VarEntry(new type.Int(), false));
        TranslateResult result = transExpr(expr.body, true);
        checkType(new type.Void(), result.type, expr.body.pos);
        vt.endScope();
        return new TranslateResult(null, new type.Void(), merge(merge(br.foreigns, er.foreigns), result.foreigns));
    }

    private TranslateResult transExpr(IfExpr expr, boolean breakable) {
        TranslateResult cr = transExpr(expr.condition, breakable);
        checkType(new type.Int(), cr.type, expr.condition.pos);
        if (expr.elseClause != null) {
            TranslateResult thenr = transExpr(expr.thenClause, breakable);
            TranslateResult elser = transExpr(expr.elseClause, breakable);
            checkType(thenr.type, elser.type, expr.thenClause.pos);
            return new TranslateResult(null, thenr.type, merge(cr.foreigns, merge(thenr.foreigns, elser.foreigns)));
        } else {
            TranslateResult thenr = transExpr(expr.thenClause, breakable);
            checkType(new type.Void(), thenr.type, expr.thenClause.pos);
            return new TranslateResult(null, new type.Void(), merge(cr.foreigns, thenr.foreigns));
        }
    }

    private TranslateResult transExpr(IntExpr expr, boolean breakable) {
        return new TranslateResult(null, new type.Int());
    }

    private TranslateResult transExpr(LetExpr expr, boolean breakable) {
        vt.beginScope();
        tt.beginScope();
        TranslateResult rd = transDeclList(expr.decls, breakable);
        TranslateResult re = transExprList(expr.exprs, breakable);
        vt.endScope();
        tt.endScope();
        return new TranslateResult(null, re.type, merge(rd.foreigns, re.foreigns));
    }

    private TranslateResult transExpr(LValueExpr expr, boolean breakable) {
        return transLValue(expr.lvalue, breakable, false);
    }

    private TranslateResult transExpr(NegationExpr expr, boolean breakable) {
        TranslateResult te = transExpr(expr.value, breakable);
        checkType(new type.Int(), te.type, expr.value.pos);
        return new TranslateResult(null, new type.Int(), te.foreigns);
    }

    private TranslateResult transExpr(NilExpr expr, boolean breakable) {
        return new TranslateResult(null, new type.Nil());
    }

    private TranslateResult transExpr(OpExpr expr, boolean breakable) {
        TranslateResult lr = transExpr(expr.left, breakable),
                        rr = transExpr(expr.right, breakable);
        type.Type ltype = lr.type, la = ltype.actual(),
            rtype = rr.type, ra = rtype.actual();
        if (la instanceof type.Int || ra instanceof type.Int) {
            checkType(new type.Int(), la, expr.left.pos);
            checkType(new type.Int(), ra, expr.right.pos);
        } else if (la instanceof type.String || ra instanceof type.String) {
            checkType(new type.String(), la, expr.left.pos);
            checkType(new type.String(), ra, expr.right.pos);
        } else if ((expr.op == OpExpr.Op.EQ || expr.op == OpExpr.Op.NEQ) &&
                (la instanceof type.Array || la instanceof type.Record
                 || ra instanceof type.Array || ra instanceof type.Record)) {
            if (!(ltype.fits(rtype) || rtype.fits(ltype)))
                e.report("Invalid comparation between " + ltype.toString()
                        + " and " + rtype.toString(), expr.pos);
        } else
            e.report("Invalid comparation between " + ltype.toString()
                    + " and " + rtype.toString(), expr.pos);
        return new TranslateResult(null, new type.Int(), merge(lr.foreigns, rr.foreigns));
    }

    private TranslateResult transExpr(RecordExpr expr, boolean breakable) {
        type.Type type = tt.get(expr.type);
        if (type == null) {
            e.report(expr.type.toString() + " undefined; empty RECORD assumed", expr.pos);
            return new TranslateResult(null, new type.Record(null, null, null));
        } else if (!(type.actual() instanceof type.Record)) {
            e.report(type.toString() + " is not a record; empty RECORD assumed", expr.pos);
            return new TranslateResult(null, new type.Record(null, null, null));
        } else {
            type.Record p = (type.Record) type.actual();
            FieldList q = expr.fields;

            SimpleLinkedList<Symbol> foreigns = new SimpleLinkedList<Symbol>();
            while ((p != null && !p.isEmpty()) && q != null) {
                if (p.field != q.name)
                    e.report("Field name mismatch: " + p.field.toString() + " expected but"
                           + q.name.toString() + " found", q.pos);
                TranslateResult qr = transExpr(q.value, breakable);
                checkType(p.type, qr.type, q.value.pos);
                foreigns = merge(foreigns, qr.foreigns);

                p = p.next;
                q = q.next;
            }

            if ((p != null && !p.isEmpty()) || q != null)
                e.report("Field number mismatch", expr.fields.pos);

            return new TranslateResult(null, type, foreigns);
        }
    }

    private TranslateResult transExpr(SeqExpr expr, boolean breakable) {
        return transExprList(expr.exprList, breakable);
    }

    private TranslateResult transExpr(StringExpr expr, boolean breakable) {
        return new TranslateResult(null, new type.String());
    }

    private TranslateResult transExpr(WhileExpr expr, boolean breakable) {
        TranslateResult cr = transExpr(expr.condition, breakable);
        TranslateResult br = transExpr(expr.body, true);
        checkType(new type.Int(), cr.type, expr.condition.pos);
        checkType(new type.Void(), br.type, expr.body.pos);
        return new TranslateResult(null, new type.Void(), merge(cr.foreigns, br.foreigns));
    }

    private TranslateResult transExprList(ExprList expr, boolean breakable) {
        type.Type retType = new type.Void();
        SimpleLinkedList<Symbol> foreigns = new SimpleLinkedList<Symbol>();
        while (expr != null) {
            TranslateResult r = transExpr(expr.expr, breakable);
            retType = r.type;
            foreigns = merge(foreigns, r.foreigns);
            expr = expr.next;
        }
        return new TranslateResult(null, retType, foreigns);
    }

    private TranslateResult transDeclList(DeclList expr, boolean breakable) {
        if (expr == null)
            return new TranslateResult(null, null);

        SimpleLinkedList<Symbol> foreigns = new SimpleLinkedList<Symbol>();
        if (expr.decl instanceof VarDecl) {

            VarDecl vd = (VarDecl) expr.decl;
            if (vd.type != null) {
                type.Type type = tt.get(vd.type);
                if (type == null)
                    e.report(vd.type.toString() + " undefined");
                else {
                    vt.put(vd.id, new VarEntry(type));
                    TranslateResult ir = transExpr(vd.value, breakable);
                    checkType(type, ir.type, vd.value.pos);
                    foreigns = merge(foreigns, ir.foreigns);
                }
            } else {
                TranslateResult ir = transExpr(vd.value, breakable);
                foreigns = merge(foreigns, ir.foreigns);
                type.Type type = ir.type;
                type.Type a = type.actual();
                if (a instanceof type.Nil || a instanceof type.Void) {
                    e.report("Invalid initialize type: " + type.toString()
                            + "; INT assumed");
                    type = new type.Int();
                }
                vt.put(vd.id, new VarEntry(type));
            }

            return new TranslateResult(null, null,
                    merge(foreigns, transDeclList(expr.next, breakable).foreigns));

        } else if (expr.decl instanceof TypeDecl) {

            DeclList p = expr;
            java.util.HashSet<Symbol> set = new java.util.HashSet<Symbol>();
            for (p = expr; p != null && p.decl instanceof TypeDecl; p = p.next) {
                TypeDecl td = (TypeDecl) p.decl;

                if (set.add(td.name))
                    tt.put(td.name, new type.Name(td.name));
                else
                    e.report(td.name.toString() + " already defined in the same block", td.pos);
            }
            for (p = expr; p != null && p.decl instanceof TypeDecl; p = p.next) {
                TypeDecl td = (TypeDecl) p.decl;
                ((type.Name) tt.get(td.name)).bind(transType(td.type));
            }
            for (p = expr; p != null && p.decl instanceof TypeDecl; p = p.next) {
                TypeDecl td = (TypeDecl) p.decl;
                if (((type.Name) tt.get(td.name)).isLoop()) {
                    e.report("Type declaration loop found on " + td.name.toString()
                            + "; INT assumed", td.pos);
                    ((type.Name) tt.get(td.name)).bind(new type.Int());
                }
            }

            return transDeclList(p, breakable);

        } else /*if (expr.decl instanceof FuncDecl)*/ {

            DeclList p = expr;
            java.util.HashSet<Symbol> set = new java.util.HashSet<Symbol>();
            for (p = expr; p != null && p.decl instanceof FuncDecl; p = p.next) {
                FuncDecl fd = (FuncDecl) p.decl;

                if (set.add(fd.name)) {
                    type.Type result = new type.Void();
                    if (fd.type != null)
                        result = tt.get(fd.type);
                    if (result == null) {
                        e.report(fd.type.toString() + " undefined; assumed INT", fd.pos);
                        result = new type.Int();
                    }
                    vt.put(fd.name, new FuncEntry(transTypeFields(fd.params), result));
                }
                else
                    e.report(fd.name.toString() + " already defined in the sasme block", fd.pos);
            }
            for (p = expr; p != null && p.decl instanceof FuncDecl; p = p.next) {
                FuncDecl fd = (FuncDecl) p.decl;
                
                vt.beginScope(true);

                FuncEntry fe = (FuncEntry) vt.get(fd.name);
                for (type.Record i = fe.params; i != null && !i.isEmpty(); i = i.next)
                    vt.put(i.field, new VarEntry(i.type));
                TranslateResult te = transExpr(fd.body, false);
                checkType(fe.result, te.type, fd.body.pos);

                String rp = "";
                for (Symbol s: te.foreigns)
                    rp += s.toString() + " ";
                System.out.println(fd.name.toString() + " at " + new Integer(fd.pos).toString() + " has ref param: " + rp);

                foreigns = merge(foreigns, te.foreigns);
                
                vt.endScope();
            }

            return new TranslateResult(null, null, merge(foreigns, transDeclList(p, breakable).foreigns));

        }
    }
    
    private type.Record transTypeFields(TypeFields fields) {
        type.Record ret = null;
        if (fields == null)
            ret = new type.EmptyRecord();
        else {
            type.Record last = null;

            while (fields != null) {
                type.Type fieldType = tt.get(fields.head.type);
                if (fieldType == null) {
                    e.report("Undefined type " + fields.head.type.toString()
                            + "; INT assumed", fields.head.pos);
                    fieldType = new type.Int();
                }
                type.Record temp = new type.Record(fields.head.name, fieldType, null);
                if (last != null) {
                    last.next = temp;
                    last = last.next;
                } else {
                    ret = temp;
                    last = temp;
                }
                fields = fields.next;
            }
        }
        return ret;
    }

    private type.Type transType(Type type) {
        if (type instanceof NameType) {
            NameType nt = (NameType) type;
            type.Type t = tt.get(nt.name);
            if (t == null) {
                e.report("Undefined type " + nt.name.toString()
                        + "; INT assumed", nt.pos);
                t = new type.Int();
            }
            return t;
        } else if (type instanceof ArrayType) {
            ArrayType at = (ArrayType) type;
            type.Type t = tt.get(at.base);
            if (t == null) {
                e.report("Undefined type " + at.base.toString()
                        + "; INT assumed", at.pos);
                t = new type.Int();
            }
            return new type.Array(t);
        } else /*if (type instanceof RecordType)*/ {
            RecordType rt = (RecordType) type;
            TypeFields fields = rt.fields;
            return transTypeFields(fields);
       }
    }

    private TranslateResult transLValue(LValue lvalue, boolean breakable, boolean assignment) {
        if (lvalue instanceof VarLValue) {

            VarLValue vl = (VarLValue) lvalue;
            Entry entry = vt.get(vl.name);
            type.Type type = null;
            SimpleLinkedList<Symbol> foreigns = new SimpleLinkedList<Symbol>();
            if (entry == null) {
                e.report("Undefined variable " + vl.name.toString()
                        + "; type INT assumed", vl.pos);
                type = new type.Int();
            } else if (entry instanceof FuncEntry) {
                e.report(vl.name.toString() + " is a function, not a variable; type INT assumed", vl.pos);
                type = new type.Int();
            } else {
                type = ((VarEntry) entry).type;
                if (assignment && !((VarEntry) entry).assignable)
                    e.report(vl.name.toString() + " cannot be assigned here", vl.pos);
                if (vt.isForeign(vl.name))
                    foreigns.add(vl.name);
            }
            return new TranslateResult(null, type, foreigns);

        } else if (lvalue instanceof FieldLValue) {

            FieldLValue fl = (FieldLValue) lvalue;
            TranslateResult tr = transLValue(fl.lvalue, breakable, assignment);
            type.Type type = tr.type, ta = type.actual(), ret = null;
            if (ta instanceof type.Record) {
                type.Record temp = (type.Record) ta;
                ret = temp.findField(fl.id);
                if (ret == null) {
                    e.report(type.toString() + " do not have field " + fl.id
                            + "; type INT assumed", fl.pos);
                    ret = new type.Int();
                }
            } else {
                e.report(type.toString() + " is not a RECORD; type INT assumed", fl.pos);
                ret = new type.Int();
            }
            return new TranslateResult(null, ret, tr.foreigns);

        } else /*if (lvalue instanceof SubscriptLValue)*/ {
            
            SubscriptLValue sl = (SubscriptLValue) lvalue;
            TranslateResult tr = transLValue(sl.lvalue, breakable, assignment);
            type.Type type = tr.type, ta = type.actual(), ret = null;
            if (!(ta instanceof type.Array)) {
                e.report(type.toString() + " is not an ARRAY", sl.pos);
                ret = new type.Int();
            } else
                ret = ((type.Array) ta).base;
            TranslateResult tr2 = transExpr(sl.expr, breakable);
            checkType(new type.Int(), tr2.type, sl.expr.pos);
            return new TranslateResult(null, ret, merge(tr.foreigns, tr2.foreigns));

        } 
    }
}

