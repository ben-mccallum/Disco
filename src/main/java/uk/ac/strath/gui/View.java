package uk.ac.strath.gui;

import javax.swing.*;

public abstract class View {
    private String id;

    protected View(String id) {
        this.id = id;
    }

    public String getID() {
        return id;
    }

    public abstract JPanel getMainPanel();
}
