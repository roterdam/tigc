package optimization;

import intermediate.*;
import frame.Frame;
import symbol.Symbol;
import symbol.Table;
import java.util.*;
import absyn.*;
import util.Graph;

public class InlineOptimizer {
    Map<Symbol, Symbol> nameMap = null;
    Table<Symbol> vt = null, tt = null;

    Graph<Symbol> callingGraph = null;
    Stack<Symbol> currentFunction = null;
    Map<Symbol, Expr> functions = null;
    Map<Symbol, TypeFields> params = null;

    public absyn.Expr optimize(absyn.Expr expr, Map<Symbol, Symbol> nameMap) {
        this.nameMap = nameMap;
        vt = new Table<Symbol>();
        tt = new Table<Symbol>();
        callingGraph = new Graph<Symbol>();
        currentFunction = new Stack<Symbol>();
        functions = new HashMap<Symbol, Expr>();
        params = new HashMap<Symbol, TypeFields>();

        currentFunction.push(Symbol.symbol("main"));
        preProcess(expr);
        Expr ret = process(expr);
        currentFunction.pop();

        return ret;
    }

    private void preProcess(absyn.Expr expr) {
        if (expr instanceof ArrayExpr)
            preProcess((ArrayExpr) expr);
        else if (expr instanceof AssignmentExpr)
            preProcess((AssignmentExpr) expr);
        else if (expr instanceof BreakExpr)
            preProcess((BreakExpr) expr);
        else if (expr instanceof CallExpr)
            preProcess((CallExpr) expr);
        else if (expr instanceof ForExpr)
            preProcess((ForExpr) expr);
        else if (expr instanceof IfExpr)
            preProcess((IfExpr) expr);
        else if (expr instanceof IntExpr)
            preProcess((IntExpr) expr);
        else if (expr instanceof LetExpr)
            preProcess((LetExpr) expr);
        else if (expr instanceof LValueExpr)
            preProcess((LValueExpr) expr);
        else if (expr instanceof NegationExpr)
            preProcess((NegationExpr) expr);
        else if (expr instanceof NilExpr)
            preProcess((NilExpr) expr);
        else if (expr instanceof OpExpr)
            preProcess((OpExpr) expr);
        else if (expr instanceof RecordExpr)
            preProcess((RecordExpr) expr);
        else if (expr instanceof SeqExpr)
            preProcess((SeqExpr) expr);
        else if (expr instanceof StringExpr)
            preProcess((StringExpr) expr);
        else if (expr instanceof WhileExpr)
            preProcess((WhileExpr) expr);
    }

    private absyn.Expr process(absyn.Expr expr) {
        if (expr instanceof ArrayExpr)
            return process((ArrayExpr) expr);
        else if (expr instanceof AssignmentExpr)
            return process((AssignmentExpr) expr);
        else if (expr instanceof BreakExpr)
            return process((BreakExpr) expr);
        else if (expr instanceof CallExpr)
            return process((CallExpr) expr);
        else if (expr instanceof ForExpr)
            return process((ForExpr) expr);
        else if (expr instanceof IfExpr)
            return process((IfExpr) expr);
        else if (expr instanceof IntExpr)
            return process((IntExpr) expr);
        else if (expr instanceof LetExpr)
            return process((LetExpr) expr);
        else if (expr instanceof LValueExpr)
            return process((LValueExpr) expr);
        else if (expr instanceof NegationExpr)
            return process((NegationExpr) expr);
        else if (expr instanceof NilExpr)
            return process((NilExpr) expr);
        else if (expr instanceof OpExpr)
            return process((OpExpr) expr);
        else if (expr instanceof RecordExpr)
            return process((RecordExpr) expr);
        else if (expr instanceof SeqExpr)
            return process((SeqExpr) expr);
        else if (expr instanceof StringExpr)
            return process((StringExpr) expr);
        else /* if (expr instanceof WhileExpr) */
            return process((WhileExpr) expr);
    }

    private void preProcess(absyn.ArrayExpr expr) {
        Symbol t = tt.get(expr.type);
        if (t != null)
            expr.type = t;
        preProcess(expr.size);
        preProcess(expr.init);
    }

    private absyn.Expr process(absyn.ArrayExpr expr) {
        return new ArrayExpr(expr.pos, expr.type,
                process(expr.size), process(expr.init));
    }

    private void preProcess(absyn.AssignmentExpr expr) {
        preProcessLValue(expr.lvalue);
        preProcess(expr.e);
    }

    private absyn.Expr process(absyn.AssignmentExpr expr) {
        return new AssignmentExpr(expr.pos,
                processLValue(expr.lvalue),
                process(expr.e));
    }

    private void preProcess(absyn.BreakExpr expr) {
        /* do nothing */
    }

    private absyn.Expr process(absyn.BreakExpr expr) {
        return new BreakExpr(expr.pos);
    }

    private void preProcess(absyn.CallExpr expr) {
        Symbol t = vt.get(expr.func);
        if (t != null) {
            expr.func = t;

            callingGraph.addEdge(currentFunction.peek(), t);
        }

        ExprList p = expr.args;
        while (p != null) {
            preProcess(p.expr);
            p = p.next;
        }
    }

    private absyn.Expr process(absyn.CallExpr expr) {
        if (vt.get(expr.func) == null ||
                callingGraph.isLoopEdge(currentFunction.peek(), expr.func)
                || !functions.containsKey(expr.func))
            return new CallExpr(expr.pos, expr.func,
                    processExprList(expr.args));
        else {
            Expr body = functions.get(expr.func);

            boolean fail = false;
            DeclList decls = null;

            ExprList p = expr.args;
            TypeFields tf = params.get(expr.func);
            while (tf != null) {
                if (p == null) {
                    fail = true;
                    break;
                }
                decls = new DeclList(expr.pos, new VarDecl(expr.pos, tf.head.name, tf.head.type, p.expr), decls);
                p = p.next;
                tf = tf.next;
            }
            if (p != null)
                fail = true;

            if (fail)
                return new CallExpr(expr.pos, expr.func,
                        processExprList(expr.args));
            else
                return process(new LetExpr(expr.pos, decls,
                    new ExprList(body.pos, body, null)));
        }
    }

    private void preProcess(absyn.ForExpr expr) {
        preProcess(expr.begin);
        preProcess(expr.end);

        vt.beginScope();
        vt.put(expr.var, newSymbol());
        nameMap.put(vt.get(expr.var), expr.var);
        expr.var = vt.get(expr.var);
        preProcess(expr.body);
        vt.endScope();
    }

    private absyn.Expr process(absyn.ForExpr expr) {
        return new ForExpr(expr.pos, expr.var,
                process(expr.begin), process(expr.end),
                process(expr.body));
    }

    private void preProcess(absyn.IfExpr expr) {
        preProcess(expr.condition);
        preProcess(expr.thenClause);
        if (expr.elseClause != null)
            preProcess(expr.elseClause);
    }

    private absyn.Expr process(absyn.IfExpr expr) {
        return new IfExpr(expr.pos, process(expr.condition),
                process(expr.thenClause),
                expr.elseClause == null ? null : process(expr.elseClause));
    }

    private void preProcess(absyn.IntExpr expr) {
        /* do nothing */
    }

    private absyn.Expr process(absyn.IntExpr expr) {
        return new IntExpr(expr.pos, expr.value);
    }

    private void preProcess(absyn.LetExpr expr) {
        vt.beginScope();
        tt.beginScope();
        preProcessDeclList(expr.decls);
        preProcessExprList(expr.exprs);
        tt.endScope();
        vt.endScope();
    }

    private absyn.Expr process(absyn.LetExpr expr) {
        vt.beginScope();
        DeclList decls = processDeclList(expr.decls);
        ExprList exprs = processExprList(expr.exprs);
        vt.endScope();
        return new LetExpr(expr.pos, decls, exprs);
    }

    private void preProcess(absyn.LValueExpr expr) {
        preProcessLValue(expr.lvalue);
    }

    private absyn.Expr process(absyn.LValueExpr expr) {
        return new LValueExpr(expr.pos, processLValue(expr.lvalue));
    }

    private void preProcess(absyn.NegationExpr expr) {
        preProcess(expr.value);
    }

    private absyn.Expr process(absyn.NegationExpr expr) {
        return new NegationExpr(expr.pos, process(expr.value));
    }

    private void preProcess(absyn.NilExpr expr) {
        /* do nothing */
    }

    private absyn.Expr process(absyn.NilExpr expr) {
        return new NilExpr(expr.pos);
    }

    private void preProcess(absyn.OpExpr expr) {
        preProcess(expr.left);
        preProcess(expr.right);
    }

    private absyn.Expr process(absyn.OpExpr expr) {
        return new OpExpr(expr.pos, expr.op,
                process(expr.left),
                process(expr.right));
    }

    private void preProcess(absyn.RecordExpr expr) {
        Symbol t = tt.get(expr.type);
        if (t != null)
            expr.type = t;

        FieldList p = expr.fields;
        while (p != null) {
            preProcess(p.value);
            p = p.next;
        }
    }

    private FieldList processFieldList(absyn.FieldList fl) {
        if (fl == null)
            return null;
        return new FieldList(fl.pos, fl.name, process(fl.value), processFieldList(fl.next));
    }

    private absyn.Expr process(absyn.RecordExpr expr) {
        return new RecordExpr(expr.pos, expr.type,
                processFieldList(expr.fields));
    }

    private void preProcess(absyn.SeqExpr expr) {
        preProcessExprList(expr.exprList);
    }

    private absyn.Expr process(absyn.SeqExpr expr) {
        return new SeqExpr(expr.pos, processExprList(expr.exprList));
    }

    private void preProcess(absyn.StringExpr expr) {
        /* do nothing */
    }

    private absyn.Expr process(absyn.StringExpr expr) {
        return new StringExpr(expr.pos, expr.value);
    }

    private void preProcess(absyn.WhileExpr expr) {
        preProcess(expr.condition);
        preProcess(expr.body);
    }

    private absyn.Expr process(absyn.WhileExpr expr) {
        return new WhileExpr(expr.pos,
                process(expr.condition),
                process(expr.body));
    }

    private void preProcessExprList(absyn.ExprList expr) {
        while (expr != null) {
            preProcess(expr.expr);
            expr = expr.next;
        }
    }

    private ExprList processExprList(ExprList expr) {
        if (expr == null)
            return null;
        return new ExprList(expr.pos, process(expr.expr),
                processExprList(expr.next));
    }

    private void preProcessDeclList(absyn.DeclList expr) {
        if (expr == null)
            return;

        if (expr.decl instanceof VarDecl) {
            VarDecl vd = (VarDecl) expr.decl;
            
            if (vd.type != null) {
                Symbol t = tt.get(vd.type);
                if (t != null)
                    vd.type = t;
            }

            preProcess(vd.value);
            vt.put(vd.id, newSymbol());
            nameMap.put(vt.get(vd.id), vd.id);
            vd.id = vt.get(vd.id);

            preProcessDeclList(expr.next);
        } else if (expr.decl instanceof TypeDecl) {
            DeclList p = expr;
            
            for (p = expr; p != null && p.decl instanceof TypeDecl; p = p.next) {
                TypeDecl td = (TypeDecl) p.decl;
                tt.put(td.name, newSymbol());
                nameMap.put(tt.get(td.name), td.name);
                td.name = tt.get(td.name);
            }
            for (p = expr; p != null && p.decl instanceof TypeDecl; p = p.next) {
                TypeDecl td = (TypeDecl) p.decl;
                preProcessType(td.type);
            }

            preProcessDeclList(p);
        } else {
            DeclList p = expr;

            for (p = expr; p != null && p.decl instanceof FuncDecl; p = p.next) {
                FuncDecl fd = (FuncDecl) p.decl;
                vt.put(fd.name, newSymbol());
                nameMap.put(vt.get(fd.name), fd.name);
                fd.name = vt.get(fd.name);

                functions.put(fd.name, fd.body);

                if (fd.type != null) {
                    Symbol t = tt.get(fd.type);
                    if (t != null)
                        fd.type = t;
                }

                preProcessTypeFields(fd.params);
            }
            for (p = expr; p != null && p.decl instanceof FuncDecl; p = p.next) {
                FuncDecl fd = (FuncDecl) p.decl;

                vt.beginScope();
                TypeFields tf = fd.params;
                while (tf != null) {
                    vt.put(tf.head.name, newSymbol());
                    nameMap.put(vt.get(tf.head.name), tf.head.name);
                    tf.head.name = vt.get(tf.head.name);

                    tf = tf.next;
                }
                params.put(fd.name, tf);

                currentFunction.push(fd.name);
                preProcess(fd.body);
                currentFunction.pop();
                vt.endScope();
            }

            preProcessDeclList(p);
        } 
    }

    private DeclList processDeclList(DeclList decls) {
        if (decls == null)
            return null;

        if (decls.decl instanceof VarDecl) {
            VarDecl vd = (VarDecl) decls.decl;

            return new DeclList(decls.pos,
                    new VarDecl(vd.pos, vd.id, vd.type == null ? null : vd.type,
                    process(vd.value)),
                    processDeclList(decls.next));
        } else if (decls.decl instanceof TypeDecl) {
            return new DeclList(decls.pos,
                    decls.decl,
                    processDeclList(decls.next));
        } else {
            DeclList p = decls;
            DeclList ret = null, tail = null;

            for (p = decls; p != null && p.decl instanceof FuncDecl; p = p.next) {
                FuncDecl fd = (FuncDecl) p.decl;
                vt.put(fd.name, fd.name);
            }
            for (p = decls; p != null && p.decl instanceof FuncDecl; p = p.next) {
                FuncDecl fd = (FuncDecl) p.decl;
                currentFunction.push(fd.name);
                DeclList d = new DeclList(decls.pos,
                        new FuncDecl(fd.pos, fd.name, fd.params,
                        fd.type, process(fd.body)), null);
                currentFunction.pop();

                if (ret == null) {
                    ret = d;
                    tail = d;
                } else {
                    tail.next = d;
                    tail = tail.next;
                }
            }

            tail.next = processDeclList(p);

            return ret;
        }
    }

    private void preProcessTypeFields(TypeFields tf) {
        while (tf != null) {
            Symbol t = tt.get(tf.head.type);
            if (t != null)
                tf.head.type = t;

            tf = tf.next;
        }
    }

    private void preProcessType(Type type) {
        if (type instanceof NameType) {
            NameType nt = (NameType) type;
            Symbol t = tt.get(nt.name);
            if (t != null)
                nt.name = t;
        } else if (type instanceof ArrayType) {
            ArrayType at = (ArrayType) type;
            Symbol t = tt.get(at.base);
            if (t != null)
                at.base = t;
        } else {
            RecordType rt = (RecordType) type;
            preProcessTypeFields(rt.fields);
        }
    }

    private void preProcessLValue(LValue lvalue) {
        if (lvalue instanceof VarLValue) {
            VarLValue vl = (VarLValue) lvalue;
            Symbol t = vt.get(vl.name);
            if (t != null)
                vl.name = t;
        } else if (lvalue instanceof FieldLValue) {
            FieldLValue fl = (FieldLValue) lvalue;
            preProcessLValue(fl.lvalue);
        } else {
            SubscriptLValue sl = (SubscriptLValue) lvalue;
            preProcessLValue(sl.lvalue);
            preProcess(sl.expr);
        }
    }

    private absyn.LValue processLValue(LValue lvalue) {
        if (lvalue instanceof VarLValue) {
            return lvalue;
        } else if (lvalue instanceof FieldLValue) {
            return lvalue;
        } else {
            SubscriptLValue sl = (SubscriptLValue) lvalue;
            return new SubscriptLValue(sl.pos, processLValue(sl.lvalue),
                    process(sl.expr));
        }
    }

    int newNameCount = 0;
    Symbol newSymbol() {
        return Symbol.symbol("_s" + new Integer(newNameCount++).toString());
    }
}

