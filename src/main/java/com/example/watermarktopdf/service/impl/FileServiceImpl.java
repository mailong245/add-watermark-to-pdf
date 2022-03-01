package com.example.watermarktopdf.service.impl;

import com.example.watermarktopdf.model.FileTable;
import com.example.watermarktopdf.service.FileService;
import com.itextpdf.io.font.FontConstants;
import com.itextpdf.io.font.PdfEncodings;
import com.itextpdf.io.image.ImageData;
import com.itextpdf.io.image.ImageDataFactory;
import com.itextpdf.kernel.color.Color;
import com.itextpdf.kernel.font.PdfFont;
import com.itextpdf.kernel.font.PdfFontFactory;
import com.itextpdf.kernel.geom.Rectangle;
import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfPage;
import com.itextpdf.kernel.pdf.PdfReader;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.kernel.pdf.canvas.PdfCanvas;
import com.itextpdf.kernel.pdf.extgstate.PdfExtGState;
import com.itextpdf.layout.Canvas;
import com.itextpdf.layout.Document;
import com.itextpdf.layout.element.Paragraph;
import com.itextpdf.layout.property.TextAlignment;
import com.itextpdf.layout.property.VerticalAlignment;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static java.awt.Color.GRAY;

@Service
public class FileServiceImpl implements FileService {

    @Value("${application.watermark.text:Watermark text}")
    String watermarkText;

    public static final String OUTPUT_DIRECTORY = "./output/";
    public static final String WATERMARK_IMG = "./src/main/resources/static/HCL_logo.png";
    public static final List<String> listExtension = Arrays.asList("png", "jpeg", "jpg");

    @Override
    public byte[] addWatermarkTextPDF(FileTable fileTable) throws Exception {
        Optional<String> fileExtension = Optional.ofNullable(fileTable.getFileName()).filter(f -> f.contains(".")).map(f -> f.substring(fileTable.getFileName().lastIndexOf(".") + 1));
        File file = new File(fileTable.getFileDirectory() + fileTable.getFileName());

        if (!fileExtension.isPresent()) {
            return Files.readAllBytes(Paths.get(file.getAbsolutePath()));
        }
        float rotationInRads;

        switch (fileExtension.get()) {
            case "png":
            case "jpeg":
            case "jpg":
                BufferedImage image = ImageIO.read(file);
                // initializes necessary graphic properties
                int fontSizeWatermark = (image.getWidth() + image.getHeight()) / 50;
                AlphaComposite alphaChannel = AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.4f);
                Font font = new Font("Arial", Font.BOLD, fontSizeWatermark);

                rotationInRads = (float) -Math.atan(((float) (image.getHeight() / 2)) / ((float) image.getWidth() / 2));

                Graphics2D g2d = (Graphics2D) image.getGraphics();
                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2d.setFont(font);
                g2d.setColor(GRAY);
                g2d.setComposite(alphaChannel);
                g2d.rotate(rotationInRads, (double) image.getWidth() / 2, (double) image.getHeight() / 2);

                // Calculate line break
                watermarkText += "\n";
                watermarkText = watermarkText.replaceAll("(.{1,50})\\s+", "$1\n");
                int centerY = image.getHeight() / 2;
                int count = 0;
                for (String line : watermarkText.split("\n")) {
                    Rectangle2D rect = g2d.getFontMetrics().getStringBounds(line, g2d);
                    int centerX = (image.getWidth() - (int) rect.getWidth()) / 2;
                    g2d.drawString(line, centerX, centerY = (count == 0) ? centerY : centerY + (int) rect.getHeight());
                    count++;
                }

                File fileOutput = new File(OUTPUT_DIRECTORY + fileTable.getFileName());
                ImageIO.write(image, fileExtension.get(), fileOutput);
                return Files.readAllBytes(Paths.get(fileOutput.getAbsolutePath()));
            case "pdf":
                ByteArrayOutputStream destinationFile = new ByteArrayOutputStream();
                PdfFont pdfFont = PdfFontFactory.createFont(FontConstants.HELVETICA, PdfEncodings.PDF_DOC_ENCODING, false);
                PdfDocument pdfDoc = new PdfDocument(new PdfReader(file), new PdfWriter(destinationFile));
                if (!fileTable.isWatermark()) {
                    pdfDoc.close();
                    return destinationFile.toByteArray();
                }
                int numberOfPages = pdfDoc.getNumberOfPages();
                PdfPage page = null;
                for (int i = 1; i <= numberOfPages; i++) {
                    page = pdfDoc.getPage(i);
                    Rectangle ps = page.getPageSize();
                    int fontSize = (int) (ps.getWidth() + ps.getHeight()) / 50;

                    rotationInRads = (float) Math.atan((ps.getHeight() / 2) / (ps.getWidth() / 2));

                    PdfCanvas position = new PdfCanvas(pdfDoc.getPage(i));
                    position.setFillColor(Color.BLACK);
                    Paragraph p = new Paragraph(watermarkText).setFontColor(Color.DARK_GRAY).setFont(pdfFont).setFontSize(fontSize).setWidth(ps.getWidth());
                    position.saveState();
                    position.setExtGState(new PdfExtGState().setFillOpacity(0.5f));
                    new Canvas(position, pdfDoc, pdfDoc.getDefaultPageSize())
                            .showTextAligned(
                                    p,
                                    ps.getWidth() / 2,
                                    ps.getHeight() / 2,
                                    pdfDoc.getPageNumber(page),
                                    TextAlignment.CENTER,
                                    VerticalAlignment.MIDDLE,
                                    rotationInRads);
                }
                assert page != null;
                page.flush();
                pdfDoc.close();
                Files.write(Paths.get("./folder/ " + fileTable.getFileName()), destinationFile.toByteArray());
                return destinationFile.toByteArray();
            default:
                throw new Exception("Invalid file");
        }
    }

    @Override
    public void addWatermarkImgPDF(String sourceFile) throws Exception {
        PdfDocument pdfDoc = new PdfDocument(new PdfReader(sourceFile), new PdfWriter(OUTPUT_DIRECTORY + "sample1.pdf"));
        Document doc = new Document(pdfDoc);
        ImageData img = ImageDataFactory.create(WATERMARK_IMG);
        img.setWidth(512);
        img.setHeight(76.8f);
        float w = img.getWidth();
        float h = img.getHeight();

        PdfExtGState gs1 = new PdfExtGState().setFillOpacity(0.5f);

        // Implement transformation matrix usage in order to scale image
        for (int i = 1; i <= pdfDoc.getNumberOfPages(); i++) {
            PdfPage pdfPage = pdfDoc.getPage(i);
            Rectangle pageSize = pdfPage.getPageSizeWithRotation();

            // When "true": in case the page has a rotation, then new content will be automatically rotated in the
            // opposite direction. On the rotated page this would look as if new content ignores page rotation.
            pdfPage.setIgnorePageRotationForContent(true);

            float x = (pageSize.getLeft() + pageSize.getRight()) / 2;
            float y = (pageSize.getTop() + pageSize.getBottom()) / 2;

            PdfCanvas over = new PdfCanvas(pdfDoc.getPage(i));
            over.saveState();
            over.setExtGState(gs1);
            over.addImage(img, w, 0, 0, h, x - (w / 2), y - (h / 2), false);
            over.restoreState();
        }
        doc.close();
    }
}
