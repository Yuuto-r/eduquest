package com.example.eduquest;

import android.app.DownloadManager;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.eduquest.database.Article;
import com.example.eduquest.database.AppDatabase;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import java.io.File;
import java.util.List;

public class BookmarkAdapter extends RecyclerView.Adapter<BookmarkAdapter.BookmarkViewHolder> {

    private List<Article> bookmarks;
    private Context context;
    private AppDatabase db;

    public BookmarkAdapter(Context context, List<Article> bookmarks) {
        this.context = context;
        this.bookmarks = bookmarks;
        db = AppDatabase.getInstance(context);
    }

    @NonNull
    @Override
    public BookmarkViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_bookmark, parent, false);
        return new BookmarkViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull BookmarkViewHolder holder, int position) {
        Article article = bookmarks.get(position);
        holder.title.setText(article.getTitle());
        holder.url.setText(article.getUrl());

        holder.itemView.setOnClickListener(v -> {
            Intent intent = new Intent(context, WebViewActivity.class);
            intent.putExtra("url", article.getUrl());
            context.startActivity(intent);
        });

        holder.deleteBtn.setOnClickListener(v -> {
            new Thread(() -> {
                db.articleDao().delete(article);
                ((BookmarkActivity) context).runOnUiThread(() -> {
                    bookmarks.remove(position);
                    notifyItemRemoved(position);
                    notifyItemRangeChanged(position, bookmarks.size());
                });
            }).start();
        });

        holder.downloadBtn.setOnClickListener(v -> {
            String pdfUrl = article.getUrl();
            String safeTitle = article.getTitle().replaceAll("[^a-zA-Z0-9]", "_");

            try {
                if (pdfUrl != null && pdfUrl.toLowerCase().endsWith(".pdf")) {
                    Uri uri = Uri.parse(pdfUrl);
                    DownloadManager.Request request = new DownloadManager.Request(uri);
                    request.setTitle(article.getTitle());
                    request.setDescription("Downloading PDF...");
                    request.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED);

                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                        request.setDestinationInExternalPublicDir(Environment.DIRECTORY_DOWNLOADS, safeTitle + ".pdf");
                    } else {
                        File directory = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS);
                        if (!directory.exists()) directory.mkdirs();
                        request.setDestinationUri(Uri.fromFile(new File(directory, safeTitle + ".pdf")));
                    }

                    DownloadManager dm = (DownloadManager) context.getSystemService(Context.DOWNLOAD_SERVICE);
                    dm.enqueue(request);
                    Toast.makeText(context, "Download started", Toast.LENGTH_SHORT).show();
                } else {
                    throw new Exception("URL is not a PDF.");
                }
            } catch (Exception e) {
                String content = "Title: " + article.getTitle() + "\nURL: " + article.getUrl() + "\n\n(Note: Original PDF unavailable. This is a generated fallback.)";
                PdfDocumentHelper.createPdfFromText(context, safeTitle, content);
                Toast.makeText(context, "PDF not available. Fallback generated.", Toast.LENGTH_LONG).show();
            }
        });
    }

    @Override
    public int getItemCount() {
        return bookmarks.size();
    }

    static class BookmarkViewHolder extends RecyclerView.ViewHolder {
        TextView title, url;
        ImageView deleteBtn, downloadBtn;

        public BookmarkViewHolder(@NonNull View itemView) {
            super(itemView);
            title = itemView.findViewById(R.id.textViewTitle);
            url = itemView.findViewById(R.id.textViewUrl);
            deleteBtn = itemView.findViewById(R.id.imageDelete);
            downloadBtn = itemView.findViewById(R.id.downloadPdfButton);
        }
    }
}
