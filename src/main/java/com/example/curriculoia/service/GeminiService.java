package com.example.curriculoia.service;

import org.apache.tika.Tika;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatusCode;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.io.InputStream;
import java.util.Collections;
import java.util.Map;

@Service
public class GeminiService {

    private final WebClient webClient;
    private final String apiKey;
    // Usando um modelo mais recente e estável
    private static final String GEMINI_MODEL = "gemini-2.5-flash";

    public GeminiService(WebClient.Builder webClientBuilder, @Value("${app.gemini.api-key}") String apiKey) {
        this.apiKey = apiKey;
        this.webClient = webClientBuilder.baseUrl("https://generativelanguage.googleapis.com").build();
    }

    public Mono<String> analisarCurriculo(MultipartFile curriculo) {
        try {
            String textoCurriculo = extrairTexto(curriculo);
            String prompt = criarPrompt(textoCurriculo);

            // URL corrigida com o novo modelo
            String url = String.format("/v1beta/models/%s:generateContent?key=%s", GEMINI_MODEL, apiKey);

            Map<String, Object> requestBody = Map.of(
                "contents", Collections.singletonList(
                    Map.of("parts", Collections.singletonList(
                        Map.of("text", prompt)
                    ))
                )
            );

            return webClient.post()
                    .uri(url)
                    .bodyValue(requestBody)
                    .retrieve()
                    // Adiciona tratamento de erro para status HTTP 4xx e 5xx
                    .onStatus(HttpStatusCode::isError, response ->
                        response.bodyToMono(String.class)
                                .flatMap(errorBody -> Mono.error(
                                    new RuntimeException("Falha na API do Gemini: " + response.statusCode() + " " + errorBody)
                                ))
                    )
                    .bodyToMono(String.class)
                    .map(this::extrairConteudoDaResposta);

        } catch (Exception e) {
            // Este erro captura falhas na extração de texto, não na chamada da API
            return Mono.error(new RuntimeException("Erro ao processar o arquivo do currículo.", e));
        }
    }

    private String extrairTexto(MultipartFile file) throws Exception {
        try (InputStream stream = file.getInputStream()) {
            return new Tika().parseToString(stream);
        }
    }

    private String criarPrompt(String textoCurriculo) {
        return "Aja como um recrutador sênior especialista em análise de currículos. " +
               "Analise o seguinte currículo e forneça um resumo estruturado com os seguintes tópicos em formato Markdown:\n\n" +
               "**Resumo Profissional:** Um parágrafo conciso sobre o perfil do candidato.\n" +
               "**Pontos Fortes:** Uma lista dos principais pontos positivos.\n" +
               "**Pontos a Melhorar:** Uma lista de pontos que poderiam ser mais bem desenvolvidos ou que estão ausentes.\n" +
               "**Sugestões de Perguntas para Entrevista:** Uma lista de 3 a 5 perguntas inteligentes para fazer ao candidato com base no currículo dele.\n\n" +
               "---\n\n" +
               "**Currículo para Análise:**\n" +
               textoCurriculo;
    }
    
    private String extrairConteudoDaResposta(String jsonResponse) {
        try {
            int textIndex = jsonResponse.indexOf("\"text\": \"");
            if (textIndex != -1) {
                String start = jsonResponse.substring(textIndex + 9);
                int endIndex = start.indexOf("\"");
                if (endIndex != -1) {
                    return start.substring(0, endIndex)
                                .replace("\\n", "\n")
                                .replace("\\\"", "\"");
                }
            }
            // Se a extração falhar, pode ser uma resposta de erro do Gemini
            if (jsonResponse.contains("\"error\"")) {
                return "A API do Gemini retornou um erro: " + jsonResponse;
            }
            return "Não foi possível extrair a análise da resposta da IA. Resposta recebida: " + jsonResponse;
        } catch (Exception e) {
            return "Erro ao processar a resposta da IA: " + e.getMessage();
        }
    }
}
