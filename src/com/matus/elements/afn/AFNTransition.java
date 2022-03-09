package com.matus.elements.afn;

public class AFNTransition {

    public String symbol;
    public AFNNode destination;
    public String type; //normal, initial, final

    public AFNTransition(String symbol, AFNNode destination, String type) {
        this.symbol = symbol;
        this.destination = destination;
        this.type = type;
    }

    public AFNTransition() {
    }


    public AFNTransition getInitialTransition(AFNNode node) {
        for (AFNTransition transition : node.transitions) {
            if (transition.type.equals("initial")) {
                return transition;
            };
        }
        System.out.println("ERROR: node lacks initial transition, this shouldn't happen");
        return null;
    }

    public AFNTransition getFinalTransition(AFNNode node) {
        for (AFNTransition transition : node.transitions) {
            if (transition.type.equals("final")) {
                return transition;
            };
        }
        System.out.println("ERROR: node lacks final transition, this shouldn't happen");
        return null;
    }
}
