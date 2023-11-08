package com.contarbn.util;

import com.contarbn.model.Cliente;
import com.contarbn.model.TipoPagamento;
import com.contarbn.service.RibaService;

import java.sql.Date;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.TemporalAdjusters;
import java.util.List;

public class RibaUtils {

    public static Date getDataScadenza(Date dataFattura, TipoPagamento tipoPagamento){
        LocalDate dataFatturaLocalDate = dataFattura.toLocalDate();
        Integer scadenzaGiorni = tipoPagamento.getScadenzaGiorni();

        switch (scadenzaGiorni) {
            case 30:
                dataFatturaLocalDate = dataFatturaLocalDate.plusMonths(1).with(TemporalAdjusters.lastDayOfMonth());
                break;
            case 60:
                dataFatturaLocalDate = dataFatturaLocalDate.plusMonths(2).with(TemporalAdjusters.lastDayOfMonth());
                break;
            case 45:
                dataFatturaLocalDate = dataFatturaLocalDate.plusMonths(1).with(TemporalAdjusters.lastDayOfMonth()).plusMonths(1).withDayOfMonth(15);
            default:
                break;
        }
        return Date.valueOf(dataFatturaLocalDate);

    }

    public static Cliente getGroupedCliente(List<RibaService.PagamentoFattura> pagamentiFatture){
        Cliente cliente = pagamentiFatture.get(0).getCliente();
        for(RibaService.PagamentoFattura pagFatt : pagamentiFatture){
            Cliente c = pagFatt.getCliente();
            if(c != null && c.getNomeGruppoRiba() != null && !c.getNomeGruppoRiba().equals("")){
                return c;
            }
        }
        return cliente;
    }

    public static String createFileName(){
        String dateTimeFormatter = "yyyyMMdd_HHmmss";
        ZonedDateTime now = ZonedDateTime.now(ZoneId.of(Constants.ZONE_ID_EUROPE_ROME));
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern(dateTimeFormatter);
        String formattedNow = now.format(formatter);

        return "export_riba_" + formattedNow + ".txt";
    }
}
