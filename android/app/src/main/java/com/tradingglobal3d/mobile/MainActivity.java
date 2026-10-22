package com.tradingglobal3d.mobile;

import android.app.Activity;
import android.graphics.Color;
import android.os.Bundle;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.webkit.WebResourceRequest;
import android.widget.TextView;
import android.widget.Toast;
import com.tradingglobal3d.core.facade.TradingFacade;
import com.tradingglobal3d.core.market.InstrumentCatalog;
import com.tradingglobal3d.core.model.GameMode;
import com.tradingglobal3d.core.storage.SaveManager;

import java.io.InputStream;
import java.io.ByteArrayOutputStream;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;

public class MainActivity extends Activity {
    private WebView webView;
    private TradingFacade facade;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // 1. Inisialisasi TradingFacade dengan penyimpanan internal Android
        SaveManager saveManager = SaveManager.forDirectory(getFilesDir());
        facade = new TradingFacade(saveManager);
        com.tradingglobal3d.core.market.HistoricalDatasetLoader.setAssetSource(path -> getAssets().open(path));

        try {
            // Muat katalog data universe dari aset lokal
            String jsonUniverse;
            try (InputStream is = getAssets().open("data/market_universe.json");
                 ByteArrayOutputStream contents = new ByteArrayOutputStream()) {
                byte[] buffer = new byte[8192];
                int length;
                while ((length = is.read(buffer)) != -1) contents.write(buffer, 0, length);
                jsonUniverse = new String(contents.toByteArray(), StandardCharsets.UTF_8);
            }
            InstrumentCatalog catalog = InstrumentCatalog.fromJsonString(jsonUniverse);
            facade.setCatalog(catalog);
        } catch (Exception e) {
            TextView error = new TextView(this);
            error.setText("Data game gagal dimuat. Tutup lalu buka kembali aplikasi. Progres tersimpan tidak diubah.");
            error.setPadding(32, 48, 32, 32);
            setContentView(error);
            facade = null;
            return;
        }

        // Save yang gagal divalidasi tidak boleh digantikan permainan baru.
        facade.startOrResume();

        // 2. Setup WebView dengan orientasi portrait dan tema navy (#0F1720)
        setupNotificationChannel();
        webView = new WebView(this);
        webView.setBackgroundColor(Color.parseColor("#0F1720"));
        setContentView(webView);
        if (android.os.Build.VERSION.SDK_INT >= 30) {
            webView.setOnApplyWindowInsetsListener((view, insets) -> {
                android.graphics.Insets safe = insets.getInsets(android.view.WindowInsets.Type.systemBars()
                        | android.view.WindowInsets.Type.displayCutout() | android.view.WindowInsets.Type.ime());
                view.setPadding(safe.left, safe.top, safe.right, safe.bottom);
                return insets;
            });
        } else if (android.os.Build.VERSION.SDK_INT >= 28) {
            webView.setOnApplyWindowInsetsListener((view, insets) -> {
                int left = insets.getSystemWindowInsetLeft();
                int top = insets.getSystemWindowInsetTop();
                int right = insets.getSystemWindowInsetRight();
                int bottom = insets.getSystemWindowInsetBottom();
                android.view.DisplayCutout cutout = insets.getDisplayCutout();
                if (cutout != null) {
                    left = Math.max(left, cutout.getSafeInsetLeft());
                    top = Math.max(top, cutout.getSafeInsetTop());
                    right = Math.max(right, cutout.getSafeInsetRight());
                    bottom = Math.max(bottom, cutout.getSafeInsetBottom());
                }
                view.setPadding(left, top, right, bottom);
                return insets;
            });
        } else {
            webView.setOnApplyWindowInsetsListener((view, insets) -> {
                view.setPadding(insets.getSystemWindowInsetLeft(),
                        insets.getSystemWindowInsetTop(),
                        insets.getSystemWindowInsetRight(),
                        insets.getSystemWindowInsetBottom());
                return insets;
            });
        }

        WebSettings settings = webView.getSettings();
        settings.setJavaScriptEnabled(true);
        settings.setDomStorageEnabled(true);
        settings.setAllowFileAccess(true);
        settings.setLoadsImagesAutomatically(true);

        // Pasang Native Bridge ke domain ekonomi
        TradingWebBridge bridge = new TradingWebBridge(facade, this);
        webView.addJavascriptInterface(bridge, "TradingNativeBridge");

        // The Java bridge is available only to packaged application content.
        settings.setAllowContentAccess(false);
        webView.setWebViewClient(new WebViewClient() {
            @Override public boolean shouldOverrideUrlLoading(WebView view, WebResourceRequest request) {
                return !request.getUrl().toString().startsWith("file:///android_asset/www/");
            }
            @Override public void onPageFinished(WebView view, String url) { showSaveFailure(); }
        });
        webView.loadUrl("file:///android_asset/www/index.html");
        if (android.os.Build.VERSION.SDK_INT >= 33) {
            getOnBackInvokedDispatcher().registerOnBackInvokedCallback(
                    android.window.OnBackInvokedDispatcher.PRIORITY_DEFAULT, this::handleBack);
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        // Rekonsiliasi transisi lifecycle ke background pada rasio tidak aktif 24x (1 jam nyata = 1 hari game)
        if (facade != null) {
            facade.onAppLifecycleChanged(false);
            try {
                facade.save();
                saveFailed = false;
            } catch (Exception error) {
                saveFailed = true;
            }
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        // Fail-safe autosave saat aplikasi diminimalkan / diswipe keluar
        if (facade != null) {
            try {
                facade.save();
            } catch (Exception ignored) {}
        }
    }

    private boolean saveFailed;

    @Override protected void onResume() {
        super.onResume();
        if (facade != null) {
            facade.onAppLifecycleChanged(true);
        }
        showSaveFailure();
    }

    private void showSaveFailure() {
        if (saveFailed && webView != null) {
            webView.evaluateJavascript("window.showNativeSaveFailure && window.showNativeSaveFailure()", null);
            Toast.makeText(this, "Progres terakhir belum tersimpan. Coba simpan ulang melalui Pengaturan.", Toast.LENGTH_LONG).show();
        }
    }

    @Override
    @android.annotation.SuppressLint("GestureBackNavigation") // API 26–32 fallback; API 33+ registers native callback above.
    public void onBackPressed() {
        handleBack();
    }

    private void setupNotificationChannel() {
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            android.app.NotificationManager nm = getSystemService(android.app.NotificationManager.class);
            if (nm != null) {
                android.app.NotificationChannel channel = new android.app.NotificationChannel(
                        "channel_ipo_alerts",
                        "Pemberitahuan IPO & Pasar",
                        android.app.NotificationManager.IMPORTANCE_DEFAULT
                );
                channel.setDescription("Notifikasi penawaran saham perdana (IPO) dan aksi korporasi");
                nm.createNotificationChannel(channel);
            }
        }
    }

    public void triggerIpoNotification(String title, String message) {
        try {
            android.app.NotificationManager nm = (android.app.NotificationManager) getSystemService(NOTIFICATION_SERVICE);
            if (nm != null) {
                android.app.Notification.Builder builder;
                if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
                    builder = new android.app.Notification.Builder(this, "channel_ipo_alerts");
                } else {
                    builder = new android.app.Notification.Builder(this);
                }
                builder.setContentTitle(title)
                        .setContentText(message)
                        .setSmallIcon(android.R.drawable.ic_dialog_info)
                        .setAutoCancel(true);
                nm.notify((int) System.currentTimeMillis(), builder.build());
            }
        } catch (Exception ignored) {}
    }

    private void handleBack() {
        if (webView == null) { finish(); return; }
        webView.evaluateJavascript("Boolean(window.closeActiveLayer && window.closeActiveLayer())", handled -> {
            if ("true".equals(handled)) return;
            if (webView.canGoBack()) webView.goBack();
            else finish();
        });
    }
}
