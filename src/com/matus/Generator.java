package com.matus;

import com.matus.elements.afd.AFDNode;
import com.matus.elements.afd.NodeTree;
import com.matus.elements.afn.AFNNode;
import com.matus.elements.afn.AFNStructure;
import com.matus.elements.afn.AFNTransition;
import com.matus.elements.language.LexicError;
import com.matus.elements.language.RegexExpression;
import com.matus.elements.language.RegexTest;
import com.matus.elements.language.SyntacticError;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.sql.SQLOutput;
import java.util.*;
import java.util.stream.Collectors;

public class Generator {


    public static boolean generateAFD(RegexExpression regexExp, String rawData, int row, int column) {

        //String rawData = regexExp.pattern; deprecated

        //*Parse this baby
        String graphvizString = "digraph {\nnodesep=3;\n margin=1;\n";
        int nodesCreated = 0;

        List<String> tmpList = new ArrayList<>(); //for separating elements into chars (except previous groups and special chars)

        Stack<NodeTree> stack = new Stack<>(); //parsed elements go here

        List<NodeTree> leafList = new ArrayList<>();
        regexExp.leavesList = leafList;

        rawData = ".<->" + rawData + "<->#";
        System.out.println("raw data is " + rawData);
        String[] _arr = rawData.split("<->");
        for (String s : _arr) {
            if (s.contains("{")) { //FIXME | s.contains("\"")
                //System.out.println("Skipping separation of group:" + s);
                System.out.println("Adding to used in regex:");
                System.out.println(s);
                tmpList.add(s);
                Main.usedGroupsInRegex.put(s, true);
                continue;
            }

            if ( s.contains("\"") ) { //FIXME | s.contains("\"")
                String b = s.substring(1,s.length()-1);
                int i = 0;
                char[] chars = b.toCharArray();

                List<String> tmpList2 = new ArrayList<>(); //expansion of "abcd" to corresponding elements


                while (i < chars.length) {
                    char theChar = chars[i];
                    if (theChar == '\\') {
                        tmpList2.add("\"" + theChar + chars[i+1] + "\"");
                        i++;
                    }
                    else {
                        tmpList2.add("\"" + theChar + "\"");
                    }
                    i++;
                }

                for (int j = 0; j < tmpList2.size()-1; j++) {
                    tmpList.add(".");
                }
                tmpList.addAll(tmpList2);

                //System.out.println("Skipping separation of group:" + s);
                continue;
            }

            //If id element (multiple single elements concatenated):
            for (int j = 0; j < s.length(); j++) {
                String subs = s.substring(j, j + 1);
                tmpList.add(subs);
            }
        }

        Main.dprint("Before expansion:");
        Main.dprint(tmpList);
        //Expansion of + and ?
        tmpList = Generator.specialOperatorsExpansion(new ArrayList<>(tmpList)); //before stuff goes wild
        //Main.dprint("After expansion:");
        //Main.dprint(tmpList);

        for (int i = tmpList.size()-1; i >= 0 ; i--) { //Reverse loop array
            String s = tmpList.get(i);
            //Main.dprint("Now analyzing " + s);
            //Main.dprint("Current stack is" + stack);
            switch (s) {
                case "." -> {
                    //stack underflow
                    if (stack.size() < 2) {
                        String f = String.format("Expresión REGEX invalida (operación . definida sin elementos previos): %s", rawData);
                        Main.cprintln(String.format("%s (f:%s c:%s)", f, row, column));
                        Main.logSyntacticError(rawData, "conj", f, row, column);
                        return false;
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
                    if (a.nullable) parent.firstPos = Utils.orderStringArray(Utils.removeStringListDuplicates(a.firstPos + "," + b.firstPos));
                    else parent.firstPos = a.firstPos;

                    //Last Pos
                    if (b.nullable) parent.lastPos = Utils.orderStringArray(Utils.removeStringListDuplicates(a.lastPos + "," + b.lastPos));
                    else parent.lastPos = b.lastPos;
                    stack.add(parent); // (a operand b)

                    //AFD Tree method next table
                    //Main.dprint("adding next for");
                    //Main.dprint(a.lastPos);
                    //Main.dprint(b.firstPos);
                    for (String ultPosC1 : a.lastPos.split(",")) {
                        for (String primPosC2 : b.firstPos.split(",")) {

                            //Main.dprint("A ultposc1:%s le sigue primposc2:%s\n",ultPosC1, primPosC2);

                            String prev = regexExp.nextTable.get(Integer.parseInt(ultPosC1)-1);
                            String f = "";
                            if (!prev.equals("")) f = prev + ",";
                            f += primPosC2;
                            regexExp.nextTable.set(Integer.parseInt(ultPosC1)-1, f);
                        }
                        //Remove duplicates and sort
                        String f = regexExp.nextTable.get(Integer.parseInt(ultPosC1)-1);
                        f = Utils.removeStringListDuplicates(f);
                        f = Utils.orderStringArray(f);
                        regexExp.nextTable.set(Integer.parseInt(ultPosC1)-1,f);
                    }

                    //Graphviz

                    graphvizString += String.format(
                            "\"node-%s\"[fixedsize=true,label=<<TABLE CELLSPACING=\"2\" CELLPADDING=\"2\" BORDER=\"0\">" +
                                    "<TR><TD></TD><TD>%s</TD><TD></TD></TR><TR><TD></TD><TD></TD><TD></TD></TR>" +
                                    "<TR><TD>%s</TD><TD>%s</TD><TD>%s</TD></TR><TR><TD></TD><TD>%s</TD><TD></TD></TR></TABLE>>]\n"
                            , nodesCreated, parent.nullable? "V":"F", parent.firstPos, Utils.centerString(parent.label,16), parent.lastPos, " ");

                    graphvizString += String.format("\"node-%s\" -> \"node-%s\" \n \"node-%s\" -> \"node-%s\"\n",nodesCreated, b.orderInTree, nodesCreated, a.orderInTree);
                    nodesCreated++;

                }
                case "|" -> {
                    //stack underflow
                    if (stack.size() < 2) {
                        String f = String.format("Expresión REGEX invalida (operación | definida sin elementos previos): %s", rawData);
                        Main.cprintln(String.format("%s (f:%s c:%s)", f, row, column));
                        Main.logSyntacticError(rawData, "conj", f, row, column);
                        return false;
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
                    parent.firstPos = Utils.removeStringListDuplicates(a.firstPos + "," + b.firstPos);

                    //Last Pos
                    parent.lastPos = Utils.removeStringListDuplicates(a.lastPos + "," + b.lastPos);
                    stack.add(parent); // (a operand b)


                    graphvizString += String.format(
                            "\"node-%s\"[fixedsize=true,label=<<TABLE CELLSPACING=\"2\" CELLPADDING=\"2\" BORDER=\"0\">" +
                                    "<TR><TD></TD><TD>%s</TD><TD></TD></TR><TR><TD></TD><TD></TD><TD></TD></TR>" +
                                    "<TR><TD>%s</TD><TD>%s</TD><TD>%s</TD></TR><TR><TD></TD><TD>%s</TD><TD></TD></TR></TABLE>>]\n"
                            , nodesCreated, parent.nullable? "V":"F", parent.firstPos, Utils.centerString(parent.label,16), parent.lastPos, " ");


                    //Graphviz
                    graphvizString += String.format("\"node-%s\" -> \"node-%s\" \n \"node-%s\" -> \"node-%s\"\n",nodesCreated, a.orderInTree, nodesCreated, b.orderInTree);
                    nodesCreated++;
                }

                case "*" -> {
                    if (stack.size() < 1) {
                        String f = String.format("Expresión REGEX invalida (operación * definida sin elemento previo): %s", rawData);
                        Main.cprintln(String.format("%s (f:%s c:%s)", f, row, column));
                        Main.logSyntacticError(rawData, "conj", f, row, column);
                        return false;
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


                    //Main.dprint("Element for next table:");
                    //Main.dprint(Arrays.toString(a.lastPos.split(",")));
                    //Main.dprint(Arrays.toString(a.firstPos.split(",")));

                    //AFD Tree method next table
                    for (String ultPosC1 : a.lastPos.split(",")) {
                        for (String primPosC1 : a.firstPos.split(",")) {
                            //Main.dprint(String.format("A ultposc1:%s le sigue primposc1:%s\n",ultPosC1, primPosC1));
                            String prev = regexExp.nextTable.get(Integer.parseInt(ultPosC1)-1);
                            String f = "";
                            if (!prev.equals("")) f = prev + ",";
                            f += primPosC1;
                            regexExp.nextTable.set(Integer.parseInt(ultPosC1)-1, f);
                        }
                        //Remove duplicates
                        String f = regexExp.nextTable.get(Integer.parseInt(ultPosC1)-1);
                        f = Utils.removeStringListDuplicates(f);
                        //Main.dprint("Before call for removing dups");
                        //Main.dprint(f);
                        regexExp.nextTable.set(Integer.parseInt(ultPosC1)-1,f);
                    }

                    //Graphviz
                    graphvizString += String.format(
                            "\"node-%s\"[fixedsize=true,label=<<TABLE CELLSPACING=\"2\" CELLPADDING=\"2\" BORDER=\"0\">" +
                                    "<TR><TD></TD><TD>%s</TD><TD></TD></TR><TR><TD></TD><TD></TD><TD></TD></TR>" +
                                    "<TR><TD>%s</TD><TD>%s</TD><TD>%s</TD></TR><TR><TD></TD><TD>%s</TD><TD></TD></TR></TABLE>>]\n"
                            , nodesCreated, "V", parent.firstPos, Utils.centerString(parent.label,16), parent.lastPos, " ");

                    graphvizString += String.format("\"node-%s\" -> \"node-%s\"[dir=none]\n",nodesCreated, a.orderInTree);
                    nodesCreated++;
                }

                // + and ? are expanded in specialOperatorsExpansion() before this loop.

                default -> { //Leaf

                    Main.dprint("Leaf encountered with " + s);
                    String _s = s;

                    if (s.equals("\"┤\"")) {
                        System.out.println("Refactoring ┤ special cheat");
                        _s = "\"\\\"\"";
                        System.out.println(_s);
                    }

                    if (s.equals("\"Á\"")) {
                        System.out.println("Refactoring Á special cheat");
                        _s = "\"\\n\"";
                        System.out.println(_s);
                    }

                    if (s.equals("\"Â\"")) {
                        System.out.println("Refactoring Â special cheat");
                        _s = "\"\\'\"";
                        System.out.println(_s);
                    }

                    if (s.equals("#")) {
                        _s = "\"#\"";
                    }






                    boolean nullable = _s.equals("{epsilon}");
                    System.out.println("Is epsilon?" + nullable);

                    NodeTree theNode;
                    if (!nullable) { //epsilon is not enumerated
                        int leafCount = leafList.size() + 1;
                        //*(String label, String firstPos, String lastPos, boolean nullable, int number, boolean isEpsilon)
                        theNode = new NodeTree(_s,Integer.toString(leafCount), Integer.toString(leafCount), false, Integer.toString(leafCount), false, nodesCreated);
                        leafList.add(theNode);
                        regexExp.nextTable.add("");
                    }
                    else {
                        theNode = new NodeTree("ɛ","", "", true, "", true, nodesCreated);
                    }

                    stack.add(theNode);
                    //Graphviz
                    graphvizString += String.format(
                            "\"node-%s\"[fixedsize=true,label=<<TABLE CELLSPACING=\"2\" CELLPADDING=\"2\" BORDER=\"0\">" +
                                    "<TR><TD></TD><TD>%s</TD><TD></TD></TR><TR><TD></TD><TD></TD><TD></TD></TR>" +
                                    "<TR><TD>%s</TD><TD>%s</TD><TD>%s</TD></TR><TR><TD></TD><TD>%s</TD><TD></TD></TR></TABLE>>]\n"
                            , nodesCreated, nullable? "V":"F", theNode.firstPos, Utils.centerString(_s,14), theNode.lastPos, theNode.number);
                    nodesCreated++;
                }
            }
            //Main.dprint("!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!");
            //Main.dprint("!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!");
            //Main.dprint(graphvizString + "\n}");
        }

        graphvizString += "\n}";
        Main.dprint(graphvizString);
        regexExp.expressionTreeGraphviz = graphvizString;

        System.out.println("RESULTADO:" + stack.peek().label);
        System.out.println(stack.size());

        if (stack.size() != 1) {//TODO this should always be 1
            String f = String.format("Expresión REGEX invalida (mas operandos que operaciones): %s", rawData);
            Main.cprintln(String.format("%s (f:%s c:%s)", f, row, column));
            Main.logSyntacticError(rawData, "conj", f, row, column);
            return false;
        }
        regexExp.treeHead = stack.peek();
        Generator.generateStates(regexExp);
        return true;
    }

    public static void generateStates(RegexExpression regex) {

        System.out.println("###################################################");
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
        node0.belongingElements = List.of(regex.treeHead.firstPos.split(",")); //note: inmutable
        nodeList.add(node0);

        int nextGroupToAnalyze = 0;
        while (nextGroupToAnalyze != nodeList.size()) {

            //Make a list (elementsList) with all elements in this group
            List<String> elementList = new ArrayList<>(nodeList.get(nextGroupToAnalyze).belongingElements);

            //Main.dprint(".............................................................................");
            //Main.dprintString.format(("Now analyzing transitions for S%d->%s\n", nextGroupToAnalyze, elementList));
            //Main.dprint(String.format("#Elements to analyze:%s\n", elementList.size()));

            //Iterate all elements in group and join same-char parts (be sure to check for nulls) (remove from elementsList all elements involved)
            while (elementList.size() != 0) {

                //If element has no nexts (idk why an empty string is being added here, so had to check
                if (elementList.get(0).equals("")) {
                    elementList.remove(0);
                    continue;
                }

                List<String> sameElements = new ArrayList<>(Arrays.asList(elementList.get(0)));
                List<Integer> foundSameIndexes = new ArrayList<>();
                //foundSameIndexes.add(0);
                //Remove dups and order
                String firstRepresent = regex.leavesList.get(Integer.parseInt(elementList.get(0))-1).label;
                //Main.dprint("Representation to match:" + firstRepresent);

                if (firstRepresent.contains("#")) {
                    nodeList.get(nextGroupToAnalyze).isAcceptState = true;
                    elementList.remove(0);
                    continue;
                }

                for (int i = 0; i < elementList.size(); i++) {
                    String next = elementList.get(i);
                    String represents = regex.leavesList.get(Integer.parseInt(next)-1).label;
                    //Main.dprint(String.format("%s: has element %s\n",next, represents));
                    //First is always already grabbed
                    if (Objects.equals(represents, firstRepresent)) {
                        //Main.dprint("same representation!");
                        //Main.dprint(represents);
                        //Main.dprint(firstRepresent);
                        //Main.dprint("before adding");
                        //Main.dprint(sameElements);
                        //Main.dprint("after adding");
                        sameElements.add(next);
                        foundSameIndexes.add(i);
                        //Main.dprint(sameElements);
                    }
                }

                //Main.dprint("##############################################");

                //Descending order of indexes to remove for it to not shift indexes while deleting.
                Collections.sort(foundSameIndexes);
                Collections.reverse(foundSameIndexes);
                //Main.dprint(String.format("elementList before deletion:%s\n", elementList));
                //Main.dprintString.format("indexes to remove:%s\n", foundSameIndexes));
                for (int i = 0; i < foundSameIndexes.size(); i++) { //FIXME is enhanced loop ordered? idk maybe later when it already works
                    elementList.remove((int)foundSameIndexes.get(i));
                }

                //Main.dprint("elementList after deletion:" + elementList);
                //Main.dprint("##############################################");

                sameElements = sameElements.stream()
                        .distinct()
                        .collect(Collectors.toList());
                Collections.sort(sameElements);

                //Main.dprint("Elements without dups are:");
                //Main.dprint(sameElements);

                List<String> resultingElements = new ArrayList<>();
                for (String sameElement : sameElements) {
                    //Main.dprint("Now adding nexts of leaf number " + sameElement);
                    resultingElements.addAll(new ArrayList<>(List.of(regex.nextTable.get(Integer.parseInt(sameElement)-1).split(","))));
                }

                resultingElements = resultingElements.stream()
                        .distinct()
                        .collect(Collectors.toList());
                Collections.sort(resultingElements);


                //Main.dprint("Group of nexts to compare in existing states:");
                //Main.dprint(resultingElements);

                boolean found = false;
                //Iterate nodeList to check if group already exist. If not, create one and insert it in nodeList

                for (AFDNode afdNode : nodeList) {
                    if (afdNode.belongingElements.equals(resultingElements)) {
                        //Main.dprint(String.format("Group %s already exists!, only doing transition for representation %s\n", resultingElements, firstRepresent));
                        found = true;
                        //add transition to current AFDNode
                        nodeList.get(nextGroupToAnalyze).transitions.put(firstRepresent, afdNode);
                    }
                }
                if (!found) {
                    //Main.dprint(String.format("Group %s doesn't exists!, creating and doing transition for representation %s\n", resultingElements, firstRepresent));
                    AFDNode newNode = new AFDNode(nodeList.size());
                    newNode.belongingElements = resultingElements;
                    //add transition to current AFDNode
                    nodeList.get(nextGroupToAnalyze).transitions.put(firstRepresent, newNode);
                    nodeList.add(newNode);
                }
            }
            nextGroupToAnalyze++;
        }

        String graphviz = "digraph {\nrankdir=LR;\n margin=1;\n";

        System.out.println("Resulting AFD is:");
        for (AFDNode afdNode : nodeList) {
            System.out.println("#########################");
            System.out.printf("S%s %s\n", afdNode.number, afdNode.isAcceptState? "(accept)" : "");


            graphviz += String.format("\"state-%s\"[shape=%s label=\"S%s\"]\n", afdNode.number, afdNode.isAcceptState? "doublecircle":"circle", afdNode.number);

            for (var entry : afdNode.transitions.entrySet()) {
                System.out.printf("Trans[S%s,%s] = S%s\n", afdNode.number, entry.getKey(),entry.getValue().number);
                String s = entry.getKey();
                String _s = s;


                boolean handled = false;

                if (s.equals("\"\\\"\"")) {
                    System.out.println("Refactoring ┤ special cheat back to \"");
                    _s = "\\\" \\\\\\\"  \\\"";
                    System.out.println(_s);
                    handled = true;
                }

                if (s.equals("\"\\n\"")) {
                    System.out.println("Refactoring Á special cheat back to n");
                    _s = "\\\" \\\\n  \\\"";
                    System.out.println(_s);
                    handled = true;
                }

                if (s.equals("\"\\'\"")) {
                    System.out.println("Refactoring Â special cheat back to '");
                    _s = "\\\" \\\\'  \\\"";
                    System.out.println(_s);
                    handled = true;
                }

                if (s.equals("#")) {
                    _s = "\"#\"";
                    handled = true;
                }


                if (_s.contains("\"") && !handled) _s = "\\" + _s.substring(0,_s.length()-1) + "\\" + _s.substring(_s.length()-1);


                String transition = String.format("\"state-%s\" -> \"state-%s\" [label=\"%s\"]\n", afdNode.number, entry.getValue().number, _s);
                System.out.println(transition);


                graphviz += transition;
            }
            System.out.println("#########################");

        }
        graphviz += "\n}";
        System.out.println("#########################");
        System.out.println(graphviz);
        System.out.println("#########################");
        regex.afdGraphviz = graphviz;
        regex.afd_nodes = nodeList;
    }

    public static boolean generateAFN(RegexExpression regexExp, String rawData, int row, int column) {

        System.out.println("Performing AFN parsing");
        //String rawData = regexExp.pattern; deprecated

        //*Parse this baby
        int nodesCreated = 0;

        List<String> tmpList = new ArrayList<>(); //for separating elements into chars (except previous groups and special chars)

        Stack<AFNStructure> stack = new Stack<>(); //parsed elements go here

        String[] _arr = rawData.split("<->");
        for (String s : _arr) {
            if (s.contains("{") | s.contains("\"")) { //FIXME | s.contains("\"")
                //System.out.println("Skipping separation of group:" + s);
                tmpList.add(s);
                continue;
            }
            //If id element (multiple single elements concatenated):
            for (int j = 0; j < s.length(); j++) {
                String subs = s.substring(j, j + 1);
                tmpList.add(subs);
            }
        }

        //Main.dprint("Before expansion:");
        //Main.dprint(tmpList);

        //Expansion of + and ? //FIXME in theory not necessary, but equivalent and reduces switch cases
        tmpList = Generator.specialOperatorsExpansion(new ArrayList<>(tmpList)); //before stuff goes wild

        //Main.dprint("After expansion:");
        //Main.dprint(tmpList);

        for (int i = tmpList.size()-1; i >= 0 ; i--) { //Reverse loop array
            String s = tmpList.get(i);
            //Main.dprint("Now analyzing " + s);
            //Main.dprint("Current stack is" + stack);
            switch (s) {

                case "." -> {

                    //TODO be sure to grab transition symbol before removing.
                    //stack underflow
                    if (stack.size() < 2) {
                        String f = String.format("Expresión REGEX invalida (operación . definida sin elementos previos): %s", rawData);
                        Main.cprintln(String.format("%s (f:%s c:%s)", f, row, column));
                        Main.logSyntacticError(rawData, "conj", f, row, column);
                        return false;
                    }

                    AFNStructure structure = new AFNStructure();

                    AFNStructure a = stack.pop();
                    AFNStructure b = stack.pop();

                    //Adopt all nodes
                    structure.nodes.addAll(a.nodes);
                    structure.nodes.addAll(b.nodes);
                    structure.nodes.add(a.acceptanceNode);

                    //Transfer b.initial to a.final transitions
                    a.acceptanceNode.transitions.addAll(b.initialNode.transitions);

                    //Update initial
                    structure.initialNode = a.initialNode;
                    //Update final
                    structure.acceptanceNode = b.acceptanceNode;

                    stack.add(structure);

                    System.out.println("#############################################");
                    System.out.println("Generated . with #nodes:" + structure.nodes.size());
                    System.out.println("#Initial:" + structure.initialNode.number);
                    System.out.println("#Accept:" + structure.acceptanceNode.number);
                    System.out.println("#############################################");

                }

                case "|" -> {


                    //TODO be sure to grab transition symbol before removing.
                    //stack underflow
                    if (stack.size() < 2) {
                        String f = String.format("Expresión REGEX invalida (operación | definida sin elementos previos): %s", rawData);
                        Main.cprintln(String.format("%s (f:%s c:%s)", f, row, column));
                        Main.logSyntacticError(rawData, "conj", f, row, column);
                        return false;
                    }

                    AFNStructure structure = new AFNStructure();
                    AFNNode initial = new AFNNode();
                    initial.number = nodesCreated;
                    nodesCreated++;
                    AFNNode acceptance = new AFNNode();
                    acceptance.number = nodesCreated;
                    nodesCreated++;
                    structure.initialNode = initial;
                    structure.acceptanceNode = acceptance;

                    AFNStructure a = stack.pop();
                    AFNStructure b = stack.pop();

                    structure.nodes.addAll(a.nodes);
                    structure.nodes.addAll(b.nodes);
                    structure.nodes.add(a.initialNode);
                    structure.nodes.add(a.acceptanceNode);
                    structure.nodes.add(b.initialNode);
                    structure.nodes.add(b.acceptanceNode);

                    //Left
                    initial.transitions.add(new AFNTransition("{e}", a.initialNode, "normal"));
                    initial.transitions.add(new AFNTransition("{e}", b.initialNode, "normal"));

                    //Right
                    a.acceptanceNode.transitions.add(new AFNTransition("{e}", acceptance, "normal"));
                    b.acceptanceNode.transitions.add(new AFNTransition("{e}", acceptance, "normal"));

                    stack.add(structure);

                }

                case "*" -> {


                    if (stack.size() < 1) {
                        String f = String.format("Expresión REGEX invalida (operación * definida sin elemento previo): %s", rawData);
                        Main.cprintln(String.format("%s (f:%s c:%s)", f, row, column));
                        Main.logSyntacticError(rawData, "conj", f, row, column);
                        return false;
                    }

                    System.out.println("a");
                    AFNStructure structure = new AFNStructure();
                    AFNNode initial = new AFNNode();
                    initial.number = nodesCreated;
                    nodesCreated++;
                    AFNNode acceptance = new AFNNode();
                    acceptance.number = nodesCreated;
                    nodesCreated++;
                    structure.initialNode = initial;
                    structure.acceptanceNode = acceptance;

                    AFNStructure a = stack.pop();

                    structure.nodes.addAll(a.nodes);
                    structure.nodes.add(a.initialNode);
                    structure.nodes.add(a.acceptanceNode);

                    //Left
                    initial.transitions.add(new AFNTransition("{e}", a.initialNode, "normal"));
                    initial.transitions.add(new AFNTransition("{e}", acceptance, "normal"));

                    //Right
                    a.acceptanceNode.transitions.add(new AFNTransition("{e}", acceptance, "normal"));
                    a.acceptanceNode.transitions.add(new AFNTransition("{e}", a.initialNode, "normal"));

                    stack.add(structure);

                }
                //TODO ? + expanded


                default -> { //Leaf

                    AFNStructure structure = new AFNStructure();
                    //First
                    AFNNode initialNode = new AFNNode(); //label of nodesCreated
                    initialNode.number = nodesCreated;
                    nodesCreated++;
                    //Second
                    AFNNode acceptanceNode = new AFNNode(); //label of nodesCreated
                    acceptanceNode.number = nodesCreated;
                    initialNode.transitions.add(new AFNTransition(s, acceptanceNode, "normal"));
                    nodesCreated++;

                    structure.initialNode = initialNode;
                    structure.acceptanceNode = acceptanceNode;
                    stack.add(structure);
                    System.out.println("#############################################");
                    System.out.println("Generated leaf with #nodes:" + structure.nodes.size());
                    System.out.println("#Initial:" + structure.initialNode.number);
                    System.out.println("#Accept:" + structure.acceptanceNode.number);
                    System.out.println("#############################################");

                }
            }
        }


        //System.out.println("RESULTADO:" + stack.peek().label);
        System.out.println(stack.size());

        if (stack.size() != 1) {//TODO this should always be 1
            String f = String.format("Expresión REGEX invalida (mas operandos que operaciones): %s", rawData);
            Main.cprintln(String.format("%s (f:%s c:%s)", f, row, column));
            Main.logSyntacticError(rawData, "conj", f, row, column);
            return false;
        }

        AFNStructure result = stack.peek(); //pop? idk

        System.out.printf("Resulting AFN has %d nodes\n", result.nodes.size() + 2/*initial and accept*/);


        //*Encode this baby
        String graphvizString = "digraph {\nrankdir=LR;\n margin=1;\n";

        //*Initial Node and it's transitions
        graphvizString += String.format("\"node-%s\"[label=\"\"]\n", result.initialNode.number);

        for (AFNTransition transition : result.initialNode.transitions) {
            String lbl = transition.symbol.equals("{e}") ? "ε" : transition.symbol;
            if (lbl.contains("\"") && !lbl.contains("┤") && !lbl.contains("Á") && !lbl.contains("Â")) {
                System.out.println("sustitution for \" without specials");
                lbl = "\\" + lbl.substring(0, lbl.length()-1) + "\\\"";
            }
            lbl = lbl.replaceAll("\"┤\"", "\\\\\" \\\\\\\\\\\\\" \\\\\"");
            lbl = lbl.replaceAll("\"Á\"", "\\\\\" \\\\\\\\n \\\\\"");
            lbl = lbl.replaceAll("\"Â\"", "\\\\\" \\\\\\\\' \\\\\"");
            String trans = String.format("\"node-%s\" -> \"node-%s\"[label=\"%s\"]\n", result.initialNode.number, transition.destination.number, lbl);
            System.out.println(trans);
            graphvizString += trans;
        }

        //*Nodes and transitions (including transits to final)
        for (AFNNode node : result.nodes) {
            graphvizString += String.format("\"node-%s\"[label=\"\"]\n", node.number);
            for (AFNTransition transition : node.transitions) {
                String lbl = transition.symbol.equals("{e}") ? "ε" : transition.symbol;
                System.out.println(lbl);
                if (lbl.contains("\"") && !lbl.contains("┤") && !lbl.contains("Á") && !lbl.contains("Â")) {
                    System.out.println("sustitution for \" without specials");
                    lbl = "\\" + lbl.substring(0, lbl.length()-1) + "\\\"";
                }
                System.out.println(lbl);

                lbl = lbl.replaceAll("\"┤\"", "\\\\\" \\\\\\\\\\\\\" \\\\\""); //why? idk, i hate my life
                lbl = lbl.replaceAll("\"Á\"", "\\\\\" \\\\\\\\n \\\\\""); //and all the time i wasted here
                lbl = lbl.replaceAll("\"Â\"", "\\\\\" \\\\\\\\' \\\\\"");
                System.out.println(lbl);

                String trans = String.format("\"node-%s\" -> \"node-%s\"[label=\"%s\"]\n", node.number, transition.destination.number, lbl);
                System.out.println(trans);
                graphvizString += trans;
            }
        }



        //*Accept Node (without transitions, as it shouldn't have?)
        graphvizString += String.format("\"node-%s\"[label=\"\" shape=\"doublecircle\"]\n}", result.acceptanceNode.number);


        Main.dprint("Generated element for AFN:");
        Main.dprint(graphvizString);

        regexExp.afnStructure = result;
        regexExp.afnGraphviz = graphvizString;

        return true;
    }

    public static void generateAutomats () {
        //I mean, more like generate files xd


        Main.runCMD("rmdir /s /q REPORTES");

        try {
            Thread.sleep(500);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }


        String[] names = Main.regexList.keySet().toArray(new String[0]);
        System.out.println(Arrays.toString(names));



        String[][] nextData;
        String[][] transitionData;
        String[][] errorData;


        System.out.println("Lexic:");
        System.out.println(Main.lexicErrorList.size());
        System.out.println("Sintac:");
        System.out.println(Main.syntacticErrorList.size());


        if ( !Main.stopLexOnError || (Main.lexicErrorList.size() == 0 && Main.syntacticErrorList.size() == 0)) {

            System.out.println("No errors, exporting...");

            for (var regexEntry : Main.regexList.entrySet()) {

                String regexName = regexEntry.getKey();
                RegexExpression regex = regexEntry.getValue();

                //Tree
                FileHandler.writeToFile(String.format("./REPORTES/ARBOLES_%s/%s.dot", Main.carnet, regexName ),
                        regex.expressionTreeGraphviz, false);
                Main.runCMD(String.format("dot -Tpng REPORTES/ARBOLES_%s/%s.dot -o REPORTES/ARBOLES_%s/%s.png",
                        Main.carnet, regexName, Main.carnet, regexName));

                //AFND
                FileHandler.writeToFile(String.format("./REPORTES/AFND_%s/%s.dot", Main.carnet, regexName ),
                        regex.afnGraphviz, false);
                Main.runCMD(String.format("dot -Tpng REPORTES/AFND_%s/%s.dot -o REPORTES/AFND_%s/%s.png",
                        Main.carnet, regexName, Main.carnet, regexName));

                //Nexts
                nextData = new String[regex.nextTable.size()][3];

                for (int i = 0; i < regex.nextTable.size(); i++) {
                    nextData[i][0] = regex.leavesList.get(i).number;
                    nextData[i][1] = regex.leavesList.get(i).label;
                    nextData[i][2] = regex.nextTable.get(i).equals("") ? "--" : regex.nextTable.get(i);
                }


                String nextHTML = Utils.arrayToHTMLTable( "Tabla Siguientes",new String[]{"Numero", "Simbolo", "Siguientes"}, nextData);
                FileHandler.writeToFile(String.format("./REPORTES/SIGUIENTES_%s/%s.html", Main.carnet, regexName ),
                        nextHTML, false);

                //Transitions
                List<String[]> _transitionData = new ArrayList<>();
                for (int i = 0; i < regex.afd_nodes.size(); i++) {
                    AFDNode node = regex.afd_nodes.get(i);
                    for (var transitionEntry : node.transitions.entrySet()) {
                        _transitionData.add(new String[] {"S" + node.number, "S" + transitionEntry.getValue().number, transitionEntry.getKey()});
                        //System.out.println(transitionEntry.getKey() + "/" + transitionEntry.getValue());
                    }
                }
                transitionData = _transitionData.toArray(new String[0][3]);

                String transitionHTML = Utils.arrayToHTMLTable( "Tabla Transiciones",new String[]{"Estado", "Transicion", "Simbolo"}, transitionData);
                FileHandler.writeToFile(String.format("./REPORTES/TRANSICIONES_%s/%s.html", Main.carnet, regexName ),
                        transitionHTML, false);

                //AFD
                FileHandler.writeToFile(String.format("REPORTES/AFD_%s/%s.dot",
                        Main.carnet, regexEntry.getKey() ), regexEntry.getValue().afdGraphviz, false);
                Main.runCMD(String.format("dot -Tpng REPORTES/AFD_%s/%s.dot -o REPORTES/AFD_%s/%s.png",
                        Main.carnet, regexEntry.getKey(), Main.carnet, regexEntry.getKey()));
            }


            String outputJson = "[";


            for (RegexTest regexTest : Main.regexTestList) {
                String str = regexTest.str;
                System.out.println("Json before curation");
                System.out.println(str);
                str = str.replaceAll("┤", " \\\\\\\\\\\\\"");
                str = str.replaceAll("Á", " \\\\\\\\n ");
                str = str.replaceAll("Â", " \\\\\\\\' ");
                System.out.println("Json after curation");
                System.out.println(str);

                outputJson += String.format("{%n\"Valor\":\"%s\", %n  \"ExpresionRegular\":\"%s\",%n \"Resultado\":\"%s\"%n},%n",
                        str, regexTest.regexNameToRun, regexTest.isValid? "Cadena Valida" : "Cadena Invalida");
            }

            outputJson = outputJson.substring(0,outputJson.length()-1); //Remove last ,
            outputJson += "]";

            System.out.println("RESULTING JSON");
            System.out.println(outputJson);

            FileHandler.writeToFile(String.format("REPORTES/SALIDAS_%s/validaciones.json",
                    Main.carnet ), outputJson, false);
        }
        List<String[]> _errorData = new ArrayList<>();
        for (LexicError lexic:Main.lexicErrorList) {
            _errorData.add(new String[]{"Lexico", lexic.lex + " no pertenece al lenguaje", Integer.toString(lexic.row), Integer.toString(lexic.column)});
        }
        for (SyntacticError syntactic:Main.syntacticErrorList) {
            _errorData.add(new String[]{"Sintactico", syntactic.expected, Integer.toString(syntactic.row), Integer.toString(syntactic.column)});
        }

        errorData = _errorData.toArray(new String[0][4]);
        String errorHTML = Utils.arrayToHTMLTable( "Tabla Errores",new String[]{"Tipo", "Descripcion", "Fila", "Columna"}, errorData);
        FileHandler.writeToFile(String.format("REPORTES/ERRORES_%s/Errores.html", Main.carnet ),
                errorHTML, false);


        Main.mainWindow.updateComboboxes(names);

        System.out.println("generate button clicked");
    }


    public static List<String> specialOperatorsExpansion(List<String> list) {


        System.out.println("TOTAL CHAIN");
        System.out.println(list);

        boolean chainWasUpdated = true;

        //This can be done more efficiently by having a lastKnownExpansionIndex to skip elements
        expansion: while (chainWasUpdated) {
            chainWasUpdated = false;

           for (int i = 0; i < list.size(); i++) {
                String s = list.get(i);

                //Converting +[expr] to .*[expr][expr]
                //length of [expr] is determined by number of .| before encountering a operand (+ one)
                if (s.equals("+")) {
                    Main.dprint("Encountered +, expanding....");
                    chainWasUpdated = true;
                    int _i = i+1;
                    int count = 1; //for including exclusive 2nd index of sublist
                    while (count > 0) {
                        if (list.get(_i).equals(".") || list.get(_i).equals("|")) {
                            count++;
                        }
                        else {
                            if ( !list.get(_i).equals("?") && !list.get(_i).equals("*")) {
                                count--;
                            } //else unary operator, do nothing
                        }
                        _i++;
                    }

                    List<String> result = new ArrayList<>(list.subList(0,i)); //stuff before
                    result.add("."); result.add("*"); //expansion
                    result.addAll(list.subList(i+1,_i)); // elements included in expansion
                    result.addAll(list.subList(i+1,list.size())); // stuff after
                    list = result; //Another list should be necessary here? idk.
                    System.out.println("After + expand:");
                    System.out.println(list);
                    continue expansion;
                }

                //Converting ?[expr] to |[expr]{epsilon}
                //length of [expr] is determined by number of .| before encountering a operand (+ one)
                if (s.equals("?")) {
                    Main.dprint("Encountered ?, expanding....");
                    System.out.println(" ?before expansion");
                    System.out.println(list);
                    chainWasUpdated = true;
                    int _i = i+1;
                    int count = 1; //for including exclusive 2nd index of sublist
                    while (count > 0) {
                        if (list.get(_i).equals(".") || list.get(_i).equals("|")|| list.get(_i).equals("?")) {
                            count++;
                        }
                        else {
                            if ( !list.get(_i).equals("?") && !list.get(_i).equals("*")) {
                                count--;
                            } //else unary operator, do nothing
                        }
                        _i++;
                    }
                    List<String> result =  new ArrayList<>(list.subList(0,i)); //stuff before

                    result.add("|"); //expansion

                    result.addAll(list.subList(i+1,_i)); // elements included in expansion
                    result.add("{epsilon}"); //expansion
                    result.addAll(list.subList(_i,list.size())); // stuff after
                    list = result;

                    System.out.println("after ? expansion:");
                    System.out.println(result);
                    continue expansion;
                }
            }
        }
        System.out.println("FINAL");
        System.out.println(list);
        return list;
    }
}
