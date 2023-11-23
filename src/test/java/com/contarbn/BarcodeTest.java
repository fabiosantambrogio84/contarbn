package com.contarbn;

import com.contarbn.model.Produzione;
import com.contarbn.util.BarcodeUtils;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.sql.Date;

public class BarcodeTest {

    private void createBarcodeImages(String barcodeEan13, String barcodeEan128) throws Exception{

        BufferedImage imageEan13 = BarcodeUtils.generateEAN13BarcodeImage(barcodeEan13);
        //System.out.println(imageEan13.getWidth() + " - "+imageEan13.getHeight());

        //Image resultingImage = imageEan13.getScaledInstance(110, 40, Image.SCALE_DEFAULT);
        //BufferedImage outputImage = new BufferedImage(110, 40, BufferedImage.TYPE_BYTE_BINARY);
        //.getGraphics().drawImage(resultingImage, 0, 0, null);

        ImageIO.write(imageEan13, "png", new File("C:\\temp\\image_ean13_ORIG.png"));
        //ImageIO.write(outputImage, "png", new File("C:\\temp\\image_ean13.png"));

        // 18 = X * 25,4 * 94

        BufferedImage imageEan128 = BarcodeUtils.generateEAN128BarcodeImage(barcodeEan128);
        //System.out.println(imageEan128.getWidth() + " - "+imageEan128.getHeight());

        Image resultingImage = imageEan128.getScaledInstance(180, 50, Image.SCALE_DEFAULT);
        BufferedImage outputImage = new BufferedImage(180, 50, BufferedImage.TYPE_BYTE_GRAY);
        outputImage.getGraphics().drawImage(resultingImage, 0, 0, null);

        ImageIO.write(imageEan128, "png", new File("C:\\temp\\image_ean128_ORIG.png"));
        //ImageIO.write(outputImage, "png", new File("C:\\temp\\image_ean128.png"));
    }

    public static void main(String[] args) throws Exception{
        String barcodeEan13 = "2200067020005";

        Produzione produzione = new Produzione();
        produzione.setBarcodeEan13(barcodeEan13);
        produzione.setLotto("232153");
        produzione.setScadenza(Date.valueOf("2023-11-27"));
        produzione.setQuantitaPredefinitaArticolo(2.123f);

        BarcodeTest barcodeTest = new BarcodeTest();

        String barcodeEan128 = BarcodeUtils.createBarcodeEan128(produzione);

        barcodeTest.createBarcodeImages(barcodeEan13, barcodeEan128);
    }
}
