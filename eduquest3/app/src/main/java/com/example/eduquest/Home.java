package com.example.eduquest;

import android.Manifest;
import android.app.AlertDialog;
import android.app.DownloadManager;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.net.ConnectivityManager;
import android.net.NetworkCapabilities;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.Settings;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.webkit.DownloadListener;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.eduquest.database.AppDatabase;
import com.example.eduquest.database.Article;

public class Home extends AppCompatActivity {

    private static final int STORAGE_PERMISSION_REQUEST_CODE = 1;

    EditText url, overlaySearch;
    Button btnSearch, btnBookmark;
    ProgressBar loading;
    WebView webView;
    ProgressDialog progressDialog;

    ImageButton usersettings, socializebtn;

    AppDatabase db;

    boolean fromBookmark = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_home);

        url = findViewById(R.id.Url);
        overlaySearch = findViewById(R.id.overlaySearch);
        btnSearch = findViewById(R.id.btnSearch);
        btnBookmark = findViewById(R.id.btnBookmark);
        loading = findViewById(R.id.loading);
        webView = findViewById(R.id.webView);
        usersettings = findViewById(R.id.usersettings);
        socializebtn = findViewById(R.id.socialization);

        ImageButton btnViewBookmarks = findViewById(R.id.btnViewBookmarks);
        btnViewBookmarks.setOnClickListener(v -> {
            Intent intent = new Intent(Home.this, BookmarkActivity.class);
            startActivity(intent);
        });

        requestStoragePermissionIfNeeded();


        socializebtn.setOnClickListener(view -> startActivity(new Intent(Home.this, Socialize.class)));
        usersettings.setOnClickListener(view -> startActivity(new Intent(Home.this, Profile.class)));

        progressDialog = new ProgressDialog(this);
        progressDialog.setMessage("Loading...");
        progressDialog.setCancelable(false);

        db = AppDatabase.getInstance(getApplicationContext());

        setupWebView();

        if (!checkConnection()) {
            showDialog();
        }

        btnSearch.setOnClickListener(v -> performSearch(url.getText().toString().trim()));

        overlaySearch.setOnEditorActionListener((v, actionId, event) -> {
            if (event != null && event.getAction() == android.view.KeyEvent.ACTION_DOWN) {
                performSearch(overlaySearch.getText().toString().trim());
                return true;
            }
            return false;
        });

        ImageView searchIcon = findViewById(R.id.search);
        searchIcon.setOnClickListener(v -> performSearch(overlaySearch.getText().toString().trim()));

        btnBookmark.setOnClickListener(v -> {
            String pageTitle = webView.getTitle();
            String pageUrl = webView.getUrl();

            if (pageUrl == null || pageUrl.isEmpty()) {
                Toast.makeText(Home.this, "No page loaded to bookmark", Toast.LENGTH_SHORT).show();
                return;
            }

            if (pageTitle == null) {
                pageTitle = "No Title";
            }

            if (pageUrl.endsWith(".pdf")) {
                downloadPdf(pageUrl, pageTitle);
            } else {
                Article article = new Article(pageTitle, pageUrl, null);
                try {
                    db.articleDao().insert(article);
                    Toast.makeText(Home.this, "Bookmark saved!", Toast.LENGTH_SHORT).show();
                } catch (Exception e) {
                    Toast.makeText(Home.this, "Error saving bookmark: " + e.getMessage(), Toast.LENGTH_LONG).show();
                    e.printStackTrace();
                }
            }
        });

        String incomingUrl = getIntent().getStringExtra("urlToLoad");
        fromBookmark = getIntent().getBooleanExtra("fromBookmark", false);

        if (fromBookmark) {
            url.setVisibility(View.GONE);
            overlaySearch.setVisibility(View.GONE);
            btnSearch.setVisibility(View.GONE);
            btnBookmark.setVisibility(View.GONE);
            usersettings.setVisibility(View.GONE);
            searchIcon.setVisibility(View.GONE);
            btnViewBookmarks.setVisibility(View.GONE);

            if (getSupportActionBar() != null) {
                getSupportActionBar().setDisplayHomeAsUpEnabled(true);
                getSupportActionBar().setTitle("Bookmark View");
            }

            if (incomingUrl != null && !incomingUrl.isEmpty()) {
                webView.loadUrl(incomingUrl);
            }
        }

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
    }

    private void requestStoragePermissionIfNeeded() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.Q) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE)
                    != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},
                        STORAGE_PERMISSION_REQUEST_CODE);
            }
        }
    }

    private void setupWebView() {
        WebSettings webSettings = webView.getSettings();
        webSettings.setBuiltInZoomControls(true);
        webSettings.setUserAgentString(WebSettings.getDefaultUserAgent(this));
        webSettings.setJavaScriptEnabled(true);
        webSettings.setDomStorageEnabled(true);
        webSettings.setCacheMode(WebSettings.LOAD_CACHE_ELSE_NETWORK);
        webSettings.setSupportZoom(true);

        webView.setWebViewClient(new MyWebViewClient());

        webView.setWebChromeClient(new WebChromeClient() {
            private boolean isLoading = false;

            @Override
            public void onProgressChanged(WebView view, int newProgress) {
                if (!isLoading && newProgress < 100) {
                    progressDialog.show();
                    isLoading = true;
                }
                if (newProgress == 100) {
                    if (progressDialog.isShowing()) progressDialog.dismiss();
                    isLoading = false;
                    setTitle(view.getTitle());
                }
                super.onProgressChanged(view, newProgress);
            }
        });

        webView.setDownloadListener(new MyDownloadListener());
    }

    private void downloadPdf(String url, String title) {
        DownloadManager.Request request = new DownloadManager.Request(Uri.parse(url));
        request.setTitle(title);
        request.setDescription("Downloading PDF...");
        request.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED);
        request.setDestinationInExternalPublicDir(Environment.DIRECTORY_DOWNLOADS, title + ".pdf");

        DownloadManager manager = (DownloadManager) getSystemService(Context.DOWNLOAD_SERVICE);
        if (manager != null) {
            manager.enqueue(request);
            Toast.makeText(this, "Downloading PDF...", Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(this, "Download manager not available", Toast.LENGTH_SHORT).show();
        }
    }

    private void performSearch(String searchTerm) {
        if (!searchTerm.isEmpty()) {
            String searchUrl = "https://scholar.google.com/scholar?q=" + Uri.encode(searchTerm);
            webView.loadUrl(searchUrl);
        }
    }

    @Override
    public void onBackPressed() {
        if (webView.canGoBack()) {
            webView.goBack();
        } else {
            if (fromBookmark) {
                finish();
            } else {
                new AlertDialog.Builder(this)
                        .setMessage("Are you sure you want to Exit?")
                        .setCancelable(false)
                        .setPositiveButton("Yes", (dialog, id) -> super.onBackPressed())
                        .setNegativeButton("No", (dialog, id) -> dialog.dismiss())
                        .show();
            }
        }
    }

    @Override
    public boolean onSupportNavigateUp() {
        finish();
        return true;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        return false;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        return super.onOptionsItemSelected(item);
    }

    private boolean checkConnection() {
        ConnectivityManager connectivityManager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        if (connectivityManager != null) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                NetworkCapabilities capabilities = connectivityManager.getNetworkCapabilities(connectivityManager.getActiveNetwork());
                if (capabilities != null) {
                    return capabilities.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) ||
                            capabilities.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) ||
                            capabilities.hasTransport(NetworkCapabilities.TRANSPORT_ETHERNET);
                }
            } else {
                NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
                return activeNetworkInfo != null && activeNetworkInfo.isConnected();
            }
        }
        return false;
    }

    private void showDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage("Connect to the internet to proceed further")
                .setCancelable(false)
                .setPositiveButton("OK", (dialog, which) -> startActivity(new Intent(Settings.ACTION_WIFI_SETTINGS)))
                .setNegativeButton("Exit", (dialog, which) -> dialog.dismiss());
        AlertDialog alertDialog = builder.create();
        alertDialog.show();
    }

    private class MyWebViewClient extends WebViewClient {
        @Override
        public boolean shouldOverrideUrlLoading(WebView view, String url) {
            view.loadUrl(url);
            return true;
        }

        @Override
        public void onPageStarted(WebView view, String url, Bitmap favicon) {
            super.onPageStarted(view, url, favicon);
            loading.setVisibility(View.VISIBLE);
            webView.setVisibility(View.GONE);
        }

        @Override
        public void onPageFinished(WebView view, String url) {
            super.onPageFinished(view, url);
            loading.setVisibility(View.GONE);
            webView.setVisibility(View.VISIBLE);
        }
    }

    private class MyDownloadListener implements DownloadListener {
        @Override
        public void onDownloadStart(String url, String userAgent, String contentDisposition, String mimetype, long contentLength) {
            if (url != null) {
                Intent intent = new Intent(Intent.ACTION_VIEW);
                intent.setData(Uri.parse(url));
                startActivity(intent);
            }
        }
    }
}
