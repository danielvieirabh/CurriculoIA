package com.example.curriculoia.controller;

import com.example.curriculoia.dto.AnaliseCurriculoResponse;
import com.example.curriculoia.dto.PdfRequest;
import com.example.curriculoia.service.GeminiService;
import com.example.curriculoia.service.PdfService;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping("/api")
public class CurriculoController {

    private final GeminiService geminiService;
    private final PdfService pdfService;

    public CurriculoController(GeminiService geminiService, PdfService pdfService) {
        this.geminiService = geminiService;
        this.pdfService = pdfService;
    }

    @PostMapping("/analisar")
    public Mono<ResponseEntity<AnaliseCurriculoResponse>> analisarCurriculo(@RequestParam("file") MultipartFile file) {
        if (file.isEmpty()) {
            return Mono.just(ResponseEntity.badRequest().build());
        }

        return geminiService.analisarCurriculo(file)
                .map(analise -> ResponseEntity.ok(new AnaliseCurriculoResponse(analise)))
                .onErrorResume(e -> {
                    e.printStackTrace();
                    String mensagemErro = "Erro ao analisar o currículo: " + e.getMessage();
                    return Mono.just(ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                                     .body(new AnaliseCurriculoResponse(mensagemErro)));
                });
    }

    @PostMapping("/gerar-pdf")
    public ResponseEntity<byte[]> gerarPdf(@RequestBody PdfRequest request) {
        try {
            byte[] pdfBytes = pdfService.criarPdfDeMarkdown(request.getTexto());
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_PDF);
            headers.setContentDispositionFormData("attachment", "analise_curriculo.pdf");
            return new ResponseEntity<>(pdfBytes, headers, HttpStatus.OK);
        } catch (Exception e) {
            e.printStackTrace();
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
}
