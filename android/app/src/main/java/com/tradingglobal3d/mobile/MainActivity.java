package com.tradingglobal3d.mobile;

import android.app.Activity;
import android.app.AlertDialog;
import android.graphics.Color;
import android.os.Bundle;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.webkit.WebChromeClient;
import android.webkit.ConsoleMessage;
import android.webkit.JsResult;
import android.webkit.JsPromptResult;
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
    private TradingWebBridge bridge;

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
            facade.setDynamicDailyNewsEnabled(true);
        } catch (Exception e) {
            TextView error = new TextView(this);
            error.setText("Data game gagal dimuat. Tutup lalu buka kembali aplikasi. Progres tersimpan tidak diubah.");
            error.setPadding(32, 48, 32, 32);
            setContentView(error);
            facade = null;
            return;
        }

        // Startup/load runs on the same clock worker as resume, never on the UI thread.
        bridge = new TradingWebBridge(facade, this);
        bridge.beginClockSync(true);
        if ((getApplicationInfo().flags & android.content.pm.ApplicationInfo.FLAG_DEBUGGABLE) != 0) {
            Thread diagnostic = new Thread(() -> {
                try { Thread.sleep(10000); } catch (InterruptedException e) { Thread.currentThread().interrupt(); return; }
                if (bridge.isClockSyncPending()) {
                    for (java.util.Map.Entry<Thread, StackTraceElement[]> entry : Thread.getAllStackTraces().entrySet()) {
                        String name = entry.getKey().getName();
                        if (name.equals("world-clock-reconciliation") || name.contains("JavaBridge") || name.equals("main")) {
                            android.util.Log.d("GameClockDiag", name + " " + entry.getKey().getState() + " " + java.util.Arrays.toString(entry.getValue()));
                        }
                    }
                }
            }, "clock-diagnostic");
            diagnostic.setDaemon(true);
            diagnostic.start();
        }

        // 2. Setup WebView dengan orientasi portrait dan akselerasi hardware
        setupNotificationChannel();
        getWindow().setFlags(
                android.view.WindowManager.LayoutParams.FLAG_HARDWARE_ACCELERATED,
                android.view.WindowManager.LayoutParams.FLAG_HARDWARE_ACCELERATED);

        webView = new WebView(this);
        webView.setBackgroundColor(Color.parseColor("#0F1720"));
        webView.setLayerType(android.view.View.LAYER_TYPE_NONE, null); // Mencegah GPU RenderThread crash pada Android WebView (Bug #31)
        webView.setVerticalScrollBarEnabled(false);
        webView.setHorizontalScrollBarEnabled(false);
        webView.setOverScrollMode(android.view.View.OVER_SCROLL_NEVER);
        android.widget.FrameLayout content = new android.widget.FrameLayout(this);
        content.setBackgroundColor(Color.parseColor("#070b14"));
        content.addView(webView, new android.widget.FrameLayout.LayoutParams(
                android.view.ViewGroup.LayoutParams.MATCH_PARENT, android.view.ViewGroup.LayoutParams.MATCH_PARENT));
        setContentView(content);
        if (android.os.Build.VERSION.SDK_INT >= 30) {
            content.setOnApplyWindowInsetsListener((view, insets) -> {
                int types = android.view.WindowInsets.Type.systemBars()
                        | android.view.WindowInsets.Type.displayCutout() | android.view.WindowInsets.Type.ime();
                android.graphics.Insets safe = insets.getInsets(types);
                view.setPadding(safe.left, safe.top, safe.right, safe.bottom);
                return new android.view.WindowInsets.Builder(insets)
                        .setInsets(types, android.graphics.Insets.NONE).setDisplayCutout(null).build();
            });
        } else if (android.os.Build.VERSION.SDK_INT >= 28) {
            content.setOnApplyWindowInsetsListener((view, insets) -> {
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
                return insets.consumeDisplayCutout().replaceSystemWindowInsets(0, 0, 0, 0);
            });
        } else {
            content.setOnApplyWindowInsetsListener((view, insets) -> {
                view.setPadding(insets.getSystemWindowInsetLeft(),
                        insets.getSystemWindowInsetTop(),
                        insets.getSystemWindowInsetRight(),
                        insets.getSystemWindowInsetBottom());
                return insets.replaceSystemWindowInsets(0, 0, 0, 0);
            });
        }
        content.requestApplyInsets();

        WebSettings settings = webView.getSettings();
        settings.setJavaScriptEnabled(true);
        settings.setDomStorageEnabled(true);
        settings.setAllowFileAccess(true);
        settings.setLoadsImagesAutomatically(true);

        // Pasang Native Bridge ke domain ekonomi
        webView.addJavascriptInterface(bridge, "TradingNativeBridge");

        webView.setWebChromeClient(new WebChromeClient() {
            @Override
            public boolean onConsoleMessage(ConsoleMessage message) {
                if (message.messageLevel() == ConsoleMessage.MessageLevel.ERROR) {
                    android.util.Log.e("GameWebView", message.sourceId() + ":" + message.lineNumber() + " " + message.message());
                }
                return true;
            }

            @Override
            public boolean onJsAlert(WebView view, String url, String message, JsResult result) {
                new AlertDialog.Builder(MainActivity.this)
                        .setTitle("Game Saham")
                        .setMessage(message)
                        .setPositiveButton(android.R.string.ok, (dialog, which) -> result.confirm())
                        .setCancelable(false)
                        .show();
                return true;
            }

            @Override
            public boolean onJsConfirm(WebView view, String url, String message, JsResult result) {
                new AlertDialog.Builder(MainActivity.this)
                        .setTitle("Konfirmasi")
                        .setMessage(message)
                        .setPositiveButton("Lanjutkan", (dialog, which) -> result.confirm())
                        .setNegativeButton("Batal", (dialog, which) -> result.cancel())
                        .setCancelable(false)
                        .show();
                return true;
            }

            @Override
            public boolean onJsPrompt(WebView view, String url, String message, String defaultValue, JsPromptResult result) {
                final android.widget.EditText input = new android.widget.EditText(MainActivity.this);
                input.setText(defaultValue);
                input.setInputType(android.text.InputType.TYPE_CLASS_NUMBER);
                new AlertDialog.Builder(MainActivity.this)
                        .setTitle("Input Angka")
                        .setMessage(message)
                        .setView(input)
                        .setPositiveButton("Lanjut", (dialog, which) -> result.confirm(input.getText().toString()))
                        .setNegativeButton("Batal", (dialog, which) -> result.cancel())
                        .setCancelable(false)
                        .show();
                return true;
            }
        });

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
        if (bridge != null) bridge.beginClockSync(false);
    }

    @Override
    protected void onStop() {
        super.onStop();
        // Fail-safe autosave saat aplikasi diminimalkan / diswipe keluar
        if (bridge != null) bridge.beginClockSync(false);
    }

    @Override
    protected void onDestroy() {
        if (bridge != null) {
            bridge.destroy();
        }
        if (webView != null) {
            webView.stopLoading();
            webView.clearHistory();
            webView.removeAllViews();
            webView.destroy();
            webView = null;
        }
        super.onDestroy();
    }

    private boolean saveFailed;

    public void notifySaveFailed(boolean failed) {
        this.saveFailed = failed;
        if (failed) {
            runOnUiThread(this::showSaveFailure);
        } else if (webView != null) {
            runOnUiThread(() -> webView.evaluateJavascript("document.getElementById('native-save-warning')?.remove();", null));
        }
    }

    @Override protected void onResume() {
        super.onResume();
        if (bridge != null) bridge.beginClockSync(true);
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
