package com.example.demo.WartsappApi;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.Map;

@Service
public class WhatsAppService {

    private final String token = "EAAOgoXMudpQBO4528jK4UzjROmfcZADmHZCi4hOZCo6CLOm0bw6PCnBJHwZB81nhVNsdtg93mo5na0uX7U9s4Fed9GQMdqElZBZBZC5cc8zfZCQZCBS28k20RjQnK9ZBHqojYZARuhlu8ZAJkFEwqa1rfw2oA4G4JX1QXiOsaLql2D6TsN9loekJkgcRyTXHZBwAZBod2eRVreSi1fkDbkznQuoCUeCrr1ZANYZD";
    private final String phoneNumberId = "+5511943222247";

    public void sendMessage(String recipientPhoneNumber, String message) {
        String url = "https://graph.facebook.com/v13.0/" + phoneNumberId + "/messages";

        RestTemplate restTemplate = new RestTemplate();
        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", "Bearer " + token);
        headers.setContentType(MediaType.APPLICATION_JSON);

        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put("messaging_product", "whatsapp");
        requestBody.put("to", recipientPhoneNumber);

        Map<String, String> textBody = new HashMap<>();
        textBody.put("body", message);
        requestBody.put("type", "text");
        requestBody.put("text", textBody);

        HttpEntity<Map<String, Object>> request = new HttpEntity<>(requestBody, headers);
        ResponseEntity<String> response = restTemplate.postForEntity(url, request, String.class);

        if (response.getStatusCode() == HttpStatus.OK) {
            System.out.println("Mensagem enviada com sucesso!");
        } else {
            System.out.println("Erro ao enviar a mensagem: " + response.getBody());
        }
    }
}