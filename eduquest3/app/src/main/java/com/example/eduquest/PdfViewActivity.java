package com.example.eduquest;

import android.Manifest;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.print.PrintAttributes;
import android.print.PrintDocumentAdapter;
import android.print.PrintManager;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

public class PdfViewActivity extends AppCompatActivity {
    private static final int REQUEST_WRITE_STORAGE = 112;

    private WebView webView;
    private String url;
    private String fileName;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        webView = new WebView(this);
        setContentView(webView);

        url = getIntent().getStringExtra("url");
        fileName = getIntent().getStringExtra("title");
        if (fileName == null) fileName = "article";
        fileName = fileName.replaceAll("[^a-zA-Z0-9]", "_") + ".pdf";

        webView.getSettings().setJavaScriptEnabled(true);
        webView.setWebViewClient(new WebViewClient() {
            @Override
            public void onPageFinished(WebView view, String url) {
                checkPermissionAndCreatePdf();
            }
        });

        webView.loadUrl(url);
    }

    private void checkPermissionAndCreatePdf() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            createPdf();
        } else {
            boolean hasPermission = ContextCompat.checkSelfPermission(this,
                    Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED;

            if (!hasPermission) {
                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, REQUEST_WRITE_STORAGE);
            } else {
                createPdf();
            }
        }
    }

    private void createPdf() {
        Toast.makeText(this, "Generating PDF, please wait...", Toast.LENGTH_SHORT).show();

        PrintManager printManager = (PrintManager) getSystemService(PRINT_SERVICE);

        if (printManager == null) {
            Toast.makeText(this, "Print service unavailable.", Toast.LENGTH_LONG).show();
            finish();
            return;
        }

        PrintDocumentAdapter printAdapter = webView.createPrintDocumentAdapter(fileName);

        PrintAttributes.Builder builder = new PrintAttributes.Builder();
        builder.setMediaSize(PrintAttributes.MediaSize.NA_LETTER);
        builder.setResolution(new PrintAttributes.Resolution("pdf", "pdf", 300, 300));
        builder.setMinMargins(PrintAttributes.Margins.NO_MARGINS);

        printManager.print(fileName, printAdapter, builder.build());

        Toast.makeText(this, "PDF generation started. Check your Downloads folder.", Toast.LENGTH_LONG).show();

        finish();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        if (requestCode == REQUEST_WRITE_STORAGE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                createPdf();
            } else {
                Toast.makeText(this, "Permission denied. Cannot save PDF.", Toast.LENGTH_LONG).show();
                finish();
            }
        } else {
            super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        }
    }
}
