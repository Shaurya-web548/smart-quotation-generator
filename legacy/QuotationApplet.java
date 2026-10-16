import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.net.*;
import java.nio.charset.StandardCharsets;

@SuppressWarnings({"deprecation", "removal"})
public class QuotationApplet extends JApplet {
    private JTextField customerField;
    private JTextField discountField;
    private JTextField gstField;
    private JTextArea itemsArea;
    private JTextArea outputArea;
    private String serverUrl = "http://localhost:8080";

    @Override
    public void init() {
        String param = getParameter("server");
        if (param != null && !param.trim().isEmpty()) serverUrl = param.trim();
        try { SwingUtilities.invokeAndWait(new Runnable() { public void run() { buildUI(); } }); }
        catch (Exception e) { e.printStackTrace(); }
    }

    private void buildUI() {
        setLayout(new BorderLayout(8, 8));
        JPanel form = new JPanel(new GridBagLayout());
        GridBagConstraints c = new GridBagConstraints();
        c.insets = new Insets(4, 4, 4, 4);
        c.anchor = GridBagConstraints.WEST;
        c.fill = GridBagConstraints.HORIZONTAL;
        customerField = new JTextField("Acme Traders", 20);
        discountField = new JTextField("10", 6);
        gstField = new JTextField("18", 6);
        c.gridx = 0; c.gridy = 0; c.weightx = 0;
        form.add(new JLabel("Customer:"), c);
        c.gridx = 1; c.weightx = 1;
        form.add(customerField, c);
        c.gridx = 0; c.gridy = 1; c.weightx = 0;
        form.add(new JLabel("Discount %:"), c);
        c.gridx = 1; c.weightx = 1;
        form.add(discountField, c);
        c.gridx = 0; c.gridy = 2; c.weightx = 0;
        form.add(new JLabel("GST %:"), c);
        c.gridx = 1; c.weightx = 1;
        form.add(gstField, c);
        itemsArea = new JTextArea("Widget|2|150.00\nGadget|1|99.50", 6, 30);
        itemsArea.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 12));
        JPanel itemsPanel = new JPanel(new BorderLayout());
        itemsPanel.setBorder(BorderFactory.createTitledBorder("Items (name|qty|price)"));
        itemsPanel.add(new JScrollPane(itemsArea), BorderLayout.CENTER);
        JPanel top = new JPanel(new BorderLayout());
        top.add(form, BorderLayout.NORTH);
        top.add(itemsPanel, BorderLayout.CENTER);
        JButton generateBtn = new JButton("Generate Quotation");
        JButton historyBtn = new JButton("History");
        JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        btnPanel.add(generateBtn);
        btnPanel.add(historyBtn);
        generateBtn.addActionListener(new java.awt.event.ActionListener() { public void actionPerformed(java.awt.event.ActionEvent e) { generateQuote(); } });
        historyBtn.addActionListener(new java.awt.event.ActionListener() { public void actionPerformed(java.awt.event.ActionEvent e) { loadHistory(); } });
        outputArea = new JTextArea(12, 40);
        outputArea.setEditable(false);
        outputArea.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 12));
        JScrollPane outScroll = new JScrollPane(outputArea);
        outScroll.setBorder(BorderFactory.createTitledBorder("Result"));
        JPanel center = new JPanel(new BorderLayout());
        center.add(btnPanel, BorderLayout.NORTH);
        center.add(outScroll, BorderLayout.CENTER);
        add(top, BorderLayout.NORTH);
        add(center, BorderLayout.CENTER);
    }
    private void generateQuote() {
        final String customer = customerField.getText().trim();
        final String discount = discountField.getText().trim();
        final String gst = gstField.getText().trim();
        final String items = itemsArea.getText().trim();
        outputArea.setText("Sending to " + serverUrl + "/api/quote ...\n");
        new Thread(new Runnable() { public void run() {
            try {
                StringBuilder body = new StringBuilder();
                body.append("customer=").append(customer.isEmpty() ? "Walk-in Customer" : customer).append("\n");
                body.append("discount=").append(discount.isEmpty() ? "0" : discount).append("\n");
                body.append("gst=").append(gst.isEmpty() ? "0" : gst).append("\n");
                for (String line : items.split("\\r?\\n")) {
                    line = line.trim();
                    if (!line.isEmpty()) body.append("item=").append(line).append("\n");
                }
                final String resp = post(serverUrl + "/api/quote", body.toString());
                SwingUtilities.invokeLater(new Runnable() { public void run() { outputArea.setText(resp); } });
            } catch (final Exception ex) {
                SwingUtilities.invokeLater(new Runnable() { public void run() { outputArea.setText("ERROR: " + ex.getMessage() + "\nIs the server running? (run.bat server)"); } });
            }
        }}).start();
    }

    private void loadHistory() {
        outputArea.setText("Loading " + serverUrl + "/api/quotes ...\n");
        new Thread(new Runnable() { public void run() {
            try {
                String resp = get(serverUrl + "/api/quotes");
                final String r = resp.trim().isEmpty() ? "(no quotations saved yet)" : resp;
                SwingUtilities.invokeLater(new Runnable() { public void run() { outputArea.setText("ID | Date | Customer | Total\n" + r); } });
            } catch (final Exception ex) {
                SwingUtilities.invokeLater(new Runnable() { public void run() { outputArea.setText("ERROR: " + ex.getMessage()); } });
            }
        }}).start();
    }

    private static String post(String urlStr, String body) throws IOException {
        byte[] data = body.getBytes(StandardCharsets.UTF_8);
        HttpURLConnection con = (HttpURLConnection) new URL(urlStr).openConnection();
        con.setRequestMethod("POST");
        con.setDoOutput(true);
        con.setRequestProperty("Content-Type", "text/plain; charset=utf-8");
        con.setConnectTimeout(5000);
        con.setReadTimeout(5000);
        try (OutputStream os = con.getOutputStream()) { os.write(data); }
        return readResponse(con);
    }

    private static String get(String urlStr) throws IOException {
        HttpURLConnection con = (HttpURLConnection) new URL(urlStr).openConnection();
        con.setRequestMethod("GET");
        con.setConnectTimeout(5000);
        con.setReadTimeout(5000);
        return readResponse(con);
    }

    private static String readResponse(HttpURLConnection con) throws IOException {
        int code = con.getResponseCode();
        InputStream in = (code >= 200 && code < 300) ? con.getInputStream() : con.getErrorStream();
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        byte[] buf = new byte[4096];
        int n;
        while ((n = in.read(buf)) != -1) bos.write(buf, 0, n);
        in.close();
        String text = new String(bos.toByteArray(), StandardCharsets.UTF_8);
        if (code >= 200 && code < 300) return text;
        throw new IOException("Server returned " + code + ": " + text);
    }
}
