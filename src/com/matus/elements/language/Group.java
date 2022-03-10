package com.matus.elements.language;

import java.util.Arrays;

public class Group {


    public String name;
    public String elements;


    public Group(String name, String elements) {
        this.name = name;
        this.elements = elements;


    }

    @Override
    public String toString() {
        return elements;
    }
}
