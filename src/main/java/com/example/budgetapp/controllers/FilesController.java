package com.example.budgetapp.controllers;

import com.example.budgetapp.services.FilesService;
import org.apache.commons.io.IOUtils;
import org.springframework.core.io.InputStreamResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.*;

@RestController
@RequestMapping("/files")
public class FilesController {
    private final FilesService filesService;

    public FilesController(FilesService filesService) {
        this.filesService = filesService;
    }


    //Выгрузка файла с сервера
    @GetMapping(value = "/export")
    public ResponseEntity<InputStreamResource> downloadDataFile() throws FileNotFoundException {
        File file = filesService.getDataFile();
        if (file.exists()) { //проверяем на наличие файла
            InputStreamResource resource = new InputStreamResource(new FileInputStream(file)); //Берем файл, открываем у него поток и записыввем его в ресурс
            return ResponseEntity.ok()
                    .contentType(MediaType.APPLICATION_JSON)
                    .contentLength(file.length()) //Для проверки размера исходного файла, например 345 байт
                    .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"TransactionsLog.json\"")
                    .body(resource); //Тело запроса не меняется, при этом выше меняем заголовки для правильной обработки
        } else {

            return ResponseEntity.noContent().build();  //Нет файла, выдаст 204 статус ошибки
        }
    }

    @PostMapping(value = "/import", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<Void> uploadDataFile(@RequestParam MultipartFile file) {
        filesService.cleanDataFile();
        File dataFile = filesService.getDataFile();

        try (FileOutputStream fos = new FileOutputStream(dataFile)) {
            IOUtils.copy(file.getInputStream(), fos);
            return ResponseEntity.ok().build();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();


        //Код ниже - без использования библиотеки appach commons io
        /*try (BufferedInputStream bis = new BufferedInputStream(file.getInputStream());
                FileOutputStream fos = new FileOutputStream(dataFile);
                BufferedOutputStream bos = new BufferedOutputStream(fos)) {
            byte[] buffer = new byte[1024];
            while (bis.read(buffer) > 0) {
                bos.write(buffer);
            }
        } catch (FileNotFoundException e) {
            throw new RuntimeException(e);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }*/
    }
}