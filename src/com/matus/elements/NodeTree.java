package com.matus.elements;

public class NodeTree {


    public NodeTree leftChildren;
    public NodeTree rightChildren;
    public NodeTree parent;

    public String label;
    public String firstPos;
    public String lastPos;
    public boolean nullable;
    public String number;

    public int orderInTree;

    public boolean isEpsilon;


    public NodeTree(String label, String firstPos, String lastPos, boolean nullable, String number, boolean isEpsilon, int orderInTree) {
        this.label = label;
        this.firstPos = firstPos;
        this.lastPos = lastPos;
        this.nullable = nullable;
        this.number = number;
        this.isEpsilon = isEpsilon;
        this.orderInTree = orderInTree;
    }

    public NodeTree() {
    }


    @Override
    public String toString() {
        return label + orderInTree;
    }
}
