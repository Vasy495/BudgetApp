package com.example.budgetapp.model;

public enum Category {
    FOOD("Еда"), CLOTHES("Одежда"), FUN("Развлечения"), TRANSPORT("Транспорт"), HOBBY("Хобби");

    private final String text;

    Category(String text) {
        this.text = text;
    }

    public String getText() {
        return text;
    }
}
