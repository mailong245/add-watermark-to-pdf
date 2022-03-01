package com.example.watermarktopdf.model;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
public class FileTable {
    private long id;
    private String fileName;
    private String fileDirectory;
    private boolean isWatermark;

}
