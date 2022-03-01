package com.example.watermarktopdf.controller;

import com.example.watermarktopdf.model.FileTable;
import com.example.watermarktopdf.service.FileService;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.ResponseBody;

import java.io.IOException;
import java.util.List;

@Controller
public class DownloadFileController {

    private final List<FileTable> fileTableList;
    private final FileService fileService;

    public DownloadFileController(List<FileTable> fileTableList, FileService fileService) {
        this.fileTableList = fileTableList;
        this.fileService = fileService;
    }

    @GetMapping(value = "/downloadFile/{id}", produces = MediaType.APPLICATION_PDF_VALUE)
    @ResponseBody
    public ResponseEntity<byte[]> downloadFileWithWatermark(@PathVariable int id) {
        FileTable fileTable = fileTableList.get(id);
        return basicReturn(fileTable);
    }

//    @GetMapping(value = "/fileWithoutWatermark", produces = MediaType.APPLICATION_PDF_VALUE)
//    @ResponseBody
//    public ResponseEntity<byte[]> downloadFileWithoutWatermark() {
//        FileTable fileTable = fileTableList.get(1);
//        return basicReturn(fileTable);
//    }

    private ResponseEntity<byte[]> basicReturn(FileTable fileTable){
        byte[] pdfDocument;
        try {
            pdfDocument = fileService.addWatermarkTextPDF(fileTable);
        } catch (Exception e) {
            System.out.println(e);
            return new ResponseEntity<>(null, new HttpHeaders(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
        if(null == pdfDocument){
            return new ResponseEntity<>(null, new HttpHeaders(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
        HttpHeaders httpHeaders = new HttpHeaders();
        httpHeaders.setContentDispositionFormData(fileTable.getFileName(), fileTable.getFileName());
        return new ResponseEntity<>(pdfDocument, httpHeaders, HttpStatus.OK);
    }
}
