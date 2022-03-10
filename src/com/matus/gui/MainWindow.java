package com.matus.gui;

import com.matus.FileHandler;
import com.matus.Generator;
import com.matus.Main;

import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import javax.swing.*;

public class MainWindow extends JFrame {
    public JTextArea inputTextArea;
    private JButton genereteAutomatsButton;
    private JButton analyzeEntryButton;
    private JMenuBar fileMenuBar;
    public JTextArea outputTextArea;
    private JComboBox treeComboBox;
    private JLabel jcomp7;
    private JLabel jcomp8;
    private JComboBox nextComboBox;
    private JLabel jcomp10;
    private JComboBox transitionComboBox;
    private JLabel jcomp12;
    private JComboBox automatComboBox;
    private JLabel jcomp14;
    private JComboBox imagesComboBox;
    private JLabel jcomp16;


    JScrollPane inputScroll;
    JScrollPane outputScroll;

    public MainWindow() {

        JFrame self = this; //for some action listeners


        JMenu archivoMenu = new JMenu ("Archivo");
        JMenuItem abrirItem = new JMenuItem ("Abrir");
        archivoMenu.add (abrirItem);
        JMenuItem guardarItem = new JMenuItem ("Guardar");
        archivoMenu.add (guardarItem);
        JMenuItem guardar_como___Item = new JMenuItem ("Guardar como...");
        archivoMenu.add (guardar_como___Item);
        JMenuItem generar_xml_de_salidaItem = new JMenuItem ("Generar XML de salida");
        archivoMenu.add (generar_xml_de_salidaItem);
        String[] treeComboBoxItems = {"Item 1"};
        String[] nextComboBoxItems = {"Item 1"};
        String[] transitionComboBoxItems = {"Item 1"};
        String[] automatComboBoxItems = {"Item 1"};
        String[] imagesComboBoxItems = {"Item 1"};

        inputTextArea = new JTextArea (5, 5);
        genereteAutomatsButton = new JButton ("Generar Automatas");
        analyzeEntryButton = new JButton ("Analizar Entrada");
        fileMenuBar = new JMenuBar();
        fileMenuBar.add (archivoMenu);
        outputTextArea = new JTextArea (5, 5);
        treeComboBox = new JComboBox (treeComboBoxItems);
        jcomp7 = new JLabel ("Arboles");
        jcomp8 = new JLabel ("Tabla de siguientes");
        nextComboBox = new JComboBox (nextComboBoxItems);
        jcomp10 = new JLabel ("Tabla de Trancisiones");
        transitionComboBox = new JComboBox (transitionComboBoxItems);
        jcomp12 = new JLabel ("Automatas");
        automatComboBox = new JComboBox (automatComboBoxItems);
        jcomp14 = new JLabel ("Ver Imagenes");
        imagesComboBox = new JComboBox (imagesComboBoxItems);
        jcomp16 = new JLabel ("newLabel");

        setSize (new Dimension (1600, 900));
        setLayout (null);

        add (genereteAutomatsButton);
        add (analyzeEntryButton);
        add (fileMenuBar);
        add (treeComboBox);
        add (jcomp7);
        add (jcomp8);
        add (nextComboBox);
        add (jcomp10);
        add (transitionComboBox);
        add (jcomp12);
        add (automatComboBox);
        add (jcomp14);
        add (imagesComboBox);
        add (jcomp16);

        genereteAutomatsButton.setBounds (20, 655, 145, 25);
        analyzeEntryButton.setBounds (175, 655, 135, 25);
        fileMenuBar.setBounds (15, 10, 90, 25);
        outputTextArea.setBounds (10, 690, 1465, 160);
        treeComboBox.setBounds (570, 85, 125, 25);
        jcomp7.setBounds (570, 60, 100, 25);
        jcomp8.setBounds (570, 115, 120, 25);
        nextComboBox.setBounds (570, 140, 125, 25);
        jcomp10.setBounds (570, 170, 130, 25);
        transitionComboBox.setBounds (570, 195, 125, 25);
        jcomp12.setBounds (570, 225, 100, 25);
        automatComboBox.setBounds (570, 250, 125, 25);
        jcomp14.setBounds (560, 465, 100, 25);
        imagesComboBox.setBounds (560, 495, 130, 30);
        jcomp16.setBounds (735, 20, 740, 585);
        this.setTitle("ExpAnalyzer");

        inputScroll = new JScrollPane (inputTextArea, JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED, JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
        add(inputScroll);
        inputScroll.setBounds (10, 65, 500, 590);


        outputScroll = new JScrollPane (outputTextArea, JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED, JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
        add(outputScroll);
        outputScroll.setBounds (10, 690, 1470, 165);



        if (Main.debugLoadExp) {
            inputTextArea.setText(String.join("\n",FileHandler.readFile("./debugentry.exp",-1)));
        }

        //Listeners

        abrirItem.addActionListener(e -> {
            JFileChooser fc = new JFileChooser("./");
            int returnVal = fc.showOpenDialog(this);
            if (returnVal != JFileChooser.APPROVE_OPTION) {
                System.out.println("Selección de archivo cancelada");
                return;
            }

            File file = fc.getSelectedFile();
            String fileContent = String.join("\n", FileHandler.readFile(file,-1));
            inputTextArea.setText(fileContent);
        });


        analyzeEntryButton.addActionListener( e -> {
            outputTextArea.setText("");
            int result = Main.parseExpFile();
            System.out.println("Parse result: " + result);
            Main.cprintln("Return code for parse:" + result);

        });

        genereteAutomatsButton.addActionListener( e -> {
            Generator.generateAutomats();
        });

    }


}
