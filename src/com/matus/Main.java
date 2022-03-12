package com.matus;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.*;

import com.matus.analyzers.LexicAnalyzer;
import com.matus.analyzers.SyntacticAnalyzer;
import com.matus.elements.afd.AFDNode;
import com.matus.elements.language.*;
import com.matus.exceptions.InvalidCharacterException;
import com.matus.gui.MainWindow;
import java_cup.runtime.Symbol;

import javax.swing.*;

public class Main {

    //Internal vars
    private static boolean debug = true;
    public static boolean debugLoadExp = true;
    public static boolean openImagesOnChange = true;
    public static final String carnet = "201801290";

    public static boolean stopLexOnError = false;
    public static boolean groupDefinitionIsUppercase = true;

    //Global utils
    private static final Scanner scanner = new Scanner(System.in);
    public static final MainWindow mainWindow = new MainWindow();

    //Lists
    public static List<Token> tokenList= new ArrayList<>();

    public static List<LexicError> lexicErrorList= new ArrayList<>();
    public static List<SyntacticError> syntacticErrorList= new ArrayList<>();

    public static Map<String, Group> groupList = new HashMap<>();
    public static Map<String, RegexExpression> regexList = new HashMap<>();
    public static List<RegexTest> regexTestList = new ArrayList<>();

    public static void main(String[] args){


        //rawData = ".<->*<->.<->a<->b<->*<->.<->c<->d";
        //rawData = ".<->{letra}<->*<->|<->\"_\"<->|<->{letra}<->{digito}";
        //rawData = ".<->{digito}<->.<->\".\"<->+<->{digito}";
        //rawData = ".<->{digito}<->*<->|<->\"_\"<->{letra}<->{digito}";
        //logRegex("RegexPrueba1",".<->*<->.<->\"z\"<->b<->*<->.<->c<->d",0,0);  //TODO please delete this lmao
        //logRegex("RegexPrueba1",".<->a<->+<->b",0,0);  //TODO please delete this lmao

        mainWindow.setDefaultCloseOperation (JFrame.HIDE_ON_CLOSE);
        mainWindow.setVisible (true);
    }

    //0: success
    //1: lexic error (remember to clean tables)
    //2: syntactic error (remember to clean tables)
    //3: File error
    public static Map<String, Boolean> usedGroupsInRegex;
    public static int parseExpFile() {

        //Cleaning
        usedGroupsInRegex = new HashMap<>();
        tokenList= new ArrayList<>();

        lexicErrorList= new ArrayList<>();
        syntacticErrorList= new ArrayList<>();

        groupList = new HashMap<>();
        regexList = new HashMap<>();
        regexTestList = new ArrayList<>();
        String input = mainWindow.inputTextArea.getText();
        //pido perdon :(
        //input = input.replaceAll("\\\\\\\"","┤");
        input = input.replaceAll("\\\\\"","┤");
        input = input.replaceAll("\\\\n","Á");
        input = input.replaceAll("\\\\'","Â");


        System.out.println("TEXTO A ANALIZAR:");
        System.out.println(input);

        //FileHandler.writeToFile("./tmp.exp", input, false);
        //input = String.join("\n",FileHandler.readFile("./tmp.exp",-1));

        try {

            /*
            LexicAnalyzer lexic = new LexicAnalyzer(
                    new BufferedReader(new FileReader("./tmp.exp"))
            );

             */

            LexicAnalyzer lexic = new LexicAnalyzer( new java.io.StringReader(input) );

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

        //Used groups without declaration
        for (var entry : usedGroupsInRegex.entrySet()) {
            String key = entry.getKey();
            key = key.substring(1, key.length()-1);
            if (!groupList.containsKey(key)) {
                String f = String.format("Se ha hecho uso del grupo %s sin ser definido en el archivo", entry.getKey());
                cprintln(String.format("%s (f:%s c:%s)", f, -1, -1));
                logSyntacticError(entry.getKey(), "{group}", f, -1, -1);
                return 2;
            }
        }


        //Testing regex that doesn't exist
        for (RegexTest regexTest : regexTestList) {
            if (!regexList.containsKey(regexTest.regexNameToRun)) {
                String f = String.format("Se ha hecho uso del regex %s sin ser definido en el archivo", regexTest.regexNameToRun);
                cprintln(String.format("%s (f:%s c:%s)", f, -1, -1));
                logSyntacticError(regexTest.regexNameToRun, "{regex}", f, -1, -1);
                return 2;
            }
        }

        //No error in the way? sheeesh
        analyze();
        return 0;
    }


    public static void analyze() {
        System.out.println("##############################################################################");
        System.out.println("##############################################################################");
        System.out.println("##############################################################################");
        System.out.println("##############################################################################");
        for (RegexTest regexTest : regexTestList) {
            System.out.println("####################");
            System.out.printf("Testing Regex->%s  with string->%s\n", regexTest.regexNameToRun, regexTest.str);
            cprintln("####################");
            cprintln(String.format("Testing Regex->%s  with string->%s\n", regexTest.regexNameToRun, regexTest.str));
            RegexExpression regex = regexList.get(regexTest.regexNameToRun); //Existence checked in parse phase

            AFDNode currentState = regex.afd_nodes.get(0);

            boolean found = true; //empty strings? idk
            str: for (char _c: regexTest.str.toCharArray()) {

                String c = "" + _c;

                //Should be unnecesary? idk
                if (c.equals("┤")) c = "\\\""; // \n
                if (c.equals("Á")) c = "\\n"; // \n
                if (c.equals("Â")) c = "\\'"; // \n


                found = false;
                System.out.println("########");
                System.out.println("Current State:S" + currentState.number);
                for (var entry : currentState.transitions.entrySet()) {
                    String key = entry.getKey();
                    System.out.println("transition to S" +  entry.getValue().number + " has symbol " + key);
                    System.out.println("comparing to " + c);


                    //Groups
                    if (key.contains("{")) {

                        System.out.println("Resolving group for" + key);

                        String k = key.substring(1, key.length()-1);
                        Group group = groupList.get(k);
                        String members = group.elements;

                        members = members.replaceAll("┤", "\\\\\"");
                        members = members.replaceAll("Á", "\\\\n");
                        members = members.replaceAll("Â", "\\\\'");
                        System.out.println("members of " + key + ": " + members);

                        if (members.contains("~")) {
                            int first = members.charAt(0);
                            int second = members.charAt(2);
                            int member = c.charAt(0);
                            /*
                            System.out.println("ascii of first:" + first);
                            System.out.println("ascii of element:" + member);
                            System.out.println("ascii of first:" + second);
                            System.out.println(member >= first && member <= second);

                             */


                            System.out.println(c + "->" + member);
                            if (member >= first && member <= second) {
                                //System.out.println("Number in range");
                                currentState = entry.getValue();
                                found = true;
                                System.out.println("range move");
                                System.out.println("moving to S" + currentState.number);
                                continue str;
                            }
                            else {
                                continue;
                            }


                        }
                        //If instead is a list
                        for (String s : members.split(",")) {
                            if (s.contains("\"")) {
                                if (s.substring(1,s.length()-1).equals(c+"")) { //TODO contains? idk
                                    System.out.println("list moving with removing \"");
                                    currentState = entry.getValue();
                                    found = true;
                                    System.out.println("moving to S" + currentState.number);
                                    continue str;
                                }
                            }
                            else {
                                if (s.equals(c+"")) { //TODO contains? idk
                                    System.out.println("list moving without removing \"");
                                    currentState = entry.getValue();
                                    found = true;
                                    System.out.println("moving to S" + currentState.number);
                                    continue str;
                                }
                            }
                        }
                        continue;

                    }


                    if (key.contains("\"")) {
                        if (key.substring(1,key.length()-1).equals(c+"")) { //TODO contains? idk
                            System.out.println("moving with removing \"");
                            currentState = entry.getValue();
                            found = true;
                            System.out.println("moving to S" + currentState.number);
                            continue str;
                        }
                    }
                    else {
                        if (key.equals(c+"")) { //TODO contains? idk
                            System.out.println("moving without removing \"");
                            currentState = entry.getValue();
                            found = true;
                            System.out.println("moving to S" + currentState.number);
                            continue str;
                        }
                    }



                }
                if (!found) {
                    break;
                }
            }
            if (currentState.isAcceptState && found) {
                regexTest.isValid = true;
            }

            System.out.println(regexTest.str + ":" + (regexTest.isValid ? "VALIDA" : "INVALIDA"));
            cprintln(regexTest.str + ":" + (regexTest.isValid ? "VALIDA" : "INVALIDA"));
        }
    }


    public static void logGroup(String keyword, String name, String rawData, int row, int column) {

        System.out.println("Logeando grupo::"  + rawData);

        //Correct keyword
        if (groupDefinitionIsUppercase) {
            if (!keyword.equals("CONJ")) {
                String f = String.format("Para definir conjuntos debes usar la palabra clave CONJ (case sensitive) %s", keyword);
                cprintln(String.format("%s (f:%s c:%s)", f, row, column));
                logSyntacticError(keyword, "conj", f, row, column);
                System.out.println("error conj");
                return;
            }
        }
        else {
            if (!keyword.equalsIgnoreCase("conj")) {
                String f = String.format("Para definir conjuntos debes usar la palabra clave CONJ (case insensitive) %s", keyword);
                cprintln(String.format("%s (f:%s c:%s)", f, row, column));
                logSyntacticError(keyword, "conj", f, row, column);
                System.out.println("error conj");
                return;
            }
        }

        //Name is always correct? idk
        //Correct range
        if (rawData.contains("~")) {


            /*
            System.out.println(rawData);
            System.out.println("Check for ┤");
            System.out.println(rawData.contains("┤"));
            System.out.println("Check for Á");
            System.out.println(rawData.contains("Á"));
            System.out.println("Check for Â");
            System.out.println(rawData.contains("Â"));

             */


            if (rawData.length() != 3 && !(rawData.contains("┤") || rawData.contains("Á")||rawData.contains("Â"))/* x~y */) {
                String f = String.format("Los elementos de un grupos definidos por rango solo pueden tener 1 caracter %s", keyword);
                cprintln(String.format("%s (f:%s c:%s)", f, row, column));
                logSyntacticError(keyword, "conj", f, row, column);
                System.out.println("error rango");
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

            if (!allOneCharacter && !(rawData.contains("┤") || rawData.contains("Á")||rawData.contains("Â"))) {
                String f = String.format("Los elementos de un grupo definidos por lista solo pueden tener 1 caracter %s", keyword);
                cprintln(String.format("%s (f:%s c:%s)", f, row, column));
                logSyntacticError(keyword, "conj", f, row, column);
                System.out.println("error lista");
                return;
            }
        }

        //sheeeesh
        if (groupList.containsKey(name)) {
            cprintln(String.format("(Advertencia) El grupo %s fue definido otra vez y sera sobreescrito", name));
        }
        groupList.put(name, new Group(name, rawData));
        System.out.printf("Grupo %s definido con exito%n", name);
        cprintln(String.format("Grupo %s definido con exito", name));
    }


    public static void logRegex(String name, String rawData, int row, int column) {
        RegexExpression exp = new RegexExpression(name, rawData.replaceAll("<->",""));
        boolean correctAFD = Generator.generateAFD(exp, rawData, row, column);
        if (!correctAFD) { return; }
        boolean correctAFN = Generator.generateAFN(exp, rawData, row, column); //FIXME correct afd means correct afn? idk, just in case
        if (!correctAFN) { return; }

        if (regexList.containsKey(name)) {
            cprintln(String.format("(Advertencia) El regex %s fue definido otra vez y sera sobreescrito", name));
        }

        regexList.put(exp.name, exp);
        cprintln(String.format("Regex %s definido con exito : %s", exp.name, exp.pattern));
        System.out.printf("Regex %s definido con exito : %s%n", exp.name, exp.pattern);
    }


    public static void logRegexTest(String regexName, String str) {
        str = str.substring(1, str.length()-1);
        //System.out.printf("Loging test regex with name %s for string <%s>\n", regexName, str);
        regexTestList.add(new RegexTest(str, regexName));
    }

    public static void logToken(String id, String lex, int row, int column) {
        System.out.printf("Reconocio token:<%s> lexema:%s en f:%s c:%s%n", id, lex, row, column);

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

        String str = s.toString();

        str = str.replaceAll("┤", "\\\\\"");
        str = str.replaceAll("Á", "\\\\n");
        str = str.replaceAll("Â", "\\\\'");

        mainWindow.outputTextArea.setText(mainWindow.outputTextArea.getText() + str + "\n");
    }

    public static void dprint(Object s) {if (debug) {System.out.println(s);}}

    public static void runCMD(String cmd) {
        try {
            Runtime.getRuntime().exec("cmd.exe  /C " + cmd);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
