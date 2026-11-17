package com.clearview;

public enum KeyProfile {
    CREAMY("Creamy"), THOCKY("Thocky"), CLICKY("Clicky"), MARBLE("Marble");

    public final String label;

    KeyProfile(String label) {
        this.label = label;
    }

    public KeyProfile next() {
        KeyProfile[] all = values();
        return all[(ordinal() + 1) % all.length];
    }
}
