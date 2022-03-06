package com.matus;

import com.matus.elements.AFDNode;
import com.matus.elements.NodeTree;
import com.matus.elements.RegexExpression;

import java.util.*;
import java.util.stream.Collectors;

public class Generator {

    public static boolean generateAutomats () {

        //Some checks before continuing
        //TODO implement

        return false;
    }


    public static RegexExpression pregenerateRegex(String name, String rawData, int row, int column) {


        RegexExpression regexExp = new RegexExpression(name, rawData);
        String graphvizString = "digraph {\nnodesep=3;\n";
        int nodesCreated = 0;
        rawData = ".<->.<->+<->a<->+<->b<->+<->c"; //TODO please delete this lmao
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

        Main.dprint("Before expansion:");
        Main.dprint(tmpList);
        //Expansion of + and ?
        tmpList = Generator.specialOperatorsExpansion(new ArrayList<>(tmpList)); //before stuff goes wild
        Main.dprint("After expansion:");
        Main.dprint(tmpList);

        for (int i = tmpList.size()-1; i >= 0 ; i--) { //Reverse loop array
            String s = tmpList.get(i);
            System.out.println("Now analyzing " + s);
            System.out.println("Current stack is" + stack);
            switch (s) {
                case "." -> {
                    //stack underflow
                    if (stack.size() < 2) {
                        String f = String.format("Expresión REGEX invalida (operación . definida sin elementos previos): %s", rawData);
                        Main.cprintln(String.format("%s (f:%s c:%s)", f, row, column));
                        Main.logSyntacticError(rawData, "conj", f, row, column);
                        return null;
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
                        return null;
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
                        return null;
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
                        f = Utils.removeStringListDuplicates(f);
                        System.out.println("Before call for removing dups");
                        System.out.println(f);
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
                            , nodesCreated, nullable? "V":"F", leafCount, Utils.centerString(s,14), leafCount, leafCount);
                    nodesCreated++;

                    regexExp.nextTable.add("");
                }
            }
            Main.dprint("!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!");
            Main.dprint("!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!");
            Main.dprint(graphvizString + "\n}");
        }

        graphvizString += "\n}";
        regexExp.afd_tree_graphviz = graphvizString;

        System.out.println("RESULTADO:" + stack.peek().label);
        System.out.println(stack.size());

        if (stack.size() != 1) {//TODO this should always be 1
            String f = String.format("Expresión REGEX invalida (mas operandos que operaciones): %s", rawData);
            Main.cprintln(String.format("%s (f:%s c:%s)", f, row, column));
            Main.logSyntacticError(rawData, "conj", f, row, column);
            return null;
        }
        regexExp.treeHead = stack.peek();
        regexExp.afd_tree = Generator.generateAFD(regexExp);



        return regexExp;
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
        node0.belongingElements = List.of(regex.treeHead.firstPos.split(",")); //note: inmutable
        nodeList.add(node0);

        int nextGroupToAnalyze = 0;
        while (nextGroupToAnalyze != nodeList.size()) {

            //Make a list (elementsList) with all elements in this group
            List<String> elementList = new ArrayList<>(nodeList.get(nextGroupToAnalyze).belongingElements);

            System.out.println(".............................................................................");
            System.out.printf("Now analyzing transitions for S%d->%s\n", nextGroupToAnalyze, elementList);
            System.out.printf("#Elements to analyze:%s\n", elementList.size());

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
                System.out.println("Representation to match:" + firstRepresent);


                if (firstRepresent.equals("#")) {
                    nodeList.get(nextGroupToAnalyze).isAcceptState = true;
                    elementList.remove(0);
                    continue;
                }


                for (int i = 0; i < elementList.size(); i++) {
                    String next = elementList.get(i);
                    String represents = regex.leavesList.get(Integer.parseInt(next)-1).label;
                    System.out.printf("%s: has element %s\n",next, represents);
                    //First is always already grabbed
                    if (Objects.equals(represents, firstRepresent)) {
                        System.out.println("same representation!");
                        System.out.println(represents);
                        System.out.println(firstRepresent);
                        System.out.println("before adding");
                        System.out.println(sameElements);
                        System.out.println("after adding");
                        sameElements.add(next);
                        foundSameIndexes.add(i);
                        System.out.println(sameElements);
                    }
                }


                System.out.println("##############################################");


                //Descending order of indexes to remove for it to not shift indexes while deleting.
                Collections.sort(foundSameIndexes);
                Collections.reverse(foundSameIndexes);
                System.out.printf("elementList before deletion:%s\n", elementList);
                System.out.printf("indexes to remove:%s\n", foundSameIndexes);
                for (int i = 0; i < foundSameIndexes.size(); i++) { //FIXME is enhanced loop ordered? idk maybe later when it already works
                    elementList.remove((int)foundSameIndexes.get(i));
                }

                System.out.printf("elementList after deletion:%s\n", elementList);
                System.out.println("##############################################");


                sameElements = sameElements.stream()
                        .distinct()
                        .collect(Collectors.toList());
                Collections.sort(sameElements);

                System.out.println("Elements without dups are:");
                System.out.println(sameElements);

                List<String> resultingElements = new ArrayList<>();
                for (String sameElement : sameElements) {
                    System.out.println("Now adding nexts of leaf number " + sameElement);
                    resultingElements.addAll(new ArrayList<>(List.of(regex.nextTable.get(Integer.parseInt(sameElement)-1).split(","))));
                }


                resultingElements = resultingElements.stream()
                        .distinct()
                        .collect(Collectors.toList());
                Collections.sort(resultingElements);


                System.out.println("Group of nexts to compare in existing states:");
                System.out.println(resultingElements);

                boolean found = false;
                //Iterate nodeList to check if group already exist. If not, create one and insert it in nodeList

                for (AFDNode afdNode : nodeList) {
                    if (afdNode.belongingElements.equals(resultingElements)) {
                        System.out.printf("Group %s already exists!, only doing transition for representation %s\n", resultingElements, firstRepresent);
                        found = true;
                        //add transition to current AFDNode
                        nodeList.get(nextGroupToAnalyze).transitions.put(firstRepresent, afdNode);
                    }
                }
                if (!found) {
                    System.out.printf("Group %s doesn't exists!, creating and doing transition for representation %s\n", resultingElements, firstRepresent);
                    AFDNode newNode = new AFDNode(nodeList.size());
                    newNode.belongingElements = resultingElements;
                    //add transition to current AFDNode
                    nodeList.get(nextGroupToAnalyze).transitions.put(firstRepresent, newNode);
                    nodeList.add(newNode);
                }
            }
            System.out.println("Exiting element size while");
            nextGroupToAnalyze++;
        }

        System.out.println("Resulting AFD is:");
        for (AFDNode afdNode : nodeList) {
            System.out.println("#########################");
            System.out.printf("S%s %s\n", afdNode.number, afdNode.isAcceptState? "(accept)" : "");
            for (var entry : afdNode.transitions.entrySet()) {
                System.out.printf("Trans[S%s,%s] = S%s\n", afdNode.number, entry.getKey(),entry.getValue().number);
            }
            System.out.println("#########################");
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
