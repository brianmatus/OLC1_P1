package com.matus;

import com.matus.elements.*;
import com.matus.elements.afn.AFNNode;
import com.matus.elements.afn.AFNStructure;
import com.matus.elements.afn.AFNTransition;

import java.util.*;
import java.util.stream.Collectors;

public class Generator {

    public static boolean generateAutomats () {

        //Some checks before continuing
        //TODO implement
        System.out.println("generate button clicked");
        System.out.println("redundant button? afd are created on load. Idk, maybe dot to svg");
        return false;
    }


    public static boolean generateAFD(RegexExpression regexExp , int row, int column) {

        String rawData = regexExp.pattern;

        //*Parse this baby
        String graphvizString = "digraph {\nnodesep=3;\n";
        int nodesCreated = 0;

        List<String> tmpList = new ArrayList<>(); //for separating elements into chars (except previous groups and special chars)

        Stack<NodeTree> stack = new Stack<>(); //parsed elements go here

        List<NodeTree> leafList = new ArrayList<>();
        regexExp.leavesList = leafList;

        rawData = ".<->" + rawData + "<->#";
        String[] _arr = rawData.split("<->");
        for (String s : _arr) {
            if (s.contains("{") | s.contains("\"") ) { //FIXME | s.contains("\"")
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
                    if (a.nullable) parent.firstPos = Utils.removeStringListDuplicates(a.firstPos + "," + b.firstPos);
                    else parent.firstPos = a.firstPos;

                    //Last Pos
                    if (b.nullable) parent.lastPos = Utils.removeStringListDuplicates(a.lastPos + "," + b.lastPos);
                    else parent.lastPos = b.lastPos;
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
                    //Main.dprint("Leaf encountered with " + s);
                    int leafCount = leafList.size() + 1;
                    //*(String label, String firstPos, String lastPos, boolean nullable, int number, boolean isEpsilon)
                    boolean nullable = s.equals("{epsilon}");
                    NodeTree theNode = new NodeTree(s,Integer.toString(leafCount), Integer.toString(leafCount), nullable, Integer.toString(leafCount), s.equals("epsilon"), nodesCreated);
                    leafList.add(theNode);
                    //Main.dprint("adding to stack"); //TODO delete me
                    stack.add(theNode);
                    //Graphviz
                    graphvizString += String.format(
                            "\"node-%s\"[fixedsize=true,label=<<TABLE CELLSPACING=\"2\" CELLPADDING=\"2\" BORDER=\"0\">" +
                                    "<TR><TD></TD><TD>%s</TD><TD></TD></TR><TR><TD></TD><TD></TD><TD></TD></TR>" +
                                    "<TR><TD>%s</TD><TD>%s</TD><TD>%s</TD></TR><TR><TD></TD><TD>%s</TD><TD></TD></TR></TABLE>>]\n"
                            , nodesCreated, nullable? "V":"F", leafCount, Utils.centerString(s,14), leafCount, leafCount);
                    nodesCreated++;

                    regexExp.nextTable.add("");
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
        regexExp.afd_nodes = Generator.generateStates(regexExp);
        return true;
    }

    public static boolean generateAFN(RegexExpression regexExp, int row, int column) {

        System.out.println("Performing AFN parsing");
        String rawData = regexExp.pattern;

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
        String graphvizString = "digraph {\nrankdir=LR;\n";

        //*Initial Node and it's transitions
        graphvizString += String.format("\"node-%s\"[label=\"\"]\n", result.initialNode.number);

        for (AFNTransition transition : result.initialNode.transitions) {
            String lbl = transition.symbol.equals("{e}") ? "ε" : transition.symbol;
            graphvizString += String.format("\"node-%s\" -> \"node-%s\"[label=\"%s\"]\n", result.initialNode.number, transition.destination.number, lbl);
        }

        //*Nodes and transitions (including transits to final)
        for (AFNNode node : result.nodes) {
            graphvizString += String.format("\"node-%s\"[label=\"\"]\n", node.number);
            for (AFNTransition transition : node.transitions) {
                String lbl = transition.symbol.equals("{e}") ? "ε" : transition.symbol;
                graphvizString += String.format("\"node-%s\" -> \"node-%s\"[label=\"%s\"]\n", node.number, transition.destination.number, lbl);
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

    public static List<AFDNode> generateStates(RegexExpression regex) {

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

                if (firstRepresent.equals("#")) {
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

        String graphviz = "digraph {\nrankdir=LR;\n";

        System.out.println("Resulting AFD is:");
        for (AFDNode afdNode : nodeList) {
            System.out.println("#########################");
            System.out.printf("S%s %s\n", afdNode.number, afdNode.isAcceptState? "(accept)" : "");

            graphviz += String.format("\"state-%s\"[shape=%s label=\"S%s\"]\n", afdNode.number, afdNode.isAcceptState? "doublecircle":"circle", afdNode.number);

            for (var entry : afdNode.transitions.entrySet()) {
                System.out.printf("Trans[S%s,%s] = S%s\n", afdNode.number, entry.getKey(),entry.getValue().number);
                String lbl = entry.getKey();
                if (lbl.contains("\"")) lbl = "\\" + lbl.substring(0,lbl.length()-1) + "\\" + lbl.substring(lbl.length()-1);
                graphviz += String.format("\"state-%s\" -> \"state-%s\" [label=\"%s\"]\n", afdNode.number, entry.getValue().number, lbl);
            }
            System.out.println("#########################");

        }
        graphviz += "\n}";
        System.out.println("#########################");
        System.out.println(graphviz);
        System.out.println("#########################");
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
                    Main.dprint("Encountered +, expanding....");
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
                    Main.dprint("Encountered ?, expanding....");
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
}
