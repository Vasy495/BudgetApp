package com.example.budgetapp.services;

public interface FilesServise {
    boolean saveToFile(String json);

    String readFromFile();
}
