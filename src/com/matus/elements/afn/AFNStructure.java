package com.matus.elements.afn;

import java.util.ArrayList;
import java.util.List;

public class AFNStructure {

    public AFNNode initialNode;
    public AFNNode acceptanceNode;
    public List<AFNNode> nodes; //not including initial nor acceptance

    public AFNStructure() {
        nodes = new ArrayList<>();
    }
}
