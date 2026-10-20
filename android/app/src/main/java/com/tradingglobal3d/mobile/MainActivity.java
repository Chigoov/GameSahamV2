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
        }

        WebSettings settings = webView.getSettings();
        settings.setJavaScriptEnabled(true);
        settings.setDomStorageEnabled(true);
        settings.setAllowFileAccess(true);
        settings.setLoadsImagesAutomatically(true);

        // Pasang Native Bridge ke domain ekonomi
        TradingWebBridge bridge = new TradingWebBridge(facade);
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

    private void handleBack() {
        if (webView == null) { finish(); return; }
        webView.evaluateJavascript("Boolean(window.closeActiveLayer && window.closeActiveLayer())", handled -> {
            if ("true".equals(handled)) return;
            if (webView.canGoBack()) webView.goBack();
            else finish();
        });
    }
}
