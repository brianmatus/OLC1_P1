package com.matus.elements;

import java.util.ArrayList;
import java.util.List;

public class RegexExpression {

    public String name;
    public String pattern;

    public NodeTree treeHead;

    public List<NodeTree> leavesList;
    public List<String> nextTable;

    public String afd_tree_graphviz;
    public List<AFDNode> afd_tree;

    public RegexExpression(String name, String pattern) {
        this.name = name;
        this.pattern = pattern;
        this.nextTable = new ArrayList<>();
    }


    public RegexExpression() {}


}
