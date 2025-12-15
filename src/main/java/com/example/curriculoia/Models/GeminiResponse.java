package com.example.curriculoia.Models;

import java.util.List;

public class GeminiResponse {
    private List<Candidate> candidates;

    public List<Candidate> getCandidates() {
        return this.candidates;
    }

    public void setCandidates(List<Candidate> candidates) {
        this.candidates = candidates;
    }

    public String extractText() {
        if (candidates == null || candidates.isEmpty()) {
            return "Não foi possível extrair a análise da resposta da IA.";
        }

        Candidate candidato = candidates.get(0);
        if (candidato == null || candidato.getContent() == null) {
            return "Não foi possível extrair a análise da resposta da IA.";
        }

        Content content = candidato.getContent();
        if (content.getParts() == null || content.getParts().isEmpty()) {
            return "Não foi possível extrair a análise da resposta da IA.";
        }

        // fica limpo no final
        return content.getParts().get(0).getText();
    }
}
