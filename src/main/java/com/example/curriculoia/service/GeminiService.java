package com.example.curriculoia.service;

import com.example.curriculoia.dto.GeminiRequest;
import com.example.curriculoia.dto.GeminiResponse;
import org.apache.tika.Tika;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatusCode;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.io.InputStream;

@Service
public class GeminiService {

    private final WebClient webClient;
    private final String apiKey;
    private static final String GEMINI_MODEL = "gemini-2.5-flash";

    public GeminiService(WebClient.Builder webClientBuilder, @Value("${app.gemini.api-key}") String apiKey) {
        this.apiKey = apiKey;
        this.webClient = webClientBuilder.baseUrl("https://generativelanguage.googleapis.com").build();
    }

    public Mono<String> analisarCurriculo(MultipartFile curriculo) {
        try {
            String textoCurriculo = extrairTexto(curriculo);
            String prompt = criarPrompt(textoCurriculo);
            String url = String.format("/v1beta/models/%s:generateContent?key=%s", GEMINI_MODEL, apiKey);

            GeminiRequest requestBody = new GeminiRequest(prompt);

            return webClient.post()
                    .uri(url)
                    .bodyValue(requestBody)
                    .retrieve()
                    .onStatus(HttpStatusCode::isError, response ->
                        response.bodyToMono(String.class)
                                .flatMap(errorBody -> Mono.error(
                                    new RuntimeException("Falha na API do Gemini: " + response.statusCode() + " " + errorBody)
                                ))
                    )
                    .bodyToMono(GeminiResponse.class)
                    .map(GeminiResponse::extractText);

        } catch (Exception error) {
            return Mono.error(new RuntimeException("Erro ao processar o arquivo do currículo.", error));
        }
    }

    private String extrairTexto(MultipartFile file) throws Exception {
        try (InputStream stream = file.getInputStream()) {
            return new Tika().parseToString(stream);
        }
    }

    private String criarPrompt(String textoCurriculo) {
        return "Aja como um recrutador sênior especialista em análise de currículos, com mais de 20 anos de experiência em tech recruiting. " +
               "Seja extremamente detalhista e analítico. " +
               "Analise o seguinte currículo e forneça um relatório completo e aprofundado em formato Markdown, cobrindo os seguintes tópicos:\n\n" +
               "**Resumo Profissional:** Um parágrafo detalhado sobre o perfil do candidato, suas ambições e adequação ao mercado.\n" +
               "**Principais Competências Técnicas (Hard Skills):** Uma lista detalhada das tecnologias, linguagens e frameworks que o candidato domina, com nível de proficiência estimado (iniciante, intermediário, avançado) baseado nas informações do currículo.\n" +
               "**Competências Comportamentais (Soft Skills):** Uma análise das soft skills demonstradas, como comunicação, liderança, proatividade, etc., com exemplos extraídos do currículo.\n" +
               "**Pontos Fortes:** Uma lista clara e objetiva dos principais diferenciais do candidato.\n" +
               "**Pontos a Melhorar e Recomendações:** Uma lista construtiva de áreas onde o candidato pode melhorar, incluindo sugestões de cursos, certificações ou experiências.\n" +
               "**Potencial de Carreira:** Uma avaliação sobre o potencial de crescimento do candidato e em quais tipos de função ou empresa ele se encaixaria melhor.\n" +
               "**Sugestões de Perguntas para Entrevista:** Uma lista de 5 a 7 perguntas técnicas e comportamentais perspicazes para aprofundar a avaliação do candidato.\n\n" +
               "---\n\n" +
               "**Currículo para Análise:**\n" +
               textoCurriculo;
    }
}
