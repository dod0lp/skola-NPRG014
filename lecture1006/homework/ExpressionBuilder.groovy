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

    Item rootItem() { root }
}

@Category(Item)
class ToStringCategory {
    static String prettyToString(Item self) {
        if (self.type in ['number', 'variable']) {
            return "${self.value}"
        };

        def c = self.children;

        if (c.size() == 1) {
            return "${self.type}(${c[0]})"
        };

        def op = (self.type == 'power') ? '^' : self.type;
        def precedence = ['+':10, '-':10, '*':20, '/':20, '^':30];

        def left = c[0];
        def right = c[1];

        def leftStr = left.toString();
        def rightStr = right.toString();

        def final _st = precedence[self.type];
        def final _lt = precedence[left.type];
        def final _rt = precedence[right.type];

        if (left.type && _lt < _st) leftStr = "(${leftStr})";
        if (right.type && _rt <= _st) rightStr = "(${rightStr})";

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

build(new groovy.xml.MarkupBuilder(), description)

def expressionBuilder = new NumericExpressionBuilder()
build(expressionBuilder, description)
def expression = expressionBuilder.rootItem()
println (expression.toString())
assert '10 + x * (2 - 3) / 8 ^ (9 - 5)' == expression.toString()