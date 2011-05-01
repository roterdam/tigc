package semant;

import symbol.*;
import error.ErrorMsg;
import absyn.*;

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

    private TypedExpr transExpr(absyn.Expr expr, boolean breakable) {
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
            return new TypedExpr(null, new type.Int());
    }

    private TypedExpr transExpr(ArrayExpr expr, boolean breakable) {
        type.Type t = tt.get(expr.type), ta = t.actual();
        if (t == null) {
            e.report("Undefined type: " + expr.type.toString() + "; int array assumed.", expr.pos);
            return new TypedExpr(null, new type.Array(new type.Int()));
        } else if (!(ta instanceof type.Array)) {
            e.report(t.toString() + " is not an array type; int array assumed.");
            return new TypedExpr(null, new type.Array(new type.Int()));
        } else {
            TypedExpr size = transExpr(expr.size, breakable);
            checkType(new type.Int(), size.type, expr.size.pos);
            TypedExpr init = transExpr(expr.init, breakable);
            checkType(((type.Array)ta).base, init.type, expr.init.pos);

            return new TypedExpr(null, t);
        }
    }

    private TypedExpr transExpr(AssignmentExpr expr, boolean breakable) {
        TypedExpr l = transLValue(expr.lvalue, breakable, true);
        TypedExpr r = transExpr(expr.e, breakable);
        checkType(l.type, r.type, expr.pos);
        return new TypedExpr(null, new type.Void());
    }

    private TypedExpr transExpr(BreakExpr expr, boolean breakable) {
        if (!breakable)
            e.report("Invalid break", expr.pos);
        return new TypedExpr(null, new type.Void());
    }

    private TypedExpr transExpr(CallExpr expr, boolean breakable) {
        Entry e = vt.get(expr.func);
        if (e == null) {
            this.e.report("Undefined function " + expr.func.toString() + "; assumed return VOID", expr.pos);
            return new TypedExpr(null, new type.Void());
        }
        if (e instanceof VarEntry) {
            this.e.report(expr.func.toString() + " is not a function; assumed return VOID", expr.pos);
            return new TypedExpr(null, new type.Void());
        }

        FuncEntry func = (FuncEntry)e;

        type.Record p = func.params;
        ExprList q = expr.args;

        while (p != null && !p.isEmpty() && q != null) {
            TypedExpr tq = transExpr(q.expr, breakable);
            checkType(p.type, tq.type, q.expr.pos);

            p = p.next;
            q = q.next;
        }

        if ((p != null && !p.isEmpty()) || q != null)
            this.e.report("Function param number mismatch", expr.pos);
        
        return new TypedExpr(null, func.result);
    }

    private TypedExpr transExpr(ForExpr expr, boolean breakable) {
        checkType(new type.Int(), transExpr(expr.begin, breakable).type, expr.begin.pos);
        checkType(new type.Int(), transExpr(expr.end, breakable).type, expr.end.pos);
        vt.beginScope();
        vt.put(expr.var, new VarEntry(new type.Int(), false));
        checkType(new type.Void(), transExpr(expr.body, true).type, expr.body.pos);
        vt.endScope();
        return new TypedExpr(null, new type.Void());
    }

    private TypedExpr transExpr(IfExpr expr, boolean breakable) {
        checkType(new type.Int(), transExpr(expr.condition, breakable).type, expr.condition.pos);
        if (expr.elseClause != null) {
            type.Type then_t = transExpr(expr.thenClause, breakable).type;
            type.Type else_t = transExpr(expr.elseClause, breakable).type;
            checkType(then_t, else_t, expr.thenClause.pos);
            return new TypedExpr(null, then_t);
        } else {
            type.Type then_t = transExpr(expr.thenClause, breakable).type;
            checkType(new type.Void(), then_t, expr.thenClause.pos);
            return new TypedExpr(null, new type.Void());
        }
    }

    private TypedExpr transExpr(IntExpr expr, boolean breakable) {
        return new TypedExpr(null, new type.Int());
    }

    private TypedExpr transExpr(LetExpr expr, boolean breakable) {
        vt.beginScope();
        tt.beginScope();
        transDeclList(expr.decls, breakable);
        TypedExpr te = transExprList(expr.exprs, breakable);
        vt.endScope();
        tt.endScope();
        return te;
    }

    private TypedExpr transExpr(LValueExpr expr, boolean breakable) {
        return transLValue(expr.lvalue, breakable, false);
    }

    private TypedExpr transExpr(NegationExpr expr, boolean breakable) {
        TypedExpr te = transExpr(expr.value, breakable);
        checkType(new type.Int(), te.type, expr.value.pos);
        return new TypedExpr(null, new type.Int());
    }

    private TypedExpr transExpr(NilExpr expr, boolean breakable) {
        return new TypedExpr(null, new type.Nil());
    }

    private TypedExpr transExpr(OpExpr expr, boolean breakable) {
        type.Type ltype = transExpr(expr.left, breakable).type, la = ltype.actual(),
            rtype = transExpr(expr.right, breakable).type, ra = rtype.actual();
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
        return new TypedExpr(null, new type.Int());
    }

    private TypedExpr transExpr(RecordExpr expr, boolean breakable) {
        type.Type type = tt.get(expr.type);
        if (type == null) {
            e.report(expr.type.toString() + " undefined; empty RECORD assumed", expr.pos);
            return new TypedExpr(null, new type.Record(null, null, null));
        } else if (!(type.actual() instanceof type.Record)) {
            e.report(type.toString() + " is not a record; empty RECORD assumed", expr.pos);
            return new TypedExpr(null, new type.Record(null, null, null));
        } else {
            type.Record p = (type.Record) type.actual();
            FieldList q = expr.fields;

            while ((p != null && !p.isEmpty()) && q != null) {
                if (p.field != q.name)
                    e.report("Field name mismatch: " + p.field.toString() + " expected but"
                           + q.name.toString() + " found", q.pos);
                checkType(p.type, transExpr(q.value, breakable).type, q.value.pos);

                p = p.next;
                q = q.next;
            }

            if ((p != null && !p.isEmpty()) || q != null)
                e.report("Field number mismatch", expr.fields.pos);

            return new TypedExpr(null, type);
        }
    }

    private TypedExpr transExpr(SeqExpr expr, boolean breakable) {
        return transExprList(expr.exprList, breakable);
    }

    private TypedExpr transExpr(StringExpr expr, boolean breakable) {
        return new TypedExpr(null, new type.String());
    }

    private TypedExpr transExpr(WhileExpr expr, boolean breakable) {
        checkType(new type.Int(), transExpr(expr.condition, breakable).type, expr.condition.pos);
        checkType(new type.Void(), transExpr(expr.body, true).type, expr.body.pos);
        return new TypedExpr(null, new type.Void());
    }

    private TypedExpr transExprList(ExprList expr, boolean breakable) {
        type.Type retType = new type.Void();
        while (expr != null) {
            retType = transExpr(expr.expr, breakable).type;
            expr = expr.next;
        }
        return new TypedExpr(null, retType);
    }

    private void transDeclList(DeclList expr, boolean breakable) {
        if (expr == null)
            return;

        if (expr.decl instanceof VarDecl) {

            VarDecl vd = (VarDecl) expr.decl;
            if (vd.type != null) {
                type.Type type = tt.get(vd.type);
                if (type == null)
                    e.report(vd.type.toString() + " undefined");
                else {
                    vt.put(vd.id, new VarEntry(type));
                    checkType(type, transExpr(vd.value, breakable).type, vd.value.pos);
                }
            } else {
                type.Type type = transExpr(vd.value, breakable).type;
                type.Type a = type.actual();
                if (a instanceof type.Nil || a instanceof type.Void) {
                    e.report("Invalid initialize type: " + type.toString()
                            + "; INT assumed");
                    type = new type.Int();
                }
                vt.put(vd.id, new VarEntry(type));
            }

            transDeclList(expr.next, breakable);

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

            transDeclList(p, breakable);

        } else if (expr.decl instanceof FuncDecl) {

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
                
                vt.beginScope();

                FuncEntry fe = (FuncEntry) vt.get(fd.name);
                for (type.Record i = fe.params; i != null && !i.isEmpty(); i = i.next)
                    vt.put(i.field, new VarEntry(i.type));
                TypedExpr te = transExpr(fd.body, false);
                checkType(fe.result, te.type, fd.body.pos);
                
                vt.endScope();
            }

            transDeclList(p, breakable);

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

    private TypedExpr transLValue(LValue lvalue, boolean breakable, boolean assignment) {
        if (lvalue instanceof VarLValue) {

            VarLValue vl = (VarLValue) lvalue;
            Entry entry = vt.get(vl.name);
            type.Type type = null;
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
            }
            return new TypedExpr(null, type);

        } else if (lvalue instanceof FieldLValue) {

            FieldLValue fl = (FieldLValue) lvalue;
            type.Type type = transLValue(fl.lvalue, breakable, assignment).type,
                ta = type.actual(), ret = null;
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
            return new TypedExpr(null, ret);

        } else /*if (lvalue instanceof SubscriptLValue)*/ {
            
            SubscriptLValue sl = (SubscriptLValue) lvalue;
            type.Type type = transLValue(sl.lvalue, breakable, assignment).type, 
                ta = type.actual(), ret = null;
            if (!(ta instanceof type.Array)) {
                e.report(type.toString() + " is not an ARRAY", sl.pos);
                ret = new type.Int();
            } else
                ret = ((type.Array) ta).base;
            checkType(new type.Int(), transExpr(sl.expr, breakable).type, sl.expr.pos);
            return new TypedExpr(null, ret);

        } 
    }
}

