package com.example.budgetapp.services;

import com.example.budgetapp.model.Transaction;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Path;
import java.time.Month;

public interface BudgetService {
    int getDailyBudget();
    int getBalance();

    //Добавление транзакции
    long addTransaction(Transaction transaction);

    Transaction getTransaction(long id);

    Transaction editTransaction(long id, Transaction transaction);

    boolean deleteTransaction(long id);

    void deleteAllTransaction();

    //Сколько мы можем потратить согласно бюджету сегодня
    int getDailyBalance();

    //Метод подсчета сколько уже потратили в этом месяце
    int getAllSpend();

    int getVacationBonus(int daysCount);

    int getSalaryWithVacation(int vacationDaysCount, int vacationWorkingDaysCount, int workingDaysInMonth);

    Path createMonthlyReport(Month month) throws IOException;

    void addTransactionsFromInputStream(InputStream inputStream) throws IOException;
}
