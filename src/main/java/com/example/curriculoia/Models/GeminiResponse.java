package com.example.curriculoia.Models;

import java.util.List;

public class GeminiResponse {
    private List<Candidate> candidates;

    public List<Candidate> getCandidates() {
        return candidates;
    }

    public void setCandidates(List<Candidate> candidates) {
        this.candidates = candidates;
    }

    public String extractText() {
        if (candidates != null && !candidates.isEmpty()) {
            Candidate firstCandidate = candidates.get(0);
            if (firstCandidate != null && firstCandidate.getContent() != null) {
                Content content = firstCandidate.getContent();
                if (content.getParts() != null && !content.getParts().isEmpty()) {
                    return content.getParts().get(0).getText();
                }
            }
        }
        return "Não foi possível extrair a análise da resposta da IA.";
    }
}
