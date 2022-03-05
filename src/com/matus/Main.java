package com.matus;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.util.*;

import com.matus.analyzers.LexicAnalyzer;
import com.matus.analyzers.SyntacticAnalyzer;
import com.matus.elements.*;
import com.matus.exceptions.InvalidCharacterException;
import com.matus.gui.MainWindow;
import java_cup.runtime.Symbol;

public class Main {

    //Internal vars
    private static boolean debug = true;
    public static boolean debugLoadExp = true;

    public static boolean stopLexOnError = true;
    public static boolean groupDefinitionIsUppercase = true;

    //Global utils
    private static final Scanner scanner = new Scanner(System.in);
    private static final MainWindow mainWindow = new MainWindow();

    //Lists
    private static List<Token> tokenList= new ArrayList<>();

    private static List<LexicError> lexicErrorList= new ArrayList<>();
    private static List<SyntacticError> syntacticErrorList= new ArrayList<>();

    private static List<Group> groupList = new ArrayList<>();
    private static List<Group> regexList = new ArrayList<>();
    //private static List<RegexTest> regexTestList = new ArrayList<>();

    public static void main(String[] args){

        logRegex("RegexPrueba1",".*.abc",0,0);


        //mainWindow.setDefaultCloseOperation (JFrame.HIDE_ON_CLOSE);
        //mainWindow.setVisible (true);
        //System.out.println("calma, se manejo todo");
    }

    //0: success
    //1: lexic error (remember to clean tables)
    //2: syntactic error (remember to clean tables)
    //3: File error
    public static int parseExpFile() {
        //Cleaning
        mainWindow.outputTextArea.setText("");

        String input = mainWindow.inputTextArea.getText();
        System.out.println("TEXTO A ANALIZAR:");
        System.out.println(input);

        FileHandler.writeToFile("./tmp.exp", input, false);
        try {
            LexicAnalyzer lexic = new LexicAnalyzer(
                    new BufferedReader(new FileReader("./tmp.exp"))
            );

            SyntacticAnalyzer syntactic = new SyntacticAnalyzer(lexic);
            Symbol result = syntactic.parse();
            System.out.println(result);

        } catch (InvalidCharacterException e) {
            //e.printStackTrace();
            //Lexic error logged by self class, no need for code
            return 1;

        } catch (FileNotFoundException e) {
            e.printStackTrace();
            return 3;
        } catch (Exception e) {
            System.out.println(e.getMessage());
            //e.printStackTrace();
            return 2;
            //Syntactic error logged by self class, no need for code
        }

        //No error catched? sheeesh
        dprint("no paso nada oiga");
        return 0;
    }


    public static void logGroup(String keyword, String name, String rawData, int row, int column) {

        //Correct keyword
        if (groupDefinitionIsUppercase) {
            if (!keyword.equals("CONJ")) {
                String f = String.format("Para definir conjuntos debes usar la palabra clave CONJ (case sensitive) %s", keyword);
                cprintln(String.format("%s (f:%s c:%s)", f, row, column));
                logSyntacticError(keyword, "conj", f, row, column);
                return;
            }
        }
        else {
            if (!keyword.equalsIgnoreCase("conj")) {
                String f = String.format("Para definir conjuntos debes usar la palabra clave CONJ (case insensitive) %s", keyword);
                cprintln(String.format("%s (f:%s c:%s)", f, row, column));
                logSyntacticError(keyword, "conj", f, row, column);
                return;
            }
        }

        //Name is always correct? idk
        //Correct range
        if (rawData.contains("~")) {

            if (rawData.length() != 3 /* x~y */) {
                String f = String.format("Los elementos de un grupos definidos por rango solo pueden tener 1 caracter %s", keyword);
                cprintln(String.format("%s (f:%s c:%s)", f, row, column));
                logSyntacticError(keyword, "conj", f, row, column);
                return;
            }
        }

        //Correct list
        if (rawData.contains(",")) {
            boolean allOneCharacter = true;
            for (String letter  : rawData.split(",")) {
                if (letter.length() != 1) {
                    allOneCharacter = false;
                    break;
                }
            }

            if (!allOneCharacter) {
                String f = String.format("Los elementos de un grupo definidos por lista solo pueden tener 1 caracter %s", keyword);
                cprintln(String.format("%s (f:%s c:%s)", f, row, column));
                logSyntacticError(keyword, "conj", f, row, column);
                return;
            }
        }

        //sheeeesh
        groupList.add(new Group(name, rawData));
    }


    public static void logRegex(String name, String rawData, int row, int column) {
        RegexExpression regexExp = new RegexExpression(name, rawData);
        String graphvizString = "digraph {\nnodesep=3;\n";
        int nodesCreated = 0;
        rawData = ".<->+<->a<->+<->b"; //TODO please delete this lmao
        //rawData = ".<->{letra}<->*<->|<->\"_\"<->|<->{letra}<->{digito}"; //TODO please delete this lmao
        //rawData = ".<->{digito}<->.<->\".\"<->+<->{digito}"; //TODO please delete this lmao
        //rawData = ".<->{digito}<->*<->|<->\"_\"<->{letra}<->{digito}"; //TODO please delete this lmao
        //Parse this baby

        List<String> tmpList = new ArrayList<>(); //for separating elements into chars (except previous groups and special chars)

        Stack<NodeTree> stack = new Stack<>(); //parsed elements go here

        List<NodeTree> leafList = new ArrayList<>();
        regexExp.leavesList = leafList;

        rawData = ".<->" + rawData + "<->#";
        String[] _arr = rawData.split("<->");


        for (String s : _arr) {
            if (s.contains("{") | s.contains("\"")) {
                //System.out.println("Skipping separation of group:" + s);
                tmpList.add(s);
                continue;
            }
            //If id element (multiple single elements concatenated):
            for (int j = 0; j < s.length(); j++) {
                tmpList.add(s.substring(j, j + 1));
            }
        }

        dprint("Before expansion:");
        dprint(tmpList);
        //Expansion of + and ?
        tmpList = specialOperatorsExpansion(new ArrayList<>(tmpList)); //before stuff goes wild
        dprint("After expansion:");
        dprint(tmpList);

        for (int i = tmpList.size()-1; i >= 0 ; i--) { //Reverse loop array
            String s = tmpList.get(i);
            System.out.println("Now analyzing " + s);
            System.out.println("Current stack is" + stack);
            switch (s) {
                case "." -> {
                    //stack underflow
                    if (stack.size() < 2) {
                        String f = String.format("Expresión REGEX invalida (operación . definida sin elementos previos): %s", rawData);
                        cprintln(String.format("%s (f:%s c:%s)", f, row, column));
                        logSyntacticError(rawData, "conj", f, row, column);
                        return;
                    }

                    NodeTree a = stack.pop();
                    NodeTree b = stack.pop();

                    NodeTree parent = new NodeTree();

                    parent.label = ".";
                    parent.orderInTree = nodesCreated;

                    parent.number = "";
                    parent.leftChildren = a;
                    a.parent = parent;
                    parent.rightChildren = b;
                    b.parent = parent;

                    //Nullable
                    parent.nullable = a.nullable && b.nullable;

                    //First Pos
                    if (a.nullable) parent.firstPos = orderStringArray(removeStringListDuplicates(a.firstPos + "," + b.firstPos));
                    else parent.firstPos = a.firstPos;

                    //Last Pos
                    if (b.nullable) parent.lastPos = orderStringArray(removeStringListDuplicates(a.lastPos + "," + b.lastPos));
                    else parent.lastPos = b.lastPos;
                    stack.add(parent); // (a operand b)

                    //AFD Tree method next table
                    System.out.println("adding next for");
                    System.out.println(a.lastPos);
                    System.out.println(b.firstPos);
                    for (String ultPosC1 : a.lastPos.split(",")) {
                        for (String primPosC2 : b.firstPos.split(",")) {

                            System.out.printf("A ultposc1:%s le sigue primposc2:%s\n",ultPosC1, primPosC2);

                            String prev = regexExp.nextTable.get(Integer.parseInt(ultPosC1)-1);
                            String f = "";
                            if (!prev.equals("")) f = prev + ",";
                            f += primPosC2;
                            regexExp.nextTable.set(Integer.parseInt(ultPosC1)-1, f);
                        }
                        //Remove duplicates and sort
                        String f = regexExp.nextTable.get(Integer.parseInt(ultPosC1)-1);
                        f = removeStringListDuplicates(f);
                        f = orderStringArray(f);
                        regexExp.nextTable.set(Integer.parseInt(ultPosC1)-1,f);
                    }

                    //Graphviz

                    graphvizString += String.format(
                            "\"node-%s\"[fixedsize=true,label=<<TABLE CELLSPACING=\"2\" CELLPADDING=\"2\" BORDER=\"0\">" +
                                    "<TR><TD></TD><TD>%s</TD><TD></TD></TR><TR><TD></TD><TD></TD><TD></TD></TR>" +
                                    "<TR><TD>%s</TD><TD>%s</TD><TD>%s</TD></TR><TR><TD></TD><TD>%s</TD><TD></TD></TR></TABLE>>]\n"
                            , nodesCreated, parent.nullable? "V":"F", parent.firstPos, centerString(parent.label,16), parent.lastPos, " ");


                    graphvizString += String.format("\"node-%s\" -> \"node-%s\" \n \"node-%s\" -> \"node-%s\"\n",nodesCreated, b.orderInTree, nodesCreated, a.orderInTree);
                    nodesCreated++;

                }
                case "|" -> {
                    //stack underflow
                    if (stack.size() < 2) {
                        String f = String.format("Expresión REGEX invalida (operación | definida sin elementos previos): %s", rawData);
                        cprintln(String.format("%s (f:%s c:%s)", f, row, column));
                        logSyntacticError(rawData, "conj", f, row, column);
                        return;
                    }
                    NodeTree a = stack.pop();
                    NodeTree b = stack.pop();

                    NodeTree parent = new NodeTree();

                    parent.label = "|";
                    parent.orderInTree = nodesCreated;

                    parent.number = "";
                    parent.leftChildren = a;
                    a.parent = parent;
                    parent.rightChildren = b;
                    b.parent = parent;

                    //Nullable
                    parent.nullable = a.nullable || b.nullable;

                    //First Pos
                    if (a.nullable) parent.firstPos = removeStringListDuplicates(a.firstPos + "," + b.firstPos);
                    else parent.firstPos = a.firstPos;

                    //Last Pos
                    if (b.nullable) parent.lastPos = removeStringListDuplicates(a.lastPos + "," + b.lastPos);
                    else parent.lastPos = b.lastPos;
                    stack.add(parent); // (a operand b)


                    graphvizString += String.format(
                            "\"node-%s\"[fixedsize=true,label=<<TABLE CELLSPACING=\"2\" CELLPADDING=\"2\" BORDER=\"0\">" +
                                    "<TR><TD></TD><TD>%s</TD><TD></TD></TR><TR><TD></TD><TD></TD><TD></TD></TR>" +
                                    "<TR><TD>%s</TD><TD>%s</TD><TD>%s</TD></TR><TR><TD></TD><TD>%s</TD><TD></TD></TR></TABLE>>]\n"
                            , nodesCreated, parent.nullable? "V":"F", parent.firstPos, centerString(parent.label,16), parent.lastPos, " ");


                    //Graphviz
                    graphvizString += String.format("\"node-%s\" -> \"node-%s\" \n \"node-%s\" -> \"node-%s\"\n",nodesCreated, a.orderInTree, nodesCreated, b.orderInTree);
                    nodesCreated++;
                }

                case "*" -> {
                    if (stack.size() < 1) {
                        String f = String.format("Expresión REGEX invalida (operación * definida sin elemento previo): %s", rawData);
                        cprintln(String.format("%s (f:%s c:%s)", f, row, column));
                        logSyntacticError(rawData, "conj", f, row, column);
                        return;
                    }
                    NodeTree a = stack.pop();
                    NodeTree parent = new NodeTree();

                    parent.label = "*";
                    parent.orderInTree = nodesCreated;

                    parent.number = "";
                    parent.leftChildren = a;
                    a.parent = parent;

                    //Nullable
                    parent.nullable = true;
                    //First Pos
                    parent.firstPos = a.firstPos;
                    //Last Pos
                    parent.lastPos = a.lastPos;
                    stack.add(parent); // (a operand b)


                    System.out.println("Element for next table:");
                    System.out.println(Arrays.toString(a.lastPos.split(",")));
                    System.out.println(Arrays.toString(a.firstPos.split(",")));

                    //AFD Tree method next table
                    for (String ultPosC1 : a.lastPos.split(",")) {
                        for (String primPosC1 : a.firstPos.split(",")) {
                            System.out.printf("A ultposc1:%s le sigue primposc1:%s\n",ultPosC1, primPosC1);
                            String prev = regexExp.nextTable.get(Integer.parseInt(ultPosC1)-1);
                            String f = "";
                            if (!prev.equals("")) f = prev + ",";
                            f += primPosC1;
                            regexExp.nextTable.set(Integer.parseInt(ultPosC1)-1, f);
                        }
                        //Remove duplicates
                        String f = regexExp.nextTable.get(Integer.parseInt(ultPosC1)-1);
                        f = removeStringListDuplicates(f);
                        System.out.println("Before call for removing dups");
                        System.out.println(f);
                        regexExp.nextTable.set(Integer.parseInt(ultPosC1)-1,f);
                    }

                    //Graphviz
                    graphvizString += String.format(
                            "\"node-%s\"[fixedsize=true,label=<<TABLE CELLSPACING=\"2\" CELLPADDING=\"2\" BORDER=\"0\">" +
                                    "<TR><TD></TD><TD>%s</TD><TD></TD></TR><TR><TD></TD><TD></TD><TD></TD></TR>" +
                                    "<TR><TD>%s</TD><TD>%s</TD><TD>%s</TD></TR><TR><TD></TD><TD>%s</TD><TD></TD></TR></TABLE>>]\n"
                            , nodesCreated, "V", parent.firstPos, centerString(parent.label,16), parent.lastPos, " ");

                    graphvizString += String.format("\"node-%s\" -> \"node-%s\"[dir=none]\n",nodesCreated, a.orderInTree);
                    nodesCreated++;
                }

                // + and ? are expanded in specialOperatorsExpansion() before this loop.

                default -> { //Leaf
                    dprint("Leaf encountered with " + s);
                    int leafCount = leafList.size() + 1;
                    //(String label, String firstPos, String lastPos, boolean nullable, int number, boolean isEpsilon)
                    boolean nullable = s.equals("{epsilon}");
                    NodeTree theNode = new NodeTree(s,Integer.toString(leafCount), Integer.toString(leafCount), nullable, Integer.toString(leafCount), s.equals("epsilon"), nodesCreated);
                    leafList.add(theNode);
                    System.out.println("adding to stack"); //TODO delete me
                    stack.add(theNode);
                    //Graphviz
                    graphvizString += String.format(
                            "\"node-%s\"[fixedsize=true,label=<<TABLE CELLSPACING=\"2\" CELLPADDING=\"2\" BORDER=\"0\">" +
                                    "<TR><TD></TD><TD>%s</TD><TD></TD></TR><TR><TD></TD><TD></TD><TD></TD></TR>" +
                                    "<TR><TD>%s</TD><TD>%s</TD><TD>%s</TD></TR><TR><TD></TD><TD>%s</TD><TD></TD></TR></TABLE>>]\n"
                            , nodesCreated, nullable? "V":"F", leafCount, centerString(s,14), leafCount, leafCount);
                    nodesCreated++;

                    regexExp.nextTable.add("");
                }
            }
            dprint("!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!");
            dprint("!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!");
            dprint(graphvizString + "\n}");
        }

        graphvizString += "\n}";
        regexExp.afd_tree_graphviz = graphvizString;

        System.out.println("RESULTADO:" + stack.peek().label);
        System.out.println(stack.size());

        if (stack.size() != 1) {//TODO this should always be 1
            String f = String.format("Expresión REGEX invalida (mas operandos que operaciones): %s", rawData);
            cprintln(String.format("%s (f:%s c:%s)", f, row, column));
            logSyntacticError(rawData, "conj", f, row, column);
            return;
        }
        regexExp.treeHead = stack.peek();
        regexExp.afd_tree = generateAFD(regexExp);
    }

    public static List<AFDNode> generateAFD(RegexExpression regex) {

        System.out.println("Generating AFD for:");
        System.out.println("###################################################");
        System.out.println("HEAD First Pos");
        System.out.println(regex.treeHead.firstPos);
        System.out.println("###################################################");
        System.out.println("Next Table");
        for (int i = 0; i < regex.nextTable.size(); i++) {
            System.out.printf("%s: %s\n",regex.leavesList.get(i).label,regex.nextTable.get(i));
        }
        System.out.println(regex.nextTable);
        System.out.println("###################################################");
        System.out.println("Leaves");
        System.out.println(regex.leavesList);

        List<AFDNode> nodeList = new ArrayList<>();
        AFDNode node0 = new AFDNode(0);
        nodeList.add(node0);

        int nextGroupToAnalyze = 0;
        while (nextGroupToAnalyze != nodeList.size()) {


            //Make a list (elementsList) with all elements in this group

            //Make a string that will contain a list
            //Iterate all elements in group and join same-char parts (be sure to check for nulls) (remove from elementsList all elements involved)
            //Remove dups and order

            //Iterate nodeList to check if group already exist. If not, create one and insert it in nodeList
            //add transition to current AFDNode

            nextGroupToAnalyze++;

        }

        return nodeList;

    }


    public static List<String> specialOperatorsExpansion(List<String> list) {


        boolean chainWasUpdated = true;

        //This can be done more efficiently by having a lastKnownExpansionIndex to skip elements
        expansion: while (chainWasUpdated) {
            chainWasUpdated = false;

           for (int i = 0; i < list.size(); i++) {
                String s = list.get(i);

                //Converting +[expr] to .*[expr][expr]
                //length of [expr] is determined by number of .| before encountering a operand (+ one)
                if (s.equals("+")) {
                    dprint("Encountered +, expanding....");
                    chainWasUpdated = true;
                    int _i = i+1;
                    int count = 1; //for including exclusive 2nd index of sublist
                    while (count > 0) {
                        if (list.get(_i).equals(".") || list.get(_i).equals("|")) {
                            count++;
                        }
                        else {
                            count--;
                        }
                        _i++;
                    }

                    List<String> result = new ArrayList<>(list.subList(0,i)); //stuff before
                    result.add("."); result.add("*"); //expansion
                    result.addAll(list.subList(i+1,_i)); // elements included in expansion
                    result.addAll(list.subList(i+1,list.size())); // stuff after
                    list = result; //Another list should be necessary here? idk.
                    continue expansion;
                }

                //Converting ?[expr] to |[expr]{epsilon}
                //length of [expr] is determined by number of .| before encountering a operand (+ one)
                if (s.equals("?")) {
                    dprint("Encountered ?, expanding....");
                    chainWasUpdated = true;
                    int _i = i+1;
                    int count = 1; //for including exclusive 2nd index of sublist
                    while (count > 0) {
                        if (list.get(_i).equals(".") || list.get(_i).equals("|")) {
                            count++;
                        }
                        else {
                            count--;
                        }
                        _i++;
                    }
                    List<String> result = list.subList(0,i); //stuff before
                    result.add("|"); //expansion
                    result.addAll(list.subList(i+1,_i)); // elements included in expansion
                    result.add("{epsilon}"); //expansion
                    result.addAll(list.subList(_i,list.size())); // stuff after
                    list = result;
                    continue expansion;
                }
            }
        }
        return list;
    }

    public static void logRegexTest(String regexName, String str) {

    }

    public static void logToken(String id, String lex, int row, int column) {
        tokenList.add(new Token(id, lex, row, column));
    }

    public static void logLexicError(String lex, int row, int column) {
        lexicErrorList.add(new LexicError(lex, row, column));
        cprintln(String.format("ERROR LEXICO PRODUCIDO:%s en f:%d c:%d", lex, row, column));
    }

    public static void logSyntacticError(String lex, String expectedInternalId, String expectedElements, int row, int column) {
        System.out.printf("Token <%s> inválido, %s --> %s en f:%d row %d",
                lex, expectedInternalId, expectedElements, row, column);
        SyntacticError err = new SyntacticError("",expectedInternalId,expectedElements,row,column);
        syntacticErrorList.add(err);
        cprintln(String.format("ERROR SINTACTICO PRODUCIDO:%s en f:%d c:%d", err, row, column));
    }


    public static String removeStringListDuplicates(String list) {
        ArrayList<String> newList = new ArrayList<>();
        for (String element : list.split(",")) {
            if (!newList.contains(element)) {
                newList.add(element);
            }
        }
        String f = String.join(",", newList);
        return f;
    }


    public static String orderStringArray(String str) {

        String[] arr = str.split(",");
        int[] intArr = new int[arr.length];

        for (int i = 0; i < arr.length; i++) {
            intArr[i] = Integer.parseInt(arr[i]);
        }
        Arrays.sort(intArr);
        for (int i = 0; i < arr.length; i++) {
            arr[i] = Integer.toString(intArr[i]);
        }
        String f = String.join(",", arr);
        return f;
    }


    public static String padRight(String s, int n) {
        return String.format("%-" + n + "s", s);
    }

    public static String padLeft(String s, int n) {
        return String.format("%" + n + "s", s);
    }

    public static String centerString (String s, int n) {
        return String.format("%-" + n  + "s", String.format("%" + (s.length() + (n - s.length()) / 2) + "s", s));
    }

    public static void cprintln(Object s) {
        mainWindow.outputTextArea.setText(mainWindow.outputTextArea.getText() + s.toString() + "\n");
    }

    public static void dprint(Object s) {if (debug) {System.out.println(s);}}

}
