package com.example.budgetapp.services.impl;

import com.example.budgetapp.model.Category;
import com.example.budgetapp.model.Transaction;
import com.example.budgetapp.services.BudgetService;
import com.example.budgetapp.services.FilesService;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.time.LocalDate;
import java.time.Month;
import java.util.*;

@Service
public class BudgetServiceImpl implements BudgetService {

    private final FilesService filesService;
    public static final int SALARY = 30_000 - 9750;
    public static final int AVG_SALARY = (10000 + 10000 + 10000 + 10000 + 10000 + 15000 + 15000 + 15000 + 15000 + 15000 + 15000 + 20000) / 12;
    public static final int SAVING = 3_000;

    public static final int DAILY_BIDGET = (SALARY - SAVING) / LocalDate.now().lengthOfMonth();
    public static int balance = 0;

    public static final double AVG_DAYS =  29.3;

    private static TreeMap<Month, LinkedHashMap<Long, Transaction>> transactions = new TreeMap<>();
    private static long lastId = 0;

    public BudgetServiceImpl(FilesService filesService) {
        this.filesService = filesService;

    }

    @PostConstruct
    public void unit() {
        try {
            readFromFile();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public int getDailyBudget() {
        return DAILY_BIDGET;
    }


    //Сколько денег всего осталось в кошельке
    @Override
    public int getBalance() {
        return SALARY - SAVING - getAllSpend();
    }

    //Добавление транзакции
    @Override
    public long addTransaction(Transaction transaction) {
       LinkedHashMap<Long, Transaction> monthTransactions = transactions.getOrDefault(LocalDate.now().getMonth(), new LinkedHashMap<>());
        monthTransactions.put(lastId, transaction);
        transactions.put(LocalDate.now().getMonth(), monthTransactions);
        saveToFile();
        return lastId++;
    }

    @Override
    public Transaction getTransaction(long id) {
        for (Map<Long, Transaction> transactionsByMonth : transactions.values()) { // Пробегаемся по каждому месяцу в поисках транзакции
            Transaction transaction = transactionsByMonth.get(id); //ищем по ключу
            if (transaction != null) {
                return transaction; // и если нашли транзакцию то возвращаем ее. Если нет, переходим на следующий месяц
            }
        }
        return null; // Если ничего не нашли
    }

    @Override
    public Transaction editTransaction(long id, Transaction transaction) {
        for (Map<Long, Transaction> transactionsByMonth : transactions.values()) {
            if (transactionsByMonth.containsKey(id)) { //Ищем есть ли в карте объекты с таким id
                transactionsByMonth.put(id, transaction); //Если находим, то полностью затираем его новыми значениями
                //transaction.setCategory(newTransaction.getCategory()); //Также можно менять просто поля через set
                saveToFile();
                return transaction;
            }
        }
        return null;
    }

    @Override
    public boolean deleteTransaction(long id) {
        for (Map<Long, Transaction> transactionsByMonth : transactions.values()) {
            if (transactionsByMonth.containsKey(id)) { //Ищем есть ли в карте объекты с таким id
                transactionsByMonth.remove(id); //Если находим, то удаляем по id
                return true;
            }
        }
        return false;
    }

    @Override
    public void deleteAllTransaction() {
        transactions = new TreeMap<>();
    }


    //Сколько мы можем потратить согласно бюджету сегодня
    @Override
    public int getDailyBalance() {
        return DAILY_BIDGET * LocalDate.now().getDayOfMonth() - getAllSpend();
    }


    //Метод подсчета сколько уже потратили в этом месяце
    @Override
    public int getAllSpend() {
        Map<Long, Transaction> monthTransactions = transactions.getOrDefault(LocalDate.now().getMonth(), new LinkedHashMap<>());
        int sum = 0;
        for (Transaction transaction : monthTransactions.values()) {
            sum += transaction.getSum();
        }
        return sum;
    }
    @Override
    public int getVacationBonus(int daysCount) {
        double avgDaysSalary = AVG_SALARY / AVG_DAYS;
        return (int) (daysCount * avgDaysSalary);
    }

    @Override
    public int getSalaryWithVacation(int vacationDaysCount, int vacationWorkingDaysCount, int workingDaysInMonth) {
        int salary = SALARY / workingDaysInMonth * (workingDaysInMonth - vacationDaysCount);
        return salary + getVacationBonus(vacationDaysCount);
    }

    @Override
    public Path createMonthlyReport(Month month) throws IOException {
        LinkedHashMap<Long, Transaction> monthlyTransactions = transactions.getOrDefault(month, new LinkedHashMap<>());
        Path path = filesService.createTempFile("monthlyReport");
        for (Transaction transaction : monthlyTransactions.values()
        ) {
            try (Writer writer = Files.newBufferedWriter(path, StandardOpenOption.APPEND)) {
                writer.append(transaction.getCategory().getText() + ": " + transaction.getSum() + " руб. - " + transaction.getComment());
                writer.append("\n");
            }
        }
        return path;
    }

    @Override
    public void addTransactionsFromInputStream(InputStream inputStream) throws IOException {
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream))) {
            String line;
            while ((line = reader.readLine()) != null) {
                String[] array = StringUtils.split(line, '|');
                Transaction transaction = new Transaction(Category.valueOf(array[0]), Integer.valueOf(array[1]), array[2]);
                addTransaction(transaction);
            }
        }
    }


    private void saveToFile() {
        try {
            DataFile dataFile = new DataFile(lastId + 1, transactions);
            String json = new ObjectMapper().writeValueAsString(dataFile); //подготовка строки через jackson - обработка объекта в json, закинули карту transactions
            filesService.saveToFile(json);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }

    private void readFromFile() {
        try {
            String json = filesService.readFromFile();
            DataFile dataFile =new ObjectMapper().readValue(json, new TypeReference<DataFile>() {
            });
            lastId = dataFile.getLastId();
            transactions = dataFile.getTransactions();
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }


    }


    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    private static class DataFile {
        private long lastId;
        private TreeMap<Month, LinkedHashMap<Long, Transaction>> transactions;
    }

}
