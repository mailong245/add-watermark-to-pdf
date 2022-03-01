package com.example.watermarktopdf.service;

import com.example.watermarktopdf.model.FileTable;

import java.io.IOException;

public interface FileService {

    byte[] addWatermarkTextPDF(FileTable fileTable) throws Exception;

    void addWatermarkImgPDF(String sourceFile) throws Exception;

}
