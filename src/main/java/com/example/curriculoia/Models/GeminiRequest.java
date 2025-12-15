package com.example.curriculoia.Models;

import java.util.List;

public class GeminiRequest {
    private List<Content> contents;

    public GeminiRequest(String text) {
        this.contents = List.of(new Content(List.of(new Part(text))));
    }

    public List<Content> getContents() {
        return contents;
    }

    public void setContents(List<Content> contents) {
        this.contents = contents;
    }
}
