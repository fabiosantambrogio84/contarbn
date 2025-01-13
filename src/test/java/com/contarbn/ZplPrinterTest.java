package com.contarbn;

import lombok.extern.slf4j.Slf4j;
import org.junit.Before;
import org.junit.Test;

import java.io.OutputStream;
import java.net.Socket;

@Slf4j
public class ZplPrinterTest {

    //private ZplPrinterService zplPrinterService;

    @Before
    public void init() {
        //zplPrinterService = new ZplPrinterService();
    }

    @Test
    public void printTemplateTest() throws Exception {

        String printerIp = "192.168.254.11";
        int printerPort = 9100;

        try (Socket socket = new Socket(printerIp, printerPort)) {
            System.out.println("Connessione alla stampante riuscita!");

            OutputStream outputStream = socket.getOutputStream();
            String zplCommand = "^XA^FO50,50^ADN,36,20^FDHello Zebra!^FS^XZ";
            outputStream.write(zplCommand.getBytes());
            outputStream.flush();

            System.out.println("Comando ZPL inviato con successo!");
        } catch (Exception e) {
            System.err.println("Impossibile connettersi alla stampante: " + e.getMessage());
        }

    }

}