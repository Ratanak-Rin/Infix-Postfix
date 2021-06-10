// Name: Ratanak Rin
// Email: ratanak.rin@stonybrook.edu

public class Parser {
    private static enum Action { Push, Pop }
    private static final Action[][] ACT;
    
    static {
        // ACT: an action table. based on the input token and the top symbol
        //      of the stack, what action needs to be taken.
        // Push: push the current input token to the stack
        // Pop: pop the operator from the stack and perform the operation
        // Undefined elements (null) mean an error

        ACT = new Action[128][128]; //[stack top][input token]

        //TODO: add table entries for unary minus '~' operator

        ACT['#']['~'] = ACT['+']['~'] = ACT['-']['~'] = ACT['*']['~'] = ACT['/']['~'] = ACT['(']['~'] = Action.Push;
                                                                        ACT['~']['('] = ACT['~']['~'] = Action.Push;
        ACT['~']['+'] = ACT['~']['-'] = ACT['~']['*'] = ACT['~']['/'] = ACT['~'][')'] = ACT['~']['$'] = Action.Pop;
        ACT['#']['('] = ACT['(']['('] = ACT['+']['('] = ACT['-']['('] = ACT['*']['('] = ACT['/']['('] = Action.Push;
                        ACT['('][')'] = ACT['+'][')'] = ACT['-'][')'] = ACT['*'][')'] = ACT['/'][')'] = Action.Pop;
        ACT['#']['+'] = ACT['(']['+'] =                                                                 Action.Push;
                                        ACT['+']['+'] = ACT['-']['+'] = ACT['*']['+'] = ACT['/']['+'] = Action.Pop;
        ACT['#']['-'] = ACT['(']['-'] =                                                                 Action.Push;
                                        ACT['+']['-'] = ACT['-']['-'] = ACT['*']['-'] = ACT['/']['-'] = Action.Pop;
        ACT['#']['*'] = ACT['(']['*'] = ACT['+']['*'] = ACT['-']['*'] =                                 Action.Push;
                                                                        ACT['*']['*'] = ACT['/']['*'] = Action.Pop;
        ACT['#']['/'] = ACT['(']['/'] = ACT['+']['/'] = ACT['-']['/'] =                                 Action.Push;
                                                                        ACT['*']['/'] = ACT['/']['/'] = Action.Pop;
        ACT['#']['$'] = ACT['(']['$'] = ACT['+']['$'] = ACT['-']['$'] = ACT['*']['$'] = ACT['/']['$'] = Action.Pop;
    }
    
    private static class Node {
        protected Integer   num;
        protected Character opr;

        public Node(Integer num)   { this.num = num; }
        public Node(Character opr) { this.opr = opr; }
        public String toString() {
            return num != null ? num.toString()
                :  opr != null ? opr.toString()
                :  ""
                ;
        }
    }
    
    public static BinaryTree<Node> parseExpr(String expr) {
        Scanner scan = new Scanner(expr);
        Stack<LinkedBinaryTree<Node>> stack_tree = new StackByArray<LinkedBinaryTree<Node>>();
        Stack<Character> stack_oper = new StackByArray<Character>();

        //      - parseExpr will be similar to evalExpr function that evaluates
        //        infix expressions.
        //      - Here, instead of using the operand stack, we push/pop subtrees of
        //        the parse tree to/from the tree stack.
        //      - When popping an operator, pop one or two parse-trees from the tree stack;
        //        build a parse-tree rooted at the operator; and push the resulting tree
        //        onto the tree stack

        stack_oper.push('#');
        for(String tok : scan) {
            char c = tok.charAt(0);
            if(Scanner.isAlpha(c))
                throw new UnsupportedOperationException("Error: " + tok);
            else if(Scanner.isDigit(c)) {
                Node n = new Node(Integer.valueOf(tok));
                LinkedBinaryTree<Node> LBTree = new LinkedBinaryTree<>();
                LBTree.addRoot(n);
                stack_tree.push(LBTree);
            }else {
                Action a;
                while((a = ACT[stack_oper.top()][c]) == Action.Pop) {
                    char op = stack_oper.pop();
                    if(op == '+' || op == '-' || op == '*' || op == '/') {
                        LinkedBinaryTree<Node> treeF = new LinkedBinaryTree<>();
                        treeF.addRoot(new Node(op));
                        LinkedBinaryTree<Node> right = stack_tree.pop();
                        treeF.attach(treeF.root(), stack_tree.pop(), right);
                        stack_tree.push(treeF);
                    }else if (op == '~') {
                        LinkedBinaryTree<Node> treeF = new LinkedBinaryTree<>();
                        treeF.addRoot(new Node(op));
                        treeF.attach(treeF.root(), null, stack_tree.pop());
                        stack_tree.push(treeF);
                    }
                    else if(op == '#' || op == '(')
                        break; //pop op & discard c
                    else
                        throw new UnsupportedOperationException("Error: " + tok);
                }
                if(a == Action.Push)
                    stack_oper.push(c);
                else if(a == null)
                    throw new IllegalStateException("Syntax error: " + tok);
            }
        }
        if (stack_oper.size() != 0 || stack_tree.size() != 1)
            throw new IllegalStateException("Syntax Error");
        return stack_tree.pop();
    }

    public static double evalExpr(BinaryTree<Node> tree) {
        Stack<Double> num = new StackByArray<Double>();
        
        //      - evalExpr will be similar to evalPostfixExpr function that evaluates
        //        postfix expressions.
        //      - While traversing the nodes of the parseTree in the post-order,
        //        evaluate the expression by pushing/popping operands to/from the stack num

        LinkedBinaryTree<Node> parseTree = (LinkedBinaryTree<Node>) tree;

        for (Position<Node> tok: parseTree.postorder()){
            Integer number = tok.getElement().num;
            Character character = tok.getElement().opr;
            if ((number != null && character != null) || (number == null && character == null))
                throw new UnsupportedOperationException("Error: " + tok);
            else if (number != null)
                num.push((double)tok.getElement().num);
            else if (character == '+' || character == '-' || character == '*' || character == '/') {
                double n2 = num.pop();
                double n1 = num.pop();
                if (character == '+') num.push(n1 + n2);
                else if (character == '-') num.push(n1 - n2);
                else if (character == '*') num.push(n1 * n2);
                else num.push(n1 / n2);
            }
            else if (character == '~'){
                double n2 = num.pop();
                double n1 = 0;
                num.push(n1 - n2);
            }
        }
        if (num.size() != 1) throw new IllegalStateException("Syntax Error");
        return num.pop();
    }



    public static String infixToPrefix(String expr) {
        String strExp = "";
        BinaryTree<Node> parseTree = parseExpr(expr);
        for(Position<Node> p: parseTree.preorder())
            strExp += p.getElement() + " ";
        return strExp;
    }

    public static String infixToPostfix(String expr) {
        String strExp = "";
        BinaryTree<Node> parseTree = parseExpr(expr);
        for(Position<Node> p: parseTree.postorder())
            strExp += p.getElement() + " ";
        return strExp;
    }

    public static String infixToInfix(String expr) {
        String strExp = "";
        BinaryTree<Node> parseTree = parseExpr(expr);
        for(Position<Node> p: parseTree.inorder())
            strExp += p.getElement() + " ";
        return strExp;
    }
}
