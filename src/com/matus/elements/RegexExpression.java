package com.matus.elements;

import java.util.List;

public class RegexExpression {

    public String name;
    public String pattern;

    public NodeTree treeHead;

    public List<NodeTree> leaves;
    public List<String[]> nextTable;

    public String afd_graphviz;


    public RegexExpression(String name, String pattern) {
        this.name = name;
        this.pattern = pattern;
    }


}
