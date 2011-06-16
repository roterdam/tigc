package semant;

import symbol.*;
import notifier.Notifier;
import absyn.*;
import java.util.*;
import intermediate.*;
import frame.*;
import optimization.InlineOptimizer;
import util.Graph;

public class Semant {
    private Table<Entry> vt;
    private Table<type.Type> tt;
    private Notifier notifier;

    private Stack<Label> breakStack;
    private Stack<Frame> currentFrame;
    private IR ir;

    private Map<Symbol, Symbol> symbolName = null;

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
                    new type.Void(), null, true));

        // function printi(i : int)
        vt.put(sym("printi"), new FuncEntry(
                    new type.Record(sym("i"), new type.Int(), null),
                    new type.Void(), null, true));

        // function flush()
        vt.put(sym("flush"), new FuncEntry(
                    null, new type.Void(), null, true));

        // function getchar() : string
        vt.put(sym("getchar"), new FuncEntry(
                    null, new type.String(), null, true));

        // function ord(s: string) : int
        vt.put(sym("ord"), new FuncEntry(
                    new type.Record(sym("s"), new type.String(), null),
                    new type.Int(), null, true));

        // function chr(i: int) : string
        vt.put(sym("chr"), new FuncEntry(
                    new type.Record(sym("i"), new type.Int(), null),
                    new type.String(), null, true));

        // function size(s: string) : int
        vt.put(sym("size"), new FuncEntry(
                    new type.Record(sym("s"), new type.String(), null),
                    new type.Int(), null, true));

        // function substring(s : string, first: int, n: int) : string
        vt.put(sym("substring"), new FuncEntry(
                    new type.Record(sym("s"), new type.String(),
                        new type.Record(sym("first"), new type.Int(),
                            new type.Record(sym("n"), new type.Int(), null)
                            )
                        ), new type.String(), null, true));
        
        // function concat(s1: string, s2: string) : string
        vt.put(sym("concat"), new FuncEntry(
                    new type.Record(sym("s1"), new type.String(),
                        new type.Record(sym("s2"), new type.String(), null)
                        ), new type.String(), null, true));

        // function not(i: int): int
        vt.put(sym("not"), new FuncEntry(
                    new type.Record(sym("i"), new type.Int(), null),
                    new type.Int(), null, true));

        // function exit(i: int)
        vt.put(sym("exit"), new FuncEntry(
                    new type.Record(sym("i"), new type.Int(), null),
                    new type.Void(), null, true));
    }

    public Semant(Notifier notifier) {
        this.notifier = notifier;

        breakStack = new Stack<Label>();
        currentFrame = new Stack<Frame>();
        tt = new Table<type.Type>();
        vt = new Table<Entry>();
        initTypes();
        initFunctions();
    }

    public IR translate(absyn.Expr expr) {
        InlineOptimizer opt = new InlineOptimizer();

        symbolName = new HashMap<Symbol, Symbol>();
        expr = opt.optimize(expr, symbolName);

        Frame globalFrame = new Frame(Label.newLabel("main"), null, true);
        ir = new IR(globalFrame);
        currentFrame.push(globalFrame);
        breakStack.push(null);
        IntermediateCodeList codes = transExpr(expr).codes;
        codes.add(new CallExternTAC(currentFrame.peek(), sym("exit"), new ConstAccess(0), null, null, null));
        breakStack.pop();
        currentFrame.pop();
        ir.codes = codes;
        return ir;
    }

    private void checkType(type.Type left, type.Type right, int pos) {
        if (!right.fits(left))
            notifier.error("Type mismatch, " + origName(left.toString())
                    + " needed, but " + origName(right.toString()) + " given", pos);
    }

    private String origName(String s) {
        Symbol r = symbolName.get(sym(s));
        if (r == null)
            return s.toString();
        else
            return r.toString();
    }

    private SimpleAccess convertToSimpleAccess(Access access, IntermediateCodeList codes) {
        if (access instanceof MemAccess) {
            Temp t = currentFrame.peek().addLocal();
            codes.add(new MoveTAC(currentFrame.peek(), access, t));
            return t;
        } else
            return (SimpleAccess)access;
    }

    private TranslateResult transExpr(absyn.Expr expr) {
        if (expr instanceof ArrayExpr)
            return transExpr((ArrayExpr) expr);
        else if (expr instanceof AssignmentExpr)
            return transExpr((AssignmentExpr) expr);
        else if (expr instanceof BreakExpr)
            return transExpr((BreakExpr) expr);
        else if (expr instanceof CallExpr)
            return transExpr((CallExpr) expr);
        else if (expr instanceof ForExpr)
            return transExpr((ForExpr) expr);
        else if (expr instanceof IfExpr)
            return transExpr((IfExpr) expr);
        else if (expr instanceof IntExpr)
            return transExpr((IntExpr) expr);
        else if (expr instanceof LetExpr)
            return transExpr((LetExpr) expr);
        else if (expr instanceof LValueExpr)
            return transExpr((LValueExpr) expr);
        else if (expr instanceof NegationExpr)
            return transExpr((NegationExpr) expr);
        else if (expr instanceof NilExpr)
            return transExpr((NilExpr) expr);
        else if (expr instanceof OpExpr)
            return transExpr((OpExpr) expr);
        else if (expr instanceof RecordExpr)
            return transExpr((RecordExpr) expr);
        else if (expr instanceof SeqExpr)
            return transExpr((SeqExpr) expr);
        else if (expr instanceof StringExpr)
            return transExpr((StringExpr) expr);
        else if (expr instanceof WhileExpr)
            return transExpr((WhileExpr) expr);
        else
            return new TranslateResult(new IntermediateCodeList(), new type.Int());
    }

    private TranslateResult transExpr(ArrayExpr expr) {
        type.Type t = tt.get(expr.type), ta = t.actual();

        if (t == null) {

            notifier.error("Undefined type: " + origName(expr.type.toString())
                    + "; int array assumed.", expr.pos);
            return new TranslateResult(new IntermediateCodeList(), new type.Array(new type.Int()));

        } else if (!(ta instanceof type.Array)) {

            notifier.error(origName(t.toString()) + " is not an array type; int array assumed.");
            return new TranslateResult(new IntermediateCodeList(), new type.Array(new type.Int()));

        } else {

            TranslateResult size = transExpr(expr.size);
            checkType(new type.Int(), size.type, expr.size.pos);
            TranslateResult init = transExpr(expr.init);
            checkType(((type.Array)ta).base, init.type, expr.init.pos);

            IntermediateCodeList codes = new IntermediateCodeList();
            codes.addAll(size.codes);
            codes.addAll(init.codes);
            Temp tres = currentFrame.peek().addLocal();
            if (!notifier.hasError()) {
                Temp tsize = currentFrame.peek().addLocal();
                codes.add(new BinOpTAC(currentFrame.peek(), BinOpTAC.BinOp.MUL, size.place, ir.wordLength, tsize));
                ir.funcTable.put(sym("malloc"));
                codes.add(new CallExternTAC(currentFrame.peek(), sym("malloc"), tsize, null, null, tres));

                Label l1 = Label.newLabel(), l2 = Label.newLabel();
                Temp ti = currentFrame.peek().addLocal(), temp = currentFrame.peek().addLocal();
                codes.add(new MoveTAC(currentFrame.peek(), tres, ti));
                codes.add(new BinOpTAC(currentFrame.peek(), BinOpTAC.BinOp.ADD, tres, tsize, temp));
                codes.add(new BranchTAC(currentFrame.peek(), BranchTAC.BranchType.GEQ, ti, temp, l2));

                codes.add(l1, new MoveTAC(currentFrame.peek(), init.place, new MemAccess(ti, new ConstAccess(0))));
                codes.add(new BinOpTAC(currentFrame.peek(), BinOpTAC.BinOp.ADD, ti, ir.wordLength, ti));
                codes.add(new BranchTAC(currentFrame.peek(), BranchTAC.BranchType.NEQ, ti, temp, l1));

                codes.add(l2);
            }

            return new TranslateResult(codes, t, tres);
        }
    }

    private TranslateResult transExpr(AssignmentExpr expr) {
        TranslateResult l = transLValue(expr.lvalue, true);
        TranslateResult r = transExpr(expr.e);
        checkType(l.type, r.type, expr.pos);
        
        IntermediateCodeList codes = new IntermediateCodeList();
        if (!notifier.hasError()) {
            codes.addAll(l.codes);
            codes.addAll(r.codes);
            codes.add(new MoveTAC(currentFrame.peek(), r.place, (AssignableAccess) l.place));
        }
        return new TranslateResult(codes, new type.Void());
    }

    private TranslateResult transExpr(BreakExpr expr) {
        if (breakStack.peek() == null)
            notifier.error("Invalid break", expr.pos);
        IntermediateCodeList codes = new IntermediateCodeList();
        codes.add(new GotoTAC(currentFrame.peek(), breakStack.peek()));
        return new TranslateResult(codes, new type.Void());
    }

    private TranslateResult transExpr(CallExpr expr) {
        Entry e = vt.get(expr.func);
        if (e == null) {
            notifier.error("Undefined function " + origName(expr.func.toString())
                    + "; assumed return VOID", expr.pos);
            return new TranslateResult(null, new type.Void());
        }
        if (e instanceof VarEntry) {
            notifier.error(origName(expr.func.toString()) +
                    " is not a function; assumed return VOID", expr.pos);
            return new TranslateResult(null, new type.Void());
        }

        FuncEntry func = (FuncEntry)e;

        type.Record p = func.params;
        ExprList q = expr.args;

        Temp ret = null;
        if (!(func.result.actual() instanceof type.Void))
            ret = currentFrame.peek().addLocal();

        IntermediateCodeList codes = new IntermediateCodeList(),
                             codesParam = new IntermediateCodeList();
        ArrayList<Access> actuals = new ArrayList<Access>();
        ThreeAddressCode call = func.isExtern ? null : new CallTAC(currentFrame.peek(), func.frame.place, ret);
        while (p != null && !p.isEmpty() && q != null) {
            TranslateResult tq = transExpr(q.expr);
            checkType(p.type, tq.type, q.expr.pos);

            actuals.add(tq.place);
            if (!notifier.hasError())
                codes.addAll(tq.codes);
            if (!func.isExtern && !notifier.hasError())
                ((CallTAC) call).addParam(tq.place);

            p = p.next;
            q = q.next;
        }

        if (!notifier.hasError()) {
            if (func.isExtern) {
                ir.funcTable.put(expr.func);
                switch (actuals.size()) {
                    case 0:
                        codes.add(new CallExternTAC(currentFrame.peek(), expr.func, null, null, null, ret));
                        break;

                    case 1:
                        codes.add(new CallExternTAC(currentFrame.peek(), expr.func, actuals.get(0), null, null, ret));
                        break;

                    case 2:
                        codes.add(new CallExternTAC(currentFrame.peek(), expr.func, actuals.get(0), actuals.get(1), null, ret));
                        break;

                    case 3:
                        codes.add(new CallExternTAC(currentFrame.peek(), expr.func, actuals.get(0), actuals.get(1), actuals.get(2), ret));
                        break;

                    default:
                        notifier.error("Too many params in extern call", q.pos);
                        break;
                }
            } else {
                codes.add(call);
                ir.callingGraph.addEdge(currentFrame.peek(), func.frame);
            }
        }

        if ((p != null && !p.isEmpty()) || q != null)
            notifier.error("Function param number mismatch", expr.pos);

        return new TranslateResult(codes, func.result, ret);
    }

    private TranslateResult transExpr(ForExpr expr) {
        TranslateResult br = transExpr(expr.begin),
                        er = transExpr(expr.end);
        checkType(new type.Int(), br.type, expr.begin.pos);
        checkType(new type.Int(), er.type, expr.end.pos);

        Label endLoop = Label.newLabel();
        Temp inductionVar = currentFrame.peek().addLocal();

        vt.beginScope();
        vt.put(expr.var, new VarEntry(new type.Int(), false, inductionVar));
        breakStack.push(endLoop);
        TranslateResult result = transExpr(expr.body);
        breakStack.pop();
        checkType(new type.Void(), result.type, expr.body.pos);
        vt.endScope();
        
        IntermediateCodeList codes = new IntermediateCodeList();
        if (!notifier.hasError()) {
            codes.addAll(br.codes);
            codes.addAll(er.codes);
            codes.add(new MoveTAC(currentFrame.peek(), br.place, inductionVar));
            codes.add(new BranchTAC(currentFrame.peek(), BranchTAC.BranchType.GT, inductionVar, er.place, endLoop));
            Temp temp = currentFrame.peek().addLocal();
            codes.add(new BinOpTAC(currentFrame.peek(), BinOpTAC.BinOp.ADD, er.place, new ConstAccess(1), temp));
            Label beginLoop = Label.newLabel();
            codes.add(beginLoop);
            codes.addAll(result.codes);
            codes.add(new BinOpTAC(currentFrame.peek(), BinOpTAC.BinOp.ADD, inductionVar, new ConstAccess(1), inductionVar));
            codes.add(new BranchTAC(currentFrame.peek(), BranchTAC.BranchType.NEQ, inductionVar, temp, beginLoop));
            codes.add(endLoop);
        }

        return new TranslateResult(codes, new type.Void());
    }

    private TranslateResult transExpr(IfExpr expr) {
        TranslateResult cr = transExpr(expr.condition);
        checkType(new type.Int(), cr.type, expr.condition.pos);
        if (expr.elseClause != null) {
            TranslateResult thenr = transExpr(expr.thenClause);
            TranslateResult elser = transExpr(expr.elseClause);
            checkType(thenr.type, elser.type, expr.thenClause.pos);

            IntermediateCodeList codes = new IntermediateCodeList();
            Label elseIf = Label.newLabel(), endIf = Label.newLabel();
            Temp place = null;
            if (!(thenr.type.actual() instanceof type.Void))
                place = currentFrame.peek().addLocal();
            if (!notifier.hasError()) {
                codes.addAll(cr.codes);
                codes.add(new BranchTAC(currentFrame.peek(), BranchTAC.BranchType.EQ, cr.place, new ConstAccess(0), elseIf));

                codes.addAll(thenr.codes);
                if (place != null)
                    codes.add(new MoveTAC(currentFrame.peek(), thenr.place, place));
                codes.add(new GotoTAC(currentFrame.peek(), endIf));

                codes.add(elseIf);
                codes.addAll(elser.codes);
                if (place != null)
                    codes.add(new MoveTAC(currentFrame.peek(), elser.place, place));

                codes.add(endIf);
            }

            return new TranslateResult(codes, thenr.type, place);

        } else {
            TranslateResult thenr = transExpr(expr.thenClause);
            checkType(new type.Void(), thenr.type, expr.thenClause.pos);

            IntermediateCodeList codes = new IntermediateCodeList();
            Label endIf = Label.newLabel();
            if (!notifier.hasError()) {
                codes.addAll(cr.codes);
                codes.add(new BranchTAC(currentFrame.peek(), BranchTAC.BranchType.EQ, cr.place, new ConstAccess(0), endIf));
                codes.addAll(thenr.codes);
                codes.add(endIf);
            }

            return new TranslateResult(codes, new type.Void());
        }
    }

    private TranslateResult transExpr(IntExpr expr) {
        return new TranslateResult(new IntermediateCodeList(), new type.Int(), new ConstAccess(expr.value));
    }

    private TranslateResult transExpr(LetExpr expr) {
        vt.beginScope();
        tt.beginScope();
        TranslateResult rd = transDeclList(expr.decls);
        TranslateResult re = transExprList(expr.exprs);
        vt.endScope();
        tt.endScope();

        IntermediateCodeList codes = new IntermediateCodeList();
        if (!notifier.hasError()) {
            codes.addAll(rd.codes);
            codes.addAll(re.codes);
        }
        return new TranslateResult(codes, re.type, re.place);
    }

    private TranslateResult transExpr(LValueExpr expr) {
        return transLValue(expr.lvalue, false);
    }

    private TranslateResult transExpr(NegationExpr expr) {
        TranslateResult te = transExpr(expr.value);
        checkType(new type.Int(), te.type, expr.value.pos);

        IntermediateCodeList codes = new IntermediateCodeList();
        Temp place = currentFrame.peek().addLocal();
        if (!notifier.hasError()) {
            codes.addAll(te.codes);
            codes.add(new UniOpTAC(currentFrame.peek(), UniOpTAC.UniOp.NEG, te.place, place));
        }

        return new TranslateResult(codes, new type.Int(), place);
    }

    private TranslateResult transExpr(NilExpr expr) {
        return new TranslateResult(new IntermediateCodeList(), new type.Nil(), new ConstAccess(0));
    }

    private TranslateResult transExpr(OpExpr expr) {
        TranslateResult lr = transExpr(expr.left),
                        rr = transExpr(expr.right);
        type.Type ltype = lr.type, la = ltype.actual(),
            rtype = rr.type, ra = rtype.actual();

        BinOpTAC.BinOp op = BinOpTAC.BinOp.ADD;
        switch (expr.op) {
            case ADD:
                op = BinOpTAC.BinOp.ADD;
                break;

            case SUB:
                op = BinOpTAC.BinOp.SUB;
                break;

            case MUL:
                op = BinOpTAC.BinOp.MUL;
                break;

            case DIV:
                op = BinOpTAC.BinOp.DIV;
                break;

            case EQ:
                op = BinOpTAC.BinOp.EQ;
                break;

            case NEQ:
                op = BinOpTAC.BinOp.NEQ;
                break;

            case LT:
                op = BinOpTAC.BinOp.LT;
                break;

            case LEQ:
                op = BinOpTAC.BinOp.LEQ;
                break;

            case GT:
                op = BinOpTAC.BinOp.GT;
                break;

            case GEQ:
                op = BinOpTAC.BinOp.GEQ;
                break;
        }


        IntermediateCodeList codes = new IntermediateCodeList();
        Temp place = currentFrame.peek().addLocal();

        if (la instanceof type.Int || ra instanceof type.Int) {
            checkType(new type.Int(), la, expr.left.pos);
            checkType(new type.Int(), ra, expr.right.pos);

            if (!notifier.hasError()) {
                if (expr.op == OpExpr.Op.AND) {
                    Label falseLabel = Label.newLabel(),
                          endLabel = Label.newLabel();

                    codes.addAll(lr.codes);
                    codes.add(new BranchTAC(currentFrame.peek(), BranchTAC.BranchType.EQ, lr.place, new ConstAccess(0), falseLabel));
                    codes.addAll(rr.codes);
                    codes.add(new BranchTAC(currentFrame.peek(), BranchTAC.BranchType.EQ, rr.place, new ConstAccess(0), falseLabel));
                    codes.add(new MoveTAC(currentFrame.peek(), new ConstAccess(1), place));
                    codes.add(new GotoTAC(currentFrame.peek(), endLabel));
                    codes.add(falseLabel, new MoveTAC(currentFrame.peek(), new ConstAccess(0), place));
                    codes.add(endLabel);

                } else if (expr.op == OpExpr.Op.OR) {
                    Label trueLabel = Label.newLabel(),
                          endLabel = Label.newLabel();

                    codes.addAll(lr.codes);
                    codes.add(new BranchTAC(currentFrame.peek(), BranchTAC.BranchType.NEQ, lr.place, new ConstAccess(0), trueLabel));
                    codes.addAll(rr.codes);
                    codes.add(new BranchTAC(currentFrame.peek(), BranchTAC.BranchType.NEQ, rr.place, new ConstAccess(0), trueLabel));
                    codes.add(new MoveTAC(currentFrame.peek(), new ConstAccess(0), place));
                    codes.add(new GotoTAC(currentFrame.peek(), endLabel));
                    codes.add(trueLabel, new MoveTAC(currentFrame.peek(), new ConstAccess(1), place));
                    codes.add(endLabel);

                } else {
                    codes.addAll(lr.codes);
                    codes.addAll(rr.codes);
                    codes.add(new BinOpTAC(currentFrame.peek(), op, lr.place, rr.place, place));
                }
            }

        } else if (la instanceof type.String || ra instanceof type.String) {
            if (expr.op == OpExpr.Op.EQ || expr.op == OpExpr.Op.NEQ
                    || expr.op == OpExpr.Op.LT || expr.op == OpExpr.Op.LEQ
                    || expr.op == OpExpr.Op.GT || expr.op == OpExpr.Op.GEQ) {
                checkType(new type.String(), la, expr.left.pos);
                checkType(new type.String(), ra, expr.right.pos);

                if (!notifier.hasError()) {
                    codes.addAll(lr.codes);
                    codes.addAll(rr.codes);
                    Temp t = currentFrame.peek().addLocal();
                    codes.add(new CallExternTAC(currentFrame.peek(), sym("strcmp"), lr.place, rr.place, null, t));
                    switch (expr.op) {
                        case EQ:
                            codes.add(new BinOpTAC(currentFrame.peek(), BinOpTAC.BinOp.EQ, t, new ConstAccess(0), place));
                            break;

                        case NEQ:
                            codes.add(new BinOpTAC(currentFrame.peek(), BinOpTAC.BinOp.NEQ, t, new ConstAccess(0), place));
                            break;

                        case LT:
                            codes.add(new BinOpTAC(currentFrame.peek(), BinOpTAC.BinOp.LT, t, new ConstAccess(0), place));
                            break;

                        case LEQ:
                            codes.add(new BinOpTAC(currentFrame.peek(), BinOpTAC.BinOp.LEQ, t, new ConstAccess(0), place));
                            break;

                        case GT:
                            codes.add(new BinOpTAC(currentFrame.peek(), BinOpTAC.BinOp.GT, t, new ConstAccess(0), place));
                            break;

                        case GEQ:
                            codes.add(new BinOpTAC(currentFrame.peek(), BinOpTAC.BinOp.GEQ, t, new ConstAccess(0), place));
                            break;
                    }
                }

            } else {
                notifier.error("Invalid comparation between strings", expr.pos);
            }
        } else if ((expr.op == OpExpr.Op.EQ || expr.op == OpExpr.Op.NEQ) &&

                (la instanceof type.Array || la instanceof type.Record
                 || ra instanceof type.Array || ra instanceof type.Record)) {
            if (!(ltype.fits(rtype) || rtype.fits(ltype)))
                notifier.error("Invalid comparation between " + origName(ltype.toString())
                        + " and " + origName(rtype.toString()), expr.pos);

            if (!notifier.hasError()) {
                codes.addAll(lr.codes);
                codes.addAll(rr.codes);
                codes.add(new BinOpTAC(currentFrame.peek(), op, lr.place, rr.place, place));
            }

        } else
            notifier.error("Invalid comparation between " + origName(ltype.toString())
                    + " and " + origName(rtype.toString()), expr.pos);
        return new TranslateResult(codes, new type.Int(), place);
    }

    private TranslateResult transExpr(RecordExpr expr) {
        type.Type type = tt.get(expr.type);
        if (type == null) {
            notifier.error(origName(expr.type.toString()) + " undefined; empty RECORD assumed", expr.pos);
            return new TranslateResult(new IntermediateCodeList(), new type.Record(null, null, null));
        } else if (!(type.actual() instanceof type.Record)) {
            notifier.error(origName(type.toString()) + " is not a record; empty RECORD assumed", expr.pos);
            return new TranslateResult(new IntermediateCodeList(), new type.Record(null, null, null));
        } else {
            type.Record p = (type.Record) type.actual();
            FieldList q = expr.fields;

            IntermediateCodeList codes = new IntermediateCodeList();
            Temp place = currentFrame.peek().addLocal();
            if (!notifier.hasError()) {
                Temp tsize = currentFrame.peek().addLocal();
                codes.add(new BinOpTAC(currentFrame.peek(), BinOpTAC.BinOp.MUL, new ConstAccess(p.length()), ir.wordLength, tsize));
                ir.funcTable.put(sym("malloc"));
                codes.add(new CallExternTAC(currentFrame.peek(), sym("malloc"), tsize, null, null, place));
            }

            int offset = 0;
            while ((p != null && !p.isEmpty()) && q != null) {
                if (p.field != q.name)
                    notifier.error("Field name mismatch: " + p.field.toString() + " expected but"
                           + q.name.toString() + " found", q.pos);
                TranslateResult qr = transExpr(q.value);
                checkType(p.type, qr.type, q.value.pos);

                if (!notifier.hasError()) {
                    codes.addAll(qr.codes);
                    codes.add(new MoveTAC(currentFrame.peek(), qr.place, new MemAccess(place, new ConstAccess(offset))));
                }

                p = p.next;
                q = q.next;
                offset += 4;
            }

            if ((p != null && !p.isEmpty()) || q != null)
                notifier.error("Field number mismatch", expr.fields.pos);

            return new TranslateResult(codes, type, place);
        }
    }

    private TranslateResult transExpr(SeqExpr expr) {
        return transExprList(expr.exprList);
    }

    private TranslateResult transExpr(StringExpr expr) {
        return new TranslateResult(new IntermediateCodeList(),
                new type.String(), ir.stringTable.get(expr.value));
    }

    private TranslateResult transExpr(WhileExpr expr) {
        Label beginWhile = Label.newLabel(),
              endWhile = Label.newLabel();

        TranslateResult cr = transExpr(expr.condition);
        breakStack.push(endWhile);
        TranslateResult br = transExpr(expr.body);
        breakStack.pop();
        checkType(new type.Int(), cr.type, expr.condition.pos);
        checkType(new type.Void(), br.type, expr.body.pos);

        IntermediateCodeList codes = new IntermediateCodeList();
        if (!notifier.hasError()) {
            codes.add(beginWhile);
            codes.addAll(cr.codes);
            codes.add(new BranchTAC(currentFrame.peek(), BranchTAC.BranchType.EQ,
                        cr.place, new ConstAccess(0), endWhile));
            codes.addAll(br.codes);
            codes.add(new GotoTAC(currentFrame.peek(), beginWhile));
            codes.add(endWhile);
        }

        return new TranslateResult(codes, new type.Void());
    }

    private TranslateResult transExprList(ExprList expr) {
        type.Type retType = new type.Void();
        IntermediateCodeList codes = new IntermediateCodeList();
        Access place = null;
        while (expr != null) {
            TranslateResult r = transExpr(expr.expr);
            retType = r.type;
            if (!notifier.hasError()) {
                codes.addAll(r.codes);
                place = r.place;
            }
            expr = expr.next;
        }
        return new TranslateResult(codes, retType, place);
    }

    private TranslateResult transDeclList(DeclList expr) {
        if (expr == null)
            return new TranslateResult(new IntermediateCodeList(), null);

        IntermediateCodeList codes = new IntermediateCodeList();
        if (expr.decl instanceof VarDecl) {

            VarDecl vd = (VarDecl) expr.decl;
            if (vd.type != null) {
                type.Type type = tt.get(vd.type);
                if (type == null)
                    notifier.error(origName(vd.type.toString()) + " undefined");
                else {
                    Temp t = currentFrame.peek().addLocal();

                    TranslateResult ir = transExpr(vd.value);
                    vt.put(vd.id, new VarEntry(type, t));
                    checkType(type, ir.type, vd.value.pos);
                    
                    if (!notifier.hasError()) {
                        codes.addAll(ir.codes);
                        codes.add(new MoveTAC(currentFrame.peek(), ir.place, t));
                    }
                }
            } else {
                Temp t = currentFrame.peek().addLocal();

                TranslateResult ir = transExpr(vd.value);
                type.Type type = ir.type;
                type.Type a = type.actual();
                if (a instanceof type.Nil || a instanceof type.Void) {
                    notifier.error("Invalid initialize type: " + origName(type.toString())
                            + "; INT assumed");
                    type = new type.Int();
                }
                vt.put(vd.id, new VarEntry(type, t));

                if (!notifier.hasError()) {
                    codes.addAll(ir.codes);
                    codes.add(new MoveTAC(currentFrame.peek(), ir.place, t));
                }
            }

            codes.addAll(transDeclList(expr.next).codes);
            return new TranslateResult(codes, null);

        } else if (expr.decl instanceof TypeDecl) {

            DeclList p = expr;
            HashSet<Symbol> set = new HashSet<Symbol>();
            for (p = expr; p != null && p.decl instanceof TypeDecl; p = p.next) {
                TypeDecl td = (TypeDecl) p.decl;

                if (set.add(td.name))
                    tt.put(td.name, new type.Name(td.name));
                else
                    notifier.error(origName(td.name.toString())
                            + " already defined in the same block", td.pos);
            }
            for (p = expr; p != null && p.decl instanceof TypeDecl; p = p.next) {
                TypeDecl td = (TypeDecl) p.decl;
                ((type.Name) tt.get(td.name)).bind(transType(td.type));
            }
            for (p = expr; p != null && p.decl instanceof TypeDecl; p = p.next) {
                TypeDecl td = (TypeDecl) p.decl;
                if (((type.Name) tt.get(td.name)).isLoop()) {
                    notifier.error("Type declaration loop found on " + origName(td.name.toString())
                            + "; INT assumed", td.pos);
                    ((type.Name) tt.get(td.name)).bind(new type.Int());
                }
            }

            return transDeclList(p);

        } else /*if (expr.decl instanceof FuncDecl)*/ {

            DeclList p = expr;
            HashSet<Symbol> set = new HashSet<Symbol>();
            for (p = expr; p != null && p.decl instanceof FuncDecl; p = p.next) {
                FuncDecl fd = (FuncDecl) p.decl;

                if (set.add(fd.name)) {
                    type.Type result = new type.Void();
                    if (fd.type != null)
                        result = tt.get(fd.type);
                    if (result == null) {
                        notifier.error(origName(fd.type.toString()) + " undefined; assumed INT", fd.pos);
                        result = new type.Int();
                    }

                    if (ir.displays.size() < currentFrame.size())
                        ir.displays.add(ir.globalFrame.addLocal());

                    Frame frame = new Frame(Label.newLabel(fd.name.toString()), ir.displays.get(currentFrame.size() - 1));
                    ir.funcFrames.add(frame);
                    Temp tResult = null;
                    if (!(result.actual() instanceof type.Void))
                        tResult = frame.addReturnValue();
                    
                    type.Record pp = transTypeFields(fd.params);
                    frame.returnValue = tResult;
                    FuncEntry entry = new FuncEntry(pp, result, frame, false);
                    while (pp != null && !pp.isEmpty()) {
                        frame.addParam();
                        pp = pp.next;
                    }
                    vt.put(fd.name, entry);
                }
                else
                    notifier.error(origName(fd.name.toString()) + " already defined in the same block", fd.pos);
            }
            for (p = expr; p != null && p.decl instanceof FuncDecl; p = p.next) {
                FuncDecl fd = (FuncDecl) p.decl;
                
                vt.beginScope(true);

                FuncEntry fe = (FuncEntry) vt.get(fd.name);
                type.Record pp = fe.params;
                for (Temp t: fe.frame.params) {
                    vt.put(pp.field, new VarEntry(pp.type, t));
                    pp = pp.next;
                }

                breakStack.push(null);
                currentFrame.push(fe.frame);

                TranslateResult te = transExpr(fd.body);

                checkType(fe.result, te.type, fd.body.pos);

                if (!notifier.hasError()) {
                    codes.add(fe.frame.place);

                    codes.addAll(te.codes);
                    if (!(fe.result.actual() instanceof type.Void)) {
                        MoveTAC rvAssign = new MoveTAC(currentFrame.peek(), te.place, fe.frame.returnValue);
                        codes.add(rvAssign);
                    }

                    codes.add(new ReturnTAC(currentFrame.peek()));
                }

                currentFrame.pop();
                breakStack.pop();

                vt.endScope();
            }

            Label skip = Label.newLabel();
            codes.addFirst(new GotoTAC(currentFrame.peek(), skip));
            codes.add(skip);
            codes.addAll(transDeclList(p).codes);
            return new TranslateResult(codes, null);

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
                    notifier.error("Undefined type " + origName(fields.head.type.toString())
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
                notifier.error("Undefined type " + origName(nt.name.toString())
                        + "; INT assumed", nt.pos);
                t = new type.Int();
            }
            return t;
        } else if (type instanceof ArrayType) {
            ArrayType at = (ArrayType) type;
            type.Type t = tt.get(at.base);
            if (t == null) {
                notifier.error("Undefined type " + origName(at.base.toString())
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

    private TranslateResult transLValue(LValue lvalue, boolean assignment) {
        if (lvalue instanceof VarLValue) {

            VarLValue vl = (VarLValue) lvalue;
            Entry entry = vt.get(vl.name);
            type.Type type = null;
            Access place = null;
            if (entry == null) {
                notifier.error("Undefined variable " + origName(vl.name.toString())
                        + "; type INT assumed", vl.pos);
                type = new type.Int();
            } else if (entry instanceof FuncEntry) {
                notifier.error(origName(vl.name.toString()) + " is a function, not a variable; type INT assumed", vl.pos);
                type = new type.Int();
            } else {
                type = ((VarEntry) entry).type;
                place = ((VarEntry) entry).place;
                if (assignment && !((VarEntry) entry).assignable)
                    notifier.error(origName(vl.name.toString()) + " cannot be assigned here", vl.pos);
            }
            return new TranslateResult(new IntermediateCodeList(), type, place);

        } else if (lvalue instanceof FieldLValue) {

            FieldLValue fl = (FieldLValue) lvalue;
            TranslateResult tr = transLValue(fl.lvalue, assignment);
            type.Type type = tr.type, ta = type.actual(), ret = null;

            Access place = null;
            IntermediateCodeList codes = new IntermediateCodeList();
            if (!notifier.hasError())
                codes.addAll(tr.codes);

            if (ta instanceof type.Record) {
                type.Record temp = (type.Record) ta;
                ret = temp.findField(fl.id);
                if (ret == null) {
                    notifier.error(origName(type.toString()) + " do not have field " + fl.id
                            + "; type INT assumed", fl.pos);
                    ret = new type.Int();
                } else {

                    if (!notifier.hasError()) {
                        int offset = temp.fieldIndex(fl.id);
                        Temp to = currentFrame.peek().addLocal();
                        codes.add(new BinOpTAC(currentFrame.peek(), BinOpTAC.BinOp.MUL, new ConstAccess(offset), ir.wordLength, to));
                        SimpleAccess sa = convertToSimpleAccess(tr.place, codes);
                        place = new MemAccess(sa, to);
                    }

                }
            } else {
                notifier.error(origName(type.toString()) + " is not a RECORD; type INT assumed", fl.pos);
                ret = new type.Int();
            }
            return new TranslateResult(codes, ret, place);

        } else /*if (lvalue instanceof SubscriptLValue)*/ {
            
            SubscriptLValue sl = (SubscriptLValue) lvalue;
            TranslateResult tr = transLValue(sl.lvalue, assignment);
            type.Type type = tr.type, ta = type.actual(), ret = null;

            Access place = null;
            IntermediateCodeList codes = new IntermediateCodeList();

            if (!(ta instanceof type.Array)) {
                notifier.error(origName(type.toString()) + " is not an ARRAY", sl.pos);
                ret = new type.Int();
            } else {
                ret = ((type.Array) ta).base;
            }
            TranslateResult tr2 = transExpr(sl.expr);
            checkType(new type.Int(), tr2.type, sl.expr.pos);

            if (!notifier.hasError()) {
                codes.addAll(tr.codes);
                codes.addAll(tr2.codes);
                SimpleAccess sa = convertToSimpleAccess(tr.place, codes);
                Temp to = currentFrame.peek().addLocal();
                codes.add(new BinOpTAC(currentFrame.peek(), BinOpTAC.BinOp.MUL, tr2.place, ir.wordLength, to));
                place = new MemAccess(sa, to);
            }

            return new TranslateResult(codes, ret, place);

        } 
    }
}

