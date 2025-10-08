// 2025/2026
// TASK The MarkupBuilder in Groovy can transform a hierarchy of method calls and nested closures into a valid XML document.
// Create a NumericExpressionBuilder builder that will read a user-specified hierarchy of simple math expressions and build a tree representation of it.
// The basic arithmetics operations as well as the power (aka '^') operation must be supported.
// It will feature a toString() method that will pretty-print the expression tree into a string with the same semantics, as verified by the assert on the last line.
// This means that parentheses must be placed where necessary with respect to the mathematical operator priorities.
// Change or add to the code in the script. Reuse the infrastructure code at the bottom of the script.
class NumericExpressionBuilder extends BuilderSupport {
    Item root;

    protected void setParent(Object parent, Object child) {
        parent.children << child;
    }

    protected Object createNode(Object name) {
        return new Item(type: name);
    }

    protected Object createNode(Object name, Map attributes) {
        def node = new Item(type: name);
        if (attributes.value != null) {
            node.value = attributes.value;
        }

        return node;
    }

    protected Object createNode(Object name, Object value) {
        new Item(type: name, value: value);
    }

    protected Object createNode(Object name, Map attributes, Object value) {
        new Item(type: name, value: attributes?.value ?: value);
    }

    protected void nodeCompleted(Object parent, Object node) {
        if (!parent) {
            root = node as Item;
        }
    }

    Item rootItem() { root; }
}

@Category(Item)
class ToStringCategory {
    static boolean needsParentheses(Item parent, Item child, boolean isRight) {
        if (child.type in ['number','variable']) {
            return false;
        }

        final def precedence = ['+':10, '-':10, '*':20,'/':20, 'power':30];
        // arbitrary "small" and "big" number to set precedence
        final def parentPrec = precedence[parent.type] ?: 0;
        final def childPrec = precedence[child.type] ?: 100;

        if (childPrec == parentPrec) {
            if (parent.type == 'power') {
                return isRight;
            }

            if (parent.type in ['-', '/'] && isRight) {
                return true;
            }
        }

        return (childPrec < parentPrec);
    }


    static String prettyToString(Item self) {
        if (self.type in ['number', 'variable']) {
            return "${self.value}";
        };

        def c = self.children;

        if (c.size() == 1) {
            return "${self.type}(${c[0]})";
        }


        final def left = c[0];
        final def right = c[1];
        final def op = (self.type == 'power') ? '^' : self.type;

        def leftStr = left.toString();
        def rightStr = right.toString();
        if (needsParentheses(self, left, false)) {
            leftStr = "(${leftStr})";
        }

        if (needsParentheses(self, right, true)) {
            rightStr = "(${rightStr})";
        }

        return "${leftStr} ${op} ${rightStr}";
    }
}

class Item {
    String type;
    def value;
    List<Item> children = [];

    @Override
    public String toString() {
        use(ToStringCategory) {
            // this.prettyToString(this);
            // prettyToString();
            this.prettyToString();
        }
    }
}
//------------------------- Do not modify beyond this point!

def build(builder, String specification) {
    def binding = new Binding()
    binding['builder'] = builder
    new GroovyShell(binding).evaluate(specification)
}

//Custom expression to display. It should be eventually pretty-printed as 10 + x * (2 - 3) / 8 ^ (9 - 5)
String description = '''
builder.'+' {
    number(value: 10)
    '*' {
        variable(value: 'x')
        '/' {
            '-' {
                number(value: 2)
                number(value: 3)
            }
            power {
                number(value: 8)
                '-' {
                    number(value: 9)
                    number(value: 5)
                }
            }
        }
    }
}
'''

//XML builder building an XML document
build(new groovy.xml.MarkupBuilder(), description)

//NumericExpressionBuilder building a hierarchy of Items to represent the expression
def expressionBuilder = new NumericExpressionBuilder()
build(expressionBuilder, description)
def expression = expressionBuilder.rootItem()
println (expression.toString())
assert '10 + x * (2 - 3) / 8 ^ (9 - 5)' == expression.toString()