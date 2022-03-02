package com.matus.elements;

public class RegexExpression {

    public String name;
    public String pattern;

    public NodeTree treeHead;

    public RegexExpression(String name, String pattern) {
        this.name = name;
        this.pattern = pattern;
    }
}
