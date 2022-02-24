package com.matus.elements;

public class LexicError {

    public String lex;
    public int row;
    public int column;
    public LexicError(String lex, int row, int column) {
        this.lex = lex;
        this.row = row;
        this.column = column;
    }
}
