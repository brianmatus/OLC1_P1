package com.matus;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.util.*;

import com.matus.analyzers.LexicAnalyzer;
import com.matus.analyzers.SyntacticAnalyzer;
import com.matus.elements.*;
import com.matus.exceptions.InvalidCharacterException;
import com.matus.gui.MainWindow;
import java_cup.runtime.Symbol;

import javax.swing.*;

public class Main {

    //Internal vars
    private static boolean debug = true;
    public static boolean debugLoadExp = true;

    public static boolean stopLexOnError = true;
    public static boolean groupDefinitionIsUppercase = true;

    //Global utils
    private static final Scanner scanner = new Scanner(System.in);
    private static final MainWindow mainWindow = new MainWindow();

    //Lists
    private static List<Token> tokenList= new ArrayList<>();

    private static List<LexicError> lexicErrorList= new ArrayList<>();
    private static List<SyntacticError> syntacticErrorList= new ArrayList<>();

    private static List<Group> groupList = new ArrayList<>();
    private static List<RegexExpression> regexList = new ArrayList<>();
    //private static List<RegexTest> regexTestList = new ArrayList<>();

    public static void main(String[] args){


        //rawData = ".<->*<->.<->a<->b<->*<->.<->c<->d";
        //rawData = ".<->{letra}<->*<->|<->\"_\"<->|<->{letra}<->{digito}";
        //rawData = ".<->{digito}<->.<->\".\"<->+<->{digito}";
        //rawData = ".<->{digito}<->*<->|<->\"_\"<->{letra}<->{digito}";
        //logRegex("RegexPrueba1",".<->*<->.<->\"z\"<->b<->*<->.<->c<->d",0,0);  //TODO please delete this lmao
        logRegex("RegexPrueba1",".<->|<->a<->b<->|<->c<->d",0,0);  //TODO please delete this lmao




        //mainWindow.setDefaultCloseOperation (JFrame.HIDE_ON_CLOSE);
        //mainWindow.setVisible (true);
        //System.out.println("calma, se manejo todo");
    }

    //0: success
    //1: lexic error (remember to clean tables)
    //2: syntactic error (remember to clean tables)
    //3: File error
    public static int parseExpFile() {
        //Cleaning
        mainWindow.outputTextArea.setText("");

        String input = mainWindow.inputTextArea.getText();
        System.out.println("TEXTO A ANALIZAR:");
        System.out.println(input);

        FileHandler.writeToFile("./tmp.exp", input, false);
        try {
            LexicAnalyzer lexic = new LexicAnalyzer(
                    new BufferedReader(new FileReader("./tmp.exp"))
            );

            SyntacticAnalyzer syntactic = new SyntacticAnalyzer(lexic);
            Symbol result = syntactic.parse();
            System.out.println(result);

        } catch (InvalidCharacterException e) {
            //e.printStackTrace();
            //Lexic error logged by self class, no need for code
            return 1;

        } catch (FileNotFoundException e) {
            e.printStackTrace();
            return 3;
        } catch (Exception e) {
            System.out.println(e.getMessage());
            //e.printStackTrace();
            return 2;
            //Syntactic error logged by self class, no need for code
        }

        //No error catched? sheeesh
        dprint("no paso nada oiga");
        return 0;
    }


    public static void logGroup(String keyword, String name, String rawData, int row, int column) {

        //Correct keyword
        if (groupDefinitionIsUppercase) {
            if (!keyword.equals("CONJ")) {
                String f = String.format("Para definir conjuntos debes usar la palabra clave CONJ (case sensitive) %s", keyword);
                cprintln(String.format("%s (f:%s c:%s)", f, row, column));
                logSyntacticError(keyword, "conj", f, row, column);
                return;
            }
        }
        else {
            if (!keyword.equalsIgnoreCase("conj")) {
                String f = String.format("Para definir conjuntos debes usar la palabra clave CONJ (case insensitive) %s", keyword);
                cprintln(String.format("%s (f:%s c:%s)", f, row, column));
                logSyntacticError(keyword, "conj", f, row, column);
                return;
            }
        }

        //Name is always correct? idk
        //Correct range
        if (rawData.contains("~")) {

            if (rawData.length() != 3 /* x~y */) {
                String f = String.format("Los elementos de un grupos definidos por rango solo pueden tener 1 caracter %s", keyword);
                cprintln(String.format("%s (f:%s c:%s)", f, row, column));
                logSyntacticError(keyword, "conj", f, row, column);
                return;
            }
        }

        //Correct list
        if (rawData.contains(",")) {
            boolean allOneCharacter = true;
            for (String letter  : rawData.split(",")) {
                if (letter.length() != 1) {
                    allOneCharacter = false;
                    break;
                }
            }

            if (!allOneCharacter) {
                String f = String.format("Los elementos de un grupo definidos por lista solo pueden tener 1 caracter %s", keyword);
                cprintln(String.format("%s (f:%s c:%s)", f, row, column));
                logSyntacticError(keyword, "conj", f, row, column);
                return;
            }
        }

        //sheeeesh
        groupList.add(new Group(name, rawData));
    }


    public static void logRegex(String name, String rawData, int row, int column) {
        RegexExpression exp = new RegexExpression(name, rawData);
        boolean correctAFD = Generator.generateAFD(exp, row, column);
        if (!correctAFD) { return; }
        boolean correctAFN = Generator.generateAFN(exp, row, column); //FIXME correct afd means correct afn? idk, just in case
        if (!correctAFN) { return; }
        regexList.add(exp);
        dprint(String.format("Regex %s logged sucessfully", exp.name));

    }


    public static void logRegexTest(String regexName, String str) {

        System.out.printf("Testing regex with name %s for string \" %s \"\n", regexName, str);

    }

    public static void logToken(String id, String lex, int row, int column) {
        tokenList.add(new Token(id, lex, row, column));
    }

    public static void logLexicError(String lex, int row, int column) {
        lexicErrorList.add(new LexicError(lex, row, column));
        cprintln(String.format("ERROR LEXICO PRODUCIDO:%s en f:%d c:%d", lex, row, column));
    }

    public static void logSyntacticError(String lex, String expectedInternalId, String expectedElements, int row, int column) {
        System.out.printf("Token <%s> inválido, %s --> %s en f:%d row %d",
                lex, expectedInternalId, expectedElements, row, column);
        SyntacticError err = new SyntacticError("",expectedInternalId,expectedElements,row,column);
        syntacticErrorList.add(err);
        cprintln(String.format("ERROR SINTACTICO PRODUCIDO:%s en f:%d c:%d", err, row, column));
    }


    public static void cprintln(Object s) {
        mainWindow.outputTextArea.setText(mainWindow.outputTextArea.getText() + s.toString() + "\n");
    }

    public static void dprint(Object s) {if (debug) {System.out.println(s);}}

}
