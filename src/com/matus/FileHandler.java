package com.matus;

import java.awt.*;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

public class FileHandler {

    public static String[] readFile(String filename, int lines) {
        FileReader fileReader;
        try {
            fileReader = new FileReader(filename);
        } catch (FileNotFoundException e) {
            File missingFile = new File(filename);
            System.out.println("No existe el archivo \"" + missingFile.getAbsolutePath() + "\"");
            return new String[]{""};
        }

        BufferedReader Buff = new BufferedReader(fileReader);
        StringBuilder records = new StringBuilder();
        String line;


        if (lines== -1) {
            while (true) {
                try {
                    line = Buff.readLine();
                    /*
                    Eso no deberia ser necesario por el try-catch
                    pero el Buff me estaba leyendo null infinitas veces al final de algunos archivos
                    */
                    if (line == null) {
                        fileReader.close();
                        break;
                    }
                    records.append("\n").append(line);
                } catch (IOException e) {
                    //e.printStackTrace();
                    //Es normal encontrar null al llegar a EOF
                    break;
                }
            }
        }
        else {
            for (int i=0;i<lines;i++) {
                try {
                    line = Buff.readLine();
                    if (line == null) {
                        fileReader.close();
                        break;
                    }
                    records.append("\n").append(line);
                } catch (IOException e) {
                    //e.printStackTrace();
                    //Es normal encontrar null al llegar a EOF
                    break;
                }
            }
        }

        if (records.toString().length()==0) {
            return new String[]{""};
        }
        return records.substring(1).split("\\n");
    }

    public static String[] readFile(File file, int lines) {
        FileReader fileReader;
        try {
            fileReader = new FileReader(file);
        } catch (FileNotFoundException e) {
            System.out.println("No existe el archivo \"" + file.getAbsolutePath() + "\"");
            return new String[]{""};
        }

        BufferedReader Buff = new BufferedReader(fileReader);
        StringBuilder records = new StringBuilder();
        String line;

        if (lines== -1) {
            while (true) {
                try {
                    line = Buff.readLine();
                    /*
                    Eso no deberia ser necesario por el try-catch
                    pero el Buff me estaba leyendo null infinitas veces al final de algunos archivos
                    */
                    if (line == null) {
                        fileReader.close();
                        break;
                    }
                    records.append("\n").append(line);
                } catch (IOException e) {
                    //e.printStackTrace();
                    //Es normal encontrar null al llegar a EOF
                    break;
                }
            }
        }
        else {
            for (int i=0;i<lines;i++) {
                try {
                    line = Buff.readLine();
                    if (line == null) {
                        fileReader.close();
                        break;
                    }
                    records.append("\n").append(line);
                } catch (IOException e) {
                    //e.printStackTrace();
                    //Es normal encontrar null al llegar a EOF
                    break;
                }
            }
        }

        if (records.toString().length()==0) {
            return new String[]{""};
        }
        return records.substring(1).split("\\n");
    }

    public static void writeToFile(String filename, String theRecord, boolean openFileAfter){


        try {
            Files.createDirectories(Paths.get(filename));
        } catch (IOException e) {
            //System.out.println("Error creando directorio");
            //e.printStackTrace();
        }

        System.out.println("writing to file with name " + filename);
        File theFile = new File(filename);
        if(theFile.exists()) {
            theFile.delete();
        }
        try {
            if (theFile.createNewFile()) {
                BufferedWriter output = new BufferedWriter(new FileWriter(filename, false));
                output.append(theRecord);
                output.close();
                if (openFileAfter) {Desktop.getDesktop().open(theFile);}
            }
        } catch (IOException e) {
            System.out.println("Error creando archivo:" + filename);
            e.printStackTrace();
        }

    }
}