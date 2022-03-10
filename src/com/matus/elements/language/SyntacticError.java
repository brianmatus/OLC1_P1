package com.matus.elements.language;

public class SyntacticError {

    public String errID;
    public String expected;
    public String expectedElements;
    public int row;
    public int column;
    public SyntacticError(String errID, String expected, String expectedElements, int row, int column) {
        this.errID = errID;
        this.expected = expected;
        this.expectedElements = expectedElements;
        this.row = row;
        this.column = column;
    }

    @Override
    public String toString() {
        return String.format("Token <%s> inválido, se esperaba %s --> %s", errID, expected, expectedElements);
    }
}
