package com.smartquote.service;

import com.smartquote.entity.Company;
import com.smartquote.entity.Quotation;
import com.smartquote.entity.QuotationItem;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.pdmodel.font.PDFont;
import org.apache.pdfbox.pdmodel.font.PDType1Font;
import org.apache.pdfbox.pdmodel.font.Standard14Fonts;
import org.apache.pdfbox.pdmodel.graphics.image.PDImageXObject;
import org.springframework.stereotype.Service;

import java.awt.Color;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.math.BigDecimal;
import java.util.Base64;
import java.util.List;

@Service
public class PdfService {

    private final QuotationService quotationService;

    public PdfService(QuotationService quotationService) {
        this.quotationService = quotationService;
    }

    public byte[] generateQuotationPdf(Long quotationId) throws IOException {
        Quotation quotation = quotationService.getAuthorizedQuotation(quotationId);
        Company company = quotation.getCompany();

        try (PDDocument doc = new PDDocument()) {
            PDPage page = new PDPage(PDRectangle.A4);
            doc.addPage(page);

            PDFont fontRegular = new PDType1Font(Standard14Fonts.FontName.HELVETICA);
            PDFont fontBold = new PDType1Font(Standard14Fonts.FontName.HELVETICA_BOLD);

            float pageWidth = page.getMediaBox().getWidth();
            float pageHeight = page.getMediaBox().getHeight();
            float margin = 40;
            float y = pageHeight - margin;

            try (PDPageContentStream cs = new PDPageContentStream(doc, page)) {
                // Top header bar / branding
                cs.setNonStrokingColor(14, 140, 233); // Brand Blue #0e8ce9
                cs.addRect(margin, y - 8, pageWidth - (2 * margin), 4);
                cs.fill();

                y -= 30;

                // Company Name (Left)
                cs.setNonStrokingColor(30, 41, 59); // Slate-800
                cs.beginText();
                cs.setFont(fontBold, 20);
                cs.newLineAtOffset(margin, y);
                cs.showText(company.getName());
                cs.endText();

                // Document Title (Right)
                cs.beginText();
                cs.setFont(fontBold, 22);
                cs.newLineAtOffset(pageWidth - margin - 150, y);
                cs.showText("QUOTATION");
                cs.endText();

                y -= 18;

                // Company address & contact
                cs.setNonStrokingColor(100, 116, 139); // Slate-500
                cs.beginText();
                cs.setFont(fontRegular, 9);
                cs.newLineAtOffset(margin, y);
                if (company.getAddress() != null && !company.getAddress().isEmpty()) {
                    String cleanAddr = company.getAddress().replace("\n", ", ").trim();
                    if (cleanAddr.length() > 50) cleanAddr = cleanAddr.substring(0, 47) + "...";
                    cs.showText(cleanAddr);
                    cs.newLineAtOffset(0, -12);
                }
                String contact = (company.getEmail() != null ? company.getEmail() : "") +
                        (company.getPhone() != null ? " | " + company.getPhone() : "");
                if (!contact.isEmpty()) {
                    cs.showText(contact);
                    cs.newLineAtOffset(0, -12);
                }
                if (company.getTaxId() != null && !company.getTaxId().isEmpty()) {
                    cs.showText("Tax / GST ID: " + company.getTaxId());
                }
                cs.endText();

                // Quotation Meta (Right)
                cs.beginText();
                cs.setFont(fontRegular, 10);
                cs.newLineAtOffset(pageWidth - margin - 180, y);
                cs.showText("Quote #: " + quotation.getQuotationNumber());
                cs.newLineAtOffset(0, -14);
                cs.showText("Date: " + quotation.getIssueDate().toString());
                cs.newLineAtOffset(0, -14);
                cs.showText("Valid Until: " + quotation.getExpiryDate().toString());
                cs.newLineAtOffset(0, -14);
                cs.showText("Status: " + quotation.getStatus().name());
                cs.endText();

                y -= 45;

                // Divider line
                cs.setStrokingColor(226, 232, 240); // Slate-200
                cs.setLineWidth(1f);
                cs.moveTo(margin, y);
                cs.lineTo(pageWidth - margin, y);
                cs.stroke();

                y -= 20;

                // Bill To Section
                cs.setNonStrokingColor(15, 23, 42); // Slate-900
                cs.beginText();
                cs.setFont(fontBold, 11);
                cs.newLineAtOffset(margin, y);
                cs.showText("QUOTATION FOR (CUSTOMER):");
                cs.endText();

                y -= 15;
                cs.beginText();
                cs.setFont(fontBold, 11);
                cs.newLineAtOffset(margin, y);
                cs.showText(quotation.getCustomerName());
                cs.endText();

                y -= 13;
                cs.setFont(fontRegular, 9);
                cs.setNonStrokingColor(71, 85, 105);
                cs.beginText();
                cs.newLineAtOffset(margin, y);
                if (quotation.getCustomerEmail() != null && !quotation.getCustomerEmail().isEmpty()) {
                    cs.showText("Email: " + quotation.getCustomerEmail());
                    cs.newLineAtOffset(0, -12);
                }
                if (quotation.getCustomerPhone() != null && !quotation.getCustomerPhone().isEmpty()) {
                    cs.showText("Phone: " + quotation.getCustomerPhone());
                    cs.newLineAtOffset(0, -12);
                }
                if (quotation.getCustomerAddress() != null && !quotation.getCustomerAddress().isEmpty()) {
                    String addr = quotation.getCustomerAddress().replace("\n", ", ");
                    if (addr.length() > 60) addr = addr.substring(0, 57) + "...";
                    cs.showText("Address: " + addr);
                }
                cs.endText();

                y -= 30;

                // Table Header
                float colItem = margin;
                float colQty = pageWidth - margin - 220;
                float colPrice = pageWidth - margin - 140;
                float colTotal = pageWidth - margin - 60;

                cs.setNonStrokingColor(241, 245, 249); // Slate-100
                cs.addRect(margin, y - 5, pageWidth - (2 * margin), 20);
                cs.fill();

                cs.setNonStrokingColor(15, 23, 42);
                cs.setFont(fontBold, 9);
                cs.beginText();
                cs.newLineAtOffset(colItem + 8, y + 2);
                cs.showText("ITEM DESCRIPTION");
                cs.endText();

                cs.beginText();
                cs.newLineAtOffset(colQty, y + 2);
                cs.showText("QTY");
                cs.endText();

                cs.beginText();
                cs.newLineAtOffset(colPrice, y + 2);
                cs.showText("UNIT PRICE (" + company.getCurrency() + ")");
                cs.endText();

                cs.beginText();
                cs.newLineAtOffset(colTotal, y + 2);
                cs.showText("AMOUNT");
                cs.endText();

                y -= 22;

                // Line items
                cs.setFont(fontRegular, 9);
                for (QuotationItem item : quotation.getItems()) {
                    cs.setNonStrokingColor(51, 65, 85);

                    cs.beginText();
                    cs.newLineAtOffset(colItem + 8, y);
                    String desc = item.getDescription();
                    if (desc.length() > 42) desc = desc.substring(0, 39) + "...";
                    cs.showText(desc);
                    cs.endText();

                    cs.beginText();
                    cs.newLineAtOffset(colQty, y);
                    cs.showText(item.getQuantity().toPlainString());
                    cs.endText();

                    cs.beginText();
                    cs.newLineAtOffset(colPrice, y);
                    cs.showText(String.format("%.2f", item.getUnitPrice()));
                    cs.endText();

                    cs.beginText();
                    cs.newLineAtOffset(colTotal, y);
                    cs.showText(String.format("%.2f", item.getAmount()));
                    cs.endText();

                    y -= 6;
                    cs.setStrokingColor(241, 245, 249);
                    cs.moveTo(margin, y);
                    cs.lineTo(pageWidth - margin, y);
                    cs.stroke();

                    y -= 14;
                }

                y -= 10;

                // Totals box on the right
                float totalsX = pageWidth - margin - 200;
                float valuesX = pageWidth - margin - 50;

                cs.setFont(fontRegular, 9);
                cs.setNonStrokingColor(71, 85, 105);

                drawTotalRow(cs, fontRegular, "Subtotal:", String.format("%.2f", quotation.getSubtotal()), totalsX, valuesX, y);
                y -= 14;

                if (quotation.getDiscountPercentage().compareTo(BigDecimal.ZERO) > 0) {
                    drawTotalRow(cs, fontRegular, "Discount (" + quotation.getDiscountPercentage() + "%):",
                            "-" + String.format("%.2f", quotation.getDiscountAmount()), totalsX, valuesX, y);
                    y -= 14;
                    drawTotalRow(cs, fontRegular, "Taxable Amount:",
                            String.format("%.2f", quotation.getTaxableAmount()), totalsX, valuesX, y);
                    y -= 14;
                }

                if (quotation.getTaxPercentage().compareTo(BigDecimal.ZERO) > 0) {
                    drawTotalRow(cs, fontRegular, "GST / Tax (" + quotation.getTaxPercentage() + "%):",
                            "+" + String.format("%.2f", quotation.getTaxAmount()), totalsX, valuesX, y);
                    y -= 14;
                }

                cs.setStrokingColor(203, 213, 225);
                cs.moveTo(totalsX, y + 2);
                cs.lineTo(pageWidth - margin, y + 2);
                cs.stroke();

                y -= 6;
                cs.setNonStrokingColor(15, 23, 42);
                drawTotalRow(cs, fontBold, "Total (" + company.getCurrency() + "):",
                        company.getCurrencySymbol() + " " + String.format("%.2f", quotation.getTotalAmount()), totalsX, valuesX, y);

                y -= 40;

                // Notes & Terms
                if (quotation.getNotes() != null && !quotation.getNotes().isEmpty()) {
                    cs.setFont(fontBold, 9);
                    cs.setNonStrokingColor(30, 41, 59);
                    cs.beginText();
                    cs.newLineAtOffset(margin, y);
                    cs.showText("Notes:");
                    cs.endText();
                    y -= 12;

                    cs.setFont(fontRegular, 8);
                    cs.setNonStrokingColor(100, 116, 139);
                    String[] noteLines = quotation.getNotes().split("\\r?\\n");
                    for (String nl : noteLines) {
                        cs.beginText();
                        cs.newLineAtOffset(margin, y);
                        cs.showText(nl.length() > 90 ? nl.substring(0, 87) + "..." : nl);
                        cs.endText();
                        y -= 10;
                    }
                    y -= 10;
                }

                if (quotation.getTerms() != null && !quotation.getTerms().isEmpty()) {
                    cs.setFont(fontBold, 9);
                    cs.setNonStrokingColor(30, 41, 59);
                    cs.beginText();
                    cs.newLineAtOffset(margin, y);
                    cs.showText("Terms & Conditions:");
                    cs.endText();
                    y -= 12;

                    cs.setFont(fontRegular, 8);
                    cs.setNonStrokingColor(100, 116, 139);
                    String[] termLines = quotation.getTerms().split("\\r?\\n");
                    for (String tl : termLines) {
                        cs.beginText();
                        cs.newLineAtOffset(margin, y);
                        cs.showText(tl.length() > 90 ? tl.substring(0, 87) + "..." : tl);
                        cs.endText();
                        y -= 10;
                    }
                }

                // Footer
                cs.setFont(fontRegular, 8);
                cs.setNonStrokingColor(148, 163, 184);
                cs.beginText();
                cs.newLineAtOffset(margin, margin + 5);
                cs.showText("Generated by Smart Quotation Generator - Professional Edition. Page 1 of 1");
                cs.endText();
            }

            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            doc.save(baos);
            return baos.toByteArray();
        }
    }

    private void drawTotalRow(PDPageContentStream cs, PDFont font, String label, String value, float x1, float x2, float y) throws IOException {
        cs.setFont(font, 9);
        cs.beginText();
        cs.newLineAtOffset(x1, y);
        cs.showText(label);
        cs.endText();

        cs.beginText();
        cs.newLineAtOffset(x2, y);
        cs.showText(value);
        cs.endText();
    }
}
