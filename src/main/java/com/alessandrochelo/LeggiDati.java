package com.alessandrochelo;

import com.fazecast.jSerialComm.SerialPort;
import java.io.BufferedReader;
import java.io.InputStreamReader;

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
        //byte[] readBuffer = new byte[1024];
        //int numRead = comPort.readBytes(readBuffer, readBuffer.length);
        //System.out.println("Letti " + numRead + " bytes.");

        // 6. Converte in testo usando la codifica standard UTF-8
        //String testo = new String(readBuffer, 0, numRead, java.nio.charset.StandardCharsets.UTF_8);
        //System.out.println("Testo ricevuto: " + testo);

        // 5. Lettura dei dati riga per riga
        if (comPort.openPort()) {
            System.out.println("Porta aperta. Ricezione dati GPS...");
        } else {
            return;
        }
        
        // Usiamo un BufferedReader per leggere comodamente una riga alla volta (\n)
        BufferedReader reader = new BufferedReader(new InputStreamReader(comPort.getInputStream()));

        try{
            String linea;
            while ((linea = reader.readLine()) != null) {
                // Controlla se la riga contiene i dati di posizionamento RMC
                if (linea.startsWith("$GPRMC") || linea.startsWith("$GNRMC")) {
                    String[] token = linea.split(",");

                    // Verifica che la stringa sia valida (il campo 2 indica lo stato: 'A' = Valid, 'V' = Invalid)
                    if (token.length > 6 && token[2].equals("A")) {
                        
                        String latRaw = token[3];  // Es. 4807.038
                        String latDir = token[4];  // N o S
                        String lonRaw = token[5];  // Es. 01131.000
                        String lonDir = token[6];  // E o W
                        
                        double latitudine = convertiInGradiDecimali(latRaw, latDir);
                        double longitudine = convertiInGradiDecimali(lonRaw, lonDir);
                        
                        System.out.printf("GPS -> Latitudine: %.6f | Longitudine: %.6f%n", latitudine, longitudine);
                    } else {
                        System.out.println("Segnale GPS debole o non valido...");
                    }
            }
        }}catch(Exception e){
            System.err.println("Errore di lettura: " + e.getMessage());
        }

        // 7. Chiudi la porta
        comPort.closePort();
        System.out.println("Porta chiusa."); 
    }

    /**
     * Converte le coordinate NMEA (DDMM.MMMM) in Gradi Decimali (DD.DDDDDD)
     */
    private double convertiInGradiDecimali(String rawValue, String direzione) {
        if (rawValue == null || rawValue.isEmpty()) return 0.0;
        
        // Trova il punto decimale per separare i gradi dai minuti
        double raw = Double.parseDouble(rawValue);
        
        // La latitudine ha 2 cifre per i gradi (DDMM.MMMM), la longitudine ne ha 3 (DDDMM.MMMM)
        // Dividendo per 100 e prendendo l'intero otteniamo sempre i gradi corretti
        int gradi = (int) (raw / 100);
        double minuti = raw - (gradi * 100);
        
        // Formula: Gradi Decimali = Gradi + (Minuti / 60)
        double gradiDecimali = gradi + (minuti / 60.0);
        
        // Se la direzione è Sud (S) o Ovest (W), il valore deve essere negativo
        if (direzione.equals("S") || direzione.equals("W")) {
            gradiDecimali = -gradiDecimali;
        }
        
        return gradiDecimali;
    }
}
