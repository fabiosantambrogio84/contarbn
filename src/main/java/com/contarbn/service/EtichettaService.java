package com.contarbn.service;

import com.contarbn.exception.OperationException;
import com.contarbn.model.Dispositivo;
import com.contarbn.model.Etichetta;
import com.contarbn.model.request.EtichettaRequest;
import com.contarbn.repository.EtichettaRepository;
import com.contarbn.util.Constants;
import com.contarbn.util.Utils;
import com.contarbn.util.enumeration.LabelPlaceholder;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;
import org.springframework.stereotype.Service;

import java.io.InputStream;
import java.io.OutputStream;
import java.math.BigDecimal;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.sql.Date;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.ZonedDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

@Slf4j
@RequiredArgsConstructor
@Service
public class EtichettaService {

    private final SimpleDateFormat simpleDateFormat = new SimpleDateFormat("dd/MM/yyyy");

    private final EtichettaRepository etichettaRepository;
    private final DispositivoService dispositivoService;

    public Map<String, Object> generate(String articolo, String ingredienti, String tracce, String conservazione, String valoriNutrizionali, Date dataConsumazione, String lotto, String peso, String barcodeEan13, String barcodeEan128, Long idDispositivo) throws Exception{

        final Map<String, Object> result = new HashMap<>();

        final InputStream templateAsInputStream = this.getClass().getResourceAsStream(Constants.LABEL_TEMPLATE);
        String template = Utils.convertInputStream(templateAsInputStream);

        template = template.replace(LabelPlaceholder.ARTICOLO.getPlaceholder(), articolo);
        template = template.replace(LabelPlaceholder.INGREDIENTI.getPlaceholder(), ingredienti);
        template = template.replace(LabelPlaceholder.TRACCE.getPlaceholder(), tracce);
        template = template.replace(LabelPlaceholder.CONSERVAZIONE.getPlaceholder(), conservazione);
        template = template.replace(LabelPlaceholder.VALORI_NUTRIZIONALI.getPlaceholder(), valoriNutrizionali);
        template = template.replace(LabelPlaceholder.CONSUMAZIONE.getPlaceholder(), createConsumazione(dataConsumazione));
        template = template.replace(LabelPlaceholder.LOTTO.getPlaceholder(), lotto);
        template = template.replace(LabelPlaceholder.PESO.getPlaceholder(), peso + " Kg");
        template = template.replace(LabelPlaceholder.BARCODE_EAN_13.getPlaceholder(), barcodeEan13);
        template = template.replace(LabelPlaceholder.BARCODE_EAN_128.getPlaceholder(), handleBarcode(barcodeEan128));

        byte[] fileContentBytes = template.getBytes(StandardCharsets.UTF_8);
        String filename = createFilename(articolo);
        String uuid = UUID.randomUUID().toString();

        Etichetta etichetta = new Etichetta();
        etichetta.setUuid(uuid);
        etichetta.setArticolo(articolo);
        etichetta.setLotto(lotto);
        etichetta.setPeso(new BigDecimal(peso));
        etichetta.setFilename(filename);
        etichetta.setFileContent(fileContentBytes);
        etichetta.setDataInserimento(Timestamp.from(ZonedDateTime.now().toInstant()));
        etichetta.setIdDispositivo(idDispositivo);
        etichettaRepository.save(etichetta);

        result.put("fileName", filename);
        result.put("fileContent", fileContentBytes);
        result.put("uuid", uuid);

        return result;
    }

    public List<Etichetta> getEtichetteToDelete(Integer days){
        int finalDays = days != null ? days : 1;
        List<Etichetta> etichette = (List<Etichetta>) etichettaRepository.findAll();
        etichette = etichette.stream().filter(e -> e.getDataInserimento().compareTo(Date.valueOf(LocalDate.now().minusDays(finalDays)))<=0).collect(Collectors.toList());
        return etichette;
    }

    public void delete(String uuid) {
        try{
            etichettaRepository.deleteById(uuid);
        } catch(Exception e){
            log.error(e.getMessage(), e);
        }
    }

    public void stampa(EtichettaRequest etichettaRequest) throws Exception {

        Map<String, Object> result = generate(etichettaRequest.getArticolo(), etichettaRequest.getIngredienti(), etichettaRequest.getTracce(), etichettaRequest.getConservazione(), etichettaRequest.getValoriNutrizionali(), etichettaRequest.getDataConsumazione(), etichettaRequest.getLotto(), etichettaRequest.getPeso(), etichettaRequest.getBarcodeEan13(), etichettaRequest.getBarcodeEan128(), etichettaRequest.getIdDispositivo());

        Dispositivo dispositivo = dispositivoService.getOne(etichettaRequest.getIdDispositivo());

        try (Socket socket = new Socket(dispositivo.getIp(), dispositivo.getPorta())) {

            OutputStream outputStream = socket.getOutputStream();
            outputStream.write((byte[])result.get("fileContent"));
            outputStream.flush();

        } catch (Exception e) {
            log.error("", e);
            throw new OperationException("Error during label printing: "+e.getMessage());
        }
    }

    private String createFilename(String articolo){
        return "label_"+ articolo.replaceAll(" ", "_").toLowerCase() +".zpl";
    }

    private String createConsumazione(Date dataConsumazione){
        return simpleDateFormat.format(dataConsumazione);
    }

    private String handleBarcode(String barcode){
        return StringUtils.isNotEmpty(barcode) ? barcode.replace(",","") : barcode;
    }
}