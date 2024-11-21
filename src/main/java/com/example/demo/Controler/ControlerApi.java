package com.example.demo.Controler;
import com.example.demo.Googleapi.SheetsQuickstart;
import com.example.demo.WartsappApi.WhatsAppService;
import jakarta.mail.MessagingException;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.security.GeneralSecurityException;
import java.util.Map;


@RestController
public class ControlerApi {

    @Autowired
    private WhatsAppService whatsAppService;

    @Autowired
    private SheetsQuickstart sheetsQuickstart;

    private static final String APPLICATION_NAME = "Google Sheets API Java Quickstart";

    @GetMapping("/callback")
    public String executeTask() {
        try {
            sheetsQuickstart.getSheetData(); // Executa a tarefa
            return "Tarefa executada com sucesso!";
        } catch (IOException | GeneralSecurityException e) {
            e.printStackTrace();
            return "Erro ao executar a tarefa: " + e.getMessage();
        } catch (MessagingException e) {
            throw new RuntimeException(e);
        }
    }


    @PostMapping("/sendMessage")
    public String sendMessage(@RequestBody Map<String, String> request) {
        String recipientPhoneNumber = request.get("to");
        String message = request.get("message");

        // Chama o método do serviço para enviar a mensagem
        whatsAppService.sendMessage(recipientPhoneNumber, message);

        return "Mensagem enviada!";
    }
}