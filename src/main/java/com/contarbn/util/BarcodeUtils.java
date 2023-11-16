package com.contarbn.util;

import com.contarbn.exception.BarcodeGenerationException;
import com.contarbn.exception.BarcodeMaskParsingException;
import com.contarbn.model.Articolo;
import com.contarbn.model.Produzione;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.krysalis.barcode4j.ChecksumMode;
import org.krysalis.barcode4j.HumanReadablePlacement;
import org.krysalis.barcode4j.impl.code128.EAN128Bean;
import org.krysalis.barcode4j.impl.upcean.EAN13Bean;
import org.krysalis.barcode4j.output.bitmap.BitmapCanvasProvider;

import java.awt.image.BufferedImage;
import java.sql.Date;
import java.text.SimpleDateFormat;

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

    public static String createBarcodeEan128(Produzione produzione){
        String barcodeEan128 = null;

        if(produzione != null && produzione.getScadenza() != null && StringUtils.isNotEmpty(produzione.getLotto()) && StringUtils.isNotEmpty(produzione.getBarcodeEan13()) && produzione.getQuantitaPredefinitaArticolo() != null){
            SimpleDateFormat simpleDateFormat = new SimpleDateFormat("ddMMyyyy");
            barcodeEan128 = produzione.getBarcodeEan13()+" "+produzione.getLotto()+" "+simpleDateFormat.format(produzione.getScadenza())+" "+produzione.getQuantitaPredefinitaArticolo();
            barcodeEan128 = barcodeEan128.replace(".",",");
        }

        return barcodeEan128;
    }

    public static BufferedImage generateEAN13BarcodeImage(String barcodeText) {
        try{
            EAN13Bean ean13Bean = new EAN13Bean();
            BitmapCanvasProvider canvas = getBitmapCanvasProvider(100);

            ean13Bean.setHeight(10.8d);
            ean13Bean.setModuleWidth(0.2d);
            ean13Bean.setFontSize(2.3d);
            ean13Bean.setChecksumMode(ChecksumMode.CP_IGNORE);

            ean13Bean.generateBarcode(canvas, barcodeText);

            return canvas.getBufferedImage();

        } catch(Exception e){
            log.error("Errore nella generazione del barcode EAN13", e);
            throw new BarcodeGenerationException("Errore nella generazione del barcode EAN13");
        }
    }

    public static BufferedImage generateEAN128BarcodeImage(String barcodeText) {

        try{
            EAN128Bean ean128Bean = new EAN128Bean();
            BitmapCanvasProvider canvas = getBitmapCanvasProvider(150);

            ean128Bean.setHeight(13.5d);
            ean128Bean.setBarHeight(6.5d);
            ean128Bean.setModuleWidth(0.2d);
            ean128Bean.setFontSize(1.5d);
            ean128Bean.setMsgPosition(HumanReadablePlacement.HRP_BOTTOM);
            ean128Bean.setChecksumMode(ChecksumMode.CP_ADD);

            ean128Bean.generateBarcode(canvas, barcodeText);

            return canvas.getBufferedImage();
        } catch(Exception e){
            log.error("Errore nella generazione del barcode EAN128", e);
            throw new BarcodeGenerationException("Errore nella generazione del barcode EAN128");
        }
    }

    private static BitmapCanvasProvider getBitmapCanvasProvider(int resolution){
        return new BitmapCanvasProvider(resolution, BufferedImage.TYPE_BYTE_GRAY, false, 0);
    }


}
