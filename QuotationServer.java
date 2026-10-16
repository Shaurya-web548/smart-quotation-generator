import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpServer;

import java.io.*;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.text.SimpleDateFormat;
import java.util.*;

/**
 * Smart Quotation Generator - Backend
 *
 * A tiny HTTP server (JDK built-in, no external libraries) that:
 *   POST /api/quote   -> calculates totals, saves the quotation, returns the result
 *   GET  /api/quotes  -> returns the saved quotation history
 *   GET  /            -> serves files from the ../applet folder (so the Applet can be loaded)
 *
 * Request body format for POST /api/quote (plain text, one field per line):
 *   customer=Acme Traders
 *   discount=10          (percent)
 *   gst=18               (percent)
 *   item=Widget|2|150.00 (name|quantity|unit price)
 *   item=Gadget|1|99.50
 */
public class QuotationServer {

    private static final int PORT = 8080;
    private static final Path STORE = Paths.get("quotes.csv");
    private static int counter = 1000;

    public static void main(String[] args) throws IOException {
        loadCounter();
        HttpServer server = HttpServer.create(new InetSocketAddress(PORT), 0);
        server.createContext("/api/quote", QuotationServer::handleQuote);
        server.createContext("/api/quotes", QuotationServer::handleHistory);
        server.createContext("/", QuotationServer::handleStatic);
        server.start();
        System.out.println("Smart Quotation server running at http://localhost:" + PORT + "/");
    }

    // ---------- POST /api/quote ----------
    private static void handleQuote(HttpExchange ex) throws IOException {
        if (!ex.getRequestMethod().equalsIgnoreCase("POST")) {
            send(ex, 405, "Use POST");
            return;
        }
        String body = new String(readAll(ex.getRequestBody()), StandardCharsets.UTF_8);

        String customer = "Walk-in Customer";
        double discountPct = 0, gstPct = 0;
        List<String[]> items = new ArrayList<>();

        try {
            for (String line : body.split("\\r?\\n")) {
                int eq = line.indexOf('=');
                if (eq < 0) continue;
                String key = line.substring(0, eq).trim();
                String val = line.substring(eq + 1).trim();
                switch (key) {
                    case "customer": if (!val.isEmpty()) customer = val; break;
                    case "discount": discountPct = Double.parseDouble(val); break;
                    case "gst":      gstPct = Double.parseDouble(val); break;
                    case "item":     items.add(val.split("\\|")); break;
                    default: break;
                }
            }
        } catch (NumberFormatException e) {
            send(ex, 400, "Invalid number in request");
            return;
        }

        if (items.isEmpty()) {
            send(ex, 400, "Add at least one item");
            return;
        }

        double subtotal = 0;
        StringBuilder lines = new StringBuilder();
        for (String[] it : items) {
            if (it.length < 3) { send(ex, 400, "Bad item line"); return; }
            String name = it[0].trim();
            int qty;
            double price;
            try {
                qty = Integer.parseInt(it[1].trim());
                price = Double.parseDouble(it[2].trim());
            } catch (NumberFormatException e) {
                send(ex, 400, "Bad quantity or price for item: " + name);
                return;
            }
            if (qty <= 0 || price < 0) { send(ex, 400, "Quantity must be > 0 and price >= 0"); return; }
            double amount = qty * price;
            subtotal += amount;
            lines.append(String.format("line=%s|%d|%.2f|%.2f%n", name, qty, price, amount));
        }

        double discount = subtotal * discountPct / 100.0;
        double taxable = subtotal - discount;
        double tax = taxable * gstPct / 100.0;
        double total = taxable + tax;

        String id;
        String date = new SimpleDateFormat("yyyy-MM-dd HH:mm").format(new Date());
        synchronized (QuotationServer.class) {
            id = "Q-" + (++counter);
            save(id, date, customer, total);
        }

        String response = "id=" + id + "\n"
                + "date=" + date + "\n"
                + "customer=" + customer + "\n"
                + lines
                + String.format("subtotal=%.2f%n", subtotal)
                + String.format("discount=%.2f%n", discount)
                + String.format("tax=%.2f%n", tax)
                + String.format("total=%.2f%n", total);
        send(ex, 200, response);
    }

    // ---------- GET /api/quotes ----------
    private static void handleHistory(HttpExchange ex) throws IOException {
        StringBuilder sb = new StringBuilder();
        if (Files.exists(STORE)) {
            for (String line : Files.readAllLines(STORE, StandardCharsets.UTF_8)) {
                sb.append(line).append('\n');
            }
        }
        send(ex, 200, sb.toString());
    }

    // ---------- static files (applet class + index.html) ----------
    private static void handleStatic(HttpExchange ex) throws IOException {
        String path = ex.getRequestURI().getPath();
        if (path.equals("/")) path = "/index.html";
        Path base = Paths.get(".").toAbsolutePath().normalize();
        Path file = base.resolve(path.substring(1)).normalize();
        if (!file.startsWith(base) || !Files.isRegularFile(file)) {
            send(ex, 404, "Not found");
            return;
        }
        String type = path.endsWith(".html") ? "text/html"
                : path.endsWith(".class") || path.endsWith(".jar") ? "application/java-archive"
                : "application/octet-stream";
        byte[] data = Files.readAllBytes(file);
        ex.getResponseHeaders().set("Content-Type", type);
        ex.sendResponseHeaders(200, data.length);
        try (OutputStream os = ex.getResponseBody()) { os.write(data); }
    }

    // ---------- helpers ----------
    private static void save(String id, String date, String customer, double total) throws IOException {
        String safeCustomer = customer.replace("|", "/").replace("\n", " ");
        String row = id + "|" + date + "|" + safeCustomer + "|" + String.format("%.2f", total) + "\n";
        Files.write(STORE, row.getBytes(StandardCharsets.UTF_8),
                StandardOpenOption.CREATE, StandardOpenOption.APPEND);
    }

    private static void loadCounter() throws IOException {
        if (!Files.exists(STORE)) return;
        for (String line : Files.readAllLines(STORE, StandardCharsets.UTF_8)) {
            String[] p = line.split("\\|");
            if (p.length > 0 && p[0].startsWith("Q-")) {
                try { counter = Math.max(counter, Integer.parseInt(p[0].substring(2))); }
                catch (NumberFormatException ignored) { }
            }
        }
    }

    private static byte[] readAll(InputStream in) throws IOException {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        byte[] buf = new byte[4096];
        int n;
        while ((n = in.read(buf)) != -1) bos.write(buf, 0, n);
        return bos.toByteArray();
    }

    private static void send(HttpExchange ex, int status, String text) throws IOException {
        byte[] data = text.getBytes(StandardCharsets.UTF_8);
        ex.getResponseHeaders().set("Content-Type", "text/plain; charset=utf-8");
        ex.getResponseHeaders().set("Access-Control-Allow-Origin", "*");
        ex.sendResponseHeaders(status, data.length);
        try (OutputStream os = ex.getResponseBody()) { os.write(data); }
    }
}
