package com.example.curriculoia.controller;

import com.example.curriculoia.dto.AnaliseCurriculoResponse;
import com.example.curriculoia.service.GeminiService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping("/api")
public class CurriculoController {

    private final GeminiService geminiService;

    public CurriculoController(GeminiService geminiService) {
        this.geminiService = geminiService;
    }

    @PostMapping("/analisar")
    public Mono<ResponseEntity<AnaliseCurriculoResponse>> analisarCurriculo(@RequestParam("file") MultipartFile file) {
        if (file.isEmpty()) {
            return Mono.just(ResponseEntity.badRequest().build());
        }

        return geminiService.analisarCurriculo(file)
                .map(analise -> ResponseEntity.ok(new AnaliseCurriculoResponse(analise)))
                .onErrorResume(e -> {
                    // Log do erro no servidor
                    e.printStackTrace();
                    // Retorna uma resposta de erro para o cliente
                    String mensagemErro = "Erro ao analisar o currículo: " + e.getMessage();
                    return Mono.just(ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                                     .body(new AnaliseCurriculoResponse(mensagemErro)));
                });
    }
}
