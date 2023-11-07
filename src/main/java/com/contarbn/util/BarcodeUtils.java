package com.contarbn.util;

import com.contarbn.exception.BarcodeMaskParsingException;
import lombok.extern.slf4j.Slf4j;
import org.krysalis.barcode4j.impl.code128.EAN128Bean;
import org.krysalis.barcode4j.impl.upcean.EAN13Bean;
import org.krysalis.barcode4j.output.bitmap.BitmapCanvasProvider;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;

@Slf4j
public class BarcodeUtils {

    public static String createRegexpLotto(String barcodeMask){
        String mask = barcodeMask.toUpperCase();

        if(!Utils.stringContainsOnlyCertainCharacters(mask)){
            log.error("La maschera barcode può contenere solo i caratteri X, L, A, M, G");
            throw new BarcodeMaskParsingException("La maschera barcode può contenere solo i caratteri X, L, A, M, G");
        }

        String lottoRegexp;

        int lottoStart = mask.indexOf('L');
        if(lottoStart == -1){
            log.error("La maschera barcode non contiene il carattere L");
            throw new BarcodeMaskParsingException("La maschera barcode non contiene il carattere L");
        }

        int lottoLength = 0;
        for(int i=lottoStart; i<mask.length(); i++){
            char ch = mask.charAt(i);
            if(ch == 'L'){
                lottoLength += 1;
            }
        }
        lottoRegexp = Constants.BARCODE_REGEXP.replace("start", String.valueOf(lottoStart)).replace("length", String.valueOf(lottoLength));

        if(lottoRegexp == null){
            log.error("Errore nella gestione della maschera barcode");
            throw new BarcodeMaskParsingException("Errore nella gestione della maschera barcode");
        }

        return lottoRegexp;
    }

    public static String createRegexpDataScadenza(String barcodeMask){
        String mask = barcodeMask.toUpperCase();

        if(!Utils.stringContainsOnlyCertainCharacters(mask)){
            log.error("La maschera barcode può contenere solo i caratteri X, L, A, M, G");
            throw new BarcodeMaskParsingException("La maschera barcode può contenere solo i caratteri X, L, A, M, G");
        }

        String dataScadenzaRegexp;

        int dataScadenzaStart = -1;
        int dataScadenzaStartYear = mask.indexOf("XA");
        int dataScadenzaStartDay = mask.indexOf("XG");

        if(dataScadenzaStartYear != -1){
            dataScadenzaStart = dataScadenzaStartYear + 1;
        } else if(dataScadenzaStartDay != -1){
            dataScadenzaStart = dataScadenzaStartDay + 1;
        }

        if(dataScadenzaStart == -1){
            throw new RuntimeException("");
        }

        int dataScadenzaLength = 0;
        for(int i=dataScadenzaStart; i<mask.length(); i++){
            char ch = mask.charAt(i);
            if(ch == 'A' || ch == 'M' || ch == 'G'){
                dataScadenzaLength += 1;
            }
        }
        dataScadenzaRegexp = Constants.BARCODE_REGEXP.replace("start", String.valueOf(dataScadenzaStart)).replace("length", String.valueOf(dataScadenzaLength));

        if(dataScadenzaRegexp == null){
            log.error("Errore nella gestione della maschera barcode");
            throw new BarcodeMaskParsingException("Errore nella gestione della maschera barcode");
        }

        return dataScadenzaRegexp;
    }

    public static BufferedImage generateEAN13BarcodeImage(String barcodeText) {
        EAN13Bean barcodeGenerator = new EAN13Bean();
        BitmapCanvasProvider canvas = getBitmapCanvasProvider(100);

        barcodeGenerator.setHeight(10.8d);
        barcodeGenerator.setModuleWidth(0.2d);
        barcodeGenerator.setFontSize(2.3d);

        barcodeGenerator.generateBarcode(canvas, barcodeText);

        return canvas.getBufferedImage();
    }

    public static BufferedImage generateEAN128BarcodeImage(String barcodeText) {
        EAN128Bean barcodeGenerator = new EAN128Bean();
        BitmapCanvasProvider canvas = getBitmapCanvasProvider(150);

        barcodeGenerator.setHeight(13.5d);
        barcodeGenerator.setBarHeight(6.5d);
        barcodeGenerator.setModuleWidth(0.2d);
        barcodeGenerator.setFontSize(1.5d);

        barcodeGenerator.generateBarcode(canvas, barcodeText);
        return canvas.getBufferedImage();
    }

    private static BitmapCanvasProvider getBitmapCanvasProvider(int resolution){
        return new BitmapCanvasProvider(resolution, BufferedImage.TYPE_BYTE_BINARY, false, 0);
    }

    public static void main(String[] args) throws Exception{


        BufferedImage imageEan13 = BarcodeUtils.generateEAN13BarcodeImage("978020137962");
        System.out.println(imageEan13.getWidth() + " - "+imageEan13.getHeight());

        //Image resultingImage = imageEan13.getScaledInstance(110, 40, Image.SCALE_DEFAULT);
        //BufferedImage outputImage = new BufferedImage(110, 40, BufferedImage.TYPE_BYTE_BINARY);
        //.getGraphics().drawImage(resultingImage, 0, 0, null);

        ImageIO.write(imageEan13, "png", new File("C:\\temp\\image_ean13_ORIG.png"));
        //ImageIO.write(outputImage, "png", new File("C:\\temp\\image_ean13.png"));

        // 18 = X * 25,4 * 94

        BufferedImage imageEan128 = BarcodeUtils.generateEAN128BarcodeImage("8013554101064");
        System.out.println(imageEan128.getWidth() + " - "+imageEan128.getHeight());

        //Image resultingImage = imageEan128.getScaledInstance(180, 50, Image.SCALE_DEFAULT);
        //BufferedImage outputImage = new BufferedImage(180, 50, BufferedImage.TYPE_BYTE_BINARY);
        //outputImage.getGraphics().drawImage(resultingImage, 0, 0, null);

        ImageIO.write(imageEan128, "png", new File("C:\\temp\\image_ean128_ORIG.png"));
        //ImageIO.write(outputImage, "png", new File("C:\\temp\\image_ean128.png"));

    }

}
