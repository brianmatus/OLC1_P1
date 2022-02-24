package com.matus;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

import com.matus.analyzers.LexicAnalyzer;
import com.matus.analyzers.Symbols;
import com.matus.analyzers.SyntacticAnalyzer;
import com.matus.elements.LexicError;
import com.matus.elements.Token;
import com.matus.exceptions.InvalidCharacterException;
import java_cup.runtime.Symbol;


public class Main {

    //Internal vars
    private static boolean debug = true;
    private static final Scanner scanner = new Scanner(System.in);

    //Global settings
    private static boolean stopLexOnError = true;

    //Lists
    private static List<Token> tokenList= new ArrayList<>();
    private static List<LexicError> lexicErrorList= new ArrayList<>();


    public static void main(String[] args){
        try {
            LexicAnalyzer lexic = new LexicAnalyzer(
                    new BufferedReader(new FileReader("./entrada.txt"))
            );

            SyntacticAnalyzer syntactic = new SyntacticAnalyzer(lexic);
            Symbol ola = syntactic.parse();
            System.out.println(ola);

        } catch (InvalidCharacterException e) {
            //e.printStackTrace();
            //Lexic error logged by self class, no need for code

        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
            //Syntactic error logged by self class, no need for code
        }
    }

    

    public static void logToken(String id, String lex, int row, int column) {
        tokenList.add(new Token(id, lex, row, column));
    }


    public static void logLexicError(String lex, int row, int column) {
        lexicErrorList.add(new LexicError(lex, row, column));

        //TODO logic to announce lexic error
    }

    public static void logSyntacticError(String lex, String expected) {

        //TODO logic to announce syntactic error
    }







    public static void dprint(Object s) {if (debug) {System.out.println(s);}}

}
