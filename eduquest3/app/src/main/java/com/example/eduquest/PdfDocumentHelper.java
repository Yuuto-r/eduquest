package com.example.eduquest;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.pdf.PdfDocument;
import android.os.Environment;
import android.widget.Toast;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

public class PdfDocumentHelper {

    public static void createPdfFromText(Context context, String fileName, String textContent) {
        PdfDocument pdfDocument = new PdfDocument();

        PdfDocument.PageInfo pageInfo = new PdfDocument.PageInfo.Builder(595, 842, 1).create(); // A4 size in points
        PdfDocument.Page page = pdfDocument.startPage(pageInfo);

        Canvas canvas = page.getCanvas();
        Paint paint = new Paint();
        paint.setTextSize(12);

        int x = 10, y = 25;

        for (String line : textContent.split("\n")) {
            canvas.drawText(line, x, y, paint);
            y += paint.descent() - paint.ascent() + 5;
        }

        pdfDocument.finishPage(page);

        File downloadsFolder = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS);
        if (!downloadsFolder.exists()) downloadsFolder.mkdirs();

        File file = new File(downloadsFolder, fileName + ".pdf");

        try (FileOutputStream fos = new FileOutputStream(file)) {
            pdfDocument.writeTo(fos);
            Toast.makeText(context, "PDF saved to Downloads/" + file.getName(), Toast.LENGTH_LONG).show();
        } catch (IOException e) {
            e.printStackTrace();
            Toast.makeText(context, "Error saving PDF: " + e.getMessage(), Toast.LENGTH_LONG).show();
        } finally {
            pdfDocument.close();
        }
    }
}
