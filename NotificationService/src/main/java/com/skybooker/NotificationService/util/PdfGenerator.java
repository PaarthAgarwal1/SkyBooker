package com.skybooker.NotificationService.util;

import org.xhtmlrenderer.pdf.ITextRenderer;

import java.io.ByteArrayOutputStream;

public class PdfGenerator {

    public static byte[] generatePdf(String html) throws Exception {
        ByteArrayOutputStream out = new ByteArrayOutputStream();

        ITextRenderer renderer = new ITextRenderer();
        renderer.setDocumentFromString(html);
        renderer.layout();
        renderer.createPDF(out);

        return out.toByteArray();
    }
}