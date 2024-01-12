package com.contarbn.service;

import com.contarbn.model.*;
import com.contarbn.model.beans.DittaInfoSingleton;
import com.contarbn.model.reports.*;
import com.contarbn.model.views.VDdt;
import com.contarbn.model.views.VDocumentoAcquisto;
import com.contarbn.model.views.VFattura;
import com.contarbn.model.views.VGiacenzaIngrediente;
import com.contarbn.util.AccountingUtils;
import com.contarbn.util.Constants;
import com.contarbn.util.Utils;
import com.contarbn.util.enumeration.Provincia;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.sf.jasperreports.engine.JREmptyDataSource;
import net.sf.jasperreports.engine.JasperRunManager;
import net.sf.jasperreports.engine.data.JRBeanCollectionDataSource;
import org.apache.commons.lang3.StringUtils;
import org.apache.pdfbox.io.MemoryUsageSetting;
import org.apache.pdfbox.multipdf.PDFMergerUtility;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Service;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import static java.util.Comparator.naturalOrder;

@Slf4j
@RequiredArgsConstructor
@Service
public class StampaService {

    private final SimpleDateFormat simpleDateFormat = new SimpleDateFormat("dd/MM/yyyy");

    private final GiacenzaIngredienteService giacenzaIngredienteService;
    private final PagamentoService pagamentoService;
    private final DdtService ddtService;
    private final AutistaService autistaService;
    private final OrdineClienteService ordineClienteService;
    private final NotaAccreditoService notaAccreditoService;
    private final FatturaService fatturaService;
    private final FatturaAccompagnatoriaService fatturaAccompagnatoriaService;
    private final NotaResoService notaResoService;
    private final RicevutaPrivatoService ricevutaPrivatoService;
    private final OrdineFornitoreService ordineFornitoreService;
    private final ListinoService listinoService;
    private final DocumentoAcquistoService documentoAcquistoService;
    private final DdtAcquistoService ddtAcquistoService;
    private final FatturaAcquistoService fatturaAcquistoService;
    private final FatturaAccompagnatoriaAcquistoService fatturaAccompagnatoriaAcquistoService;
    private final SchedaTecnicaService schedaTecnicaService;

    private List<VGiacenzaIngrediente> getGiacenzeIngredienti(String ids){
        log.info("Retrieving the list of 'giacenze-ingredienti' with id in '{}' for creating pdf file", ids);

        List<VGiacenzaIngrediente> giacenzeIngredienti = giacenzaIngredienteService.getAll().stream()
                .filter(gi -> ids.contains(gi.getIdIngrediente().toString()))
                .sorted(Comparator.comparing(VGiacenzaIngrediente::getIngrediente))
                .sorted(Comparator.comparing(VGiacenzaIngrediente::getFornitore))
                .sorted(Comparator.comparingDouble(VGiacenzaIngrediente::getQuantita))
                .collect(Collectors.toList());
        log.info("Retrieved {} 'giacenze-ingredienti'", giacenzeIngredienti.size());
        return giacenzeIngredienti;
    }

    private List<PagamentoDataSource> getPagamenti(String ids){
        log.info("Retrieving the list of 'pagamenti' with id in '{}' for creating pdf file", ids);

        List<Pagamento> pagamenti = pagamentoService.getPagamenti().stream()
                .filter(p -> ids.contains(p.getId().toString()))
                .sorted(Comparator.comparing(Pagamento::getData).reversed())
                .collect(Collectors.toList());

        List<PagamentoDataSource> pagamentiDataSource = new ArrayList<>();
        if(!pagamenti.isEmpty()){
            pagamenti.forEach(p -> {
                PagamentoDataSource pagamentoDataSource = new PagamentoDataSource();
                pagamentoDataSource.setData(simpleDateFormat.format(p.getData()));
                String clienteFornitore = "";
                Ddt ddt = p.getDdt();
                NotaAccredito notaAccredito = p.getNotaAccredito();
                Cliente cliente = null;
                Fornitore fornitore = null;
                NotaReso notaReso = p.getNotaReso();
                if(ddt != null){
                    cliente = ddt.getCliente();
                }
                if(notaAccredito != null){
                    cliente = notaAccredito.getCliente();
                }
                if(notaReso != null){
                    fornitore = notaReso.getFornitore();
                }
                if(cliente != null){
                    if(cliente.getDittaIndividuale()){
                        clienteFornitore = cliente.getNome()+" "+cliente.getCognome();
                    } else {
                        clienteFornitore = cliente.getRagioneSociale();
                    }
                }
                if(fornitore != null){
                    clienteFornitore = fornitore.getRagioneSociale();
                }
                pagamentoDataSource.setClienteFornitore(clienteFornitore);

                pagamentoDataSource.setDescrizione(p.getDescrizione());
                pagamentoDataSource.setImporto(p.getImporto());
                if(p.getTipoPagamento() != null){
                    pagamentoDataSource.setTipo(p.getTipoPagamento().getDescrizione());
                }

                pagamentiDataSource.add(pagamentoDataSource);

            });
        }

        log.info("Retrieved {} 'pagamenti'", pagamentiDataSource.size());
        return pagamentiDataSource;
    }

    public Ddt getDdt(Long idDdt){
        log.info("Retrieving 'ddt' with id '{}' for creating pdf file", idDdt);
        Ddt ddt = ddtService.getOne(idDdt);
        log.info("Retrieved 'ddt' with id '{}'", idDdt);
        return ddt;
    }

    public DdtDataSource getDdtDataSource(Ddt ddt){
        Cliente cliente = ddt.getCliente();
        TipoPagamento tipoPagamento = cliente.getTipoPagamento();
        Agente agente = cliente.getAgente();

        DdtDataSource ddtDataSource = new DdtDataSource();
        ddtDataSource.setNumero(ddt.getProgressivo()+"/"+ddt.getAnnoContabile());
        ddtDataSource.setData(simpleDateFormat.format(ddt.getData()));
        ddtDataSource.setClientePartitaIva("");
        ddtDataSource.setClienteCodiceFiscale("");
        ddtDataSource.setCausale(ddt.getCausale() != null ? ddt.getCausale().getDescrizione() : "");
        ddtDataSource.setPagamento("");
        ddtDataSource.setAgente("");
        if(cliente != null){
            if(!StringUtils.isEmpty(cliente.getPartitaIva())){
                ddtDataSource.setClientePartitaIva(cliente.getPartitaIva());
            }
            if(!StringUtils.isEmpty(cliente.getCodiceFiscale())){
                ddtDataSource.setClienteCodiceFiscale(cliente.getCodiceFiscale());
            }
        }
        if(tipoPagamento != null){
            if(!StringUtils.isEmpty(tipoPagamento.getDescrizione())){
                ddtDataSource.setPagamento(tipoPagamento.getDescrizione());
            }
        }
        if(agente != null){
            StringBuilder sb = new StringBuilder();
            if(!StringUtils.isEmpty(agente.getNome())){
                sb.append(agente.getNome()).append(" ");
            }
            if(!StringUtils.isEmpty(agente.getCognome())){
                sb.append(agente.getCognome());
            }
            ddtDataSource.setAgente(sb.toString());
        }
        return ddtDataSource;
    }

    public List<DdtArticoloDataSource> getDdtArticoliDataSource(Ddt ddt){
        List<DdtArticoloDataSource> ddtArticoloDataSources = new ArrayList<>();
        if(ddt.getDdtArticoli() != null && !ddt.getDdtArticoli().isEmpty()){
            ddt.getDdtArticoli().forEach(da -> {
                DdtArticoloDataSource ddtArticoloDataSource = new DdtArticoloDataSource();
                ddtArticoloDataSource.setCodiceArticolo(da.getArticolo().getCodice());
                ddtArticoloDataSource.setDescrizioneArticolo(da.getArticolo().getDescrizione());
                ddtArticoloDataSource.setLotto(da.getLotto());
                Date dataScadenza = da.getScadenza();
                if(dataScadenza != null){
                    ddtArticoloDataSource.setDataScadenza(simpleDateFormat.format(dataScadenza));
                } else {
                    ddtArticoloDataSource.setDataScadenza("");
                }
                ddtArticoloDataSource.setUdm(da.getArticolo().getUnitaMisura().getEtichetta());
                ddtArticoloDataSource.setQuantita(da.getQuantita());
                ddtArticoloDataSource.setPrezzo(da.getPrezzo() != null ? Utils.roundPrice(da.getPrezzo()) : Utils.roundPrice(new BigDecimal(0)));
                ddtArticoloDataSource.setSconto(da.getSconto() != null ? Utils.roundPrice(da.getSconto()) : Utils.roundPrice(new BigDecimal(0)));
                ddtArticoloDataSource.setImponibile(da.getImponibile() != null ? Utils.roundPrice(da.getImponibile()): Utils.roundPrice(new BigDecimal(0)));
                ddtArticoloDataSource.setIva(da.getArticolo().getAliquotaIva().getValore().intValue());

                ddtArticoloDataSources.add(ddtArticoloDataSource);
            });
        }
        ddtArticoloDataSources.sort(Comparator.comparing(DdtArticoloDataSource::getCodiceArticolo).thenComparing(DdtArticoloDataSource::getDescrizioneArticolo));

        return ddtArticoloDataSources;
    }

    private List<DdtDataSource> getDdtDataSources(java.sql.Date dataDa, java.sql.Date dataA, Integer progressivo, Integer idCliente, String cliente, Integer idAgente, Integer idAutista, Integer idStato, Boolean pagato, Boolean fatturato, Float importo, Integer idTipoPagamento, Integer idArticolo){
        log.info("Retrieving the list of 'ddts' for creating pdf file");

        List<DdtDataSource> ddtDataSources = new ArrayList<>();

        List<VDdt> ddts = ddtService.getAllByFilters(-1, null, null, null, dataDa, dataA, progressivo, idCliente, cliente, idAgente, idAutista, idStato, pagato, fatturato, importo, idTipoPagamento, idArticolo);

        if(!ddts.isEmpty()){
            ddts.forEach(ddt -> {
                DdtDataSource ddtDataSource = new DdtDataSource();
                ddtDataSource.setNumero(ddt.getProgressivo().toString());
                ddtDataSource.setData(simpleDateFormat.format(ddt.getData()));
                ddtDataSource.setClienteDescrizione(ddt.getCliente());

                BigDecimal totaleAcconto = ddt.getTotaleAcconto() != null ? Utils.roundPrice(ddt.getTotaleAcconto()) : new BigDecimal(0);
                BigDecimal totale = ddt.getTotale() != null ? Utils.roundPrice(ddt.getTotale()) : new BigDecimal(0);
                ddtDataSource.setAcconto(totaleAcconto);
                ddtDataSource.setTotale(totale);
                ddtDataSource.setTotaleDaPagare(totale.subtract(totaleAcconto));

                ddtDataSources.add(ddtDataSource);
            });
        }

        log.info("Retrieved {} 'ddts'", ddtDataSources.size());
        return ddtDataSources;
    }

    private List<DocumentoAcquistoDataSource> getDocumentoAcquistoDataSources(String fornitore, String numDocumento, String tipoDocumento, java.sql.Date dataDa, java.sql.Date dataA){
        log.info("Retrieving the list of 'documento-acquisto' for creating pdf file");

        List<DocumentoAcquistoDataSource> documentoAcquistoDataSources = new ArrayList<>();

        List<VDocumentoAcquisto> documentiAcquisto = documentoAcquistoService.getAllByFilters(null, null, null, null, fornitore, numDocumento, tipoDocumento, dataDa, dataA, null, null);

        if(!documentiAcquisto.isEmpty()){
            documentiAcquisto.forEach(da -> documentoAcquistoDataSources.add(da.toDocumentoAcquistoDataSource(simpleDateFormat)));
        }

        log.info("Retrieved {} 'documento-acquisto'", documentoAcquistoDataSources.size());
        return documentoAcquistoDataSources;
    }

    public DdtAcquisto getDdtAcquisto(Long idDdtAcquisto){
        log.info("Retrieving 'ddt acquisto' with id '{}' for creating pdf file", idDdtAcquisto);
        DdtAcquisto ddtAcquisto = ddtAcquistoService.getOne(idDdtAcquisto);
        log.info("Retrieved 'ddt acquisto' with id '{}'", idDdtAcquisto);
        return ddtAcquisto;
    }

    public DdtAcquistoDataSource getDdtAcquistoDataSource(DdtAcquisto ddtAcquisto){
        Fornitore fornitore = ddtAcquisto.getFornitore();

        DdtAcquistoDataSource ddtAcquistoDataSource = new DdtAcquistoDataSource();
        ddtAcquistoDataSource.setNumero(ddtAcquisto.getNumero());
        ddtAcquistoDataSource.setData(simpleDateFormat.format(ddtAcquisto.getData()));
        ddtAcquistoDataSource.setFornitorePartitaIva("");
        ddtAcquistoDataSource.setFornitoreDescrizione("");
        ddtAcquistoDataSource.setFornitoreCodiceFiscale("");
        ddtAcquistoDataSource.setPagamento("");
        if(fornitore != null){
            if(!StringUtils.isEmpty(fornitore.getPartitaIva())){
                ddtAcquistoDataSource.setFornitorePartitaIva(fornitore.getPartitaIva());
            }
            if(!StringUtils.isEmpty(fornitore.getCodiceFiscale())){
                ddtAcquistoDataSource.setFornitoreCodiceFiscale(fornitore.getCodiceFiscale());
            }
            if(!StringUtils.isEmpty(fornitore.getPagamento())){
                ddtAcquistoDataSource.setPagamento(fornitore.getPagamento());
            }
        }
        return ddtAcquistoDataSource;
    }

    public List<DdtAcquistoArticoloDataSource> getDdtAcquistoArticoliDataSource(DdtAcquisto ddtAcquisto){
        List<DdtAcquistoArticoloDataSource> ddtAcquistoArticoloDataSources = new ArrayList<>();
        if(ddtAcquisto.getDdtAcquistoArticoli() != null && !ddtAcquisto.getDdtAcquistoArticoli().isEmpty()){
            ddtAcquisto.getDdtAcquistoArticoli().forEach(da -> {
                DdtAcquistoArticoloDataSource ddtAcquistoArticoloDataSource = new DdtAcquistoArticoloDataSource();
                ddtAcquistoArticoloDataSource.setCodiceArticolo(da.getArticolo().getCodice());
                ddtAcquistoArticoloDataSource.setDescrizioneArticolo(da.getArticolo().getDescrizione());
                ddtAcquistoArticoloDataSource.setLotto(da.getLotto());
                ddtAcquistoArticoloDataSource.setDataScadenza(da.getDataScadenza() != null ? simpleDateFormat.format(da.getDataScadenza()) : "");
                ddtAcquistoArticoloDataSource.setUdm(da.getArticolo().getUnitaMisura().getEtichetta());
                ddtAcquistoArticoloDataSource.setQuantita(da.getQuantita());
                ddtAcquistoArticoloDataSource.setPrezzo(da.getPrezzo() != null ? Utils.roundPrice(da.getPrezzo()) : Utils.roundPrice(new BigDecimal(0)));
                ddtAcquistoArticoloDataSource.setSconto(da.getSconto() != null ? Utils.roundPrice(da.getSconto()) : Utils.roundPrice(new BigDecimal(0)));
                ddtAcquistoArticoloDataSource.setImponibile(da.getImponibile() != null ? Utils.roundPrice(da.getImponibile()): Utils.roundPrice(new BigDecimal(0)));
                ddtAcquistoArticoloDataSource.setIva(da.getArticolo().getAliquotaIva().getValore().intValue());

                ddtAcquistoArticoloDataSources.add(ddtAcquistoArticoloDataSource);
            });
        }
        if(ddtAcquisto.getDdtAcquistoIngredienti() != null && !ddtAcquisto.getDdtAcquistoIngredienti().isEmpty()){
            ddtAcquisto.getDdtAcquistoIngredienti().forEach(da -> {
                DdtAcquistoArticoloDataSource ddtAcquistoArticoloDataSource = new DdtAcquistoArticoloDataSource();
                ddtAcquistoArticoloDataSource.setCodiceArticolo(da.getIngrediente().getCodice());
                ddtAcquistoArticoloDataSource.setDescrizioneArticolo(da.getIngrediente().getDescrizione());
                ddtAcquistoArticoloDataSource.setLotto(da.getLotto());
                ddtAcquistoArticoloDataSource.setDataScadenza(da.getDataScadenza() != null ? simpleDateFormat.format(da.getDataScadenza()) : "");
                ddtAcquistoArticoloDataSource.setUdm(da.getIngrediente().getUnitaMisura().getEtichetta());
                ddtAcquistoArticoloDataSource.setQuantita(da.getQuantita());
                ddtAcquistoArticoloDataSource.setPrezzo(da.getPrezzo() != null ? Utils.roundPrice(da.getPrezzo()) : Utils.roundPrice(new BigDecimal(0)));
                ddtAcquistoArticoloDataSource.setSconto(da.getSconto() != null ? Utils.roundPrice(da.getSconto()) : Utils.roundPrice(new BigDecimal(0)));
                ddtAcquistoArticoloDataSource.setImponibile(da.getImponibile() != null ? Utils.roundPrice(da.getImponibile()): Utils.roundPrice(new BigDecimal(0)));
                ddtAcquistoArticoloDataSource.setIva(da.getIngrediente().getAliquotaIva().getValore().intValue());

                ddtAcquistoArticoloDataSources.add(ddtAcquistoArticoloDataSource);
            });
        }
        ddtAcquistoArticoloDataSources.sort(Comparator.comparing(DdtAcquistoArticoloDataSource::getCodiceArticolo).thenComparing(DdtAcquistoArticoloDataSource::getDescrizioneArticolo));

        return ddtAcquistoArticoloDataSources;
    }

    private Autista getAutista(Long idAutista){
        return autistaService.getOne(idAutista);
    }

    private List<OrdineAutistaDataSource> getOrdiniAutista(Long idAutista, Date dataConsegnaDa, Date dataConsegnaA){
        log.info("Retrieving the list of 'ordini-clienti' of autista '{}', dataConsegnaDa '{}' and  dataConsegnaA '{}' for creating pdf file", idAutista, dataConsegnaDa, dataConsegnaA);

        Predicate<OrdineCliente> isOrdineClienteDataConsegnaGreaterOrEquals = ordineCliente -> {
            if(dataConsegnaDa != null){
                return ordineCliente.getDataConsegna().compareTo(dataConsegnaDa) >= 0;
            }
            return true;
        };
        Predicate<OrdineCliente> isOrdineClienteDataConsegnaLessOrEquals = ordineCliente -> {
            if(dataConsegnaA != null){
                return ordineCliente.getDataConsegna().compareTo(dataConsegnaA) <= 0;
            }
            return true;
        };
        Predicate<OrdineCliente> isOrdineClienteAutistaEquals = ordineCliente -> {
            if(idAutista != null){
                Autista autista = ordineCliente.getAutista();
                if(autista != null){
                    return autista.getId().equals(idAutista);
                }
                return false;
            }
            return true;
        };
        Predicate<OrdineCliente> isOrdineClienteStatoNotEquals = ordineCliente -> {
            StatoOrdine statoOrdine = ordineCliente.getStatoOrdine();
            if(statoOrdine != null){
                return !statoOrdine.getId().equals(2L);
            }
            return false;
        };

        List<OrdineCliente> ordiniClienti = ordineClienteService.getAll().stream()
                .filter(isOrdineClienteDataConsegnaGreaterOrEquals
                        .and(isOrdineClienteDataConsegnaLessOrEquals)
                        .and(isOrdineClienteAutistaEquals)
                        .and(isOrdineClienteStatoNotEquals))
                .collect(Collectors.toList());

        List<OrdineAutistaDataSource> ordiniAutistaDataSource = new ArrayList<>();
        if(!ordiniClienti.isEmpty()){
            ordiniClienti.forEach(oc -> {
                Integer annoContabile = oc.getAnnoContabile();
                Integer progressivo = oc.getProgressivo();
                String codiceOrdine = progressivo +"/" + annoContabile;

                String cliente = "";
                if(oc.getCliente() != null){
                    if(oc.getCliente().getDittaIndividuale()){
                        cliente = oc.getCliente().getNome() + " " + oc.getCliente().getCognome();
                    } else if(oc.getCliente().getPrivato()){
                        cliente = oc.getCliente().getNome() + " " + oc.getCliente().getCognome();
                    } else {
                        cliente = oc.getCliente().getRagioneSociale();
                    }
                }
                String puntoConsegna = "";
                if(oc.getPuntoConsegna() != null){
                    puntoConsegna = oc.getPuntoConsegna().getIndirizzo() + " " + oc.getPuntoConsegna().getLocalita();
                }

                OrdineAutistaDataSource ordineAutistaDataSource = new OrdineAutistaDataSource();
                ordineAutistaDataSource.setCodiceOrdine(codiceOrdine);
                ordineAutistaDataSource.setCliente(cliente);
                ordineAutistaDataSource.setPuntoConsegna(puntoConsegna);

                List<OrdineAutistaArticoloDataSource> ordineAutistaArticoliDataSource = new ArrayList<>();
                Set<OrdineClienteArticolo> ordineClienteArticoli = oc.getOrdineClienteArticoli();
                if(ordineClienteArticoli != null && !ordineClienteArticoli.isEmpty()){
                    ordineClienteArticoli.forEach(oca -> {

                        OrdineAutistaArticoloDataSource ordineAutistaArticoloDataSource = new OrdineAutistaArticoloDataSource();

                        String articolo = "";
                        if(oca.getArticolo() != null){
                            articolo = oca.getArticolo().getCodice() + " " + oca.getArticolo().getDescrizione();
                        }
                        ordineAutistaArticoloDataSource.setArticolo(articolo);
                        ordineAutistaArticoloDataSource.setNumeroPezzi(oca.getNumeroPezziOrdinati());
                        ordineAutistaArticoliDataSource.add(ordineAutistaArticoloDataSource);
                    });
                }

                ordineAutistaDataSource.setOrdineAutistaArticoloDataSources(ordineAutistaArticoliDataSource);

                ordiniAutistaDataSource.add(ordineAutistaDataSource);

            });
        }

        log.info("Retrieved {} 'ordini-clienti'", ordiniAutistaDataSource.size());
        return ordiniAutistaDataSource;
    }

    public NotaAccredito getNotaAccredito(Long idNotaAccredito){
        log.info("Retrieving 'nota-accredito' with id '{}' for creating pdf file", idNotaAccredito);
        NotaAccredito notaAccredito = notaAccreditoService.getOne(idNotaAccredito);
        log.info("Retrieved 'nota-accredito' with id '{}'", idNotaAccredito);
        return notaAccredito;
    }

    public NotaAccreditoDataSource getNotaAccreditoDataSource(NotaAccredito notaAccredito){
        Cliente cliente = notaAccredito.getCliente();
        TipoPagamento tipoPagamento = cliente.getTipoPagamento();
        Agente agente = cliente.getAgente();

        NotaAccreditoDataSource notaAccreditoDataSource = new NotaAccreditoDataSource();
        notaAccreditoDataSource.setNumero(notaAccredito.getProgressivo()+"/"+notaAccredito.getAnno());
        notaAccreditoDataSource.setData(simpleDateFormat.format(notaAccredito.getData()));
        notaAccreditoDataSource.setClientePartitaIva("");
        notaAccreditoDataSource.setClienteCodiceFiscale("");
        notaAccreditoDataSource.setCausale(notaAccredito.getCausale() != null ? notaAccredito.getCausale().getDescrizione() : "");
        notaAccreditoDataSource.setPagamento("");
        notaAccreditoDataSource.setAgente("");
        if(cliente != null){
            if(!StringUtils.isEmpty(cliente.getPartitaIva())){
                notaAccreditoDataSource.setClientePartitaIva(cliente.getPartitaIva());
            }
            if(!StringUtils.isEmpty(cliente.getCodiceFiscale())){
                notaAccreditoDataSource.setClienteCodiceFiscale(cliente.getCodiceFiscale());
            }
        }
        if(tipoPagamento != null){
            if(!StringUtils.isEmpty(tipoPagamento.getDescrizione())){
                notaAccreditoDataSource.setPagamento(tipoPagamento.getDescrizione());
            }
        }
        if(agente != null){
            StringBuilder sb = new StringBuilder();
            if(!StringUtils.isEmpty(agente.getNome())){
                sb.append(agente.getNome()).append(" ");
            }
            if(!StringUtils.isEmpty(agente.getCognome())){
                sb.append(agente.getCognome());
            }
            notaAccreditoDataSource.setAgente(sb.toString());
        }
        return notaAccreditoDataSource;
    }

    public List<NotaAccreditoRigaDataSource> getNotaAccreditoRigheDataSource(NotaAccredito notaAccredito){
        List<NotaAccreditoRigaDataSource> notaAccreditoRigaDataSources = new ArrayList<>();
        if(notaAccredito.getNotaAccreditoRighe() != null && !notaAccredito.getNotaAccreditoRighe().isEmpty()){
            notaAccredito.getNotaAccreditoRighe().stream().sorted(Comparator.comparing(NotaAccreditoRiga::getNumRiga, Comparator.nullsLast(naturalOrder()))).forEach(na -> {
                NotaAccreditoRigaDataSource notaAccreditoRigaDataSource = new NotaAccreditoRigaDataSource();
                notaAccreditoRigaDataSource.setCodiceArticolo(na.getArticolo() != null ? na.getArticolo().getCodice() : "");
                notaAccreditoRigaDataSource.setDescrizioneArticolo(na.getDescrizione());
                notaAccreditoRigaDataSource.setLotto(na.getLotto());
                notaAccreditoRigaDataSource.setUdm(na.getArticolo() != null ? (na.getArticolo().getUnitaMisura() != null ? na.getArticolo().getUnitaMisura().getEtichetta() : "") : "");
                notaAccreditoRigaDataSource.setQuantita(na.getQuantita());
                notaAccreditoRigaDataSource.setPrezzo(na.getPrezzo() != null ? Utils.roundPrice(na.getPrezzo()) : new BigDecimal(0));
                notaAccreditoRigaDataSource.setSconto(na.getSconto() != null ? Utils.roundPrice(na.getSconto()) : new BigDecimal(0));
                notaAccreditoRigaDataSource.setImponibile(na.getImponibile() != null ? Utils.roundPrice(na.getImponibile()) : new BigDecimal(0));
                notaAccreditoRigaDataSource.setIva(na.getAliquotaIva() != null ? na.getAliquotaIva().getValore().intValue() : null);

                notaAccreditoRigaDataSources.add(notaAccreditoRigaDataSource);
            });
        }
        return notaAccreditoRigaDataSources;
    }

    public List<NotaAccreditoTotaleDataSource> getNotaAccreditoTotaliDataSource(NotaAccredito notaAccredito){
        List<NotaAccreditoTotaleDataSource> notaAccreditoTotaleDataSources = new ArrayList<>();
        if(notaAccredito.getNotaAccreditoTotali() != null && !notaAccredito.getNotaAccreditoTotali().isEmpty()){
            notaAccredito.getNotaAccreditoTotali().forEach(na -> {
                NotaAccreditoTotaleDataSource notaAccreditoTotaleDataSource = new NotaAccreditoTotaleDataSource();
                notaAccreditoTotaleDataSource.setAliquotaIva(na.getAliquotaIva().getValore().intValue());
                notaAccreditoTotaleDataSource.setTotaleImponibile(na.getTotaleImponibile() != null ? Utils.roundPrice(na.getTotaleImponibile()) : new BigDecimal(0));
                notaAccreditoTotaleDataSource.setTotaleIva(na.getTotaleIva() != null ? Utils.roundPrice(na.getTotaleIva()) : new BigDecimal(0));

                notaAccreditoTotaleDataSources.add(notaAccreditoTotaleDataSource);
            });
        }
        return notaAccreditoTotaleDataSources.stream().sorted(Comparator.comparing(NotaAccreditoTotaleDataSource::getAliquotaIva))
                .collect(Collectors.toList());
    }

    private List<NotaAccreditoDataSource> getNotaAccreditoDataSources(java.sql.Date dataDa, java.sql.Date dataA, Integer progressivo, Float importo, String cliente, Integer idAgente, Integer idArticolo, Integer idStato){
        log.info("Retrieving the list of 'note accredito' for creating pdf file");

        List<NotaAccreditoDataSource> notaAccreditoDataSources = new ArrayList<>();

        List<NotaAccredito> noteAccredito = notaAccreditoService.search(dataDa, dataA, progressivo, importo, cliente, idAgente, idArticolo, idStato).stream()
                .sorted(Comparator.comparing(NotaAccredito::getProgressivo).reversed())
                .collect(Collectors.toList());

        if(!noteAccredito.isEmpty()){
            noteAccredito.forEach(notaAccredito -> {
                NotaAccreditoDataSource notaAccreditoDataSource = new NotaAccreditoDataSource();
                notaAccreditoDataSource.setNumero(notaAccredito.getProgressivo().toString());
                notaAccreditoDataSource.setData(simpleDateFormat.format(notaAccredito.getData()));
                Cliente notaAccreditoCliente = notaAccredito.getCliente();
                if(notaAccreditoCliente != null){
                    if(notaAccreditoCliente.getDittaIndividuale()){
                        notaAccreditoDataSource.setClienteDescrizione(notaAccreditoCliente.getNome()+" "+notaAccreditoCliente.getCognome());
                    } else if (notaAccreditoCliente.getPrivato()){
                        notaAccreditoDataSource.setClienteDescrizione(notaAccreditoCliente.getNome()+" "+notaAccreditoCliente.getCognome());
                    } else {
                        notaAccreditoDataSource.setClienteDescrizione(notaAccreditoCliente.getRagioneSociale());
                    }
                }
                BigDecimal totaleAcconto = notaAccredito.getTotaleAcconto() != null ? Utils.roundPrice(notaAccredito.getTotaleAcconto()) : new BigDecimal(0);
                BigDecimal totale = notaAccredito.getTotale() != null ? Utils.roundPrice(notaAccredito.getTotale()) : new BigDecimal(0);
                notaAccreditoDataSource.setAcconto(totaleAcconto);
                notaAccreditoDataSource.setTotale(totale);
                notaAccreditoDataSource.setTotaleDaPagare(totale.subtract(totaleAcconto));

                notaAccreditoDataSources.add(notaAccreditoDataSource);
            });
        }

        log.info("Retrieved {} 'note accredito'", notaAccreditoDataSources.size());
        return notaAccreditoDataSources;
    }

    private List<FatturaDataSource> getFatturaDataSources(java.sql.Date dataDa, java.sql.Date dataA, Integer progressivo, Float importo, String idTipoPagamento, String cliente, Integer idAgente, Integer idArticolo, Integer idStato, Integer idTipo){
        log.info("Retrieving the list of 'fatture' for creating pdf file");

        List<FatturaDataSource> fatturaDataSources = new ArrayList<>();

        List<VFattura> fatture = fatturaService.search(dataDa, dataA, progressivo, importo, idTipoPagamento, cliente, idAgente, idArticolo, idStato, idTipo);

        if(!fatture.isEmpty()){
            fatture.forEach(fattura -> {
                FatturaDataSource fatturaDataSource = new FatturaDataSource();
                fatturaDataSource.setNumero(fattura.getProgressivo().toString());
                fatturaDataSource.setData(simpleDateFormat.format(fattura.getData()));
                fatturaDataSource.setClienteDescrizione(fattura.getCliente());
                BigDecimal totaleAcconto = fattura.getTotaleAcconto() != null ? Utils.roundPrice(fattura.getTotaleAcconto()) : new BigDecimal(0);
                BigDecimal totale = fattura.getTotale() != null ? Utils.roundPrice(fattura.getTotale()) : new BigDecimal(0);
                fatturaDataSource.setAcconto(totaleAcconto);
                fatturaDataSource.setTotale(totale);
                fatturaDataSource.setTotaleDaPagare(totale.subtract(totaleAcconto));

                fatturaDataSources.add(fatturaDataSource);
            });
        }

        log.info("Retrieved {} 'fatture'", fatturaDataSources.size());
        return fatturaDataSources;
    }

    public Fattura getFattura(Long idFattura){
        log.info("Retrieving 'fattura' with id '{}' for creating pdf file", idFattura);
        Fattura fattura = fatturaService.getOne(idFattura);
        log.info("Retrieved 'fattura' with id '{}'", idFattura);
        return fattura;
    }

    private FatturaDataSource getFatturaDataSource(Fattura fattura){
        Cliente cliente = fattura.getCliente();
        TipoPagamento tipoPagamento = cliente.getTipoPagamento();
        Agente agente = cliente.getAgente();

        FatturaDataSource fatturaDataSource = new FatturaDataSource();
        fatturaDataSource.setNumero(fattura.getProgressivo()+"/"+fattura.getAnno());
        fatturaDataSource.setData(simpleDateFormat.format(fattura.getData()));
        fatturaDataSource.setClientePartitaIva("");
        fatturaDataSource.setClienteCodiceFiscale("");
        fatturaDataSource.setCausale(fattura.getCausale() != null ? fattura.getCausale().getDescrizione() : "");
        fatturaDataSource.setPagamento("");
        fatturaDataSource.setAgente("");
        if(cliente != null){
            if(!StringUtils.isEmpty(cliente.getPartitaIva())){
                fatturaDataSource.setClientePartitaIva(cliente.getPartitaIva());
            }
            if(!StringUtils.isEmpty(cliente.getCodiceFiscale())){
                fatturaDataSource.setClienteCodiceFiscale(cliente.getCodiceFiscale());
            }
        }
        if(tipoPagamento != null){
            if(!StringUtils.isEmpty(tipoPagamento.getDescrizione())){
                fatturaDataSource.setPagamento(tipoPagamento.getDescrizione());
            }
        }
        if(agente != null){
            StringBuilder sb = new StringBuilder();
            if(!StringUtils.isEmpty(agente.getNome())){
                sb.append(agente.getNome()).append(" ");
            }
            if(!StringUtils.isEmpty(agente.getCognome())){
                sb.append(agente.getCognome());
            }
            fatturaDataSource.setAgente(sb.toString());
        }
        return fatturaDataSource;
    }

    private List<FatturaRigaDataSource> getFatturaRigheDataSource(Fattura fattura){
        int numeroRiga = 0;

        List<FatturaRigaDataSource> fatturaRigaDataSources = new ArrayList<>();
        Set<FatturaDdt> fatturaDdts = fattura.getFatturaDdts();

        Comparator<FatturaDdt> compareByDdtProgressivo = (fd1, fd2) -> {
            Ddt ddt1 = fd1.getDdt();
            Ddt ddt2 = fd2.getDdt();
            return ddt1.getProgressivo().compareTo(ddt2.getProgressivo());
        };

        List<FatturaDdt> fatturaDdtsOrdered = fatturaDdts.stream().sorted(compareByDdtProgressivo).collect(Collectors.toList());

        if(fatturaDdtsOrdered != null && !fatturaDdtsOrdered.isEmpty()){
            for(FatturaDdt fatturaDdt : fatturaDdtsOrdered){
                Ddt ddt = fatturaDdt.getDdt();
                if(ddt != null){
                    String descrizione = "Riferimento ns. DDT n. "+ddt.getProgressivo()+"/"+ddt.getAnnoContabile()+" del "+simpleDateFormat.format(ddt.getData());
                    FatturaRigaDataSource fatturaRigaDdtDataSource = new FatturaRigaDataSource();
                    fatturaRigaDdtDataSource.setNumeroRiga(numeroRiga);
                    fatturaRigaDdtDataSource.setDescrizioneArticolo(descrizione);
                    fatturaRigaDdtDataSource.setCodiceArticolo("");
                    fatturaRigaDdtDataSource.setLotto("");
                    fatturaRigaDataSources.add(fatturaRigaDdtDataSource);

                    numeroRiga += 1;

                    if(ddt.getDdtArticoli() != null && !ddt.getDdtArticoli().isEmpty()){
                        List<DdtArticolo> ddtArticoli = ddt.getDdtArticoli().stream().sorted(Comparator.comparing(da->da.getArticolo().getCodice())).collect(Collectors.toList());
                        for(DdtArticolo da : ddtArticoli){
                            Articolo articolo = da.getArticolo();

                            FatturaRigaDataSource fatturaRigaDataSource = new FatturaRigaDataSource();
                            fatturaRigaDataSource.setNumeroRiga(numeroRiga);
                            fatturaRigaDataSource.setCodiceArticolo(articolo != null ? articolo.getCodice() : "");
                            fatturaRigaDataSource.setDescrizioneArticolo(articolo != null ? articolo.getDescrizione() : "");
                            fatturaRigaDataSource.setLotto(da.getLotto());
                            fatturaRigaDataSource.setUdm(articolo != null ? (articolo.getUnitaMisura() != null ? articolo.getUnitaMisura().getEtichetta() : "") : "");
                            fatturaRigaDataSource.setQuantita(da.getQuantita());

                            BigDecimal prezzo = da.getPrezzo() != null ? Utils.roundPrice(da.getPrezzo()) : new BigDecimal(0);
                            BigDecimal sconto = da.getSconto() != null ? Utils.roundPrice(da.getSconto()) : new BigDecimal(0);
                            fatturaRigaDataSource.setPrezzo(prezzo);
                            fatturaRigaDataSource.setSconto(sconto);
                            fatturaRigaDataSource.setImponibile(da.getImponibile() != null ? Utils.roundPrice(da.getImponibile()) : new BigDecimal(0));

                            fatturaRigaDataSource.setIva(articolo != null ? (articolo.getAliquotaIva() != null ? articolo.getAliquotaIva().getValore().intValue() : null) : null);

                            fatturaRigaDataSources.add(fatturaRigaDataSource);

                            numeroRiga += 1;
                        }
                    }
                }
            }
        }

        return fatturaRigaDataSources.stream().sorted(Comparator.comparing(FatturaRigaDataSource::getNumeroRiga))
                .collect(Collectors.toList());
    }

    private List<FatturaTotaleDataSource> getFatturaTotaliDataSource(Fattura fattura){
        List<FatturaTotaleDataSource> fatturaTotaleDataSources = new ArrayList<>();

        Map<AliquotaIva, BigDecimal> imponibiliByIva = AccountingUtils.createFatturaTotaliImponibiliByIva(fattura);

        for(AliquotaIva aliquotaIva : imponibiliByIva.keySet()){
            BigDecimal ivaValore = aliquotaIva.getValore();
            BigDecimal imponibile = imponibiliByIva.get(aliquotaIva);
            BigDecimal totaleIva = imponibile.multiply(ivaValore.divide(new BigDecimal(100)));

            FatturaTotaleDataSource fatturaTotaleDataSource = new FatturaTotaleDataSource();
            fatturaTotaleDataSource.setAliquotaIva(ivaValore.intValue());
            fatturaTotaleDataSource.setTotaleImponibile(Utils.roundPrice(imponibile));
            fatturaTotaleDataSource.setTotaleIva(Utils.roundPrice(totaleIva));
            fatturaTotaleDataSource.setTotaleIvaNotRounded(totaleIva);

            fatturaTotaleDataSources.add(fatturaTotaleDataSource);
        }

        return fatturaTotaleDataSources.stream().sorted(Comparator.comparing(FatturaTotaleDataSource::getAliquotaIva))
                .collect(Collectors.toList());
    }

    private FatturaAccompagnatoria getFatturaAccompagnatoria(Long idFatturaAccompagnatoria){
        log.info("Retrieving 'fattura accompagnatoria' with id '{}' for creating pdf file", idFatturaAccompagnatoria);
        FatturaAccompagnatoria fatturaAccompagnatoria = fatturaAccompagnatoriaService.getOne(idFatturaAccompagnatoria);
        log.info("Retrieved 'fattura accompagnatoria' with id '{}'", idFatturaAccompagnatoria);
        return fatturaAccompagnatoria;
    }

    private FatturaAccompagnatoriaDataSource getFatturaAccompagnatoriaDataSource(FatturaAccompagnatoria fatturaAccompagnatoria){
        Cliente cliente = fatturaAccompagnatoria.getCliente();
        TipoPagamento tipoPagamento = cliente.getTipoPagamento();
        Agente agente = cliente.getAgente();

        FatturaAccompagnatoriaDataSource fatturaAccompagnatoriaDataSource = new FatturaAccompagnatoriaDataSource();
        fatturaAccompagnatoriaDataSource.setNumero(fatturaAccompagnatoria.getProgressivo()+"/"+fatturaAccompagnatoria.getAnno());
        fatturaAccompagnatoriaDataSource.setData(simpleDateFormat.format(fatturaAccompagnatoria.getData()));
        fatturaAccompagnatoriaDataSource.setClientePartitaIva("");
        fatturaAccompagnatoriaDataSource.setClienteCodiceFiscale("");
        fatturaAccompagnatoriaDataSource.setCausale(fatturaAccompagnatoria.getCausale() != null ? fatturaAccompagnatoria.getCausale().getDescrizione() : "");
        fatturaAccompagnatoriaDataSource.setPagamento("");
        fatturaAccompagnatoriaDataSource.setAgente("");
        if(cliente != null){
            if(!StringUtils.isEmpty(cliente.getPartitaIva())){
                fatturaAccompagnatoriaDataSource.setClientePartitaIva(cliente.getPartitaIva());
            }
            if(!StringUtils.isEmpty(cliente.getCodiceFiscale())){
                fatturaAccompagnatoriaDataSource.setClienteCodiceFiscale(cliente.getCodiceFiscale());
            }
        }
        if(tipoPagamento != null){
            if(!StringUtils.isEmpty(tipoPagamento.getDescrizione())){
                fatturaAccompagnatoriaDataSource.setPagamento(tipoPagamento.getDescrizione());
            }
        }
        if(agente != null){
            StringBuilder sb = new StringBuilder();
            if(!StringUtils.isEmpty(agente.getNome())){
                sb.append(agente.getNome()).append(" ");
            }
            if(!StringUtils.isEmpty(agente.getCognome())){
                sb.append(agente.getCognome());
            }
            fatturaAccompagnatoriaDataSource.setAgente(sb.toString());
        }
        return fatturaAccompagnatoriaDataSource;
    }

    private List<FatturaAccompagnatoriaRigaDataSource> getFatturaAccompagnatoriaRigheDataSource(FatturaAccompagnatoria fatturaAccompagnatoria){
        List<FatturaAccompagnatoriaRigaDataSource> fatturaAccompagnatoriaRigaDataSources = new ArrayList<>();
        if(fatturaAccompagnatoria.getFatturaAccompagnatoriaArticoli() != null && !fatturaAccompagnatoria.getFatturaAccompagnatoriaArticoli().isEmpty()){
            fatturaAccompagnatoria.getFatturaAccompagnatoriaArticoli().forEach(faa -> {
                Articolo articolo = faa.getArticolo();
                FatturaAccompagnatoriaRigaDataSource fatturaAccompagnatoriaRigaDataSource = new FatturaAccompagnatoriaRigaDataSource();
                fatturaAccompagnatoriaRigaDataSource.setCodiceArticolo(articolo != null ? articolo.getCodice() : "");
                fatturaAccompagnatoriaRigaDataSource.setDescrizioneArticolo(articolo != null ? articolo.getDescrizione() : "");
                fatturaAccompagnatoriaRigaDataSource.setScadenza(faa.getScadenza() != null ? simpleDateFormat.format(faa.getScadenza()) : "");
                fatturaAccompagnatoriaRigaDataSource.setLotto(faa.getLotto());
                fatturaAccompagnatoriaRigaDataSource.setUdm(articolo != null ? (articolo.getUnitaMisura() != null ? articolo.getUnitaMisura().getEtichetta() : "") : "");
                fatturaAccompagnatoriaRigaDataSource.setQuantita(faa.getQuantita());
                fatturaAccompagnatoriaRigaDataSource.setPrezzo(faa.getPrezzo() != null ? Utils.roundPrice(faa.getPrezzo()) : new BigDecimal(0));
                fatturaAccompagnatoriaRigaDataSource.setSconto(faa.getSconto() != null ? Utils.roundPrice(faa.getSconto()) : new BigDecimal(0));
                fatturaAccompagnatoriaRigaDataSource.setImponibile(faa.getImponibile() != null ? Utils.roundPrice(faa.getImponibile()) : new BigDecimal(0));
                fatturaAccompagnatoriaRigaDataSource.setIva(articolo != null ? (articolo.getAliquotaIva() != null ? articolo.getAliquotaIva().getValore().intValue() : null) : null);

                fatturaAccompagnatoriaRigaDataSources.add(fatturaAccompagnatoriaRigaDataSource);
            });
        }
        fatturaAccompagnatoriaRigaDataSources.sort(Comparator.comparing(FatturaAccompagnatoriaRigaDataSource::getCodiceArticolo).thenComparing(FatturaAccompagnatoriaRigaDataSource::getDescrizioneArticolo));
        return fatturaAccompagnatoriaRigaDataSources;
    }

    private List<FatturaAccompagnatoriaTotaleDataSource> getFatturaAccompagnatoriaTotaliDataSource(FatturaAccompagnatoria fatturaAccompagnatoria){
        List<FatturaAccompagnatoriaTotaleDataSource> fatturaAccompagnatoriaTotaleDataSources = new ArrayList<>();
        if(fatturaAccompagnatoria.getFatturaAccompagnatoriaTotali() != null && !fatturaAccompagnatoria.getFatturaAccompagnatoriaTotali() .isEmpty()){
            fatturaAccompagnatoria.getFatturaAccompagnatoriaTotali().forEach(fat -> {
                FatturaAccompagnatoriaTotaleDataSource fatturaAccompagnatoriaTotaleDataSource = new FatturaAccompagnatoriaTotaleDataSource();
                fatturaAccompagnatoriaTotaleDataSource.setAliquotaIva(fat.getAliquotaIva().getValore().intValue());
                fatturaAccompagnatoriaTotaleDataSource.setTotaleImponibile(fat.getTotaleImponibile() != null ? Utils.roundPrice(fat.getTotaleImponibile()) : new BigDecimal(0));
                fatturaAccompagnatoriaTotaleDataSource.setTotaleIva(fat.getTotaleIva() != null ? Utils.roundPrice(fat.getTotaleIva()) : new BigDecimal(0));

                fatturaAccompagnatoriaTotaleDataSources.add(fatturaAccompagnatoriaTotaleDataSource);
            });
        }
        return fatturaAccompagnatoriaTotaleDataSources.stream().sorted(Comparator.comparing(FatturaAccompagnatoriaTotaleDataSource::getAliquotaIva))
                .collect(Collectors.toList());
    }

    private FatturaAcquisto getFatturaAcquisto(Long idFatturaAcquisto){
        log.info("Retrieving 'fattura acquisto' with id '{}' for creating pdf file", idFatturaAcquisto);
        FatturaAcquisto fatturaAcquisto = fatturaAcquistoService.getOne(idFatturaAcquisto);
        log.info("Retrieved 'fattura acquisto' with id '{}'", idFatturaAcquisto);
        return fatturaAcquisto;
    }

    private FatturaAcquistoDataSource getFatturaAcquistoDataSource(FatturaAcquisto fatturaAcquisto){
        Fornitore fornitore = fatturaAcquisto.getFornitore();

        FatturaAcquistoDataSource fatturaAcquistoDataSource = new FatturaAcquistoDataSource();
        fatturaAcquistoDataSource.setNumero(fatturaAcquisto.getNumero());
        fatturaAcquistoDataSource.setData(simpleDateFormat.format(fatturaAcquisto.getData()));
        fatturaAcquistoDataSource.setFornitorePartitaIva("");
        fatturaAcquistoDataSource.setFornitoreCodiceFiscale("");
        fatturaAcquistoDataSource.setCausale(fatturaAcquisto.getCausale() != null ? fatturaAcquisto.getCausale().getDescrizione() : "");
        fatturaAcquistoDataSource.setPagamento("");
        fatturaAcquistoDataSource.setAgente("");
        if(fornitore != null){
            if(!StringUtils.isEmpty(fornitore.getPartitaIva())){
                fatturaAcquistoDataSource.setFornitorePartitaIva(fornitore.getPartitaIva());
            }
            if(!StringUtils.isEmpty(fornitore.getCodiceFiscale())){
                fatturaAcquistoDataSource.setFornitoreCodiceFiscale(fornitore.getCodiceFiscale());
            }
            if(!StringUtils.isEmpty(fornitore.getPagamento())){
                fatturaAcquistoDataSource.setPagamento(fornitore.getPagamento());
            }
        }
        return fatturaAcquistoDataSource;
    }

    private List<FatturaAcquistoRigaDataSource> getFatturaAcquistoRigheDataSource(FatturaAcquisto fatturaAcquisto){
        int numeroRiga = 0;

        List<FatturaAcquistoRigaDataSource> fatturaAcquistoRigaDataSources = new ArrayList<>();
        Set<FatturaAcquistoDdtAcquisto> fatturaAcquistoDdtAcquisti = fatturaAcquisto.getFatturaAcquistoDdtAcquisti();

        Comparator<FatturaAcquistoDdtAcquisto> compareByDdtAcquistoProgressivo = (fd1, fd2) -> {
            DdtAcquisto ddtAcquisto1 = fd1.getDdtAcquisto();
            DdtAcquisto ddtAcquisto2 = fd2.getDdtAcquisto();
            return ddtAcquisto1.getNumero().compareTo(ddtAcquisto2.getNumero());
        };

        List<FatturaAcquistoDdtAcquisto> fatturaAcquistoDdtAcquistiOrdered = fatturaAcquistoDdtAcquisti.stream().sorted(compareByDdtAcquistoProgressivo).collect(Collectors.toList());

        if(fatturaAcquistoDdtAcquistiOrdered != null && !fatturaAcquistoDdtAcquistiOrdered.isEmpty()){
            for(FatturaAcquistoDdtAcquisto fatturaAcquistoDdtAcquisto : fatturaAcquistoDdtAcquistiOrdered){
                DdtAcquisto ddtAcquisto = fatturaAcquistoDdtAcquisto.getDdtAcquisto();
                if(ddtAcquisto != null){
                    String descrizione = "Riferimento ns. DDT n. "+ddtAcquisto.getNumero()+" del "+simpleDateFormat.format(ddtAcquisto.getData());
                    FatturaAcquistoRigaDataSource fatturaAcquistoRigaDdtDataSource = new FatturaAcquistoRigaDataSource();
                    fatturaAcquistoRigaDdtDataSource.setNumeroRiga(numeroRiga);
                    fatturaAcquistoRigaDdtDataSource.setDescrizioneArticolo(descrizione);
                    fatturaAcquistoRigaDdtDataSource.setCodiceArticolo("");
                    fatturaAcquistoRigaDdtDataSource.setLotto("");
                    fatturaAcquistoRigaDataSources.add(fatturaAcquistoRigaDdtDataSource);

                    numeroRiga += 1;

                    if(ddtAcquisto.getDdtAcquistoArticoli() != null && !ddtAcquisto.getDdtAcquistoArticoli().isEmpty()){
                        List<DdtAcquistoArticolo> ddtAcquistoArticoli = ddtAcquisto.getDdtAcquistoArticoli().stream().sorted(Comparator.comparing(da->da.getArticolo().getCodice())).collect(Collectors.toList());
                        for(DdtAcquistoArticolo da : ddtAcquistoArticoli){
                            Articolo articolo = da.getArticolo();

                            FatturaAcquistoRigaDataSource fatturaAcquistoRigaDataSource = new FatturaAcquistoRigaDataSource();
                            fatturaAcquistoRigaDataSource.setNumeroRiga(numeroRiga);
                            fatturaAcquistoRigaDataSource.setCodiceArticolo(articolo != null ? articolo.getCodice() : "");
                            fatturaAcquistoRigaDataSource.setDescrizioneArticolo(articolo != null ? articolo.getDescrizione() : "");
                            fatturaAcquistoRigaDataSource.setLotto(da.getLotto());
                            fatturaAcquistoRigaDataSource.setUdm(articolo != null ? (articolo.getUnitaMisura() != null ? articolo.getUnitaMisura().getEtichetta() : "") : "");
                            fatturaAcquistoRigaDataSource.setQuantita(da.getQuantita());

                            BigDecimal prezzo = da.getPrezzo() != null ? Utils.roundPrice(da.getPrezzo()) : new BigDecimal(0);
                            BigDecimal sconto = da.getSconto() != null ? Utils.roundPrice(da.getSconto()) : new BigDecimal(0);
                            fatturaAcquistoRigaDataSource.setPrezzo(prezzo);
                            fatturaAcquistoRigaDataSource.setSconto(sconto);
                            fatturaAcquistoRigaDataSource.setImponibile(da.getImponibile() != null ? Utils.roundPrice(da.getImponibile()) : new BigDecimal(0));

                            fatturaAcquistoRigaDataSource.setIva(articolo != null ? (articolo.getAliquotaIva() != null ? articolo.getAliquotaIva().getValore().intValue() : null) : null);

                            fatturaAcquistoRigaDataSources.add(fatturaAcquistoRigaDataSource);

                            numeroRiga += 1;
                        }
                    }
                    if(ddtAcquisto.getDdtAcquistoIngredienti() != null && !ddtAcquisto.getDdtAcquistoIngredienti().isEmpty()){
                        List<DdtAcquistoIngrediente> ddtAcquistoIngredienti = ddtAcquisto.getDdtAcquistoIngredienti().stream().sorted(Comparator.comparing(da->da.getIngrediente().getCodice())).collect(Collectors.toList());
                        for(DdtAcquistoIngrediente di : ddtAcquistoIngredienti){
                            Ingrediente ingrediente = di.getIngrediente();

                            FatturaAcquistoRigaDataSource fatturaAcquistoRigaDataSource = new FatturaAcquistoRigaDataSource();
                            fatturaAcquistoRigaDataSource.setNumeroRiga(numeroRiga);
                            fatturaAcquistoRigaDataSource.setCodiceArticolo(ingrediente != null ? ingrediente.getCodice() : "");
                            fatturaAcquistoRigaDataSource.setDescrizioneArticolo(ingrediente != null ? ingrediente.getDescrizione() : "");
                            fatturaAcquistoRigaDataSource.setLotto(di.getLotto());
                            fatturaAcquistoRigaDataSource.setUdm(ingrediente != null ? (ingrediente.getUnitaMisura() != null ? ingrediente.getUnitaMisura().getEtichetta() : "") : "");
                            fatturaAcquistoRigaDataSource.setQuantita(di.getQuantita());

                            BigDecimal prezzo = di.getPrezzo() != null ? Utils.roundPrice(di.getPrezzo()) : new BigDecimal(0);
                            BigDecimal sconto = di.getSconto() != null ? Utils.roundPrice(di.getSconto()) : new BigDecimal(0);
                            fatturaAcquistoRigaDataSource.setPrezzo(prezzo);
                            fatturaAcquistoRigaDataSource.setSconto(sconto);
                            fatturaAcquistoRigaDataSource.setImponibile(di.getImponibile() != null ? Utils.roundPrice(di.getImponibile()) : new BigDecimal(0));

                            fatturaAcquistoRigaDataSource.setIva(ingrediente != null ? (ingrediente.getAliquotaIva() != null ? ingrediente.getAliquotaIva().getValore().intValue() : null) : null);

                            fatturaAcquistoRigaDataSources.add(fatturaAcquistoRigaDataSource);

                            numeroRiga += 1;
                        }
                    }
                }
            }
        }

        return fatturaAcquistoRigaDataSources.stream().sorted(Comparator.comparing(FatturaAcquistoRigaDataSource::getNumeroRiga))
                .collect(Collectors.toList());
    }

    private List<FatturaAcquistoTotaleDataSource> getFatturaAcquistoTotaliDataSource(FatturaAcquisto fatturaAcquisto){
        List<FatturaAcquistoTotaleDataSource> fatturaAcquistoTotaleDataSources = new ArrayList<>();

        Map<AliquotaIva, BigDecimal> imponibiliByIva = AccountingUtils.createFatturaAcquistoTotaliImponibiliByIva(fatturaAcquisto);

        for(AliquotaIva aliquotaIva : imponibiliByIva.keySet()){
            BigDecimal ivaValore = aliquotaIva.getValore();
            BigDecimal imponibile = imponibiliByIva.get(aliquotaIva);
            BigDecimal totaleIva = imponibile.multiply(ivaValore.divide(new BigDecimal(100)));

            FatturaAcquistoTotaleDataSource fatturaAcquistoTotaleDataSource = new FatturaAcquistoTotaleDataSource();
            fatturaAcquistoTotaleDataSource.setAliquotaIva(ivaValore.intValue());
            fatturaAcquistoTotaleDataSource.setTotaleImponibile(Utils.roundPrice(imponibile));
            fatturaAcquistoTotaleDataSource.setTotaleIva(Utils.roundPrice(totaleIva));
            fatturaAcquistoTotaleDataSource.setTotaleIvaNotRounded(totaleIva);

            fatturaAcquistoTotaleDataSources.add(fatturaAcquistoTotaleDataSource);
        }

        return fatturaAcquistoTotaleDataSources.stream().sorted(Comparator.comparing(FatturaAcquistoTotaleDataSource::getAliquotaIva))
                .collect(Collectors.toList());
    }

    private FatturaAccompagnatoriaAcquisto getFatturaAccompagnatoriaAcquisto(Long idFatturaAccompagnatoriaAcquisto){
        log.info("Retrieving 'fattura accompagnatoria acquisto' with id '{}' for creating pdf file", idFatturaAccompagnatoriaAcquisto);
        FatturaAccompagnatoriaAcquisto fatturaAccompagnatoriaAcquisto = fatturaAccompagnatoriaAcquistoService.getOne(idFatturaAccompagnatoriaAcquisto);
        log.info("Retrieved 'fattura accompagnatoria acquisto' with id '{}'", idFatturaAccompagnatoriaAcquisto);
        return fatturaAccompagnatoriaAcquisto;
    }

    private FatturaAccompagnatoriaAcquistoDataSource getFatturaAccompagnatoriaAcquistoDataSource(FatturaAccompagnatoriaAcquisto fatturaAccompagnatoriaAcquisto){
        Fornitore fornitore = fatturaAccompagnatoriaAcquisto.getFornitore();
        String pagamento = fornitore.getPagamento();

        FatturaAccompagnatoriaAcquistoDataSource fatturaAccompagnatoriaAcquistoDataSource = new FatturaAccompagnatoriaAcquistoDataSource();
        fatturaAccompagnatoriaAcquistoDataSource.setNumero(fatturaAccompagnatoriaAcquisto.getNumero());
        fatturaAccompagnatoriaAcquistoDataSource.setData(simpleDateFormat.format(fatturaAccompagnatoriaAcquisto.getData()));
        fatturaAccompagnatoriaAcquistoDataSource.setFornitorePartitaIva("");
        fatturaAccompagnatoriaAcquistoDataSource.setFornitoreCodiceFiscale("");
        fatturaAccompagnatoriaAcquistoDataSource.setCausale(fatturaAccompagnatoriaAcquisto.getCausale() != null ? fatturaAccompagnatoriaAcquisto.getCausale().getDescrizione() : "");
        fatturaAccompagnatoriaAcquistoDataSource.setPagamento("");
        if(fornitore != null){
            if(!StringUtils.isEmpty(fornitore.getPartitaIva())){
                fatturaAccompagnatoriaAcquistoDataSource.setFornitorePartitaIva(fornitore.getPartitaIva());
            }
            if(!StringUtils.isEmpty(fornitore.getCodiceFiscale())){
                fatturaAccompagnatoriaAcquistoDataSource.setFornitoreCodiceFiscale(fornitore.getCodiceFiscale());
            }
        }
        fatturaAccompagnatoriaAcquistoDataSource.setPagamento(!StringUtils.isEmpty(pagamento) ? pagamento : "");
        return fatturaAccompagnatoriaAcquistoDataSource;
    }

    private List<FatturaAccompagnatoriaAcquistoRigaDataSource> getFatturaAccompagnatoriaAcquistoRigheDataSource(FatturaAccompagnatoriaAcquisto fatturaAccompagnatoriaAcquisto){
        List<FatturaAccompagnatoriaAcquistoRigaDataSource> fatturaAccompagnatoriaAcquistoRigaDataSources = new ArrayList<>();
        if(fatturaAccompagnatoriaAcquisto.getFatturaAccompagnatoriaAcquistoArticoli() != null && !fatturaAccompagnatoriaAcquisto.getFatturaAccompagnatoriaAcquistoArticoli().isEmpty()){
            fatturaAccompagnatoriaAcquisto.getFatturaAccompagnatoriaAcquistoArticoli().forEach(faa -> {
                Articolo articolo = faa.getArticolo();
                FatturaAccompagnatoriaAcquistoRigaDataSource fatturaAccompagnatoriaAcquistoRigaDataSource = new FatturaAccompagnatoriaAcquistoRigaDataSource();
                fatturaAccompagnatoriaAcquistoRigaDataSource.setCodiceArticolo(articolo != null ? articolo.getCodice() : "");
                fatturaAccompagnatoriaAcquistoRigaDataSource.setDescrizioneArticolo(articolo != null ? articolo.getDescrizione() : "");
                fatturaAccompagnatoriaAcquistoRigaDataSource.setScadenza(faa.getScadenza() != null ? simpleDateFormat.format(faa.getScadenza()) : "");
                fatturaAccompagnatoriaAcquistoRigaDataSource.setLotto(faa.getLotto());
                fatturaAccompagnatoriaAcquistoRigaDataSource.setUdm(articolo != null ? (articolo.getUnitaMisura() != null ? articolo.getUnitaMisura().getEtichetta() : "") : "");
                fatturaAccompagnatoriaAcquistoRigaDataSource.setQuantita(faa.getQuantita());
                fatturaAccompagnatoriaAcquistoRigaDataSource.setPrezzo(faa.getPrezzo() != null ? Utils.roundPrice(faa.getPrezzo()) : new BigDecimal(0));
                fatturaAccompagnatoriaAcquistoRigaDataSource.setSconto(faa.getSconto() != null ? Utils.roundPrice(faa.getSconto()) : new BigDecimal(0));
                fatturaAccompagnatoriaAcquistoRigaDataSource.setImponibile(faa.getImponibile() != null ? Utils.roundPrice(faa.getImponibile()) : new BigDecimal(0));
                fatturaAccompagnatoriaAcquistoRigaDataSource.setIva(articolo != null ? (articolo.getAliquotaIva() != null ? articolo.getAliquotaIva().getValore().intValue() : null) : null);

                fatturaAccompagnatoriaAcquistoRigaDataSources.add(fatturaAccompagnatoriaAcquistoRigaDataSource);
            });
        }
        if(fatturaAccompagnatoriaAcquisto.getFatturaAccompagnatoriaAcquistoIngredienti() != null && !fatturaAccompagnatoriaAcquisto.getFatturaAccompagnatoriaAcquistoIngredienti().isEmpty()){
            fatturaAccompagnatoriaAcquisto.getFatturaAccompagnatoriaAcquistoIngredienti().forEach(fai -> {
                Ingrediente ingrediente = fai.getIngrediente();
                FatturaAccompagnatoriaAcquistoRigaDataSource fatturaAccompagnatoriaAcquistoRigaDataSource = new FatturaAccompagnatoriaAcquistoRigaDataSource();
                fatturaAccompagnatoriaAcquistoRigaDataSource.setCodiceArticolo(ingrediente != null ? ingrediente.getCodice() : "");
                fatturaAccompagnatoriaAcquistoRigaDataSource.setDescrizioneArticolo(ingrediente != null ? ingrediente.getDescrizione() : "");
                fatturaAccompagnatoriaAcquistoRigaDataSource.setScadenza(fai.getScadenza() != null ? simpleDateFormat.format(fai.getScadenza()) : "");
                fatturaAccompagnatoriaAcquistoRigaDataSource.setLotto(fai.getLotto());
                fatturaAccompagnatoriaAcquistoRigaDataSource.setUdm(ingrediente != null ? (ingrediente.getUnitaMisura() != null ? ingrediente.getUnitaMisura().getEtichetta() : "") : "");
                fatturaAccompagnatoriaAcquistoRigaDataSource.setQuantita(fai.getQuantita());
                fatturaAccompagnatoriaAcquistoRigaDataSource.setPrezzo(fai.getPrezzo() != null ? Utils.roundPrice(fai.getPrezzo()) : new BigDecimal(0));
                fatturaAccompagnatoriaAcquistoRigaDataSource.setSconto(fai.getSconto() != null ? Utils.roundPrice(fai.getSconto()) : new BigDecimal(0));
                fatturaAccompagnatoriaAcquistoRigaDataSource.setImponibile(fai.getImponibile() != null ? Utils.roundPrice(fai.getImponibile()) : new BigDecimal(0));
                fatturaAccompagnatoriaAcquistoRigaDataSource.setIva(ingrediente != null ? (ingrediente.getAliquotaIva() != null ? ingrediente.getAliquotaIva().getValore().intValue() : null) : null);

                fatturaAccompagnatoriaAcquistoRigaDataSources.add(fatturaAccompagnatoriaAcquistoRigaDataSource);
            });
        }
        fatturaAccompagnatoriaAcquistoRigaDataSources.sort(Comparator.comparing(FatturaAccompagnatoriaAcquistoRigaDataSource::getCodiceArticolo).thenComparing(FatturaAccompagnatoriaAcquistoRigaDataSource::getDescrizioneArticolo));
        return fatturaAccompagnatoriaAcquistoRigaDataSources;
    }

    private List<FatturaAccompagnatoriaAcquistoTotaleDataSource> getFatturaAccompagnatoriaAcquistoTotaliDataSource(FatturaAccompagnatoriaAcquisto fatturaAccompagnatoriaAcquisto){
        List<FatturaAccompagnatoriaAcquistoTotaleDataSource> fatturaAccompagnatoriaAcquistoTotaleDataSources = new ArrayList<>();
        if(fatturaAccompagnatoriaAcquisto.getFatturaAccompagnatoriaAcquistoTotali() != null && !fatturaAccompagnatoriaAcquisto.getFatturaAccompagnatoriaAcquistoTotali().isEmpty()){
            fatturaAccompagnatoriaAcquisto.getFatturaAccompagnatoriaAcquistoTotali().forEach(fat -> {
                FatturaAccompagnatoriaAcquistoTotaleDataSource fatturaAccompagnatoriaAcquistoTotaleDataSource = new FatturaAccompagnatoriaAcquistoTotaleDataSource();
                fatturaAccompagnatoriaAcquistoTotaleDataSource.setAliquotaIva(fat.getAliquotaIva().getValore().intValue());
                fatturaAccompagnatoriaAcquistoTotaleDataSource.setTotaleImponibile(fat.getTotaleImponibile() != null ? Utils.roundPrice(fat.getTotaleImponibile()) : new BigDecimal(0));
                fatturaAccompagnatoriaAcquistoTotaleDataSource.setTotaleIva(fat.getTotaleIva() != null ? Utils.roundPrice(fat.getTotaleIva()) : new BigDecimal(0));

                fatturaAccompagnatoriaAcquistoTotaleDataSources.add(fatturaAccompagnatoriaAcquistoTotaleDataSource);
            });
        }
        return fatturaAccompagnatoriaAcquistoTotaleDataSources.stream().sorted(Comparator.comparing(FatturaAccompagnatoriaAcquistoTotaleDataSource::getAliquotaIva))
                .collect(Collectors.toList());
    }

    private NotaReso getNotaReso(Long idNotaReso){
        log.info("Retrieving 'nota reso' with id '{}' for creating pdf file", idNotaReso);
        NotaReso notaReso = notaResoService.getOne(idNotaReso);
        log.info("Retrieved 'nota reso' with id '{}'", idNotaReso);
        return notaReso;
    }

    private NotaResoDataSource getNotaResoDataSource(NotaReso notaReso){
        Fornitore fornitore = notaReso.getFornitore();
        String tipoPagamento = fornitore.getPagamento();

        NotaResoDataSource notaResoDataSource = new NotaResoDataSource();
        notaResoDataSource.setNumero(notaReso.getProgressivo()+"/"+notaReso.getAnno());
        notaResoDataSource.setData(simpleDateFormat.format(notaReso.getData()));
        notaResoDataSource.setFornitorePartitaIva("");
        notaResoDataSource.setFornitoreCodiceFiscale("");
        notaResoDataSource.setCausale(notaReso.getCausale() != null ? notaReso.getCausale().getDescrizione() : "");
        notaResoDataSource.setPagamento(tipoPagamento);
        notaResoDataSource.setAgente("");
        if(fornitore != null){
            if(!StringUtils.isEmpty(fornitore.getPartitaIva())){
                notaResoDataSource.setFornitorePartitaIva(fornitore.getPartitaIva());
            }
            if(!StringUtils.isEmpty(fornitore.getCodiceFiscale())){
                notaResoDataSource.setFornitoreCodiceFiscale(fornitore.getCodiceFiscale());
            }
        }

        return notaResoDataSource;
    }

    private List<NotaResoRigaDataSource> getNotaResoRigheDataSource(NotaReso notaReso){
        List<NotaResoRigaDataSource> notaResoRigaDataSources = new ArrayList<>();
        if(notaReso.getNotaResoRighe() != null && !notaReso.getNotaResoRighe().isEmpty()){
            notaReso.getNotaResoRighe().forEach(nr -> {

                String codiceArticolo = "";
                UnitaMisura unitaMisura = null;
                if(nr.getArticolo() != null){
                    codiceArticolo = nr.getArticolo().getCodice();
                    unitaMisura = nr.getArticolo().getUnitaMisura();
                } else if(nr.getIngrediente() != null){
                    codiceArticolo = nr.getIngrediente().getCodice();
                    unitaMisura = nr.getIngrediente().getUnitaMisura();
                }

                NotaResoRigaDataSource notaResoRigaDataSource = new NotaResoRigaDataSource();
                notaResoRigaDataSource.setCodiceArticolo(codiceArticolo);
                notaResoRigaDataSource.setDescrizioneArticolo(nr.getDescrizione());
                notaResoRigaDataSource.setLotto(nr.getLotto());
                notaResoRigaDataSource.setUdm(unitaMisura != null ? unitaMisura.getEtichetta() : "");
                notaResoRigaDataSource.setQuantita(nr.getQuantita());
                notaResoRigaDataSource.setPrezzo(nr.getPrezzo() != null ? Utils.roundPrice(nr.getPrezzo()) : new BigDecimal(0));
                notaResoRigaDataSource.setSconto(nr.getSconto() != null ? Utils.roundPrice(nr.getSconto()) : new BigDecimal(0));
                notaResoRigaDataSource.setImponibile(nr.getImponibile() != null ? Utils.roundPrice(nr.getImponibile()) : new BigDecimal(0));
                notaResoRigaDataSource.setIva(nr.getAliquotaIva() != null ? nr.getAliquotaIva().getValore().intValue() : null);

                notaResoRigaDataSources.add(notaResoRigaDataSource);
            });
        }
        return notaResoRigaDataSources;
    }

    private List<NotaResoTotaleDataSource> getNotaResoTotaliDataSource(NotaReso notaReso){
        List<NotaResoTotaleDataSource> notaResoTotaleDataSources = new ArrayList<>();
        if(notaReso.getNotaResoTotali() != null && !notaReso.getNotaResoTotali().isEmpty()){
            notaReso.getNotaResoTotali().forEach(nr -> {
                NotaResoTotaleDataSource notaResoTotaleDataSource = new NotaResoTotaleDataSource();
                notaResoTotaleDataSource.setAliquotaIva(nr.getAliquotaIva().getValore().intValue());
                notaResoTotaleDataSource.setTotaleImponibile(nr.getTotaleImponibile() != null ? Utils.roundPrice(nr.getTotaleImponibile()) : new BigDecimal(0));
                notaResoTotaleDataSource.setTotaleIva(nr.getTotaleIva() != null ? Utils.roundPrice(nr.getTotaleIva()) : new BigDecimal(0));

                notaResoTotaleDataSources.add(notaResoTotaleDataSource);
            });
        }
        return notaResoTotaleDataSources.stream().sorted(Comparator.comparing(NotaResoTotaleDataSource::getAliquotaIva))
                .collect(Collectors.toList());
    }

    private RicevutaPrivato getRicevutaPrivato(Long idRicevutaPrivato){
        log.info("Retrieving 'ricevuta privato' with id '{}' for creating pdf file", idRicevutaPrivato);
        RicevutaPrivato ricevutaPrivato = ricevutaPrivatoService.getOne(idRicevutaPrivato);
        log.info("Retrieved 'ricevuta privato' with id '{}'", idRicevutaPrivato);
        return ricevutaPrivato;
    }

    private RicevutaPrivatoDataSource getRicevutaPrivatoDataSource(RicevutaPrivato ricevutaPrivato){
        Cliente cliente = ricevutaPrivato.getCliente();
        TipoPagamento tipoPagamento = cliente.getTipoPagamento();
        Agente agente = cliente.getAgente();

        RicevutaPrivatoDataSource ricevutaPrivatoDataSource = new RicevutaPrivatoDataSource();
        ricevutaPrivatoDataSource.setNumero(ricevutaPrivato.getProgressivo()+"/"+ricevutaPrivato.getAnno());
        ricevutaPrivatoDataSource.setData(simpleDateFormat.format(ricevutaPrivato.getData()));
        ricevutaPrivatoDataSource.setClientePartitaIva("");
        ricevutaPrivatoDataSource.setClienteCodiceFiscale("");
        ricevutaPrivatoDataSource.setCausale(ricevutaPrivato.getCausale() != null ? ricevutaPrivato.getCausale().getDescrizione() : "");
        ricevutaPrivatoDataSource.setPagamento("");
        ricevutaPrivatoDataSource.setAgente("");
        if(cliente != null){
            if(!StringUtils.isEmpty(cliente.getCodiceFiscale())){
                ricevutaPrivatoDataSource.setClienteCodiceFiscale(cliente.getCodiceFiscale());
            }
        }
        if(tipoPagamento != null){
            if(!StringUtils.isEmpty(tipoPagamento.getDescrizione())){
                ricevutaPrivatoDataSource.setPagamento(tipoPagamento.getDescrizione());
            }
        }
        if(agente != null){
            StringBuilder sb = new StringBuilder();
            if(!StringUtils.isEmpty(agente.getNome())){
                sb.append(agente.getNome()).append(" ");
            }
            if(!StringUtils.isEmpty(agente.getCognome())){
                sb.append(agente.getCognome());
            }
            ricevutaPrivatoDataSource.setAgente(sb.toString());
        }
        return ricevutaPrivatoDataSource;
    }

    private List<RicevutaPrivatoDataSource> getRicevutaPrivatoDataSources(String ids){
        log.info("Retrieving the list of 'ricevute privati' with id in '{}' for creating pdf file", ids);

        List<RicevutaPrivatoDataSource> ricevutaPrivatoDataSources = new ArrayList<>();

        List<RicevutaPrivato> ricevutePrivato = ricevutaPrivatoService.getAll().stream()
                .filter(ricevutaPrivato -> ids.contains(ricevutaPrivato.getId().toString()))
                .sorted(Comparator.comparing(RicevutaPrivato::getProgressivo).reversed())
                .collect(Collectors.toList());

        if(ricevutePrivato != null && !ricevutePrivato.isEmpty()){
            ricevutePrivato.forEach(ricevutaPrivato -> {
                RicevutaPrivatoDataSource ricevutaPrivatoDataSource = new RicevutaPrivatoDataSource();
                ricevutaPrivatoDataSource.setNumero(ricevutaPrivato.getProgressivo().toString());
                ricevutaPrivatoDataSource.setData(simpleDateFormat.format(ricevutaPrivato.getData()));
                Cliente cliente = ricevutaPrivato.getCliente();
                if(cliente != null){
                    ricevutaPrivatoDataSource.setClienteDescrizione(cliente.getNome()+" "+cliente.getCognome());
                }
                BigDecimal totaleAcconto = ricevutaPrivato.getTotaleAcconto() != null ? Utils.roundPrice(ricevutaPrivato.getTotaleAcconto()) : new BigDecimal(0);
                BigDecimal totale = ricevutaPrivato.getTotale() != null ? Utils.roundPrice(ricevutaPrivato.getTotale()) : new BigDecimal(0);
                ricevutaPrivatoDataSource.setAcconto(totaleAcconto);
                ricevutaPrivatoDataSource.setTotale(totale);
                ricevutaPrivatoDataSource.setTotaleDaPagare(totale.subtract(totaleAcconto));

                ricevutaPrivatoDataSources.add(ricevutaPrivatoDataSource);
            });
        }

        log.info("Retrieved {} 'ricevute privati'", ricevutaPrivatoDataSources.size());
        return ricevutaPrivatoDataSources;
    }

    private List<RicevutaPrivatoArticoloDataSource> getRicevutaPrivatoArticoliDataSource(RicevutaPrivato ricevutaPrivato){
        List<RicevutaPrivatoArticoloDataSource> ricevutaPrivatoArticoloDataSources = new ArrayList<>();
        if(ricevutaPrivato.getRicevutaPrivatoArticoli() != null && !ricevutaPrivato.getRicevutaPrivatoArticoli().isEmpty()){
            ricevutaPrivato.getRicevutaPrivatoArticoli().forEach(rpa -> {
                RicevutaPrivatoArticoloDataSource ricevutaPrivatoArticoloDataSource = new RicevutaPrivatoArticoloDataSource();

                Float quantita = rpa.getQuantita();
                BigDecimal imponibile = rpa.getImponibile() != null ? Utils.roundPrice(rpa.getImponibile()) : new BigDecimal(0);
                BigDecimal iva = rpa.getArticolo().getAliquotaIva().getValore();

                BigDecimal ivaValore = iva.divide(BigDecimal.valueOf(100)).multiply(imponibile);

                ricevutaPrivatoArticoloDataSource.setCodiceArticolo(rpa.getArticolo().getCodice());
                ricevutaPrivatoArticoloDataSource.setDescrizioneArticolo(rpa.getArticolo().getDescrizione());
                ricevutaPrivatoArticoloDataSource.setLotto(rpa.getLotto());
                ricevutaPrivatoArticoloDataSource.setUdm(rpa.getArticolo().getUnitaMisura().getEtichetta());
                ricevutaPrivatoArticoloDataSource.setQuantita(quantita);
                ricevutaPrivatoArticoloDataSource.setPezzi(rpa.getNumeroPezzi() != null ? rpa.getNumeroPezzi() : 0);
                ricevutaPrivatoArticoloDataSource.setPrezzo(rpa.getPrezzo() != null ? Utils.roundPrice(rpa.getPrezzo()) : new BigDecimal(0));
                ricevutaPrivatoArticoloDataSource.setSconto(rpa.getSconto() != null ? Utils.roundPrice(rpa.getSconto()) : new BigDecimal(0));
                ricevutaPrivatoArticoloDataSource.setImponibile(imponibile);
                ricevutaPrivatoArticoloDataSource.setIva(iva.intValue());
                ricevutaPrivatoArticoloDataSource.setPrezzoConIva(rpa.getPrezzoIva() != null ? Utils.roundPrice(rpa.getPrezzoIva()) : new BigDecimal(0));
                ricevutaPrivatoArticoloDataSource.setTotale(rpa.getTotale() != null ? Utils.roundPrice(rpa.getTotale()) : new BigDecimal(0));
                ricevutaPrivatoArticoloDataSource.setIvaValore(ivaValore != null ? Utils.roundPrice(ivaValore) : new BigDecimal(0));

                ricevutaPrivatoArticoloDataSources.add(ricevutaPrivatoArticoloDataSource);
            });
        }
        return ricevutaPrivatoArticoloDataSources;
    }

    private List<FatturaCommercianteDataSource> getFattureCommercianti(Set<Fattura> inputFatture){
        log.info("Retrieving the list of 'fatture commercianti' for creating pdf file");

        List<Fattura> fatture = inputFatture.stream()
                .sorted(Comparator.comparing(Fattura::getProgressivo))
                .collect(Collectors.toList());

        List<FatturaCommercianteDataSource> fatturaCommercianteDataSources = new ArrayList<>();
        if(!fatture.isEmpty()){
            fatture.forEach(fattura -> {
                String date = simpleDateFormat.format(fattura.getData());

                String ragioneSociale = "";
                String indirizzo = "";
                String citta = "";
                String partitaIva = "";
                String codiceFiscale = "";

                Cliente cliente = fattura.getCliente();
                if(cliente != null){
                    if(cliente.getDittaIndividuale()){
                        ragioneSociale = cliente.getNome() + " " + cliente.getCognome();
                    } else {
                        ragioneSociale = cliente.getRagioneSociale();
                    }
                    indirizzo = cliente.getIndirizzo();
                    citta = cliente.getCitta();
                    if(!StringUtils.isEmpty(cliente.getProvincia())){
                        Provincia provincia = Provincia.getByLabel(cliente.getProvincia());
                        if(provincia != null){
                            citta += " ("+provincia.getSigla()+")";
                        }
                    }
                    partitaIva = "P.I. " + cliente.getPartitaIva();
                    codiceFiscale = "C.F. " + cliente.getCodiceFiscale();
                }

                FatturaCommercianteDataSource fatturaCommercianteDataSource = new FatturaCommercianteDataSource();
                fatturaCommercianteDataSource.setNumero(fattura.getProgressivo().toString());
                fatturaCommercianteDataSource.setData(date);
                fatturaCommercianteDataSource.setRagioneSociale(ragioneSociale);
                fatturaCommercianteDataSource.setIndirizzo(indirizzo);
                fatturaCommercianteDataSource.setCitta(citta);
                fatturaCommercianteDataSource.setPartitaIva(partitaIva);
                fatturaCommercianteDataSource.setCodiceFiscale(codiceFiscale);
                fatturaCommercianteDataSource.setTotale(fattura.getTotale());

                List<FatturaCommercianteTotaleDataSource> fatturaCommercianteTotaleDataSources = new ArrayList<>();
                Map<AliquotaIva, Set<DdtArticolo>> ivaDdtArticoliMap = new HashMap<>();

                // create a map with the list of DdtArticoli grouped by AliquotaIva
                Set<FatturaDdt> fatturaDdts = fattura.getFatturaDdts();
                if(fatturaDdts != null && !fatturaDdts.isEmpty()){
                    for(FatturaDdt fatturaDdt : fatturaDdts){
                        Ddt ddt = fatturaDdt.getDdt();
                        if(ddt != null){
                            if(ddt.getDdtArticoli() != null && !ddt.getDdtArticoli().isEmpty()){
                                for(DdtArticolo ddtArticolo : ddt.getDdtArticoli()){
                                    AliquotaIva aliquotaIva = ddtArticolo.getArticolo().getAliquotaIva();
                                    Set<DdtArticolo> ddtArticoli = ivaDdtArticoliMap.getOrDefault(aliquotaIva, new HashSet<>());
                                    ddtArticoli.add(ddtArticolo);
                                    ivaDdtArticoliMap.put(aliquotaIva, ddtArticoli);
                                }
                            }
                        }
                    }
                }

                for (Map.Entry<AliquotaIva, Set<DdtArticolo>> entry : ivaDdtArticoliMap.entrySet()) {
                    BigDecimal iva = entry.getKey().getValore();
                    BigDecimal totaleImponibile = BigDecimal.ZERO;
                    BigDecimal totaleIva = BigDecimal.ZERO;

                    for(DdtArticolo ddtArticolo : entry.getValue()){
                        BigDecimal imponibile = ddtArticolo.getImponibile() != null ? ddtArticolo.getImponibile() : BigDecimal.ZERO;
                        BigDecimal ddtArticoloIva = ddtArticolo.getImponibile() != null ? (imponibile.multiply(iva.divide(new BigDecimal(100)))) : BigDecimal.ZERO;
                        totaleImponibile = totaleImponibile.add(imponibile);
                        totaleIva = totaleIva.add(ddtArticoloIva);
                    }

                    FatturaCommercianteTotaleDataSource fatturaCommercianteTotaleDataSource = new FatturaCommercianteTotaleDataSource();
                    fatturaCommercianteTotaleDataSource.setImponibile(Utils.roundPrice(totaleImponibile));
                    fatturaCommercianteTotaleDataSource.setIva(iva.intValue());
                    fatturaCommercianteTotaleDataSource.setImposta(Utils.roundPrice(totaleIva));

                    fatturaCommercianteTotaleDataSources.add(fatturaCommercianteTotaleDataSource);
                }

                fatturaCommercianteDataSource.setFatturaCommercianteTotaleDataSources(fatturaCommercianteTotaleDataSources);

                fatturaCommercianteDataSources.add(fatturaCommercianteDataSource);

            });
        }

        log.info("Retrieved {} 'fatture commercianti'", fatturaCommercianteDataSources.size());
        return fatturaCommercianteDataSources;
    }

    private Map<String, Object> getFattureCommerciantiTotaliCompleti(List<FatturaCommercianteDataSource> fatturaCommercianteDataSources){

        Map<String, Object> result = new HashMap<>();

        Map<Integer, FatturaCommercianteTotaleCompletoDataSource> totaliCompletiMap = new HashMap<>();

        BigDecimal totaleCompleto = BigDecimal.ZERO;
        List<FatturaCommercianteTotaleCompletoDataSource> fatturaCommercianteTotaleCompletoDataSources = new ArrayList<>();

        if(fatturaCommercianteDataSources != null && !fatturaCommercianteDataSources.isEmpty()){
            for(FatturaCommercianteDataSource fatturaCommercianteDataSource : fatturaCommercianteDataSources){
                totaleCompleto = totaleCompleto.add(fatturaCommercianteDataSource.getTotale());
                List<FatturaCommercianteTotaleDataSource> fatturaCommercianteTotaleDataSources = fatturaCommercianteDataSource.getFatturaCommercianteTotaleDataSources();
                if(fatturaCommercianteTotaleDataSources != null && !fatturaCommercianteTotaleDataSources.isEmpty()){
                    for(FatturaCommercianteTotaleDataSource fatturaCommercianteTotaleDataSource : fatturaCommercianteTotaleDataSources){
                        FatturaCommercianteTotaleCompletoDataSource fatturaCommercianteTotaleCompletoDataSource;
                        Integer iva = fatturaCommercianteTotaleDataSource.getIva();
                        BigDecimal imponibile = fatturaCommercianteTotaleDataSource.getImponibile();
                        BigDecimal imposta = fatturaCommercianteTotaleDataSource.getImposta();
                        if(totaliCompletiMap.containsKey(iva)){
                            fatturaCommercianteTotaleCompletoDataSource = totaliCompletiMap.get(iva);
                            fatturaCommercianteTotaleCompletoDataSource.setTotaleImponibile(fatturaCommercianteTotaleCompletoDataSource.getTotaleImponibile().add(imponibile));
                            fatturaCommercianteTotaleCompletoDataSource.setTotaleIva(fatturaCommercianteTotaleCompletoDataSource.getTotaleIva().add(imposta));
                        } else {
                            fatturaCommercianteTotaleCompletoDataSource = new FatturaCommercianteTotaleCompletoDataSource();
                            fatturaCommercianteTotaleCompletoDataSource.setAliquotaIva(iva);
                            fatturaCommercianteTotaleCompletoDataSource.setTotaleImponibile(imponibile);
                            fatturaCommercianteTotaleCompletoDataSource.setTotaleIva(imposta);
                        }
                        totaliCompletiMap.put(iva, fatturaCommercianteTotaleCompletoDataSource);
                    }
                }
            }
        }
        if(!totaliCompletiMap.isEmpty()){
            for(Integer key : totaliCompletiMap.keySet()){
                fatturaCommercianteTotaleCompletoDataSources.add(totaliCompletiMap.get(key));
            }
        }

        result.put("totaleCompleto", totaleCompleto);
        result.put("fatturaTotaliCompletiCollection", fatturaCommercianteTotaleCompletoDataSources);
        return result;
    }

    public OrdineFornitore getOrdineFornitore(Long idOrdineFornitore){
        log.info("Retrieving 'ordine-fornitore' with id '{}' for creating pdf file", idOrdineFornitore);
        OrdineFornitore ordineFornitore = ordineFornitoreService.getOne(idOrdineFornitore);
        log.info("Retrieved 'ordine-fornitore' with id '{}'", idOrdineFornitore);
        return ordineFornitore;
    }

    public List<OrdineFornitoreArticoloDataSource> getOrdineFornitoreArticoliDataSource(OrdineFornitore ordineFornitore){
        List<OrdineFornitoreArticoloDataSource> ordineFornitoreArticoloDataSources = new ArrayList<>();
        if(ordineFornitore.getOrdineFornitoreArticoli() != null && !ordineFornitore.getOrdineFornitoreArticoli().isEmpty()){
            ordineFornitore.getOrdineFornitoreArticoli().forEach(ofa -> {
                Articolo articolo = ofa.getArticolo();

                OrdineFornitoreArticoloDataSource ordineFornitoreArticoloDataSource = new OrdineFornitoreArticoloDataSource();
                ordineFornitoreArticoloDataSource.setUdm("Pz");
                ordineFornitoreArticoloDataSource.setQuantita(ofa.getNumeroPezziOrdinati());
                if(articolo != null){
                    ordineFornitoreArticoloDataSource.setCodiceArticolo(articolo.getCodice());
                    ordineFornitoreArticoloDataSource.setDescrizioneArticolo(articolo.getDescrizione());
                } else {
                    ordineFornitoreArticoloDataSource.setCodiceArticolo("");
                    ordineFornitoreArticoloDataSource.setDescrizioneArticolo("");
                }

                ordineFornitoreArticoloDataSources.add(ordineFornitoreArticoloDataSource);
            });
        }
        return ordineFornitoreArticoloDataSources.stream().sorted(Comparator.comparing(oads -> oads.getDescrizioneArticolo().toLowerCase())).collect(Collectors.toList());
    }

    public byte[] generateDdt(Long idDdt) throws Exception{

        // retrieve the Ddt
        Ddt ddt = getDdt(idDdt);
        PuntoConsegna puntoConsegna = ddt.getPuntoConsegna();
        Cliente cliente = ddt.getCliente();
        Boolean nascondiPrezzi = cliente.getNascondiPrezzi();
        if(nascondiPrezzi == null){
            nascondiPrezzi = false;
        }

        // create DdtDataSource
        List<DdtDataSource> ddtDataSources = new ArrayList<>();
        ddtDataSources.add(getDdtDataSource(ddt));

        // create data parameters
        String ddtTitleParam = ddt.getProgressivo()+"/"+ddt.getAnnoContabile()+" del "+simpleDateFormat.format(ddt.getData());
        String puntoConsegnaParam = "";
        String destinatarioParam = "";

        // create data parameters for PuntoConsegna
        if(puntoConsegna != null){
            StringBuilder sb = new StringBuilder();
            if(!StringUtils.isEmpty(puntoConsegna.getNome())){
                sb.append(puntoConsegna.getNome()).append("\n");
            }
            if(!StringUtils.isEmpty(puntoConsegna.getIndirizzo())){
                sb.append(puntoConsegna.getIndirizzo()).append("\n");
            }
            if(!StringUtils.isEmpty(puntoConsegna.getCap())){
                sb.append(puntoConsegna.getCap()).append(" ");
            }
            if(!StringUtils.isEmpty(puntoConsegna.getLocalita())){
                sb.append(puntoConsegna.getLocalita()).append(" ");
            }
            if(!StringUtils.isEmpty(puntoConsegna.getProvincia())){
                sb.append("(").append(Provincia.getByLabel(puntoConsegna.getProvincia()).getSigla()).append(")");
            }

            puntoConsegnaParam = sb.toString();
        }

        // create data parameters for Cliente
        if(cliente != null){
            StringBuilder sb = new StringBuilder();
            if(!StringUtils.isEmpty(cliente.getRagioneSociale())){
                sb.append(cliente.getRagioneSociale()).append("\n");
            }
            if(!StringUtils.isEmpty(cliente.getIndirizzo())){
                sb.append(cliente.getIndirizzo()).append("\n");
            }
            if(!StringUtils.isEmpty(cliente.getCap())){
                sb.append(cliente.getCap()).append(" ");
            }
            if(!StringUtils.isEmpty(cliente.getCitta())){
                sb.append(cliente.getCitta()).append(" ");
            }
            if(!StringUtils.isEmpty(cliente.getProvincia())){
                sb.append("(").append(Provincia.getByLabel(cliente.getProvincia()).getSigla()).append(")");
            }

            destinatarioParam = sb.toString();
        }

        // create 'ddtTrasportoDataOra' param
        String ddtTrasportoDataOraParam = simpleDateFormat.format(ddt.getDataTrasporto())+" "+ddt.getOraTrasporto();

        // create list of DdtArticoloDataSource from DdtArticolo
        List<DdtArticoloDataSource> ddtArticoloDataSources = getDdtArticoliDataSource(ddt);

        // fetching the .jrxml file from the resources folder.
        final InputStream stream = this.getClass().getResourceAsStream(Constants.JASPER_REPORT_DDT);

        // create report datasource for Ddt
        JRBeanCollectionDataSource ddtCollectionDataSource = new JRBeanCollectionDataSource(ddtDataSources);

        // create report datasource for DdtArticoli
        JRBeanCollectionDataSource ddtArticoliCollectionDataSource = new JRBeanCollectionDataSource(ddtArticoloDataSources);

        // create report parameters
        Map<String, Object> parameters = createParameters();

        // add data to parameters
        parameters.put("ddtTitle", ddtTitleParam);
        parameters.put("puntoConsegna", puntoConsegnaParam);
        parameters.put("destinatario", destinatarioParam);
        parameters.put("note", ddt.getNote());
        parameters.put("trasportatore", ddt.getTrasportatore());
        parameters.put("nascondiPrezzi", nascondiPrezzi);
        parameters.put("nota", Constants.JASPER_PARAMETER_DDT_NOTA);
        parameters.put("ddtTrasportoTipo", ddt.getTipoTrasporto());
        parameters.put("ddtTrasportoDataOra", ddtTrasportoDataOraParam);
        parameters.put("ddtNumeroColli", ddt.getNumeroColli());
        parameters.put("ddtTotImponibile", Utils.roundPrice(ddt.getTotaleImponibile()));
        parameters.put("ddtTotIva", Utils.roundPrice(ddt.getTotaleIva()));
        parameters.put("ddtTotDocumento", Utils.roundPrice(ddt.getTotale()));
        parameters.put("ddtArticoliCollection", ddtArticoliCollectionDataSource);
        parameters.put("ddtCollection", ddtCollectionDataSource);

        return JasperRunManager.runReportToPdf(stream, parameters, new JREmptyDataSource());
    }

    public byte[] generateDdts(java.sql.Date dataDa, java.sql.Date dataA, Integer progressivo, Integer idCliente, String cliente, Integer idAgente, Integer idAutista, Integer idStato, Boolean pagato, Boolean fatturato, Float importo, Integer idTipoPagamento, Integer idArticolo) throws Exception {

        // retrieve the list of Ddt
        List<DdtDataSource> ddts = getDdtDataSources(dataDa, dataA, progressivo, idCliente, cliente, idAgente, idAutista, idStato, pagato, fatturato, importo, idTipoPagamento, idArticolo);

        // fetching the .jrxml file from the resources folder.
        final InputStream stream = this.getClass().getResourceAsStream(Constants.JASPER_REPORT_DDTS);

        // create report datasource
        JRBeanCollectionDataSource dataSource = new JRBeanCollectionDataSource(ddts);

        // create report parameters
        Map<String, Object> parameters = createParameters();

        BigDecimal totaleAcconto = new BigDecimal(0);
        BigDecimal totale = new BigDecimal(0);
        BigDecimal totaleDaPagare = new BigDecimal(0);

        for(DdtDataSource ddt: ddts){
            totaleAcconto = totaleAcconto.add(ddt.getAcconto());
            totale = totale.add(ddt.getTotale());
            totaleDaPagare = totaleDaPagare.add(ddt.getTotaleDaPagare());
        }

        // add data to parameters
        parameters.put("totaleAcconto", totaleAcconto);
        parameters.put("totale", totale);
        parameters.put("totaleDaPagare", totaleDaPagare);
        parameters.put("ddtsCollection", dataSource);

        // create report
        return JasperRunManager.runReportToPdf(stream, parameters, new JREmptyDataSource());
    }

    public byte[] generateSingleDdt(Long[] ddts) throws Exception{
        PDFMergerUtility pdfMergerUtility = new PDFMergerUtility();

        for(Long idDdt : ddts){
            byte[] ddtPdf = generateDdt(idDdt);
            try(InputStream inputStream = new ByteArrayInputStream(ddtPdf)){
                pdfMergerUtility.addSource(inputStream);
            }
        }
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        pdfMergerUtility.setDestinationStream(byteArrayOutputStream);
        pdfMergerUtility.mergeDocuments(MemoryUsageSetting.setupMainMemoryOnly());

        return byteArrayOutputStream.toByteArray();
    }

    public byte[] generateDdtAcquisto(Long idDdtAcquisto) throws Exception{

        DdtAcquisto ddtAcquisto = getDdtAcquisto(idDdtAcquisto);
        Fornitore fornitore = ddtAcquisto.getFornitore();

        List<DdtAcquistoDataSource> ddtAcquistoDataSources = new ArrayList<>();
        ddtAcquistoDataSources.add(getDdtAcquistoDataSource(ddtAcquisto));

        String ddtAcquistoTitleParam = ddtAcquisto.getNumero()+" del "+simpleDateFormat.format(ddtAcquisto.getData());
        String destinatarioParam = "";

        if(fornitore != null){
            StringBuilder sb = new StringBuilder();
            if(!StringUtils.isEmpty(fornitore.getRagioneSociale())){
                sb.append(fornitore.getRagioneSociale()).append("\n");
            }
            if(!StringUtils.isEmpty(fornitore.getIndirizzo())){
                sb.append(fornitore.getIndirizzo()).append("\n");
            }
            if(!StringUtils.isEmpty(fornitore.getCap())){
                sb.append(fornitore.getCap()).append(" ");
            }
            if(!StringUtils.isEmpty(fornitore.getCitta())){
                sb.append(fornitore.getCitta()).append(" ");
            }
            if(!StringUtils.isEmpty(fornitore.getProvincia())){
                sb.append("(").append(Provincia.getByLabel(fornitore.getProvincia()).getSigla()).append(")");
            }
            destinatarioParam = sb.toString();
        }

        // create list of DdtAcquistoArticoloDataSource from DdtAcquistoArticolo
        List<DdtAcquistoArticoloDataSource> ddtAcquistoArticoloDataSources = getDdtAcquistoArticoliDataSource(ddtAcquisto);

        // fetching the .jrxml file from the resources folder.
        final InputStream stream = this.getClass().getResourceAsStream(Constants.JASPER_REPORT_DDT_ACQUISTO);

        // create report datasource for DdtAcquisto
        JRBeanCollectionDataSource ddtAcquistoCollectionDataSource = new JRBeanCollectionDataSource(ddtAcquistoDataSources);

        // create report datasource for DdtArticoli
        JRBeanCollectionDataSource ddtAcquistoArticoliCollectionDataSource = new JRBeanCollectionDataSource(ddtAcquistoArticoloDataSources);

        // create report parameters
        Map<String, Object> parameters = createParameters();

        // add data to parameters
        parameters.put("ddtAcquistoTitle", ddtAcquistoTitleParam);
        parameters.put("destinatario", destinatarioParam);
        parameters.put("note", ddtAcquisto.getNote());
        parameters.put("nota", Constants.JASPER_PARAMETER_DDT_NOTA);
        parameters.put("ddtAcquistoNumeroColli", ddtAcquisto.getNumeroColli());
        parameters.put("ddtAcquistoTotImponibile", Utils.roundPrice(ddtAcquisto.getTotaleImponibile()));
        parameters.put("ddtAcquistoTotIva", Utils.roundPrice(ddtAcquisto.getTotaleIva()));
        parameters.put("ddtAcquistoTotDocumento", Utils.roundPrice(ddtAcquisto.getTotale()));
        parameters.put("ddtAcquistoArticoliCollection", ddtAcquistoArticoliCollectionDataSource);
        parameters.put("ddtAcquistoCollection", ddtAcquistoCollectionDataSource);

        return JasperRunManager.runReportToPdf(stream, parameters, new JREmptyDataSource());
    }

    public byte[] generateDocumentiAcquistoDistinta(String fornitore, String numDocumento, String tipoDocumento, java.sql.Date dataDa, java.sql.Date dataA) throws Exception {

        // retrieve the list of DocumentiAcquisto
        List<DocumentoAcquistoDataSource> documentiAcquisto = getDocumentoAcquistoDataSources(fornitore, numDocumento, tipoDocumento, dataDa, dataA);

        // fetching the .jrxml file from the resources folder.
        final InputStream stream = this.getClass().getResourceAsStream(Constants.JASPER_REPORT_DOCUMENTI_ACQUISTO);

        // create report datasource
        JRBeanCollectionDataSource dataSource = new JRBeanCollectionDataSource(documentiAcquisto);

        BigDecimal totale = new BigDecimal(0);
        BigDecimal totaleImponibile = new BigDecimal(0);
        BigDecimal totaleIva = new BigDecimal(0);

        for(DocumentoAcquistoDataSource documentoAcquisto: documentiAcquisto){
            totale = totale.add(documentoAcquisto.getTotale());
            totaleImponibile = totaleImponibile.add(documentoAcquisto.getTotaleImponibile());
            totaleIva = totaleIva.add(documentoAcquisto.getTotaleIva());
        }

        // create report parameters
        Map<String, Object> parameters = createParameters();
        parameters.put("documentiAcquistoCollection", dataSource);
        parameters.put("totale", totale);
        parameters.put("totaleImponibile", totaleImponibile);
        parameters.put("totaleIva", totaleIva);

        // create report
        return JasperRunManager.runReportToPdf(stream, parameters, new JREmptyDataSource());
    }

    public byte[] generateFattura(Long idFattura) throws Exception{

        // retrieve the Fattura
        Fattura fattura = getFattura(idFattura);
        Cliente cliente = fattura.getCliente();

        // create data parameters
        String fatturaTitleParam = fattura.getProgressivo()+"/"+fattura.getAnno()+" del "+simpleDateFormat.format(fattura.getData());
        String destinatarioParam = "";

        // create data parameters for Cliente
        if(cliente != null){
            StringBuilder sb = new StringBuilder();
            if(!StringUtils.isEmpty(cliente.getRagioneSociale())){
                sb.append(cliente.getRagioneSociale()).append("\n");
            }
            if(!StringUtils.isEmpty(cliente.getIndirizzo())){
                sb.append(cliente.getIndirizzo()).append("\n");
            }
            if(!StringUtils.isEmpty(cliente.getCap())){
                sb.append(cliente.getCap()).append(" ");
            }
            if(!StringUtils.isEmpty(cliente.getCitta())){
                sb.append(cliente.getCitta()).append(" ");
            }
            if(!StringUtils.isEmpty(cliente.getProvincia())){
                sb.append("(").append(Provincia.getByLabel(cliente.getProvincia()).getSigla()).append(")");
            }

            destinatarioParam = sb.toString();
        }

        // create FatturaDataSource
        List<FatturaDataSource> fatturaDataSources = new ArrayList<>();
        fatturaDataSources.add(getFatturaDataSource(fattura));

        // create list of FatturaRigheDataSource from FatturaDdts
        List<FatturaRigaDataSource> fatturaRigaDataSources = getFatturaRigheDataSource(fattura);

        // create list of FatturaAccompagnatoriaTotaliDataSource from FatturaAccompagnatoriaTotale
        List<FatturaTotaleDataSource> fatturaTotaleDataSources = getFatturaTotaliDataSource(fattura);

        BigDecimal totaleImponibile = new BigDecimal(0);
        BigDecimal totaleIva = new BigDecimal(0);

        for(FatturaTotaleDataSource fatturaTotale: fatturaTotaleDataSources){
            totaleImponibile = totaleImponibile.add(fatturaTotale.getTotaleImponibile());
            totaleIva = totaleIva.add(fatturaTotale.getTotaleIvaNotRounded());
        }
        totaleImponibile = Utils.roundPrice(totaleImponibile);
        totaleIva = Utils.roundPrice(totaleIva);

        // fetching the .jrxml file from the resources folder.
        final InputStream stream = this.getClass().getResourceAsStream(Constants.JASPER_REPORT_FATTURA);

        // create report datasource for Fattura
        JRBeanCollectionDataSource fatturaCollectionDataSource = new JRBeanCollectionDataSource(fatturaDataSources);

        // create report datasource for FatturaRighe
        JRBeanCollectionDataSource fatturaRigheCollectionDataSource = new JRBeanCollectionDataSource(fatturaRigaDataSources);

        // create report datasource for FatturaTotali
        JRBeanCollectionDataSource fatturaTotaliCollectionDataSource = new JRBeanCollectionDataSource(fatturaTotaleDataSources);

        // create report parameters
        Map<String, Object> parameters = createParameters();

        // add data to parameters
        parameters.put("fatturaTitle", fatturaTitleParam);
        parameters.put("destinatario", destinatarioParam);
        parameters.put("note", fattura.getNote());
        parameters.put("nota", Constants.JASPER_PARAMETER_FATTURA_NOTA);
        parameters.put("totaleImponibile", totaleImponibile);
        parameters.put("totaleIva", totaleIva);
        parameters.put("fatturaTotDocumento", Utils.roundPrice(fattura.getTotale()));
        parameters.put("fatturaCollection", fatturaCollectionDataSource);
        parameters.put("fatturaRigheCollection", fatturaRigheCollectionDataSource);
        parameters.put("fatturaTotaliCollection", fatturaTotaliCollectionDataSource);

        // create report
        return JasperRunManager.runReportToPdf(stream, parameters, new JREmptyDataSource());
    }

    public byte[] generateFatturaAccompagnatoria(Long idFatturaAccompagnatoria) throws Exception{

        // retrieve the FatturaAccompagnatoria
        FatturaAccompagnatoria fatturaAccompagnatoria = getFatturaAccompagnatoria(idFatturaAccompagnatoria);
        PuntoConsegna puntoConsegna = fatturaAccompagnatoria.getPuntoConsegna();
        Cliente cliente = fatturaAccompagnatoria.getCliente();

        // create data parameters
        String fatturaAccompagnatoriaTitleParam = fatturaAccompagnatoria.getProgressivo()+"/"+fatturaAccompagnatoria.getAnno()+" del "+simpleDateFormat.format(fatturaAccompagnatoria.getData());
        String puntoConsegnaParam = "";
        String destinatarioParam = "";

        // create data parameters for PuntoConsegna
        if(puntoConsegna != null){
            StringBuilder sb = new StringBuilder();
            if(!StringUtils.isEmpty(puntoConsegna.getNome())){
                sb.append(puntoConsegna.getNome()).append("\n");
            }
            if(!StringUtils.isEmpty(puntoConsegna.getIndirizzo())){
                sb.append(puntoConsegna.getIndirizzo()).append("\n");
            }
            if(!StringUtils.isEmpty(puntoConsegna.getCap())){
                sb.append(puntoConsegna.getCap()).append(" ");
            }
            if(!StringUtils.isEmpty(puntoConsegna.getLocalita())){
                sb.append(puntoConsegna.getLocalita()).append(" ");
            }
            if(!StringUtils.isEmpty(puntoConsegna.getProvincia())){
                sb.append("(").append(Provincia.getByLabel(puntoConsegna.getProvincia()).getSigla()).append(")");
            }

            puntoConsegnaParam = sb.toString();
        }

        // create data parameters for Cliente
        if(cliente != null){
            StringBuilder sb = new StringBuilder();
            if(!StringUtils.isEmpty(cliente.getRagioneSociale())){
                sb.append(cliente.getRagioneSociale()).append("\n");
            }
            if(!StringUtils.isEmpty(cliente.getIndirizzo())){
                sb.append(cliente.getIndirizzo()).append("\n");
            }
            if(!StringUtils.isEmpty(cliente.getCap())){
                sb.append(cliente.getCap()).append(" ");
            }
            if(!StringUtils.isEmpty(cliente.getCitta())){
                sb.append(cliente.getCitta()).append(" ");
            }
            if(!StringUtils.isEmpty(cliente.getProvincia())){
                sb.append(cliente.getProvincia());
            }

            destinatarioParam = sb.toString();
        }

        // create FatturaAccompagnatoriaDataSource
        List<FatturaAccompagnatoriaDataSource> fatturaAccompagnatoriaDataSources = new ArrayList<>();
        fatturaAccompagnatoriaDataSources.add(getFatturaAccompagnatoriaDataSource(fatturaAccompagnatoria));

        // create list of FatturaAccompagnatoriaRigheDataSource from FatturaAccompagnatoriaRiga
        List<FatturaAccompagnatoriaRigaDataSource> fatturaAccompagnatoriaRigaDataSources = getFatturaAccompagnatoriaRigheDataSource(fatturaAccompagnatoria);

        // create list of FatturaAccompagnatoriaTotaliDataSource from FatturaAccompagnatoriaTotale
        List<FatturaAccompagnatoriaTotaleDataSource> fatturaAccompagnatoriaTotaleDataSources = getFatturaAccompagnatoriaTotaliDataSource(fatturaAccompagnatoria);

        BigDecimal totaleImponibile = new BigDecimal(0);
        BigDecimal totaleIva = new BigDecimal(0);

        for(FatturaAccompagnatoriaTotaleDataSource fatturaAccompagnatoriaTotale: fatturaAccompagnatoriaTotaleDataSources){
            totaleImponibile = totaleImponibile.add(fatturaAccompagnatoriaTotale.getTotaleImponibile());
            totaleIva = totaleIva.add(fatturaAccompagnatoriaTotale.getTotaleIva());
        }

        // fetching the .jrxml file from the resources folder.
        final InputStream stream = this.getClass().getResourceAsStream(Constants.JASPER_REPORT_FATTURA_ACCOMPAGNATORIA);

        // create report datasource for FatturaAccompagnatoria
        JRBeanCollectionDataSource fatturaAccompagnatoriaCollectionDataSource = new JRBeanCollectionDataSource(fatturaAccompagnatoriaDataSources);

        // create report datasource for FatturaAccompagnatoriaRighe
        JRBeanCollectionDataSource fatturaAccompagnatoriaRigheCollectionDataSource = new JRBeanCollectionDataSource(fatturaAccompagnatoriaRigaDataSources);

        // create report datasource for FatturaAccompagnatoriaTotali
        JRBeanCollectionDataSource fatturaAccompagnatoriaTotaliCollectionDataSource = new JRBeanCollectionDataSource(fatturaAccompagnatoriaTotaleDataSources);

        // create report parameters
        Map<String, Object> parameters = createParameters();

        // add data to parameters
        parameters.put("fatturaAccompagnatoriaTitle", fatturaAccompagnatoriaTitleParam);
        parameters.put("destinatario", destinatarioParam);
        parameters.put("puntoConsegna", puntoConsegnaParam);
        parameters.put("note", fatturaAccompagnatoria.getNote());
        parameters.put("nota", Constants.JASPER_PARAMETER_FATTURA_ACCOMPAGNATORIA_NOTA);
        parameters.put("trasportatore", fatturaAccompagnatoria.getTrasportatore());
        parameters.put("tipoTrasporto", fatturaAccompagnatoria.getTipoTrasporto());
        parameters.put("numeroColli", fatturaAccompagnatoria.getNumeroColli());
        parameters.put("dataOraTrasporto", simpleDateFormat.format(fatturaAccompagnatoria.getDataTrasporto())+" "+fatturaAccompagnatoria.getOraTrasporto());
        parameters.put("totaleImponibile", totaleImponibile);
        parameters.put("totaleIva", totaleIva);
        parameters.put("fatturaAccompagnatoriaTotDocumento", Utils.roundPrice(totaleImponibile.add(totaleIva)));
        parameters.put("fatturaAccompagnatoriaCollection", fatturaAccompagnatoriaCollectionDataSource);
        parameters.put("fatturaAccompagnatoriaRigheCollection", fatturaAccompagnatoriaRigheCollectionDataSource);
        parameters.put("fatturaAccompagnatoriaTotaliCollection", fatturaAccompagnatoriaTotaliCollectionDataSource);


        // create report
        return JasperRunManager.runReportToPdf(stream, parameters, new JREmptyDataSource());
    }

    public byte[] generateFatturaAcquisto(Long idFatturaAcquisto) throws Exception{

        FatturaAcquisto fatturaAcquisto = getFatturaAcquisto(idFatturaAcquisto);
        Fornitore fornitore = fatturaAcquisto.getFornitore();

        String fatturaAcquistoTitleParam = fatturaAcquisto.getNumero()+" del "+simpleDateFormat.format(fatturaAcquisto.getData());
        String fornitoreParam = "";

        if(fornitore != null){
            StringBuilder sb = new StringBuilder();
            if(!StringUtils.isEmpty(fornitore.getRagioneSociale())){
                sb.append(fornitore.getRagioneSociale()).append("\n");
            }
            if(!StringUtils.isEmpty(fornitore.getIndirizzo())){
                sb.append(fornitore.getIndirizzo()).append("\n");
            }
            if(!StringUtils.isEmpty(fornitore.getCap())){
                sb.append(fornitore.getCap()).append(" ");
            }
            if(!StringUtils.isEmpty(fornitore.getCitta())){
                sb.append(fornitore.getCitta()).append(" ");
            }
            if(!StringUtils.isEmpty(fornitore.getProvincia())){
                sb.append("(").append(Provincia.getByLabel(fornitore.getProvincia()).getSigla()).append(")");
            }

            fornitoreParam = sb.toString();
        }

        // create FatturaAcquistoDataSource
        List<FatturaAcquistoDataSource> fatturaAcquistoDataSources = new ArrayList<>();
        fatturaAcquistoDataSources.add(getFatturaAcquistoDataSource(fatturaAcquisto));

        // create list of FatturaAcquistoRigheDataSource from FatturaAcquistoDdtAcquisti
        List<FatturaAcquistoRigaDataSource> fatturaAcquistoRigaDataSources = getFatturaAcquistoRigheDataSource(fatturaAcquisto);

        // create list of FatturaAcquistoTotaliDataSource from FatturaAcquistoTotale
        List<FatturaAcquistoTotaleDataSource> fatturaAcquistoTotaleDataSources = getFatturaAcquistoTotaliDataSource(fatturaAcquisto);

        BigDecimal totaleImponibile = fatturaAcquisto.getTotaleImponibile();
        BigDecimal totaleIva = fatturaAcquisto.getTotaleIva();

        // fetching the .jrxml file from the resources folder.
        final InputStream stream = this.getClass().getResourceAsStream(Constants.JASPER_REPORT_FATTURA_ACQUISTO);

        // create report datasource for FatturaAcquisto
        JRBeanCollectionDataSource fatturaAcquistoCollectionDataSource = new JRBeanCollectionDataSource(fatturaAcquistoDataSources);

        // create report datasource for FatturaAcquistoRighe
        JRBeanCollectionDataSource fatturaAcquistoRigheCollectionDataSource = new JRBeanCollectionDataSource(fatturaAcquistoRigaDataSources);

        // create report datasource for FatturaAcquistoTotali
        JRBeanCollectionDataSource fatturaAcquistoTotaliCollectionDataSource = new JRBeanCollectionDataSource(fatturaAcquistoTotaleDataSources);

        // create report parameters
        Map<String, Object> parameters = createParameters();

        // add data to parameters
        parameters.put("fatturaAcquistoTitle", fatturaAcquistoTitleParam);
        parameters.put("fornitoreParam", fornitoreParam);
        parameters.put("note", fatturaAcquisto.getNote());
        parameters.put("nota", Constants.JASPER_PARAMETER_FATTURA_NOTA);
        parameters.put("totaleImponibile", totaleImponibile);
        parameters.put("totaleIva", totaleIva);
        parameters.put("fatturaAcquistoTotDocumento", Utils.roundPrice(fatturaAcquisto.getTotale()));
        parameters.put("fatturaAcquistoCollection", fatturaAcquistoCollectionDataSource);
        parameters.put("fatturaAcquistoRigheCollection", fatturaAcquistoRigheCollectionDataSource);
        parameters.put("fatturaAcquistoTotaliCollection", fatturaAcquistoTotaliCollectionDataSource);

        // create report
        return JasperRunManager.runReportToPdf(stream, parameters, new JREmptyDataSource());
    }

    public byte[] generateFatturaAccompagnatoriaAcquisto(Long idFatturaAccompagnatoriaAcquisto) throws Exception{

        // retrieve the FatturaAccompagnatoriaAcquisto
        FatturaAccompagnatoriaAcquisto fatturaAccompagnatoriaAcquisto = getFatturaAccompagnatoriaAcquisto(idFatturaAccompagnatoriaAcquisto);
        Fornitore fornitore = fatturaAccompagnatoriaAcquisto.getFornitore();

        // create data parameters
        String fatturaAccompagnatoriaAcquistoTitleParam = fatturaAccompagnatoriaAcquisto.getNumero()+" del "+simpleDateFormat.format(fatturaAccompagnatoriaAcquisto.getData());

        String fornitoreParam = "";

        // create data parameters for Fornitore
        if(fornitore != null){
            StringBuilder sb = new StringBuilder();
            if(!StringUtils.isEmpty(fornitore.getRagioneSociale())){
                sb.append(fornitore.getRagioneSociale()).append("\n");
            }
            if(!StringUtils.isEmpty(fornitore.getIndirizzo())){
                sb.append(fornitore.getIndirizzo()).append("\n");
            }
            if(!StringUtils.isEmpty(fornitore.getCap())){
                sb.append(fornitore.getCap()).append(" ");
            }
            if(!StringUtils.isEmpty(fornitore.getCitta())){
                sb.append(fornitore.getCitta()).append(" ");
            }
            if(!StringUtils.isEmpty(fornitore.getProvincia())){
                sb.append(fornitore.getProvincia());
            }

            fornitoreParam = sb.toString();
        }

        // create FatturaAccompagnatoriaAcquistoDataSource
        List<FatturaAccompagnatoriaAcquistoDataSource> fatturaAccompagnatoriaAcquistoDataSources = new ArrayList<>();
        fatturaAccompagnatoriaAcquistoDataSources.add(getFatturaAccompagnatoriaAcquistoDataSource(fatturaAccompagnatoriaAcquisto));

        // create list of FatturaAccompagnatoriaAcquistoRigheDataSource from FatturaAccompagnatoriaAcquistoRiga
        List<FatturaAccompagnatoriaAcquistoRigaDataSource> fatturaAccompagnatoriaAcquistoRigaDataSources = getFatturaAccompagnatoriaAcquistoRigheDataSource(fatturaAccompagnatoriaAcquisto);

        // create list of FatturaAccompagnatoriaAcquistoTotaliDataSource from FatturaAccompagnatoriaAcquistoTotale
        List<FatturaAccompagnatoriaAcquistoTotaleDataSource> fatturaAccompagnatoriaAcquistoTotaleDataSources = getFatturaAccompagnatoriaAcquistoTotaliDataSource(fatturaAccompagnatoriaAcquisto);

        BigDecimal totaleImponibile = fatturaAccompagnatoriaAcquisto.getTotaleImponibile();
        BigDecimal totaleIva = fatturaAccompagnatoriaAcquisto.getTotaleIva();

        // fetching the .jrxml file from the resources folder.
        final InputStream stream = this.getClass().getResourceAsStream(Constants.JASPER_REPORT_FATTURA_ACCOMPAGNATORIA_ACQUISTO);

        // create report datasource for FatturaAccompagnatoriaAcquisto
        JRBeanCollectionDataSource fatturaAccompagnatoriaAcquistoCollectionDataSource = new JRBeanCollectionDataSource(fatturaAccompagnatoriaAcquistoDataSources);

        // create report datasource for FatturaAccompagnatoriaAcquistoRighe
        JRBeanCollectionDataSource fatturaAccompagnatoriaAcquistoRigheCollectionDataSource = new JRBeanCollectionDataSource(fatturaAccompagnatoriaAcquistoRigaDataSources);

        // create report datasource for FatturaAccompagnatoriaAcquistoTotali
        JRBeanCollectionDataSource fatturaAccompagnatoriaAcquistoTotaliCollectionDataSource = new JRBeanCollectionDataSource(fatturaAccompagnatoriaAcquistoTotaleDataSources);

        // create report parameters
        Map<String, Object> parameters = createParameters();

        // add data to parameters
        parameters.put("fatturaAccompagnatoriaAcquistoTitle", fatturaAccompagnatoriaAcquistoTitleParam);
        parameters.put("fornitore", fornitoreParam);
        parameters.put("note", fatturaAccompagnatoriaAcquisto.getNote());
        //parameters.put("nota", Constants.JASPER_PARAMETER_FATTURA_ACCOMPAGNATORIA_NOTA);
        parameters.put("trasportatore", fatturaAccompagnatoriaAcquisto.getTrasportatore());
        parameters.put("tipoTrasporto", fatturaAccompagnatoriaAcquisto.getTipoTrasporto());
        parameters.put("numeroColli", fatturaAccompagnatoriaAcquisto.getNumeroColli());
        parameters.put("dataOraTrasporto", simpleDateFormat.format(fatturaAccompagnatoriaAcquisto.getDataTrasporto())+" "+fatturaAccompagnatoriaAcquisto.getOraTrasporto());
        parameters.put("totaleImponibile", totaleImponibile);
        parameters.put("totaleIva", totaleIva);
        parameters.put("fatturaAccompagnatoriaAcquistoTotDocumento", Utils.roundPrice(totaleImponibile.add(totaleIva)));
        parameters.put("fatturaAccompagnatoriaAcquistoCollection", fatturaAccompagnatoriaAcquistoCollectionDataSource);
        parameters.put("fatturaAccompagnatoriaAcquistoRigheCollection", fatturaAccompagnatoriaAcquistoRigheCollectionDataSource);
        parameters.put("fatturaAccompagnatoriaAcquistoTotaliCollection", fatturaAccompagnatoriaAcquistoTotaliCollectionDataSource);

        // create report
        return JasperRunManager.runReportToPdf(stream, parameters, new JREmptyDataSource());
    }

    public byte[] generateFatture(java.sql.Date dataDa, java.sql.Date dataA, Integer progressivo, Float importo, String idTipoPagamento, String cliente, Integer idAgente, Integer idArticolo, Integer idStato, Integer idTipo) throws Exception {

        // retrieve the list of Fatture
        List<FatturaDataSource> fatture = getFatturaDataSources(dataDa, dataA, progressivo, importo, idTipoPagamento, cliente, idAgente, idArticolo, idStato, idTipo);

        // fetching the .jrxml file from the resources folder.
        final InputStream stream = this.getClass().getResourceAsStream(Constants.JASPER_REPORT_FATTURE);

        // create report datasource
        JRBeanCollectionDataSource dataSource = new JRBeanCollectionDataSource(fatture);

        // create report parameters
        Map<String, Object> parameters = createParameters();

        BigDecimal totaleAcconto = new BigDecimal(0);
        BigDecimal totale = new BigDecimal(0);
        BigDecimal totaleDaPagare = new BigDecimal(0);

        for(FatturaDataSource fattura: fatture){
            totaleAcconto = totaleAcconto.add(fattura.getAcconto());
            totale = totale.add(fattura.getTotale());
            totaleDaPagare = totaleDaPagare.add(fattura.getTotaleDaPagare());
        }

        // add data to parameters
        parameters.put("totaleAcconto", totaleAcconto);
        parameters.put("totale", totale);
        parameters.put("totaleDaPagare", totaleDaPagare);
        parameters.put("fattureCollection", dataSource);

        // create report
        return JasperRunManager.runReportToPdf(stream, parameters, new JREmptyDataSource());
    }

    public byte[] generateFatturePerCommercianti(String from, String to, Set<Fattura> fatture) throws Exception{

        // retrieve the list of FatturaCommercianti
        List<FatturaCommercianteDataSource> fatturaCommercianteDataSource = getFattureCommercianti(fatture);

        Map<String, Object> totaliCompleti = getFattureCommerciantiTotaliCompleti(fatturaCommercianteDataSource);

        // fetching the .jrxml file from the resources folder.
        final InputStream stream = this.getClass().getResourceAsStream(Constants.JASPER_REPORT_FATTURE_COMMERCIANTI);

        // create report datasource
        JRBeanCollectionDataSource dataSource = new JRBeanCollectionDataSource(fatturaCommercianteDataSource);

        // create report datasource for FatturaTotaliCompleti
        JRBeanCollectionDataSource fatturaTotaliCompletiCollectionDataSource = new JRBeanCollectionDataSource((List<FatturaCommercianteTotaleCompletoDataSource>)totaliCompleti.get("fatturaTotaliCompletiCollection"));

        // create report parameters
        Map<String, Object> parameters = createParameters();

        // add data to parameters
        parameters.put("from", from);
        parameters.put("to", to);
        parameters.put("totaleCompleto", totaliCompleti.get("totaleCompleto"));
        parameters.put("fatturaCommercianteCollection", dataSource);
        parameters.put("fatturaTotaliCompletiCollection", fatturaTotaliCompletiCollectionDataSource);

        // create report
        return JasperRunManager.runReportToPdf(stream, parameters, new JREmptyDataSource());
    }

    public byte[] generateGiacenzeIngredienti(String giacenzeIngredientiIds) throws Exception{

        // retrieve the list of GiacenzeIngredienti
        List<VGiacenzaIngrediente> giacenzeIngredienti = getGiacenzeIngredienti(giacenzeIngredientiIds);

        // fetching the .jrxml file from the resources folder.
        final InputStream stream = this.getClass().getResourceAsStream(Constants.JASPER_REPORT_GIACENZE_INGREDIENTI);

        // create report datasource
        JRBeanCollectionDataSource dataSource = new JRBeanCollectionDataSource(giacenzeIngredienti);

        // create report parameters
        Map<String, Object> parameters = createParameters();

        // add data to parameters
        parameters.put("CollectionBeanParam", dataSource);

        // create report
        return JasperRunManager.runReportToPdf(stream, parameters, new JREmptyDataSource());
    }

    public byte[] generateListino(Long idListino, Long idFornitore, Long idCategoriaArticolo, String orderBy) throws Exception{

        orderBy = StringUtils.isNotEmpty(orderBy) ? orderBy : "categoria-articolo";

        // retrieve the Listino
        Listino listino = listinoService.getOne(idListino);

        String listinoTitleParam = listino.getNome();

        List<ListinoPrezzo> listinoPrezzi = listinoService.getListiniPrezziByListinoId(idListino);
        listinoPrezzi = listinoPrezzi.stream().filter(lp -> lp.getArticolo().getAttivo()).collect(Collectors.toList());
        if(idFornitore != null){
            listinoPrezzi = listinoPrezzi.stream()
                    .filter(lp -> lp.getArticolo().getFornitore() != null)
                    .filter(lp -> lp.getArticolo().getFornitore().getId().equals(idFornitore))
                    .collect(Collectors.toList());
        }
        if(idCategoriaArticolo != null){
            listinoPrezzi = listinoPrezzi.stream()
                    .filter(lp -> lp.getArticolo().getCategoria() != null)
                    .filter(lp -> lp.getArticolo().getCategoria().getId().equals(idCategoriaArticolo))
                    .collect(Collectors.toList());
        }

        List<ListinoPrezzoDataSource> listinoPrezziDataSource = new ArrayList<>();
        if(!listinoPrezzi.isEmpty()){
            for(ListinoPrezzo lp : listinoPrezzi){
                ListinoPrezzoDataSource listinoPrezzoDataSource = new ListinoPrezzoDataSource();
                listinoPrezzoDataSource.setPrezzo(lp.getPrezzo() != null ? Utils.roundPrice(lp.getPrezzo()) : Utils.roundPrice(new BigDecimal(0)));

                Articolo articolo = lp.getArticolo();
                if(articolo != null){
                    listinoPrezzoDataSource.setDescrizioneFullArticolo(articolo.getCodice() + " - " + articolo.getDescrizione().trim());
                    listinoPrezzoDataSource.setDescrizioneArticolo(articolo.getDescrizione().toLowerCase().trim());
                    if(articolo.getCategoria() != null) {
                        listinoPrezzoDataSource.setCategoriaArticolo(articolo.getCategoria().getNome());
                    }
                    if(articolo.getUnitaMisura() != null){
                        listinoPrezzoDataSource.setUnitaDiMisura(articolo.getUnitaMisura().getNome());
                    }
                    Fornitore fornitore = null;
                    if(articolo.getFornitore() != null){
                        fornitore = articolo.getFornitore();
                        listinoPrezzoDataSource.setFornitore(fornitore.getRagioneSociale());
                    }

                    if(orderBy.equals("fornitore")){
                        listinoPrezzoDataSource.setGroupField(fornitore != null ? fornitore.getRagioneSociale() : null);
                        listinoPrezzoDataSource.setIsGroup(1);
                    } else if(orderBy.equals("categoria-articolo")){
                        listinoPrezzoDataSource.setGroupField(articolo.getCategoria() != null ? articolo.getCategoria().getNome() : null);
                        listinoPrezzoDataSource.setIsGroup(1);
                    } else {
                        listinoPrezzoDataSource.setIsGroup(0);
                    }
                }
                listinoPrezziDataSource.add(listinoPrezzoDataSource);
            }
        }

        if(orderBy.equals("descrizione-articolo")){
            listinoPrezziDataSource = listinoPrezziDataSource
                    .stream()
                    .sorted(Comparator.comparing(ListinoPrezzoDataSource::getDescrizioneArticolo, Comparator.nullsLast(Comparator.naturalOrder())))
                    .collect(Collectors.toList());
        } else if(orderBy.equals("fornitore")){
            listinoPrezziDataSource = listinoPrezziDataSource
                    .stream()
                    .sorted(Comparator.comparing(ListinoPrezzoDataSource::getFornitore, Comparator.nullsLast(Comparator.naturalOrder())).thenComparing(ListinoPrezzoDataSource::getDescrizioneArticolo, Comparator.nullsLast(Comparator.naturalOrder())))
                    .collect(Collectors.toList());
        } else {
            listinoPrezziDataSource = listinoPrezziDataSource
                    .stream()
                    .sorted(Comparator.comparing(ListinoPrezzoDataSource::getCategoriaArticolo, Comparator.nullsLast(Comparator.naturalOrder())).thenComparing(ListinoPrezzoDataSource::getDescrizioneArticolo, Comparator.nullsLast(Comparator.naturalOrder())))
                    .collect(Collectors.toList());
        }

        // fetching the .jrxml file from the resources folder.
        final InputStream stream = this.getClass().getResourceAsStream(Constants.JASPER_REPORT_LISTINO);

        // create report datasource for OrdineFornitoreArticoli
        JRBeanCollectionDataSource dataSource = new JRBeanCollectionDataSource(listinoPrezziDataSource);

        // create report parameters
        Map<String, Object> parameters = createParameters();

        // add data to parameters
        parameters.put("listinoTitle", listinoTitleParam);
        parameters.put("listinoPrezziCollection", dataSource);

        // create report
        return JasperRunManager.runReportToPdf(stream, parameters, new JREmptyDataSource());
    }

    public byte[] generateNotaAccredito(Long idNotaAccredito) throws Exception {

        // retrieve the NotaAccredito
        NotaAccredito notaAccredito = getNotaAccredito(idNotaAccredito);
        Cliente cliente = notaAccredito.getCliente();

        // create data parameters
        String notaAccreditoTitleParam = notaAccredito.getProgressivo()+"/"+notaAccredito.getAnno()+" del "+simpleDateFormat.format(notaAccredito.getData());
        String destinatarioParam = "";

        String riferimentoDocumento = "Riferimento "+notaAccredito.getTipoRiferimento();
        if(!StringUtils.isEmpty(notaAccredito.getDocumentoRiferimento())){
            riferimentoDocumento += " n. "+notaAccredito.getDocumentoRiferimento();
        }
        if(notaAccredito.getDataDocumentoRiferimento() != null){
            riferimentoDocumento += " del "+simpleDateFormat.format(notaAccredito.getDataDocumentoRiferimento());
        }

        // create data parameters for Cliente
        if(cliente != null){
            StringBuilder sb = new StringBuilder();
            if(cliente.getPrivato() || cliente.getDittaIndividuale()){
                sb.append(cliente.getNome()).append(" ").append(cliente.getCognome()).append("\n");
            } else {
                sb.append(cliente.getRagioneSociale()).append("\n");
            }

            if(!StringUtils.isEmpty(cliente.getIndirizzo())){
                sb.append(cliente.getIndirizzo()).append("\n");
            }
            if(!StringUtils.isEmpty(cliente.getCap())){
                sb.append(cliente.getCap()).append(" ");
            }
            if(!StringUtils.isEmpty(cliente.getCitta())){
                sb.append(cliente.getCitta()).append(" ");
            }
            if(!StringUtils.isEmpty(cliente.getProvincia())){
                sb.append(cliente.getProvincia());
            }

            destinatarioParam = sb.toString();
        }

        // create NotaAccreditoDataSource
        List<NotaAccreditoDataSource> notaAccreditoDataSources = new ArrayList<>();
        notaAccreditoDataSources.add(getNotaAccreditoDataSource(notaAccredito));

        // create list of NotaAccreditoRigheDataSource from NotaAccreditoRiga
        List<NotaAccreditoRigaDataSource> notaAccreditoRigaDataSources = getNotaAccreditoRigheDataSource(notaAccredito);

        // create list of NotaAccreditoTotaliDataSource from NotaAccreditoTotale
        List<NotaAccreditoTotaleDataSource> notaAccreditoTotaleDataSources = getNotaAccreditoTotaliDataSource(notaAccredito);

        BigDecimal totaleImponibile = new BigDecimal(0);
        BigDecimal totaleIva = new BigDecimal(0);

        for(NotaAccreditoTotaleDataSource notaAccreditoTotale: notaAccreditoTotaleDataSources){
            totaleImponibile = totaleImponibile.add(notaAccreditoTotale.getTotaleImponibile());
            totaleIva = totaleIva.add(notaAccreditoTotale.getTotaleIva());
        }

        // fetching the .jrxml file from the resources folder.
        final InputStream stream = this.getClass().getResourceAsStream(Constants.JASPER_REPORT_NOTA_ACCREDITO);

        // create report datasource for NotaAccredito
        JRBeanCollectionDataSource notaAccreditoCollectionDataSource = new JRBeanCollectionDataSource(notaAccreditoDataSources);

        // create report datasource for NotaAccreditoRighe
        JRBeanCollectionDataSource notaAccreditoRigheCollectionDataSource = new JRBeanCollectionDataSource(notaAccreditoRigaDataSources);

        // create report datasource for NotaAccreditoTotali
        JRBeanCollectionDataSource notaAccreditoTotaliCollectionDataSource = new JRBeanCollectionDataSource(notaAccreditoTotaleDataSources);

        // create report parameters
        Map<String, Object> parameters = createParameters();

        // add data to parameters
        parameters.put("notaAccreditoTitle", notaAccreditoTitleParam);
        parameters.put("destinatario", destinatarioParam);
        parameters.put("riferimentoDocumento", riferimentoDocumento);
        parameters.put("note", notaAccredito.getNote());
        parameters.put("totaleImponibile", totaleImponibile);
        parameters.put("totaleIva", totaleIva);
        parameters.put("notaAccreditoTotDocumento", Utils.roundPrice(totaleImponibile.add(totaleIva)));
        parameters.put("notaAccreditoCollection", notaAccreditoCollectionDataSource);
        parameters.put("notaAccreditoRigheCollection", notaAccreditoRigheCollectionDataSource);
        parameters.put("notaAccreditoTotaliCollection", notaAccreditoTotaliCollectionDataSource);

        // create report
        return JasperRunManager.runReportToPdf(stream, parameters, new JREmptyDataSource());
    }

    public byte[] generateNotaReso(Long idNotaReso) throws Exception {

        // retrieve the NotaReso
        NotaReso notaReso = getNotaReso(idNotaReso);
        Fornitore fornitore = notaReso.getFornitore();

        // create data parameters
        String notaResoTitleParam = notaReso.getProgressivo()+"/"+notaReso.getAnno()+" del "+simpleDateFormat.format(notaReso.getData());
        String destinatarioParam = "";

        // create data parameters for Fornitore
        if(fornitore != null){
            StringBuilder sb = new StringBuilder();
            if(!StringUtils.isEmpty(fornitore.getRagioneSociale())){
                sb.append(fornitore.getRagioneSociale()).append("\n");
            }
            if(!StringUtils.isEmpty(fornitore.getIndirizzo())){
                sb.append(fornitore.getIndirizzo()).append("\n");
            }
            if(!StringUtils.isEmpty(fornitore.getCap())){
                sb.append(fornitore.getCap()).append(" ");
            }
            if(!StringUtils.isEmpty(fornitore.getCitta())){
                sb.append(fornitore.getCitta()).append(" ");
            }
            if(!StringUtils.isEmpty(fornitore.getProvincia())){
                sb.append(fornitore.getProvincia());
            }

            destinatarioParam = sb.toString();
        }

        // create NotaResoDataSource
        List<NotaResoDataSource> notaResoDataSources = new ArrayList<>();
        notaResoDataSources.add(getNotaResoDataSource(notaReso));

        // create list of NotaResoRigheDataSource from NotaResoRiga
        List<NotaResoRigaDataSource> notaResoRigaDataSources = getNotaResoRigheDataSource(notaReso);

        // create list of NotaResoTotaliDataSource from NotaResoTotale
        List<NotaResoTotaleDataSource> notaResoTotaleDataSources = getNotaResoTotaliDataSource(notaReso);

        BigDecimal totaleImponibile = new BigDecimal(0);
        BigDecimal totaleIva = new BigDecimal(0);

        for(NotaResoTotaleDataSource notaResoTotale: notaResoTotaleDataSources){
            totaleImponibile = totaleImponibile.add(notaResoTotale.getTotaleImponibile());
            totaleIva = totaleIva.add(notaResoTotale.getTotaleIva());
        }

        // fetching the .jrxml file from the resources folder.
        final InputStream stream = this.getClass().getResourceAsStream(Constants.JASPER_REPORT_NOTA_RESO);

        // create report datasource for NotaReso
        JRBeanCollectionDataSource notaResoCollectionDataSource = new JRBeanCollectionDataSource(notaResoDataSources);

        // create report datasource for NotaResoRighe
        JRBeanCollectionDataSource notaResoRigheCollectionDataSource = new JRBeanCollectionDataSource(notaResoRigaDataSources);

        // create report datasource for NotaResoTotali
        JRBeanCollectionDataSource notaResoTotaliCollectionDataSource = new JRBeanCollectionDataSource(notaResoTotaleDataSources);

        // create report parameters
        Map<String, Object> parameters = createParameters();

        // add data to parameters
        parameters.put("notaResoTitle", notaResoTitleParam);
        parameters.put("destinatario", destinatarioParam);
        parameters.put("note", notaReso.getNote());
        parameters.put("totaleImponibile", totaleImponibile);
        parameters.put("totaleIva", totaleIva);
        parameters.put("notaResoTotDocumento", Utils.roundPrice(totaleImponibile.add(totaleIva)));
        parameters.put("notaResoCollection", notaResoCollectionDataSource);
        parameters.put("notaResoRigheCollection", notaResoRigheCollectionDataSource);
        parameters.put("notaResoTotaliCollection", notaResoTotaliCollectionDataSource);

        // create report
        return JasperRunManager.runReportToPdf(stream, parameters, new JREmptyDataSource());
    }

    public byte[] generateNoteAccredito(java.sql.Date dataDa, java.sql.Date dataA, Integer progressivo, Float importo, String cliente, Integer idAgente, Integer idArticolo, Integer idStato) throws Exception {

        // retrieve the list of NoteAccredito
        List<NotaAccreditoDataSource> noteAccredito = getNotaAccreditoDataSources(dataDa, dataA, progressivo, importo, cliente, idAgente, idArticolo, idStato);

        // fetching the .jrxml file from the resources folder.
        final InputStream stream = this.getClass().getResourceAsStream(Constants.JASPER_REPORT_NOTE_ACCREDITO);

        // create report datasource
        JRBeanCollectionDataSource dataSource = new JRBeanCollectionDataSource(noteAccredito);

        // create report parameters
        Map<String, Object> parameters = createParameters();

        BigDecimal totaleAcconto = new BigDecimal(0);
        BigDecimal totale = new BigDecimal(0);
        BigDecimal totaleDaPagare = new BigDecimal(0);

        for(NotaAccreditoDataSource notaAccredito: noteAccredito){
            totaleAcconto = totaleAcconto.add(notaAccredito.getAcconto());
            totale = totale.add(notaAccredito.getTotale());
            totaleDaPagare = totaleDaPagare.add(notaAccredito.getTotaleDaPagare());
        }

        // add data to parameters
        parameters.put("totaleAcconto", totaleAcconto);
        parameters.put("totale", totale);
        parameters.put("totaleDaPagare", totaleDaPagare);
        parameters.put("noteAccreditoCollection", dataSource);

        // create report
        return JasperRunManager.runReportToPdf(stream, parameters, new JREmptyDataSource());
    }

    public byte[] generateOrdineFornitore(Long idOrdineFornitore) throws Exception{

        // retrieve the Fattura
        OrdineFornitore ordineFornitore = getOrdineFornitore(idOrdineFornitore);

        // create data parameters
        String ordineFornitoreTitleParam = ordineFornitore.getProgressivo()+"/"+ordineFornitore.getAnnoContabile();
        String ordineFornitoreFooterParam = "San Giovanni Ilarione " + LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss"));

        // create list of OrdineFornitoreArticoliDataSource
        List<OrdineFornitoreArticoloDataSource> ordineFornitoreArticoliDataSources = getOrdineFornitoreArticoliDataSource(ordineFornitore);

        // fetching the .jrxml file from the resources folder.
        final InputStream stream = this.getClass().getResourceAsStream(Constants.JASPER_REPORT_ORDINE_FORNITORE);

        // create report datasource for OrdineFornitoreArticoli
        JRBeanCollectionDataSource ordineFornitoreArticoliCollectionDataSource = new JRBeanCollectionDataSource(ordineFornitoreArticoliDataSources);

        // create report parameters
        Map<String, Object> parameters = createParameters();

        // add data to parameters
        parameters.put("ordineFornitoreTitle", ordineFornitoreTitleParam);
        parameters.put("ordineFornitoreFooter", ordineFornitoreFooterParam);
        parameters.put("ordineFornitoreArticoliCollection", ordineFornitoreArticoliCollectionDataSource);

        // create report
        return JasperRunManager.runReportToPdf(stream, parameters, new JREmptyDataSource());
    }

    public byte[] generateOrdiniAutisti(Long idAutista, Date dataConsegnaDa, Date dataConsegnaA) throws Exception {

        // retrieve Autista with id 'idAutista'
        Autista autista = getAutista(idAutista);
        String autistaLabel = "";
        if(autista != null){
            autistaLabel = autista.getNome()+" "+autista.getCognome();
        }

        // retrieve the list of OrdiniClienti
        List<OrdineAutistaDataSource> ordineAutistaDataSources = getOrdiniAutista(idAutista, dataConsegnaDa, dataConsegnaA);

        // fetching the .jrxml file from the resources folder.
        final InputStream stream = this.getClass().getResourceAsStream(Constants.JASPER_REPORT_ORDINI_AUTISTI);

        // create report datasource
        JRBeanCollectionDataSource dataSource = new JRBeanCollectionDataSource(ordineAutistaDataSources);

        // create report parameters
        Map<String, Object> parameters = createParameters();

        // add data to parameters
        parameters.put("autista", autistaLabel);
        parameters.put("dataConsegnaDa", simpleDateFormat.format(dataConsegnaDa));
        parameters.put("dataConsegnaA", simpleDateFormat.format(dataConsegnaA));
        parameters.put("ordineAutistaCollection", dataSource);

        // create report
        return JasperRunManager.runReportToPdf(stream, parameters, new JREmptyDataSource());
    }

    public byte[] generatePagamenti(String pagamentiIds) throws Exception {

        // retrieve the list of Pagamenti
        List<PagamentoDataSource> pagamenti = getPagamenti(pagamentiIds);

        // fetching the .jrxml file from the resources folder.
        final InputStream stream = this.getClass().getResourceAsStream(Constants.JASPER_REPORT_PAGAMENTI);

        // create report datasource
        JRBeanCollectionDataSource dataSource = new JRBeanCollectionDataSource(pagamenti);

        // create report parameters
        Map<String, Object> parameters = createParameters();

        // add data to parameters
        parameters.put("pagamentiCollection", dataSource);

        // create report
        return JasperRunManager.runReportToPdf(stream, parameters, new JREmptyDataSource());
    }

    public byte[] generateRicevutaPrivato(Long idRicevutaPrivato) throws Exception {

        // retrieve the RicevutaPrivato
        RicevutaPrivato ricevutaPrivato = getRicevutaPrivato(idRicevutaPrivato);
        PuntoConsegna puntoConsegna = ricevutaPrivato.getPuntoConsegna();
        Cliente cliente = ricevutaPrivato.getCliente();

        // create RicevutaPrivatoDataSource
        List<RicevutaPrivatoDataSource> ricevutaPrivatoDataSources = new ArrayList<>();
        ricevutaPrivatoDataSources.add(getRicevutaPrivatoDataSource(ricevutaPrivato));

        // create data parameters
        String ricevutaPrivatoTitleParam = ricevutaPrivato.getProgressivo()+"/"+ricevutaPrivato.getAnno()+" del "+simpleDateFormat.format(ricevutaPrivato.getData());
        String puntoConsegnaParam = "";
        String destinatarioParam = "";

        // create data parameters for PuntoConsegna
        if(puntoConsegna != null){
            StringBuilder sb = new StringBuilder();
            if(!StringUtils.isEmpty(puntoConsegna.getNome())){
                sb.append(puntoConsegna.getNome()).append("\n");
            }
            if(!StringUtils.isEmpty(puntoConsegna.getIndirizzo())){
                sb.append(puntoConsegna.getIndirizzo()).append("\n");
            }
            if(!StringUtils.isEmpty(puntoConsegna.getCap())){
                sb.append(puntoConsegna.getCap()).append(" ");
            }
            if(!StringUtils.isEmpty(puntoConsegna.getLocalita())){
                sb.append(puntoConsegna.getLocalita()).append(" ");
            }
            if(!StringUtils.isEmpty(puntoConsegna.getProvincia())){
                sb.append("(").append(Provincia.getByLabel(puntoConsegna.getProvincia()).getSigla()).append(")");
            }

            puntoConsegnaParam = sb.toString();
        }

        // create data parameters for Cliente
        if(cliente != null){
            StringBuilder sb = new StringBuilder();
            if(!StringUtils.isEmpty(cliente.getNome())){
                sb.append(cliente.getNome());
            }
            if(!StringUtils.isEmpty(cliente.getCognome())){
                sb.append(" ").append(cliente.getCognome()).append("\n");
            }
            if(!StringUtils.isEmpty(cliente.getIndirizzo())){
                sb.append(cliente.getIndirizzo()).append("\n");
            }
            if(!StringUtils.isEmpty(cliente.getCap())){
                sb.append(cliente.getCap()).append(" ");
            }
            if(!StringUtils.isEmpty(cliente.getCitta())){
                sb.append(cliente.getCitta()).append(" ");
            }
            if(!StringUtils.isEmpty(cliente.getProvincia())){
                sb.append("(").append(Provincia.getByLabel(cliente.getProvincia()).getSigla()).append(")");
            }

            destinatarioParam = sb.toString();
        }

        // create 'ricevutaPrivatoTrasportoDataOra' param
        String ricevutaPrivatoTrasportoDataOraParam = simpleDateFormat.format(ricevutaPrivato.getDataTrasporto())+" "+ricevutaPrivato.getOraTrasporto();

        // create list of RicevutaPrivatoArticoloDataSource from RicevutaPrivatoArticolo
        List<RicevutaPrivatoArticoloDataSource> ricevutaPrivatoArticoloDataSources = getRicevutaPrivatoArticoliDataSource(ricevutaPrivato);

        BigDecimal totaleIva = BigDecimal.ZERO;
        if(!ricevutaPrivatoArticoloDataSources.isEmpty()){
            totaleIva = ricevutaPrivatoArticoloDataSources.stream().map(RicevutaPrivatoArticoloDataSource::getIvaValore).reduce(BigDecimal.ZERO, BigDecimal::add);
        }

        // fetching the .jrxml file from the resources folder.
        final InputStream stream = this.getClass().getResourceAsStream(Constants.JASPER_REPORT_RICEVUTA_PRIVATO);

        // create report datasource for RicevutaPrivato
        JRBeanCollectionDataSource ricevutaPrivatoCollectionDataSource = new JRBeanCollectionDataSource(ricevutaPrivatoDataSources);

        // create report datasource for RicevutaPrivatoArticoli
        JRBeanCollectionDataSource ricevutaPrivatoArticoliCollectionDataSource = new JRBeanCollectionDataSource(ricevutaPrivatoArticoloDataSources);

        // create report parameters
        Map<String, Object> parameters = createParameters();

        // add data to parameters
        parameters.put("ricevutaPrivatoTitle", ricevutaPrivatoTitleParam);
        parameters.put("puntoConsegna", puntoConsegnaParam);
        parameters.put("destinatario", destinatarioParam);
        parameters.put("note", ricevutaPrivato.getNote());
        parameters.put("trasportatore", ricevutaPrivato.getTrasportatore());
        parameters.put("nota", Constants.JASPER_PARAMETER_RICEVUTA_PRIVATO_NOTA);
        parameters.put("totaleIva", totaleIva);
        parameters.put("ricevutaPrivatoTrasportoTipo", ricevutaPrivato.getTipoTrasporto());
        parameters.put("ricevutaPrivatoTrasportoDataOra", ricevutaPrivatoTrasportoDataOraParam);
        parameters.put("ricevutaPrivatoNumeroColli", ricevutaPrivato.getNumeroColli());
        parameters.put("ricevutaPrivatoTotImponibile", Utils.roundPrice(ricevutaPrivato.getTotaleImponibile()));
        parameters.put("ricevutaPrivatoTotIva", Utils.roundPrice(ricevutaPrivato.getTotaleIva()));
        parameters.put("ricevutaPrivatoTotDocumento", Utils.roundPrice(ricevutaPrivato.getTotale()));
        parameters.put("ricevutaPrivatoArticoliCollection", ricevutaPrivatoArticoliCollectionDataSource);
        parameters.put("ricevutaPrivatoCollection", ricevutaPrivatoCollectionDataSource);

        // create report
        return JasperRunManager.runReportToPdf(stream, parameters, new JREmptyDataSource());
    }

    public byte[] generateRicevutePrivati(String ricevutePrivatiIds) throws Exception {

        // retrieve the list of RicevutePrivato
        List<RicevutaPrivatoDataSource> ricevutePrivato = getRicevutaPrivatoDataSources(ricevutePrivatiIds);

        // fetching the .jrxml file from the resources folder.
        final InputStream stream = this.getClass().getResourceAsStream(Constants.JASPER_REPORT_RICEVUTE_PRIVATI);

        // create report datasource
        JRBeanCollectionDataSource dataSource = new JRBeanCollectionDataSource(ricevutePrivato);

        // create report parameters
        Map<String, Object> parameters = createParameters();

        BigDecimal totaleAcconto = new BigDecimal(0);
        BigDecimal totale = new BigDecimal(0);
        BigDecimal totaleDaPagare = new BigDecimal(0);

        for(RicevutaPrivatoDataSource ricevutaPrivato: ricevutePrivato){
            totaleAcconto = totaleAcconto.add(ricevutaPrivato.getAcconto());
            totale = totale.add(ricevutaPrivato.getTotale());
            totaleDaPagare = totaleDaPagare.add(ricevutaPrivato.getTotaleDaPagare());
        }

        // add data to parameters
        parameters.put("totaleAcconto", totaleAcconto);
        parameters.put("totale", totale);
        parameters.put("totaleDaPagare", totaleDaPagare);
        parameters.put("ricevutePrivatoCollection", dataSource);

        // create report
        return JasperRunManager.runReportToPdf(stream, parameters, new JREmptyDataSource());
    }

    public byte[] generateSchedaTecnica(Long idSchedaTecnica) throws Exception {

        SchedaTecnica schedaTecnica = schedaTecnicaService.getById(idSchedaTecnica);

        SchedaTecnicaDataSource schedaTecnicaDataSource = SchedaTecnicaDataSource.from(schedaTecnica);
        List<SchedaTecnicaNutrienteDataSource> schedaTecnicaNutrienteDataSources = new ArrayList<>();
        if(!schedaTecnica.getSchedaTecnicaNutrienti().isEmpty()){
            for(SchedaTecnicaNutriente schedaTecnicaNutriente : schedaTecnica.getSchedaTecnicaNutrienti()){
                schedaTecnicaNutrienteDataSources.add(SchedaTecnicaNutrienteDataSource.from(schedaTecnicaNutriente));
            }
            schedaTecnicaNutrienteDataSources.sort(Comparator.comparing(n -> n.getNutriente().toLowerCase()));
        }
        List<SchedaTecnicaAnalisiDataSource> schedaTecnicaAnalisiDataSources = new ArrayList<>();
        if(!schedaTecnica.getSchedaTecnicaAnalisi().isEmpty()){
            for(SchedaTecnicaAnalisi schedaTecnicaAnalisi : schedaTecnica.getSchedaTecnicaAnalisi()){
                schedaTecnicaAnalisiDataSources.add(SchedaTecnicaAnalisiDataSource.from(schedaTecnicaAnalisi));
            }
            schedaTecnicaAnalisiDataSources.sort(Comparator.comparing(a -> a.getAnalisi().toLowerCase()));
        }
        List<SchedaTecnicaRaccoltaDataSource> schedaTecnicaRaccoltaDataSources = new ArrayList<>();
        if(!schedaTecnica.getSchedaTecnicaRaccolte().isEmpty()){
            for(SchedaTecnicaRaccolta schedaTecnicaRaccolta : schedaTecnica.getSchedaTecnicaRaccolte()){
                schedaTecnicaRaccoltaDataSources.add(SchedaTecnicaRaccoltaDataSource.from(schedaTecnicaRaccolta));
            }
            schedaTecnicaRaccoltaDataSources.sort(Comparator.comparing(m -> m.getMateriale().toLowerCase()));
        }

        final InputStream stream = this.getClass().getResourceAsStream(Constants.JASPER_REPORT_SCHEDA_TECNICA);

        JRBeanCollectionDataSource schedaTecnicaNutrientiCollectionDataSource = new JRBeanCollectionDataSource(schedaTecnicaNutrienteDataSources);
        JRBeanCollectionDataSource schedaTecnicaAnalisiCollectionDataSource = new JRBeanCollectionDataSource(schedaTecnicaAnalisiDataSources);
        JRBeanCollectionDataSource schedaTecnicaRaccolteCollectionDataSource = new JRBeanCollectionDataSource(schedaTecnicaRaccoltaDataSources);

        // create report parameters
        Map<String, Object> parameters = createParameters(Constants.JASPER_REPORT_SCHEDA_TECNICA);

        // add data to parameters
        parameters.put("revisione", schedaTecnicaDataSource.getRevisione());
        parameters.put("notaSchedaTecnica", Constants.JASPER_PARAMETER_SCHEDA_TECNICA_NOTA);
        parameters.put("notaDisposizioniComune", Constants.JASPER_PARAMETER_DISPOSIZIONI_COMUNE_NOTA);
        parameters.put("codiceProdotto", schedaTecnicaDataSource.getCodiceProdotto());
        parameters.put("pesoNettoConfezione", schedaTecnicaDataSource.getPesoNettoConfezione());
        parameters.put("ingredienti", schedaTecnicaDataSource.getIngredienti());
        parameters.put("allergeniTracce", schedaTecnicaDataSource.getAllergeniTracce());
        parameters.put("durata", schedaTecnicaDataSource.getDurata());
        parameters.put("conservazione", schedaTecnicaDataSource.getConservazione());
        parameters.put("consigliConsumo", schedaTecnicaDataSource.getConsigliConsumo());
        parameters.put("tipologiaConfezionamento", schedaTecnicaDataSource.getTipologiaConfezionamento());
        parameters.put("imballo", schedaTecnicaDataSource.getImballo());
        parameters.put("imballoDimensioni", schedaTecnicaDataSource.getImballoDimensioni());
        parameters.put("schedaTecnicaNutrientiCollection", schedaTecnicaNutrientiCollectionDataSource);
        parameters.put("schedaTecnicaAnalisiCollection", schedaTecnicaAnalisiCollectionDataSource);
        parameters.put("schedaTecnicaRaccolteCollection", schedaTecnicaRaccolteCollectionDataSource);

        return JasperRunManager.runReportToPdf(stream, parameters, new JREmptyDataSource());
    }

    public static HttpHeaders createHttpHeaders(String fileName){
        HttpHeaders headers = new HttpHeaders();
        headers.add(HttpHeaders.CONTENT_DISPOSITION, "inline; filename="+fileName);
        headers.add(HttpHeaders.CACHE_CONTROL, Constants.HTTP_HEADER_CACHE_CONTROL_VALUE);
        headers.add(HttpHeaders.PRAGMA, Constants.HTTP_HEADER_PRAGMA_VALUE);
        headers.add(HttpHeaders.EXPIRES, Constants.HTTP_HEADER_EXPIRES_VALUE);
        return headers;
    }

    public Map<String, Object> createParameters(){

        Map<String, DittaInfo> dittaInfoMap = DittaInfoSingleton.get().getDittaInfoMap();

        Map<String, Object> parameters = new HashMap<>();
        parameters.put("logo", this.getClass().getResource(Constants.JASPER_REPORT_LOGO_IMAGE_PATH));
        parameters.put("bollino", this.getClass().getResource(Constants.JASPER_REPORT_BOLLINO_IMAGE_PATH));
        parameters.put("headerSubReportPath", this.getClass().getResource(Constants.JASPER_REPORT_HEADER_SUBREPORT_PATH).toString());
        parameters.put("headerIntestazione", dittaInfoMap.get("PDF_INTESTAZIONE").getValore());
        parameters.put("headerIntestazione2", dittaInfoMap.get("PDF_INTESTAZIONE_2").getValore());
        parameters.put("headerIndirizzo", dittaInfoMap.get("PDF_INDIRIZZO").getValore());
        parameters.put("headerPartitaIva", "P.Iva "+ dittaInfoMap.get("PARTITA_IVA").getValore());
        parameters.put("headerCodiceFiscale", "Cod. Fisc. " + dittaInfoMap.get("CODICE_FISCALE").getValore());
        parameters.put("headerRea", "REA " + dittaInfoMap.get("REA").getValore());
        parameters.put("headerTelefono", "Tel: " + dittaInfoMap.get("TELEFONO").getValore());
        parameters.put("headerCellulare", "Cell: " + dittaInfoMap.get("CELLULARE").getValore());
        parameters.put("headerWebsite", "Website " + dittaInfoMap.get("WEBSITE").getValore());
        parameters.put("headerEmail", "E-mail " + dittaInfoMap.get("EMAIL").getValore());
        parameters.put("headerPec", "Pec " + dittaInfoMap.get("PEC").getValore());

        return parameters;
    }

    public Map<String, Object> createParameters(String reportName){

        if(StringUtils.isEmpty(reportName)){
            return createParameters();
        }

        Map<String, DittaInfo> dittaInfoMap = DittaInfoSingleton.get().getDittaInfoMap();

        Map<String, Object> parameters = new HashMap<>();

        if(reportName.equals(Constants.JASPER_REPORT_SCHEDA_TECNICA)){
            parameters.put("logo", this.getClass().getResource(Constants.JASPER_REPORT_LOGO_IMAGE_PATH));
            parameters.put("bollino", this.getClass().getResource(Constants.JASPER_REPORT_BOLLINO_IMAGE_PATH));
            parameters.put("headerIntestazione", dittaInfoMap.get("PDF_INTESTAZIONE").getValore());
            parameters.put("headerPartitaIva", dittaInfoMap.get("PARTITA_IVA").getValore());
            parameters.put("headerRea", dittaInfoMap.get("REA").getValore());
            parameters.put("headerIndirizzo", dittaInfoMap.get("PDF_INDIRIZZO").getValore());
            parameters.put("headerTelefono", dittaInfoMap.get("TELEFONO").getValore());
            parameters.put("headerWebsite", dittaInfoMap.get("WEBSITE").getValore());
            parameters.put("headerEmail", dittaInfoMap.get("EMAIL").getValore());
            parameters.put("headerPec", dittaInfoMap.get("PEC").getValore());
        }

        return parameters;
    }

}
