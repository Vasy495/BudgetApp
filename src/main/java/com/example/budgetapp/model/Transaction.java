package com.example.budgetapp.model;


import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data // Благодарая lombok эта аннотация создает геттеры и сеттеры, equals и hashcode, пустой конструктор
@AllArgsConstructor // Дополнительно добавляем полный конструктор
@NoArgsConstructor //Создаем для маппера пустой конструктор
public class Transaction {
    private Category category;
    private int sum;
    private String comment;


}
