package com.matus.elements;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class AFDNode {

    public Map<String, AFDNode> transitions;
    public int number;
    public List<String> belongingElements;

    public AFDNode(int number) {
        this.number = number;
        this.transitions = new HashMap<String, AFDNode>();
    }
}
