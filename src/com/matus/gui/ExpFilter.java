package com.matus.gui;

import javax.swing.filechooser.FileFilter;
import java.io.File;

class ExpFilter extends FileFilter {
    @Override
    public boolean accept(File f) {
        if (f.isDirectory()) {
            return false;
        }

        String s = f.getName().toLowerCase();

        return s.endsWith(".exp");
    }

    @Override
    public String getDescription() {
        return "*.exp,*.EXP";
    }
}