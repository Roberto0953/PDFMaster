# PDFMaster (Android)

Semplice app Android per sbloccare PDF (rimuovere restrizioni) usando `pdfbox-android`.
UI minimale: seleziona PDF, inserisci opzionale password, salva copia sbloccata.

## Build su Codemagic
Questo repo **non** contiene il Gradle Wrapper; `codemagic.yaml` lo genera automaticamente
(`gradle wrapper --gradle-version 8.7`). Gli artifact APK sono in `app/build/outputs/**`.

## Librerie
- com.tom-roush:pdfbox-android:2.0.27.0
