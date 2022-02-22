package com.matus;

import java.io.BufferedReader;
import java.io.FileReader;

import com.matus.analyzers.LexicAnalyzer;
import com.matus.analyzers.Symbols;
import com.matus.analyzers.SyntacticAnalyzer;
import java_cup.runtime.*;


public class Main {

    public static void main(String[] args){

        try {
            LexicAnalyzer lexic = new LexicAnalyzer(
                    new BufferedReader(new FileReader("./entrada.txt"))
            );

            SyntacticAnalyzer syntactic = new SyntacticAnalyzer(lexic);
            Symbol ola = syntactic.parse();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}
