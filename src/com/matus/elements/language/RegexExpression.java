package com.matus.elements.language;

import com.matus.elements.afd.AFDNode;
import com.matus.elements.afd.NodeTree;
import com.matus.elements.afn.AFNStructure;

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
    public String afdGraphviz;


    //AFN
    public AFNStructure afnStructure;
    public String afnGraphviz;

    public RegexExpression(String name, String pattern) {
        this.name = name;
        this.pattern = pattern;
        this.nextTable = new ArrayList<>();
    }

    public RegexExpression() {}


}
