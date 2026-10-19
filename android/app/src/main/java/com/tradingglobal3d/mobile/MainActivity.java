package com.tradingglobal3d.mobile;

import android.app.Activity;
import android.graphics.Color;
import android.os.Bundle;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import com.tradingglobal3d.core.facade.TradingFacade;
import com.tradingglobal3d.core.market.InstrumentCatalog;
import com.tradingglobal3d.core.model.GameMode;
import com.tradingglobal3d.core.storage.SaveManager;

import java.io.InputStream;
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
            InputStream is = getAssets().open("data/market_universe.json");
            byte[] buffer = new byte[is.available()];
            is.read(buffer);
            is.close();
            String jsonUniverse = new String(buffer, StandardCharsets.UTF_8);
            InstrumentCatalog catalog = InstrumentCatalog.fromJsonString(jsonUniverse);
            facade.setCatalog(catalog);
        } catch (Exception e) {
            e.printStackTrace();
        }

        // Save yang gagal divalidasi tidak boleh digantikan permainan baru.
        facade.startOrResume();

        // 2. Setup WebView dengan orientasi portrait dan tema navy (#0F1720)
        webView = new WebView(this);
        webView.setBackgroundColor(Color.parseColor("#0F1720"));
        setContentView(webView);

        WebSettings settings = webView.getSettings();
        settings.setJavaScriptEnabled(true);
        settings.setDomStorageEnabled(true);
        settings.setAllowFileAccess(true);
        settings.setLoadsImagesAutomatically(true);

        // Pasang Native Bridge ke domain ekonomi
        TradingWebBridge bridge = new TradingWebBridge(facade);
        webView.addJavascriptInterface(bridge, "TradingNativeBridge");

        webView.setWebViewClient(new WebViewClient());
        webView.loadUrl("file:///android_asset/www/index.html");
        if (android.os.Build.VERSION.SDK_INT >= 33) {
            getOnBackInvokedDispatcher().registerOnBackInvokedCallback(
                    android.window.OnBackInvokedDispatcher.PRIORITY_DEFAULT, this::handleBack);
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        // Pause waktu game saat aplikasi di-background (anti ghost draining - FR-01.08, FR-19.08)
        if (facade != null && facade.getClock() != null) {
            facade.getClock().pause();
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
