package com.example.curriculoia.service;

import com.example.curriculoia.exception.PdfGenerationException;
import com.openhtmltopdf.pdfboxout.PdfRendererBuilder;
import org.commonmark.node.Node;
import org.commonmark.parser.Parser;
import org.commonmark.renderer.html.HtmlRenderer;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;

@Service
public class PdfService {

    public byte[] criarPdfDeMarkdown(String markdown) {
        try {
            String html = converterMarkdownParaHtml(markdown);
            String htmlCompleto = envolverComHtml(html);

            try (ByteArrayOutputStream os = new ByteArrayOutputStream()) {
                PdfRendererBuilder builder = new PdfRendererBuilder();
                builder.useFastMode();
                builder.withHtmlContent(htmlCompleto, null);
                builder.toStream(os);
                builder.run();
                return os.toByteArray();
            }
        } catch (Exception e) {
            throw new PdfGenerationException("Erro ao gerar o PDF a partir do Markdown.", e);
        }
    }

    private String converterMarkdownParaHtml(String markdown) {
        Parser parser = Parser.builder().build();
        Node document = parser.parse(markdown);
        HtmlRenderer renderer = HtmlRenderer.builder().build();
        return renderer.render(document);
    }

    private String envolverComHtml(String content) {
        return "<html><head><style>" +
               "body { font-family: sans-serif; }" +
               "h1, h2, h3 { color: #333; }" +
               "ul { list-style-type: disc; margin-left: 20px; }" +
               "strong { color: #555; }" +
               "</style></head><body>" +
               content +
               "</body></html>";
    }
}
