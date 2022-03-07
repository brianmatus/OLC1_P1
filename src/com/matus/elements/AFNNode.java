package com.matus.elements;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class AFNNode {

    public Map<String, AFNNode> transitions;
    public int number;
    public boolean isAcceptState;

    public AFNNode(int number) {
        this.number = number;
        this.transitions = new HashMap<String, AFNNode>();
        this.isAcceptState = false;
    }


}
