JC = javac -d bin/ -cp bin/ -Xlint
JC2 = javac -d bin/ -cp bin/



.PHONY: all clean absyn intermediate debug



all: bin/Main.class

debug: all bin/tester/SymbolTableTester.class bin/tester/ParserTester.class bin/tester/SemantTester.class bin/tester/Mid.class bin/tester/SimpleLinkedListTester.class bin/tester/GraphTester.class bin/tester/BasicBlockOptimizerTester.class

bin/Main.class: src/Main.java bin/parser/Parser.class bin/scanner/Scanner.class absyn bin/absyn/Printer.class bin/semant/Semant.class bin/notifier/Notifier.class intermediate bin/mips32/CodeGen.class bin/mips32/Optimizer.class
	$(JC) src/Main.java

bin/tester/SymbolTableTester.class: src/tester/SymbolTableTester.java bin/symbol/Table.class bin/notifier/Notifier.class
	$(JC) src/tester/SymbolTableTester.java

bin/tester/ParserTester.class: src/tester/ParserTester.java bin/parser/Parser.class absyn bin/notifier/Notifier.class
	$(JC) src/tester/ParserTester.java

bin/tester/SemantTester.class: src/tester/SemantTester.java bin/parser/Parser.class absyn bin/semant/Semant.class
	$(JC) src/tester/SemantTester.java

bin/tester/Mid.class: src/tester/Mid.java bin/parser/Parser.class absyn bin/semant/Semant.class
	$(JC) src/tester/Mid.java

bin/tester/SimpleLinkedListTester.class: bin/util/SimpleLinkedList.class src/tester/SimpleLinkedListTester.java
	$(JC) src/tester/SimpleLinkedListTester.java

bin/tester/GraphTester.class: bin/util/Graph.class bin/util/GraphNode.class src/tester/GraphTester.java
	$(JC) src/tester/GraphTester.java

bin/tester/BasicBlockOptimizerTester.class: bin/optimization/BasicBlockOptimizer.class bin/flow/LifeAnalysis.class bin/mips32/Instruction.class bin/mips32/InstructionGenerator.class bin/frame/Frame.class src/tester/BasicBlockOptimizerTester.java
	$(JC) src/tester/BasicBlockOptimizerTester.java


bin/symbol/Symbol.class: src/symbol/Symbol.java
	$(JC) src/symbol/Symbol.java

bin/symbol/Table.class: src/symbol/Table.java bin/symbol/Symbol.class
	$(JC) src/symbol/Table.java

absyn: bin/absyn/Absyn.class bin/absyn/Expr.class bin/absyn/StringExpr.class bin/absyn/IntExpr.class bin/absyn/NilExpr.class bin/absyn/LValueExpr.class bin/absyn/NegationExpr.class bin/absyn/OpExpr.class bin/absyn/AssignmentExpr.class bin/absyn/CallExpr.class bin/absyn/SeqExpr.class bin/absyn/RecordExpr.class bin/absyn/ArrayExpr.class bin/absyn/IfExpr.class bin/absyn/WhileExpr.class bin/absyn/ForExpr.class bin/absyn/BreakExpr.class bin/absyn/LetExpr.class bin/absyn/TypeDecl.class bin/absyn/NameType.class bin/absyn/RecordType.class bin/absyn/ArrayType.class bin/absyn/VarDecl.class bin/absyn/FuncDecl.class bin/absyn/VarLValue.class bin/absyn/FieldLValue.class bin/absyn/SubscriptLValue.class

bin/absyn/ExprList.class: src/absyn/ExprList.java bin/absyn/Expr.class
	$(JC) src/absyn/ExprList.java

bin/absyn/FieldList.class: src/absyn/FieldList.java
	$(JC) src/absyn/FieldList.java

bin/absyn/DeclList.class: src/absyn/DeclList.java bin/absyn/Decl.class
	$(JC) src/absyn/DeclList.java

bin/absyn/Decl.class: src/absyn/Decl.java
	$(JC) src/absyn/Decl.java

bin/absyn/TypeDecl.class: src/absyn/TypeDecl.java bin/symbol/Symbol.class bin/absyn/Type.class
	$(JC) src/absyn/TypeDecl.java

bin/absyn/VarDecl.class: src/absyn/VarDecl.java bin/symbol/Symbol.class
	$(JC) src/absyn/VarDecl.java

bin/absyn/FuncDecl.class: src/absyn/FuncDecl.java bin/symbol/Symbol.class
	$(JC) src/absyn/FuncDecl.java

bin/absyn/Type.class: src/absyn/Type.java
	$(JC) src/absyn/Type.java

bin/absyn/NameType.class: src/absyn/NameType.java bin/absyn/Type.class bin/symbol/Symbol.class
	$(JC) src/absyn/NameType.java

bin/absyn/RecordType.class: src/absyn/RecordType.java bin/absyn/Type.class bin/absyn/TypeFields.class
	$(JC) src/absyn/RecordType.java

bin/absyn/ArrayType.class: src/absyn/ArrayType.java bin/absyn/Type.class bin/symbol/Symbol.class
	$(JC) src/absyn/ArrayType.java

bin/absyn/TypeFields.class: src/absyn/TypeFields.java bin/absyn/TypeField.class
	$(JC) src/absyn/TypeFields.java

bin/absyn/TypeField.class: src/absyn/TypeField.java bin/symbol/Symbol.class
	$(JC) src/absyn/TypeField.java

bin/absyn/LValue.class: src/absyn/LValue.java
	$(JC) src/absyn/LValue.java

bin/absyn/VarLValue.class: src/absyn/VarLValue.java bin/absyn/LValue.class bin/symbol/Symbol.class
	$(JC) src/absyn/VarLValue.java

bin/absyn/FieldLValue.class: src/absyn/FieldLValue.java bin/absyn/LValue.class bin/symbol/Symbol.class
	$(JC) src/absyn/FieldLValue.java

bin/absyn/SubscriptLValue.class: src/absyn/SubscriptLValue.java bin/absyn/LValue.class
	$(JC) src/absyn/SubscriptLValue.java

bin/absyn/Absyn.class: src/absyn/Absyn.java
	$(JC) src/absyn/Absyn.java

bin/absyn/Expr.class: src/absyn/Expr.java bin/absyn/Absyn.class
	$(JC) src/absyn/Expr.java

bin/absyn/StringExpr.class: src/absyn/StringExpr.java bin/absyn/Expr.class
	$(JC) src/absyn/StringExpr.java

bin/absyn/IntExpr.class: src/absyn/IntExpr.java bin/absyn/Expr.class
	$(JC) src/absyn/IntExpr.java

bin/absyn/NilExpr.class: src/absyn/NilExpr.java bin/absyn/Expr.class
	$(JC) src/absyn/NilExpr.java

bin/absyn/LValueExpr.class: src/absyn/LValueExpr.java bin/absyn/Expr.class bin/absyn/LValue.class
	$(JC) src/absyn/LValueExpr.java

bin/absyn/NegationExpr.class: src/absyn/NegationExpr.java bin/absyn/Expr.class
	$(JC) src/absyn/NegationExpr.java

bin/absyn/OpExpr.class: src/absyn/OpExpr.java bin/absyn/Expr.class
	$(JC) src/absyn/OpExpr.java

bin/absyn/AssignmentExpr.class: src/absyn/AssignmentExpr.java bin/absyn/Expr.class bin/absyn/LValue.class
	$(JC) src/absyn/AssignmentExpr.java

bin/absyn/CallExpr.class: src/absyn/CallExpr.java bin/absyn/Expr.class bin/absyn/ExprList.class bin/symbol/Symbol.class
	$(JC) src/absyn/CallExpr.java

bin/absyn/SeqExpr.class: src/absyn/SeqExpr.java bin/absyn/Expr.class bin/absyn/ExprList.class
	$(JC) src/absyn/SeqExpr.java

bin/absyn/RecordExpr.class: src/absyn/RecordExpr.java bin/absyn/Expr.class bin/absyn/FieldList.class bin/symbol/Symbol.class
	$(JC) src/absyn/RecordExpr.java

bin/absyn/ArrayExpr.class: src/absyn/ArrayExpr.java bin/absyn/Expr.class bin/symbol/Symbol.class
	$(JC) src/absyn/ArrayExpr.java

bin/absyn/IfExpr.class: src/absyn/IfExpr.java bin/absyn/Expr.class
	$(JC) src/absyn/IfExpr.java

bin/absyn/WhileExpr.class: src/absyn/WhileExpr.java bin/absyn/Expr.class
	$(JC) src/absyn/WhileExpr.java

bin/absyn/ForExpr.class: src/absyn/ForExpr.java bin/absyn/Expr.class bin/symbol/Symbol.class
	$(JC) src/absyn/ForExpr.java

bin/absyn/BreakExpr.class: src/absyn/BreakExpr.java bin/absyn/Expr.class
	$(JC) src/absyn/BreakExpr.java

bin/absyn/LetExpr.class: src/absyn/LetExpr.java bin/absyn/Expr.class bin/absyn/DeclList.class bin/absyn/ExprList.class
	$(JC) src/absyn/LetExpr.java

bin/absyn/Printer.class: absyn src/absyn/Printer.java
	$(JC) src/absyn/Printer.java


intermediate: bin/intermediate/ThreeAddressCode.class bin/intermediate/Label.class bin/intermediate/Access.class bin/intermediate/AssignableAccess.class bin/intermediate/SimpleAccess.class bin/intermediate/ConstAccess.class bin/intermediate/UnknownConstAccess.class bin/intermediate/MemAccess.class bin/intermediate/Temp.class bin/intermediate/MoveTAC.class bin/intermediate/OpTAC.class bin/intermediate/BinOpTAC.class bin/intermediate/UniOpTAC.class bin/intermediate/CallTAC.class bin/intermediate/CallExternTAC.class bin/intermediate/ReturnTAC.class bin/intermediate/GotoTAC.class bin/intermediate/BranchTAC.class bin/intermediate/IntermediateCodeList.class bin/intermediate/IR.class

bin/intermediate/ThreeAddressCode.class: bin/frame/Frame.class bin/intermediate/Access.class src/intermediate/ThreeAddressCode.java
	$(JC) src/intermediate/ThreeAddressCode.java

bin/intermediate/Label.class: src/intermediate/Label.java
	$(JC) src/intermediate/Label.java

bin/intermediate/Access.class: src/intermediate/Access.java
	$(JC) src/intermediate/Access.java

bin/intermediate/AssignableAccess.class: bin/intermediate/Access.class src/intermediate/AssignableAccess.java
	$(JC) src/intermediate/AssignableAccess.java

bin/intermediate/SimpleAccess.class: bin/intermediate/Access.class src/intermediate/SimpleAccess.java
	$(JC) src/intermediate/SimpleAccess.java

bin/intermediate/Temp.class: bin/arch/Const.class bin/intermediate/AssignableAccess.class bin/intermediate/SimpleAccess.class src/intermediate/Temp.java src/frame/Frame.java
	$(JC) src/intermediate/Temp.java src/frame/Frame.java

bin/intermediate/ConstAccess.class: bin/intermediate/SimpleAccess.class src/intermediate/ConstAccess.java
	$(JC) src/intermediate/ConstAccess.java

bin/intermediate/UnknownConstAccess.class: bin/intermediate/ConstAccess.class src/intermediate/UnknownConstAccess.java
	$(JC) src/intermediate/UnknownConstAccess.java

bin/intermediate/MemAccess.class: bin/intermediate/AssignableAccess.class src/intermediate/MemAccess.java
	$(JC) src/intermediate/MemAccess.java

bin/intermediate/MoveTAC.class: bin/frame/Frame.class bin/intermediate/ThreeAddressCode.class src/intermediate/MoveTAC.java
	$(JC) src/intermediate/MoveTAC.java

bin/intermediate/OpTAC.class: bin/frame/Frame.class bin/intermediate/ThreeAddressCode.class src/intermediate/OpTAC.java
	$(JC) src/intermediate/OpTAC.java

bin/intermediate/BinOpTAC.class: bin/frame/Frame.class bin/intermediate/OpTAC.class src/intermediate/BinOpTAC.java
	$(JC) src/intermediate/BinOpTAC.java

bin/intermediate/UniOpTAC.class: bin/frame/Frame.class bin/intermediate/OpTAC.class src/intermediate/UniOpTAC.java
	$(JC) src/intermediate/UniOpTAC.java

bin/intermediate/CallTAC.class: bin/frame/Frame.class bin/intermediate/Label.class bin/intermediate/ThreeAddressCode.class src/intermediate/CallTAC.java
	$(JC) src/intermediate/CallTAC.java

bin/intermediate/CallExternTAC.class: bin/frame/Frame.class bin/intermediate/ThreeAddressCode.class src/intermediate/CallExternTAC.java
	$(JC) src/intermediate/CallExternTAC.java

bin/intermediate/ReturnTAC.class: bin/frame/Frame.class bin/intermediate/ThreeAddressCode.class src/intermediate/ReturnTAC.java
	$(JC) src/intermediate/ReturnTAC.java

bin/intermediate/GotoTAC.class: bin/frame/Frame.class bin/intermediate/ThreeAddressCode.class src/intermediate/GotoTAC.java
	$(JC) src/intermediate/GotoTAC.java

bin/intermediate/BranchTAC.class: bin/frame/Frame.class bin/intermediate/ThreeAddressCode.class src/intermediate/BranchTAC.java
	$(JC) src/intermediate/BranchTAC.java

bin/intermediate/IntermediateCode.class: bin/intermediate/ThreeAddressCode.class src/intermediate/IntermediateCode.java
	$(JC) src/intermediate/IntermediateCode.java

bin/intermediate/IntermediateCodeList.class: bin/intermediate/Label.class bin/intermediate/IntermediateCode.class bin/util/SimpleLinkedList.class src/intermediate/IntermediateCodeList.java
	$(JC) src/intermediate/IntermediateCodeList.java

bin/intermediate/IR.class: bin/util/Graph.class bin/intermediate/UnknownConstAccess.class bin/intermediate/IntermediateCodeList.class bin/arch/StringTable.class bin/arch/ExternFunctionTable.class bin/frame/Frame.class src/intermediate/IR.java
	$(JC) src/intermediate/IR.java


bin/arch/StringTable.class: bin/intermediate/UnknownConstAccess.class src/arch/StringTable.java
	$(JC) src/arch/StringTable.java

bin/arch/ExternFunctionTable.class: bin/symbol/Symbol.class src/arch/ExternFunctionTable.java
	$(JC) src/arch/ExternFunctionTable.java

bin/arch/Const.class: src/arch/Const.java
	$(JC) src/arch/Const.java

bin/arch/Instruction.class: bin/frame/Frame.class bin/intermediate/Temp.class src/arch/Instruction.java
	$(JC) src/arch/Instruction.java

bin/arch/InstructionGenerator.class: bin/frame/Frame.class bin/arch/Instruction.class src/arch/InstructionGenerator.java
	$(JC) src/arch/InstructionGenerator.java


bin/parser/sym.class: src/parser/sym.java
	$(JC) src/parser/sym.java

bin/scanner/Scanner.class: src/scanner/Scanner.java bin/parser/sym.class
	$(JC2) src/scanner/Scanner.java

bin/notifier/Notifier.class: src/notifier/Notifier.java
	$(JC) src/notifier/Notifier.java

bin/parser/Parser.class: src/parser/Parser.java bin/parser/sym.class bin/notifier/Notifier.class bin/scanner/Scanner.class absyn
	$(JC2) src/parser/Parser.java

src/scanner/Scanner.java: doc/scanner.jflex
	jflex -d src/scanner/ doc/scanner.jflex

src/parser/Parser.java: doc/parser.cup
	cd src/parser; java -jar ../../lib/java-cup-11a.jar -parser Parser ../../doc/parser.cup

src/parser/sym.java: doc/parser.cup
	cd src/parser; java -jar ../../lib/java-cup-11a.jar -parser Parser ../../doc/parser.cup


bin/frame/Frame.class: bin/arch/Const.class bin/intermediate/Label.class bin/intermediate/Temp.class src/frame/Frame.java
	$(JC) src/frame/Frame.java


bin/semant/Semant.class: src/semant/Semant.java bin/optimization/InlineOptimizer.class bin/type/Type.class bin/type/Int.class bin/type/String.class bin/type/Record.class bin/type/EmptyRecord.class bin/type/Array.class bin/type/Name.class bin/type/Nil.class bin/type/Void.class bin/symbol/Table.class bin/notifier/Notifier.class bin/semant/Entry.class bin/semant/TranslateResult.class absyn intermediate bin/frame/Frame.class bin/util/Graph.class
	$(JC) src/semant/Semant.java

bin/semant/TranslateResult.class: src/semant/TranslateResult.java bin/intermediate/IntermediateCodeList.class
	$(JC) src/semant/TranslateResult.java

bin/semant/Entry.class: bin/intermediate/Label.class bin/intermediate/Temp.class src/semant/Entry.java bin/frame/Frame.class
	$(JC) src/semant/Entry.java

bin/type/Type.class: src/type/Type.java
	$(JC) src/type/Type.java

bin/type/Int.class: src/type/Int.java bin/type/Type.class
	$(JC) src/type/Int.java

bin/type/String.class: src/type/String.java bin/type/Type.class
	$(JC) src/type/String.java

bin/type/Record.class: src/type/Record.java bin/type/Type.class bin/symbol/Symbol.class
	$(JC) src/type/Record.java

bin/type/EmptyRecord.class: src/type/EmptyRecord.java bin/type/Record.class
	$(JC) src/type/EmptyRecord.java

bin/type/Array.class: src/type/Array.java bin/type/Type.class
	$(JC) src/type/Array.java

bin/type/Nil.class: src/type/Nil.java bin/type/Type.class
	$(JC) src/type/Nil.java

bin/type/Void.class: src/type/Void.java bin/type/Type.class
	$(JC) src/type/Void.java

bin/type/Name.class: src/type/Name.java bin/type/Type.class bin/symbol/Symbol.class
	$(JC) src/type/Name.java

bin/util/SimpleLinkedList.class: src/util/SimpleLinkedList.java
	$(JC) src/util/SimpleLinkedList.java

bin/util/Graph.class: src/util/Graph.java
	$(JC) src/util/Graph.java

bin/util/GraphNode.class: src/util/GraphNode.java
	$(JC) src/util/GraphNode.java

bin/mips32/CodeGen.class: bin/intermediate/Temp.class bin/notifier/Notifier.class bin/intermediate/Label.class bin/intermediate/Temp.class bin/intermediate/IR.class bin/frame/Frame.class bin/util/Graph.class bin/mips32/InstructionList.class bin/intermediate/ThreeAddressCode.class bin/intermediate/MoveTAC.class bin/intermediate/OpTAC.class bin/intermediate/BinOpTAC.class bin/intermediate/UniOpTAC.class bin/intermediate/CallTAC.class bin/intermediate/CallExternTAC.class bin/intermediate/ReturnTAC.class bin/intermediate/GotoTAC.class bin/intermediate/BranchTAC.class bin/mips32/Instruction.class bin/arch/Const.class bin/regalloc/RegAlloc.class bin/symbol/Symbol.class bin/flow/FlowGraph.class bin/flow/LifeAnalysis.class bin/mips32/SpimAsm.class bin/mips32/Optimizer.class bin/mips32/Util.class src/mips32/CodeGen.java
	$(JC) src/mips32/CodeGen.java

bin/mips32/InstructionList.class: bin/regalloc/RegAlloc.class bin/mips32/Instruction.class src/mips32/InstructionList.java
	$(JC) src/mips32/InstructionList.java

bin/mips32/Instruction.class: bin/arch/Const.class bin/arch/Instruction.class bin/frame/Frame.class bin/regalloc/RegAlloc.class src/mips32/Instruction.java
	$(JC) src/mips32/Instruction.java

bin/mips32/InstructionGenerator.class: bin/mips32/Instruction.class bin/arch/InstructionGenerator.class src/mips32/InstructionGenerator.java
	$(JC) src/mips32/InstructionGenerator.java

bin/mips32/Optimizer.class: bin/mips32/InstructionGenerator.class bin/intermediate/IR.class bin/optimization/BasicBlockOptimizer.class bin/flow/FlowGraph.class bin/flow/LifeAnalysis.class bin/mips32/Util.class src/mips32/Optimizer.java
	$(JC) src/mips32/Optimizer.java

bin/mips32/SpimAsm.class: bin/regalloc/Register.class bin/mips32/InstructionList.class src/mips32/SpimAsm.java
	$(JC) src/mips32/SpimAsm.java

bin/mips32/Util.class: bin/flow/FlowGraph.class bin/flow/BasicBlock.class bin/intermediate/Label.class src/mips32/Util.java
	$(JC) src/mips32/Util.java


bin/regalloc/Register.class: src/regalloc/Register.java
	$(JC) src/regalloc/Register.java

bin/regalloc/RegAlloc.class: bin/regalloc/Register.class bin/util/Graph.class src/regalloc/RegAlloc.java
	$(JC) src/regalloc/RegAlloc.java

bin/flow/BasicBlock.class: bin/intermediate/Temp.class bin/intermediate/Label.class bin/arch/Instruction.class src/flow/BasicBlock.java
	$(JC) src/flow/BasicBlock.java

bin/flow/FlowGraph.class: bin/util/Graph.class bin/flow/BasicBlock.class src/flow/FlowGraph.java
	$(JC) src/flow/FlowGraph.java

bin/flow/LifeAnalysis.class: bin/util/Graph.class bin/flow/BasicBlock.class bin/arch/Instruction.class bin/intermediate/Temp.class bin/flow/FlowGraph.class src/flow/LifeAnalysis.java
	$(JC) src/flow/LifeAnalysis.java

bin/optimization/InlineOptimizer.class: bin/frame/Frame.class absyn bin/symbol/Symbol.class bin/symbol/Table.class bin/intermediate/IR.class bin/intermediate/Temp.class bin/intermediate/Label.class bin/intermediate/CallTAC.class bin/intermediate/MoveTAC.class bin/util/Graph.class bin/intermediate/IntermediateCodeList.class src/optimization/InlineOptimizer.java
	$(JC) src/optimization/InlineOptimizer.java

bin/optimization/BasicBlockOptimizer.class: bin/frame/Frame.class bin/arch/InstructionGenerator.class bin/flow/BasicBlock.class bin/flow/LifeAnalysis.class bin/intermediate/Temp.class bin/util/Graph.class bin/arch/Instruction.class src/optimization/BasicBlockOptimizer.java
	$(JC) src/optimization/BasicBlockOptimizer.java

clean:
	rm -fR src/scanner/Scanner.java src/scanner/Scanner.java~ src/parser/Parser.java src/parser/sym.java bin/Main.class bin/parser bin/scanner bin/absyn bin/symbol bin/tester bin/type bin/semant bin/notifier bin/intermediate bin/arch bin/frame bin/util bin/mips32 bin/regalloc bin/flow bin/optimization

