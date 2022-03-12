package com.matus.gui;

import com.matus.FileHandler;
import com.matus.Generator;
import com.matus.Main;

import java.awt.*;
import java.io.File;
import java.io.IOException;
import java.util.Objects;
import javax.swing.*;

public class MainWindow extends JFrame {

    public JTextArea inputTextArea;
    private JButton genereteAutomatsButton;
    private JButton analyzeEntryButton;
    private JMenuBar fileMenuBar;
    public JTextArea outputTextArea;
    private JComboBox<String> treeComboBox;
    private JLabel jcomp7;
    private JLabel jcomp8;
    private JComboBox<String> nextComboBox;
    private JLabel jcomp10;
    private JComboBox<String> transitionComboBox;
    private JLabel jcomp12;
    private JComboBox<String> automatComboBox;
    private JLabel jcomp14;
    private JComboBox<String> reportsComboBox;
    private JPanel imagePanel;

    String openFile = "./debugentry.exp";


    JScrollPane inputScroll;
    JScrollPane outputScroll;

    public MainWindow() {





        JMenu archivoMenu = new JMenu ("Archivo");
        JMenuItem abrirItem = new JMenuItem ("Abrir");
        archivoMenu.add (abrirItem);
        JMenuItem guardarItem = new JMenuItem ("Guardar");
        archivoMenu.add (guardarItem);
        JMenuItem guardar_como___Item = new JMenuItem ("Guardar como...");
        archivoMenu.add (guardar_como___Item);

        inputTextArea = new JTextArea (5, 5);
        genereteAutomatsButton = new JButton ("Generar Automatas");
        //genereteAutomatsButton.setEnabled(false);
        analyzeEntryButton = new JButton ("Analizar Entrada");
        fileMenuBar = new JMenuBar();
        fileMenuBar.add (archivoMenu);
        outputTextArea = new JTextArea (5, 5);
        treeComboBox = new JComboBox<>();
        jcomp7 = new JLabel ("Arboles");
        jcomp8 = new JLabel ("Tabla de siguientes");
        nextComboBox = new JComboBox<> ();
        jcomp10 = new JLabel ("Tabla de Trancisiones");
        transitionComboBox = new JComboBox<> ();
        jcomp12 = new JLabel ("Automatas");
        automatComboBox = new JComboBox<> ();
        jcomp14 = new JLabel ("Ver Reportes");
        reportsComboBox = new JComboBox<> (new String[]{"Salida", "Errores"});
        imagePanel = new JPanel();

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
        add (reportsComboBox);
        add (imagePanel);

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
        reportsComboBox.setBounds (560, 495, 130, 30);
        imagePanel.setBounds (735, 20, 740, 585);
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

            //System.out.println(fc.getSelectedFile().getName() + " fue abierto xd");
            openFile = fc.getSelectedFile().getAbsolutePath();
        });





        guardarItem.addActionListener( e -> {
            FileHandler.writeToFile(openFile, inputTextArea.getText(),false);
        });

        guardar_como___Item.addActionListener(e -> {

            JFileChooser fc = new JFileChooser("./");
            fc.addChoosableFileFilter(new ExpFilter());

            int retrieval = fc.showSaveDialog(null);

            if (retrieval == JFileChooser.APPROVE_OPTION) {
                FileHandler.writeToFile(fc.getSelectedFile().getAbsolutePath() + ".exp", inputTextArea.getText(),false);
            }
            openFile = fc.getSelectedFile().getAbsolutePath() + ".exp";


        });

        analyzeEntryButton.addActionListener( e -> {
            outputTextArea.setText("");
            int result = Main.parseExpFile();
            System.out.println("Parse result: " + result);
            Main.cprintln("Return code for parse:" + result);

            //genereteAutomatsButton.setEnabled(result == 0);
            genereteAutomatsButton.setEnabled(true);

        });

        genereteAutomatsButton.addActionListener( e -> {
            Generator.generateAutomats();
        });

        treeComboBox.addActionListener( e -> {
            String selected = Objects.requireNonNull(treeComboBox.getSelectedItem()).toString();
            String path = String.format("REPORTES/ARBOLES_%s/%s.png", Main.carnet, selected);
            //System.out.println(path);
            setDisplayImage(path);
        });

        nextComboBox.addActionListener( e -> {
            String selected = Objects.requireNonNull(nextComboBox.getSelectedItem()).toString();
            String path = String.format("REPORTES/SIGUIENTES_%s/%s.html", Main.carnet, selected);
            try {
                Desktop.getDesktop().open(new File(path));
            } catch (IOException ex) {
                ex.printStackTrace();
            }
        });

        transitionComboBox.addActionListener( e -> {
            String selected = Objects.requireNonNull(transitionComboBox.getSelectedItem()).toString();
            String path = String.format("REPORTES/TRANSICIONES_%s/%s.html", Main.carnet, selected);
            try {
                Desktop.getDesktop().open(new File(path));
            } catch (IOException ex) {
                ex.printStackTrace();
            }
        });

        automatComboBox.addActionListener( e -> {
            String selected = Objects.requireNonNull(automatComboBox.getSelectedItem()).toString();
            String path;
            System.out.println("Original selected is ");
            if (selected.contains("AFD")) {
                path = String.format("REPORTES/AFD_%s/%s.png", Main.carnet, selected.substring(4));
            }
            else {
                path = String.format("REPORTES/AFND_%s/%s.png", Main.carnet, selected.substring(4));
            }
            setDisplayImage(path);
        });

        reportsComboBox.addActionListener( e -> {

            String path;
            if (Objects.requireNonNull(reportsComboBox.getSelectedItem()).toString().equals("Salida")) {
                path = String.format("REPORTES/SALIDAS_%s/validaciones.json", Main.carnet);
            }
            else {
                path = String.format("REPORTES/ERRORES_%s/Errores.html", Main.carnet);
            }

            System.out.println("sheeeesh path");
            System.out.println(path);

            try {
                Desktop.getDesktop().open(new File(path));
            } catch (IOException ex) {
                ex.printStackTrace();
            }
        });


    }


    public void setDisplayImage(String path) {
        ImageIcon image = new ImageIcon(path);
        JLabel picLabel = new JLabel(image);
        imagePanel.removeAll();
        imagePanel.add(picLabel);
        repaint();
        revalidate();
        if (Main.openImagesOnChange) {
            try {
                Desktop.getDesktop().open(new File(path));
            } catch (IOException ex) {
                ex.printStackTrace();
            }
        }

    }

    public void updateComboboxes(String[] names/*, String[][] nextData, String[][] transitionData, String[][] outputData*/) {
        DefaultComboBoxModel<String> treeModel = new DefaultComboBoxModel<>( names );
        treeComboBox.setModel(treeModel);
        DefaultComboBoxModel<String> nextModel = new DefaultComboBoxModel<>( names );
        nextComboBox.setModel(nextModel);
        DefaultComboBoxModel<String> transitionModel = new DefaultComboBoxModel<>( names );
        transitionComboBox.setModel(transitionModel);

        DefaultComboBoxModel<String> afdModel = new DefaultComboBoxModel<>();
        for (String name : names) {
            afdModel.addElement("AFD_" + name);
            afdModel.addElement("AFN_" + name);
        }
        automatComboBox.setModel(afdModel);




    }

}
