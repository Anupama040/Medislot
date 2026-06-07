package com.medislot.app.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class GeminiService {

    @Value("${gemini.api.key}")
    private String apiKey;

    private final RestTemplate restTemplate = new RestTemplate();
    private final ObjectMapper objectMapper = new ObjectMapper();

    public String analyzeSymptoms(String symptoms, List<String> availableSpecializations) {
        String prompt = "You are a smart medical routing assistant. A patient has provided the following symptoms: '" 
                + symptoms + "'. "
                + "Based on these symptoms, which of the following medical specializations is the most appropriate? "
                + "Available specializations: " + String.join(", ", availableSpecializations) + ". "
                + "Reply with ONLY the exact name of the specialization from the list. If none match exactly, pick the closest one or reply with 'General Physician'.";

        return callGemini(prompt).trim();
    }

    public String summarizeMedicalHistory(String historyText) {
        if (historyText == null || historyText.trim().isEmpty()) {
            return "No prior medical history available.";
        }
        String prompt = "You are an AI assistant for a doctor. Summarize the following patient's past medical history and prescriptions into a brief, professional 2-3 sentence summary that a doctor can quickly read before a consultation. \n\nHistory:\n" + historyText;
        return callGemini(prompt).trim();
    }
    
    public String enhancePrescription(String rawNotes) {
        String prompt = "You are a medical AI assistant. Rewrite the following rough notes into a professional medical prescription format. Ensure medicines, dosages, and instructions are clearly stated if provided. Do not invent new medicines, just format what is given.\n\nRough Notes:\n" + rawNotes;
        return callGemini(prompt).trim();
    }
    
    public String suggestWorkingHours(String specialization) {
        String prompt = "You are a medical scheduling AI. For a doctor with specialization '" + specialization + "', suggest typical daily working hours. "
                + "Return ONLY a string in the exact format 'HH:MM-HH:MM' (24-hour time). Do not include any other text, markdown, or explanation. "
                + "Example: 09:00-17:00";
        String response = callGemini(prompt).trim();
        if (response.matches("^\\d{2}:\\d{2}-\\d{2}:\\d{2}$")) {
            return response;
        }
        return "09:00-17:00"; // Fallback
    }

    private String callGemini(String prompt) {
        String url = "https://generativelanguage.googleapis.com/v1beta/models/gemini-2.5-flash:generateContent?key=" + apiKey;

        try {
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);

            Map<String, Object> textPart = new HashMap<>();
            textPart.put("text", prompt);

            Map<String, Object> partNode = new HashMap<>();
            partNode.put("parts", List.of(textPart));

            Map<String, Object> requestBody = new HashMap<>();
            requestBody.put("contents", List.of(partNode));

            HttpEntity<Map<String, Object>> entity = new HttpEntity<>(requestBody, headers);

            ResponseEntity<String> response = restTemplate.postForEntity(url, entity, String.class);

            JsonNode root = objectMapper.readTree(response.getBody());
            return root.path("candidates").get(0).path("content").path("parts").get(0).path("text").asText();
        } catch (Exception e) {
            System.err.println("Gemini API Error: " + e.getMessage());
            e.printStackTrace();
            return "Unable to process via AI. Error: " + e.getMessage();
        }
    }
}
