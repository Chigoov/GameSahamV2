package com.tradingglobal3d.mobile;

import android.webkit.JavascriptInterface;
import com.sun.net.httpserver.HttpServer;
import com.tradingglobal3d.core.facade.TradingFacade;
import com.tradingglobal3d.core.market.HistoricalDatasetLoader;
import com.tradingglobal3d.core.market.InstrumentCatalog;
import com.tradingglobal3d.core.storage.SaveManager;
import org.json.JSONArray;
import org.json.JSONObject;
import java.lang.reflect.Method;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.LinkedHashMap;
import java.util.Map;

/** Local-only browser harness: actual Java domain, isolated saves, never Android evidence. */
public final class WireQaServer {
    public static void main(String[] args) throws Exception {
        Path assets = Path.of(args[0]).toAbsolutePath().normalize();
        Path saves = Path.of(args[1]).toAbsolutePath().normalize();
        String savesNormalized = saves.toString().replace('\\','/');
        if (!savesNormalized.contains("/Artifacts/") || !savesNormalized.contains("qa-save")) throw new IllegalArgumentException("QA-only save required");
        Files.createDirectories(saves);
        HistoricalDatasetLoader.setAssetSource(path -> Files.newInputStream(assets.resolve(path)));
        TradingFacade facade = new TradingFacade(SaveManager.forDirectory(saves.toFile()));
        facade.setCatalog(InstrumentCatalog.fromJsonString(Files.readString(assets.resolve("data/market_universe.json"))));
        if (!facade.startOrResume()) throw new IllegalStateException("QA save needs recovery; refusing reset");
        facade.generateNpcLiquidity();
        TradingWebBridge bridge = new TradingWebBridge(facade);
        Map<String, Method> methods = new LinkedHashMap<>();
        for (Method method : TradingWebBridge.class.getDeclaredMethods()) {
            if (method.isAnnotationPresent(JavascriptInterface.class)) methods.put(method.getName(), method);
        }
        JSONObject platform = new JSONObject().put("platform", "QA Java host").put("nativeHost", false).put("testFixture", true)
            .put("label", "QA HOST JAVA — SAVE TERISOLASI — BUKAN ANDROID");
        String bridgeScript = "window.TradingNativeBridge={};for(const method of " + new JSONArray(methods.keySet()) + ")"
            + "TradingNativeBridge[method]=(...args)=>{const x=new XMLHttpRequest();x.open('POST','/qa-bridge',false);x.setRequestHeader('Content-Type','application/json');x.send(JSON.stringify({method,args}));if(x.status!==200)throw Error('QA bridge '+method+': '+x.responseText);return x.responseText;};";
        int port = Integer.getInteger("qa.port", 8784);
        if (args.length >= 3) {
            try { port = Integer.parseInt(args[2]); } catch (Exception ignored) {}
        }
        HttpServer server = HttpServer.create(new InetSocketAddress("127.0.0.1", port), 0);
        server.createContext("/", exchange -> {
            int status = 200;
            byte[] response;
            String mime = "application/json; charset=utf-8";
            try {
                String route = exchange.getRequestURI().getPath();
                if (route.equals("/qa-meta")) {
                    response = new JSONObject().put("platform", platform).put("methods", new JSONArray(methods.keySet())).toString().getBytes(StandardCharsets.UTF_8);
                } else if (route.equals("/qa-action")) {
                    if (!exchange.getRequestMethod().equals("POST")) throw new IllegalArgumentException("POST required");
                    JSONObject request = new JSONObject(new String(exchange.getRequestBody().readAllBytes(), StandardCharsets.UTF_8));
                    String action = request.getString("action");
                    synchronized (facade) {
                        if ("generateNpcLiquidity".equals(action)) {
                            facade.generateNpcLiquidity();
                            response = new JSONObject().put("success", true).toString().getBytes(StandardCharsets.UTF_8);
                        } else if ("advanceToTradingSession".equals(action)) {
                            facade.getClock().play();
                            facade.tick(1800);
                            response = new JSONObject().put("success", true).toString().getBytes(StandardCharsets.UTF_8);
                        } else {
                            throw new IllegalArgumentException("Unknown QA action: " + action);
                        }
                    }
                } else if (route.equals("/qa-bridge")) {
                    if (!exchange.getRequestMethod().equals("POST")) throw new IllegalArgumentException("POST required");
                    JSONObject request = new JSONObject(new String(exchange.getRequestBody().readAllBytes(), StandardCharsets.UTF_8));
                    String name = request.getString("method");
                    Method method = methods.get(name);
                    if (method == null) throw new IllegalArgumentException("Not an exposed bridge method");
                    JSONArray supplied = request.getJSONArray("args");
                    Class<?>[] types = method.getParameterTypes();
                    if (supplied.length() != types.length) throw new IllegalArgumentException("Wrong argument count: " + name);
                    Object[] converted = new Object[types.length];
                    for (int i=0;i<types.length;i++) {
                        if (types[i] == String.class) converted[i] = supplied.isNull(i) ? null : supplied.getString(i);
                        else if (types[i] == boolean.class) converted[i] = supplied.getBoolean(i);
                        else if (types[i] == int.class) converted[i] = supplied.getInt(i);
                        else if (types[i] == long.class) converted[i] = supplied.getLong(i);
                        else throw new IllegalArgumentException("Unsupported bridge parameter");
                    }
                    synchronized (facade) {
                        String result = name.equals("getPlatformInfo") ? platform.toString() : (String)method.invoke(bridge, converted);
                        response = result.getBytes(StandardCharsets.UTF_8);
                    }
                } else if (route.equals("/browser-bridge.js")) {
                    response = bridgeScript.getBytes(StandardCharsets.UTF_8); mime = "text/javascript; charset=utf-8";
                } else {
                    Path web = assets.resolve("www");
                    Path file = web.resolve(route.equals("/") ? "index.html" : route.substring(1)).normalize();
                    if (!file.startsWith(web) || !Files.isRegularFile(file)) throw new IllegalArgumentException("Not found");
                    response = Files.readAllBytes(file);
                    String filename = file.getFileName().toString();
                    if (filename.equals("index.html")) response = new String(response, StandardCharsets.UTF_8).replace("</head>", "<script src=\"/browser-bridge.js\"></script></head>").getBytes(StandardCharsets.UTF_8);
                    mime = filename.endsWith(".js") ? "text/javascript; charset=utf-8" : filename.endsWith(".css") ? "text/css; charset=utf-8" : filename.endsWith(".ttf") ? "font/ttf" : "text/html; charset=utf-8";
                }
            } catch (Exception error) {
                status = 400;
                Throwable cause = error.getCause() == null ? error : error.getCause();
                response = new JSONObject().put("success", false).put("message", cause.toString()).toString().getBytes(StandardCharsets.UTF_8);
                cause.printStackTrace();
            }
            exchange.getResponseHeaders().set("Content-Type", mime);
            exchange.sendResponseHeaders(status, response.length);
            exchange.getResponseBody().write(response); exchange.close();
        });
        server.start();
        System.out.println("QA Java domain http://127.0.0.1:" + port + "; " + methods.size() + " exposed APIs; isolated save " + saves);
    }
}
