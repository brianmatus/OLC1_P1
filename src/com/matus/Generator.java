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

            //System.out.println("Now analyzing transitions for ");

            //Iterate all elements in group and join same-char parts (be sure to check for nulls) (remove from elementsList all elements involved)
            while (elementList.size() != 0) {
                List<String> sameElements = new ArrayList<>(Arrays.asList(elementList.get(0)));
                //Remove dups and order

                String firstRepresent = regex.leavesList.get(Integer.parseInt(elementList.get(0))-1).label;
                for (int i = 0; i < elementList.size(); i++) {
                    String next = elementList.get(i);
                    String represents = regex.leavesList.get(Integer.parseInt(next)-1).label;
                    System.out.printf("%s: has element %s\n",next, represents);
                    //First is always already grabbed
                    if (Objects.equals(represents, firstRepresent)) {
                        sameElements.add(next);
                    }
                }

                sameElements = sameElements.stream()
                        .distinct()
                        .collect(Collectors.toList());
                Collections.sort(sameElements);

                System.out.println("Elements without dups are:");
                System.out.println(sameElements);


                for (AFDNode afdNode : nodeList) {
                    //if a59fdNode.belongingElements
                }


                break; //TODO delete, only for depuration
                //Iterate nodeList to check if group already exist. If not, create one and insert it in nodeList
                //add transition to current AFDNode
            }





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
