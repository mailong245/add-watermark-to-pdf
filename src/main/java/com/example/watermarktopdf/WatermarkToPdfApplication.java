package com.example.watermarktopdf;

import com.example.watermarktopdf.model.FileTable;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.ArrayList;
import java.util.List;

@SpringBootApplication
@Configuration
public class WatermarkToPdfApplication {

    public static void main(String[] args) {
        SpringApplication.run(WatermarkToPdfApplication.class, args);
    }

    @Bean
    List<FileTable> fileTable() {
        List<FileTable> list = new ArrayList<>();
        list.add(0, new FileTable(0, "Lorem ipsum Lorem ipsum dolor sit amet.pdf", "./src/main/resources/document/pdf/", true));
        list.add(1, new FileTable(1, "file-example_PDF_1MB.pdf", "./src/main/resources/document/pdf/", true));
        list.add(2, new FileTable(2, "Portrait.pdf", "./src/main/resources/document/pdf/", true));
        list.add(3, new FileTable(3, "sample3.pdf", "./src/main/resources/document/pdf/", true));
        list.add(4, new FileTable(4, "picture.pdf", "./src/main/resources/document/pdf/", true));

        list.add(5, new FileTable(5, "testIMG1.img", "./src/main/resources/document/img/", true));

        list.add(6, new FileTable(6, "testJPG1.jpg", "./src/main/resources/document/jpg/", true));
        list.add(7, new FileTable(7, "testJPG2.jpg", "./src/main/resources/document/jpg/", true));

        list.add(8, new FileTable(8, "testPNG1.png", "./src/main/resources/document/png/", true));

        return list;
    }

}
