package com.example.pdfunlocker;

import com.tom_roush.pdfbox.pdmodel.PDDocument;
import com.tom_roush.pdfbox.pdmodel.encryption.InvalidPasswordException;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

public class PdfUnlocker {
    /**
     * Sblocca un PDF se possibile:
     * - se password è fornita la usa; altrimenti tenta senza
     * - rimuove restrizioni (printing/copy, ecc.)
     * - salva su outputStream
     * Se il file richiede user-password e non è fornita/corretta -> InvalidPasswordException.
     */
    public static void unlockIfPossible(InputStream in, OutputStream out, String passwordIfAny)
            throws IOException {
        PDDocument doc = null;
        try {
            if (passwordIfAny != null && !passwordIfAny.isEmpty()) {
                doc = PDDocument.load(in, passwordIfAny);
            } else {
                doc = PDDocument.load(in);
            }
            doc.setAllSecurityToBeRemoved(true);
            doc.save(out);
        } catch (InvalidPasswordException e) {
            throw new InvalidPasswordException("Il PDF richiede una password per l'apertura. Fornisci la password corretta.");
        } finally {
            if (doc != null) try { doc.close(); } catch (IOException ignored) {}
            try { in.close(); } catch (IOException ignored) {}
            try { out.close(); } catch (IOException ignored) {}
        }
    }
}
