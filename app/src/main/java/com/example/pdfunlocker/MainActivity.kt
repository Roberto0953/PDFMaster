package com.example.pdfunlocker

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.ProgressBar
import android.widget.TextView
import androidx.activity.ComponentActivity
import androidx.activity.result.contract.ActivityResultContracts
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.InputStream
import java.io.OutputStream

class MainActivity : ComponentActivity() {

    private var inputUri: Uri? = null

    private lateinit var pickBtn: Button
    private lateinit var unlockBtn: Button
    private lateinit var passwordEt: EditText
    private lateinit var statusTv: TextView
    private lateinit var progress: ProgressBar

    private val pickPdf = registerForActivityResult(
        ActivityResultContracts.OpenDocument()
    ) { uri ->
        if (uri != null) {
            try {
                contentResolver.takePersistableUriPermission(
                    uri, Intent.FLAG_GRANT_READ_URI_PERMISSION
                )
            } catch (_: Exception) {}
            inputUri = uri
            statusTv.text = "Selezionato: ${uri.lastPathSegment}"
        }
    }

    private val createOut = registerForActivityResult(
        ActivityResultContracts.CreateDocument("application/pdf")
    ) { uri ->
        if (uri != null) unlockToUri(uri)
        else statusTv.text = "Operazione annullata."
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        pickBtn = findViewById(R.id.btnPick)
        unlockBtn = findViewById(R.id.btnUnlock)
        passwordEt = findViewById(R.id.etPassword)
        statusTv = findViewById(R.id.tvStatus)
        progress = findViewById(R.id.progress)

        pickBtn.setOnClickListener {
            pickPdf.launch(arrayOf("application/pdf"))
        }

        unlockBtn.setOnClickListener {
            val src = inputUri
            if (src == null) {
                statusTv.text = "Seleziona prima un PDF."
                return@setOnClickListener
            }
            val suggested = "document_unlocked.pdf"
            createOut.launch(suggested)
        }
    }

    private fun unlockToUri(outUri: Uri) {
        val src = inputUri ?: return
        val pwd = passwordEt.text?.toString()?.trim().orEmpty()

        progress.visibility = android.view.View.VISIBLE
        statusTv.text = "Sblocco in corso…"

        lifecycleScope.launchWhenStarted {
            val result = withContext(Dispatchers.IO) {
                try {
                    contentResolver.openInputStream(src).use { inStream: InputStream? ->
                        contentResolver.openOutputStream(outUri, "w").use { outStream: OutputStream? ->
                            if (inStream == null || outStream == null) {
                                return@withContext "Errore: stream null."
                            }
                            PdfUnlocker.unlockIfPossible(inStream, outStream, pwd)
                        }
                    }
                    null // ok
                } catch (e: com.tom_roush.pdfbox.pdmodel.encryption.InvalidPasswordException) {
                    e.message ?: "Password richiesta/non corretta."
                } catch (e: Exception) {
                    "Errore: ${e.message}"
                }
            }

            progress.visibility = android.view.View.GONE
            statusTv.text = result ?: "PDF sbloccato con successo."
        }
    }
}
