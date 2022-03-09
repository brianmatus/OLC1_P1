package com.matus.elements.afn;

import java.util.ArrayList;
import java.util.List;

public class AFNNode {

    public String label;
    public int number;
    public List<AFNTransition> transitions;

    public AFNNode() {
        transitions = new ArrayList<>();
        label = "";
    }
}
