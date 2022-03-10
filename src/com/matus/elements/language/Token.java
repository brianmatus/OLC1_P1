package com.matus.elements.language;

public class Token {

    public String id;
    public String lex;
    public int row;
    public int column;

    public Token(String id, String lex, int row, int column) {
        this.id = id;
        this.lex = lex;
        this.row = row;
        this.column = column;
    }
}
