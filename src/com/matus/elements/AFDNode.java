package com.matus.elements;

import java.util.HashMap;
import java.util.Map;

public class AFDNode {

    public Map<String, AFDNode> transitions;
    public int number;
    public int[] belongingElements;

    public AFDNode(int number) {
        this.number = number;
        this.transitions = new HashMap<String, AFDNode>();
    }
}
