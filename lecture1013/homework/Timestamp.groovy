// 2025/2026
import java.lang.annotation.ElementType
import java.lang.annotation.Retention
import java.lang.annotation.RetentionPolicy
import java.lang.annotation.Target
import org.codehaus.groovy.ast.expr.*
import org.codehaus.groovy.ast.ASTNode
import org.codehaus.groovy.ast.MethodNode
import org.codehaus.groovy.ast.ClassNode
import org.codehaus.groovy.ast.Parameter
import org.codehaus.groovy.control.SourceUnit
import org.codehaus.groovy.transform.GroovyASTTransformation
import org.codehaus.groovy.transform.GroovyASTTransformationClass
import org.codehaus.groovy.ast.stmt.TryCatchStatement
import static org.codehaus.groovy.control.CompilePhase.SEMANTIC_ANALYSIS
import org.codehaus.groovy.transform.ASTTransformation
import org.codehaus.groovy.ast.*
import org.codehaus.groovy.ast.expr.*
import org.codehaus.groovy.ast.stmt.*
import org.codehaus.groovy.control.*
import org.codehaus.groovy.syntax.*
import org.codehaus.groovy.transform.*
import static org.codehaus.groovy.syntax.Types.*


@Retention(RetentionPolicy.SOURCE)
@Target([ElementType.TYPE])
@GroovyASTTransformationClass("CreatedAtTransformation")
public @interface CreatedAt {
    String name() default "";
}


@GroovyASTTransformation(phase = SEMANTIC_ANALYSIS)
public class CreatedAtTransformation implements ASTTransformation {
    static final int ACC_PRIVATE = 2
    static final int ACC_PUBLIC = 1
    static final int ACC_FINAL = 16

    public void visit(ASTNode[] astNodes, SourceUnit source) {

        //...
        // TASK Ensure the annotated class has a private long field holding the time of instantiation of the object.
        // Also, generate a public final method returning the value stored in the field. The name of the method should be configurable through 
        // the annotation 'name' parameter.
        // Additionally, all existing methods of the class should be enhanced so that they reset the time stored in the field to the current time,
        // whenever they are called, but ONLY if more than 1 second has elapsed since the latest update to the time stored in the field.
        // A new method, named "clearTimestamp()" must be added to the class. This method sets the time stored in the field to "0".

        // Fill in the missing AST generation code to make the script pass
        // You can take inspiration from exercises
        // Documentation and hints:
        // http://docs.groovy-lang.org/docs/next/html/documentation/
        // http://docs.groovy-lang.org/docs/groovy-latest/html/api/org/codehaus/groovy/ast/package-summary.html
        // http://docs.groovy-lang.org/docs/groovy-latest/html/api/org/codehaus/groovy/ast/expr/package-summary.html
        // http://docs.groovy-lang.org/docs/groovy-latest/html/api/org/codehaus/groovy/ast/stmt/package-summary.html
        // http://docs.groovy-lang.org/docs/groovy-latest/html/api/org/codehaus/groovy/ast/tools/package-summary.html        
        // http://docs.groovy-lang.org/docs/groovy-latest/html/api/org/codehaus/groovy/ast/tools/GeneralUtils.html

        // Use ClassHelper.long_TYPE to specify a long type.
        // buildFromString() returns an array, which holds a BlockStatement for the passed-in code as its first element.
        // ClassNode.addField() accepts an expression, which can be obtained from a BlockStatement as blockStatement.statements.expression
        // ClassNode.addMethod() accepts a BlockStatement

        //TODO Implement this method
        AnnotationNode annotationNode = astNodes[0]
        if (!(astNodes[1] instanceof ClassNode)) return
        ClassNode classNode = astNodes[1]

        // --- Add private long field ---
        FieldNode timestampField = new FieldNode("__createdAt",
                ACC_PRIVATE,
                ClassHelper.long_TYPE,
                classNode,
                new ConstantExpression(0L))
        classNode.addField(timestampField)

        // --- Add public final getter method ---
        String getterName = "createdAt"
        def nameAttr = annotationNode.getMember("name")
        if (nameAttr instanceof ConstantExpression && nameAttr.value) {
            getterName = nameAttr.value
        }

        BlockStatement getterBody = new BlockStatement()
        getterBody.addStatement(new ReturnStatement(new VariableExpression("__createdAt")))

        MethodNode getterMethod = new MethodNode(getterName,
                ACC_PUBLIC | ACC_FINAL,
                ClassHelper.long_TYPE,
                Parameter.EMPTY_ARRAY,
                ClassNode.EMPTY_ARRAY,
                getterBody)
        classNode.addMethod(getterMethod)

        // --- "Enhance" all existing methods ---
        classNode.methods.each { MethodNode method ->
            // smth i seen in docs
            if (method.isSynthetic() || method.name.contains("<init>")) return
            BlockStatement originalCode = method.code instanceof BlockStatement ? method.code : new BlockStatement()

            // update timestamp if more than 1s has passed
            BlockStatement newCode = new BlockStatement()

            // if (System.currentTimeMillis() - __createdAt > 1000) { __createdAt = System.currentTimeMillis() }
            // --- if (System.currentTimeMillis() - this.__createdAt > 1000) { this.__createdAt = System.currentTimeMillis() } ---
            newCode.addStatement(new IfStatement(
                    // wrap the BinaryExpression in a BooleanExpression (required by IfStatement constructors)
                    new BooleanExpression(new BinaryExpression(
                            // left side: System.currentTimeMillis() - this.__createdAt
                            new BinaryExpression(
                                new MethodCallExpression(
                                    new ClassExpression(ClassHelper.make(System)),
                                        "currentTimeMillis",
                                        ArgumentListExpression.EMPTY_ARGUMENTS),
                                        Token.newSymbol(MINUS, -1, -1),
                                        new FieldExpression(timestampField)),
                            Token.newSymbol(COMPARE_GREATER_THAN, -1, -1),
                            new ConstantExpression(1000L))),
                    // then: this.__createdAt = System.currentTimeMillis()
                    new ExpressionStatement(new BinaryExpression(new FieldExpression(timestampField),
                            Token.newSymbol(EQUAL, -1, -1),
                            new MethodCallExpression(new ClassExpression(ClassHelper.make(System)),
                                    "currentTimeMillis",
                                    ArgumentListExpression.EMPTY_ARGUMENTS))),
                    EmptyStatement.INSTANCE))


            newCode.addStatements(originalCode.statements)
            method.code = newCode
        }

        // --- Add clearTimestamp() method ---
        BlockStatement clearBody = new BlockStatement()
        clearBody.addStatement(new ExpressionStatement(new BinaryExpression(new VariableExpression("__createdAt"),
                Token.newSymbol(Types.EQUAL, -1, -1),
                new ConstantExpression(0L))))

        MethodNode clearMethod = new MethodNode("clearTimestamp",
                ACC_PUBLIC,
                ClassHelper.VOID_TYPE,
                Parameter.EMPTY_ARRAY,
                ClassNode.EMPTY_ARRAY,
                clearBody)
        classNode.addMethod(clearMethod)
    }
}

final calculator = new GroovyShell(this.class.getClassLoader()).evaluate('''
@CreatedAt(name = "timestamp")
class Calculator {
    int sum = 0
    
    def add(int value) {
        int v = sum + value
        sum = v
    }

    def subtract(int value) {
        sum -= value
    }
}

new Calculator()
''')

assert System.currentTimeMillis() >= calculator.timestamp()
assert calculator.timestamp() == calculator.timestamp()
def oldTimeStamp = calculator.timestamp()

sleep(1500)
calculator.add(10)
assert calculator.sum == 10

assert oldTimeStamp < calculator.timestamp()
//The timestamp should have been updated since the pause was longer than 1s
assert calculator.timestamp() == calculator.timestamp()
oldTimeStamp = calculator.timestamp()

sleep(1500)
calculator.subtract(1)
assert calculator.sum == 9
//The timestamp should have been updated since the pause was longer than 1s
assert oldTimeStamp < calculator.timestamp()
assert calculator.timestamp() == calculator.timestamp()

oldTimeStamp = calculator.timestamp()
sleep(100)
calculator.subtract(1)
assert calculator.sum == 8
//The timestamp should not have been updated since the pause was shorter than 1s
assert oldTimeStamp == calculator.timestamp()
assert calculator.timestamp() == calculator.timestamp()

calculator.clearTimestamp()
assert calculator.timestamp() == 0

println 'well done'