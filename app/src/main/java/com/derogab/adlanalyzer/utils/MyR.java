package com.derogab.adlanalyzer.utils;

import java.util.ArrayList;

public class MyR {

    public static final int NO_ID = -1;

    private static ArrayList<MyR> list = new ArrayList<>();

    private int id;
    private String name;

    private MyR() {}

    private MyR(int id, String name) {
        this.id = id;
        this.name = name;
    }

    public int getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public static void set(int id, String name) {
        list.add(new MyR(id, name));
    }

    public static int get(String name) {

        for (int i = 0; i < list.size(); i++) {

            if (list.get(i).getName().equals(name)) {
                return list.get(i).getId();
            }

        }

        return -1; // not found

    }
}
