package com.contarbn.service;

import com.contarbn.exception.ResourceNotFoundException;
import com.contarbn.model.Etichetta;
import com.contarbn.repository.EtichettaRepository;
import com.contarbn.util.BarcodeUtils;
import com.contarbn.util.Constants;
import com.contarbn.util.Utils;
import com.contarbn.util.enumeration.LabelPlaceholder;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.sql.Date;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
public class EtichettaService {

    private final SimpleDateFormat simpleDateFormat = new SimpleDateFormat("dd/MM/yyyy");

    private final EtichettaRepository etichettaRepository;

    public EtichettaService(final EtichettaRepository etichettaRepository){
        this.etichettaRepository = etichettaRepository;
    }

    public Map<String, String> generate(Long idProduzione, String articolo, String ingredienti, String ingredienti2, String conservazione, String valoriNutrizionali, Date dataConsumazione, String lotto, Double peso, String disposizioniComune, String footer, MultipartFile barcodeEan13File, MultipartFile barcodeEan128File, String barcodeEan13, String barcodeEan128, MultipartFile bollinoFile) throws Exception{

        final Map<String, String> result = new HashMap<>();

        final InputStream templateAsInputStream = this.getClass().getResourceAsStream(Constants.LABEL_TEMPLATE);
        String template = Utils.convertInputStream(templateAsInputStream);

        template = template.replace(LabelPlaceholder.ARTICOLO.getPlaceholder(), articolo);
        template = template.replace(LabelPlaceholder.INGREDIENTI.getPlaceholder(), ingredienti);
        template = template.replace(LabelPlaceholder.INGREDIENTI_2.getPlaceholder(), ingredienti2);
        template = template.replace(LabelPlaceholder.CONSERVAZIONE.getPlaceholder(), conservazione);
        template = template.replace(LabelPlaceholder.VALORI_NUTRIZIONALI.getPlaceholder(), valoriNutrizionali);
        template = template.replace(LabelPlaceholder.CONSUMAZIONE.getPlaceholder(), createConsumazione(dataConsumazione, lotto, peso));
        if(barcodeEan13File != null){
            template = template.replace(LabelPlaceholder.BARCODE_EAN_13.getPlaceholder(), createBarcodeImgSrc(barcodeEan13File, 90, 40));
        } else if(!StringUtils.isEmpty(barcodeEan13)){
            template = template.replace(LabelPlaceholder.BARCODE_EAN_13.getPlaceholder(), createBarcodeImgSrc(barcodeEan13, Constants.BARCODE_EAN13_TYPE));
        }
        if(barcodeEan128File != null){
            template = template.replace(LabelPlaceholder.BARCODE_EAN_128.getPlaceholder(), createBarcodeImgSrc(barcodeEan128File, 180, 50));
        } else if(!StringUtils.isEmpty(barcodeEan128)){
            template = template.replace(LabelPlaceholder.BARCODE_EAN_128.getPlaceholder(), createBarcodeImgSrc(barcodeEan128, Constants.BARCODE_EAN128_TYPE));
        }

        template = template.replace(LabelPlaceholder.DISPOSIZIONI_COMUNE.getPlaceholder(), disposizioniComune);
        template = template.replace(LabelPlaceholder.FOOTER.getPlaceholder(), footer);
        template = template.replace(LabelPlaceholder.BOLLINO.getPlaceholder(), createBollinoImgSrc(bollinoFile, 59, 39));

        byte[] htmlBytes = template.getBytes(StandardCharsets.UTF_8);
        String html = new String(htmlBytes, StandardCharsets.UTF_8);
        String filename = createFilename(articolo);
        String uuid = UUID.randomUUID().toString();

        Etichetta etichetta = new Etichetta();
        etichetta.setUuid(uuid);
        etichetta.setArticolo(articolo);
        etichetta.setLotto(lotto);
        etichetta.setPeso(new BigDecimal(peso));
        etichetta.setFilename(filename);
        etichetta.setHtml(html);
        etichetta.setDataInserimento(Timestamp.from(ZonedDateTime.now().toInstant()));
        etichettaRepository.save(etichetta);

        result.put("filename", filename);
        result.put("uuid", uuid);

        return result;
    }

    public Etichetta get(String uuid){
        return etichettaRepository.findById(uuid).orElseThrow(ResourceNotFoundException::new);
    }

    public void delete(String uuid) {
        try{
            etichettaRepository.deleteById(uuid);
        } catch(Exception e){
            log.error(e.getMessage(), e);
        }
    }

    public List<Etichetta> getEtichetteToDelete(Integer days){
        int finalDays = days != null ? days : 1;
        List<Etichetta> etichette = (List<Etichetta>) etichettaRepository.findAll();
        etichette = etichette.stream().filter(e -> e.getDataInserimento().compareTo(Date.valueOf(LocalDate.now().minusDays(finalDays)))<=0).collect(Collectors.toList());
        return etichette;
    }

    private String createFilename(String articolo){
        return "label_"+ articolo.replaceAll(" ", "_").toLowerCase() +".html";
    }

    private String createConsumazione(Date dataConsumazione, String lotto, Double peso){
        return "Da consumarsi preferibilmente entro il: " +
                simpleDateFormat.format(dataConsumazione) +
                "<br/>Lotto: " +
                lotto +
                "<br/>Peso: " +
                String.valueOf(peso).replace('.',',') +
                "Kg";
    }

    private String createBarcodeImgSrc(MultipartFile file, int widthPixels, int heightPixels) throws Exception{

        BufferedImage image = resizeImage(file, widthPixels, heightPixels);

        return createImgSrc(image);
    }

    private String createBarcodeImgSrc(String barcode, String eanType) throws Exception{

        BufferedImage image = null;
        if(Constants.BARCODE_EAN13_TYPE.equals(eanType)){
            image = BarcodeUtils.generateEAN13BarcodeImage(barcode);
        } else if(Constants.BARCODE_EAN128_TYPE.equals(eanType)){
            image = BarcodeUtils.generateEAN128BarcodeImage(barcode);
        }

        return createImgSrc(image);
    }

    private String createBollinoImgSrc(MultipartFile file, int widthPixels, int heightPixels) throws Exception{

        if(file != null){
            BufferedImage image = resizeImage(file, widthPixels, heightPixels);

            return createImgSrc(image);
        } else {
            return "data:image/jpeg;base64,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";
        }

    }

    private String createImgSrc(BufferedImage image) throws IOException {
        String imgSrc = "data:image/jpeg;base64,";
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();

        ImageIO.write(image, "jpg", byteArrayOutputStream);

        imgSrc += Base64.getEncoder().encodeToString(byteArrayOutputStream.toByteArray());

        return imgSrc;
    }

    private BufferedImage resizeImage(MultipartFile file, int widthPixels, int heightPixels) throws Exception{

        BufferedImage originalImage = ImageIO.read(file.getInputStream());

        Image resultingImage = originalImage.getScaledInstance(widthPixels, heightPixels, Image.SCALE_DEFAULT);
        BufferedImage outputImage = new BufferedImage(widthPixels, heightPixels, BufferedImage.TYPE_BYTE_GRAY);
        outputImage.getGraphics().drawImage(resultingImage, 0, 0, null);

        return outputImage;
    }

    //public static void main(String[] args) {

        /*
        String articolo = "Patate al forno";
        String ingredienti = "Ingredienti: patate novelle, olio di semi di girasole, sale, salvia.";
        String ingredienti2 = "Può contenere tracce di: glutine, crostacei, pesce, latte, sedano, solfiti, molluschi, uova, soia.";
        String conservazione = "Conservare a 4°C. Dopo l’apertura consumare entro 4 giorni.";
        String valoriNutrizionali = "Valori nutrizionali medi su 100 grammi di prodotto: Energia: 576kj – 138 kcal, Grassi: 6,8g, di cui saturi: 0g, Carboidrati: 16,8g, di cui zuccheri: 1,1g, Proteine: 2,3g, Sale: 0,8g.";
        Date dataConsumazione = new Date(System.currentTimeMillis());
        String lotto = "221597";
        Double peso = 300D;
        String disposizioniComune = "Verifica le disposizioni del tuo comune";
        String footer = "Prodotto e confezionato da:<br/> URBANI ELIA E MARTA<br/>Via 11 Settembre, 17 SAN GIOVANNI ILARIONE (VR)<br/>TEL. 045/6550993 CEL. 328/4694654 www.urbanialimentari.com";

        EtichettaService etichettaService = new EtichettaService();

        Map<String, String> result = etichettaService.generate(articolo, ingredienti, ingredienti2, conservazione, valoriNutrizionali, dataConsumazione, lotto, peso, disposizioniComune, footer, null, null);

        File outputFile = new File("C:\\temp\\"+filename);
        try (FileOutputStream outputStream = new FileOutputStream(outputFile)) {
            outputStream.write(label);
        }

        try{
            File img = new File("C:\\temp\\barcode_ORIG.jpg");
            BufferedImage originalImage = ImageIO.read(img);

            Image resultingImage = originalImage.getScaledInstance(90, 50, Image.SCALE_DEFAULT);
            BufferedImage outputImage = new BufferedImage(90, 50, BufferedImage.TYPE_INT_RGB);
            outputImage.getGraphics().drawImage(resultingImage, 0, 0, null);

            ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();

            //File outputfile = new File("C:\\temp\\barcode_NEW.jpg");
            ImageIO.write(outputImage, "jpg", byteArrayOutputStream);

            Base64.getEncoder().encodeToString(byteArrayOutputStream.toByteArray());

        } catch(Exception e){
            e.printStackTrace();
        }
        */
    //}
}
