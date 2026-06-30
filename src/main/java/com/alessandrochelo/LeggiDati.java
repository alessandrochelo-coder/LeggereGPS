package com.alessandrochelo;

import com.fazecast.jSerialComm.SerialPort;


public class LeggiDati 
{
    public LeggiDati() 
    {

    }

    public void leggereDati() 
    {
         // 1. Elenca tutte le porte disponibili
        SerialPort[] ports = SerialPort.getCommPorts();
        
        if (ports.length == 0) {
            System.out.println("Nessuna porta seriale trovata.");
            return;
        }

        // 2. Seleziona la prima porta (es. COM3 o /dev/ttyUSB0)
        SerialPort comPort = ports[0];
        System.out.println("Porta selezionata: " + comPort.getSystemPortName());

        // 3. Apri la porta
        if (comPort.openPort()) {
            System.out.println("Porta aperta con successo.");
        } else {
            System.out.println("Impossibile aprire la porta.");
            return;
        }

        // 4. Configura i parametri (BaudRate, DataBits, StopBits, Parity)
        comPort.setComPortParameters(460800, 8, SerialPort.ONE_STOP_BIT, SerialPort.NO_PARITY);
        comPort.setComPortTimeouts(SerialPort.TIMEOUT_READ_BLOCKING, 1000, 0);

        // 5. Leggi i dati dalla porta
        byte[] readBuffer = new byte[1024];
        int numRead = comPort.readBytes(readBuffer, readBuffer.length);
        System.out.println("Letti " + numRead + " bytes.");

        // Converte in testo usando la codifica standard UTF-8
        String testo = new String(readBuffer, 0, numRead, java.nio.charset.StandardCharsets.UTF_8);
        System.out.println("Testo ricevuto: " + testo);

        // 6. Chiudi la porta
        comPort.closePort();
        System.out.println("Porta chiusa.");
    }
}
