package com.matus.elements;

import java.util.ArrayList;
import java.util.List;

public class RegexExpression {

    public String name;
    public String pattern;


    //AFD
    public NodeTree treeHead;
    public List<NodeTree> leavesList;
    public List<String> nextTable;
    public String expressionTreeGraphviz;
    public List<AFDNode> afd_nodes;

    //AFN
    public String afnGraphviz;
    public List<AFNNode> afn_nodes;

    public RegexExpression(String name, String pattern) {
        this.name = name;
        this.pattern = pattern;
        this.nextTable = new ArrayList<>();
    }

    public RegexExpression() {}


}
