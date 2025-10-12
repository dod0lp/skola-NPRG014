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
    // my IDE couldnt read it even tho docs say these rae "inherited from interface org.objectweb.asm.Opcodes"
    static final int ACC_PRIVATE = 2
    static final int ACC_PUBLIC = 1
    static final int ACC_FINAL = 16
    private static String _SYS_CUR_TIME = "currentTimeMillis"
    private static String _LAST_UPD = "__lastUpdated"
    private static String _CREATED_AT = "__createdAt"


    private static MethodCallExpression getSysCurTime() {
        return new MethodCallExpression(
                new ClassExpression(ClassHelper.make(System)),
                _SYS_CUR_TIME,
                ArgumentListExpression.EMPTY_ARGUMENTS
        )
    }

    private static BinaryExpression assignMethodCallToField(FieldNode timestampField, MethodCallExpression nowCall) {
        new BinaryExpression(
                new FieldExpression(timestampField),
                Token.newSymbol(EQUAL, -1, -1), // I put compare_equal here at first accidentally and was debugging code for so long💀
                nowCall
        )
    }

    private static IfStatement updateTimestampStatement(FieldNode lastUpdatedField, FieldNode timestampField) {
        // --- update timestamp only if >1s has passed ---
        // (System.currentTimeMillis() - this.__lastUpdated) > 1000
        MethodCallExpression nowCall = getSysCurTime()
        BinaryExpression timeDiff = new BinaryExpression(
                nowCall,
                Token.newSymbol(MINUS, -1, -1),
                new FieldExpression(lastUpdatedField)
        )
        BinaryExpression condition = new BinaryExpression(
                timeDiff,
                Token.newSymbol(COMPARE_GREATER_THAN, -1, -1),
                new ConstantExpression(1000L)
        )

        // { this.__createdAt = now; this.__lastUpdated = now; }
        BlockStatement thenBlock = new BlockStatement()
        thenBlock.addStatement(new ExpressionStatement(
                assignMethodCallToField(timestampField, nowCall)
        ))
        thenBlock.addStatement(new ExpressionStatement(
                new BinaryExpression(
                        new FieldExpression(lastUpdatedField),
                        Token.newSymbol(EQUAL, -1, -1),
                        nowCall
                )
        ))

        return new IfStatement(new BooleanExpression(condition), thenBlock, EmptyStatement.INSTANCE)
    }

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

        // --- private long field(s) ---
        FieldNode timestampField = new FieldNode(
            _CREATED_AT,
            ACC_PRIVATE,
            ClassHelper.long_TYPE,
            classNode,
            getSysCurTime()
        )

        FieldNode lastUpdatedField = new FieldNode(
            _LAST_UPD,
            ACC_PRIVATE,
            ClassHelper.long_TYPE,
            classNode,
            getSysCurTime()
        )

        classNode.addField(timestampField)
        classNode.addField(lastUpdatedField)

        // --- public final getter named ---
        String getterName = "createdAt"
        def nameAttr = annotationNode.getMember("name")
        if (nameAttr instanceof ConstantExpression && nameAttr.value) {
            getterName = nameAttr.value
        }

        BlockStatement getterBody = new BlockStatement()
        getterBody.addStatement(updateTimestampStatement(lastUpdatedField, timestampField))
        getterBody.addStatement(new ReturnStatement(new VariableExpression(_CREATED_AT)))

        MethodNode getterMethod = new MethodNode(getterName,
                ACC_PUBLIC | ACC_FINAL,
                ClassHelper.long_TYPE,
                Parameter.EMPTY_ARRAY,
                ClassNode.EMPTY_ARRAY,
                getterBody)
        classNode.addMethod(getterMethod)

        classNode.methods.each { MethodNode method ->
            BlockStatement originalCode = method.code instanceof BlockStatement
                    ? (BlockStatement) method.code
                    : new BlockStatement()

            BlockStatement newCode = new BlockStatement()
            newCode.addStatement(updateTimestampStatement(lastUpdatedField, timestampField))
            newCode.addStatements(originalCode.statements)
            method.code = newCode
        }

        // --- clearTimestamp() ---
        BlockStatement clearBody = new BlockStatement()
        clearBody.addStatement(new ExpressionStatement(new BinaryExpression(new VariableExpression(_CREATED_AT),
                Token.newSymbol(EQUAL, -1, -1),
                new ConstantExpression(0L))))

        MethodNode clearMethod = new MethodNode(
                "clearTimestamp",
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