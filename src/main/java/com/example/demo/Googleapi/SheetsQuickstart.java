package com.example.demo.Googleapi;

import com.google.api.client.auth.oauth2.Credential;
import com.google.api.client.extensions.java6.auth.oauth2.AuthorizationCodeInstalledApp;
import com.google.api.client.extensions.jetty.auth.oauth2.LocalServerReceiver;
import com.google.api.client.googleapis.auth.oauth2.GoogleAuthorizationCodeFlow;
import com.google.api.client.googleapis.auth.oauth2.GoogleClientSecrets;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.gson.GsonFactory;
import com.google.api.client.util.store.FileDataStoreFactory;
import com.google.api.services.sheets.v4.Sheets;
import com.google.api.services.sheets.v4.SheetsScopes;
import com.google.api.services.sheets.v4.model.ValueRange;
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.json.JsonFactory;
import jakarta.mail.MessagingException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.io.*;
import java.security.GeneralSecurityException;
import java.util.Collections;
import java.util.List;
import java.util.Scanner;

@Service
@EnableScheduling
public class SheetsQuickstart {

    private static final String APPLICATION_NAME = "Google Sheets API Java Quickstart";
    private static final JsonFactory JSON_FACTORY = GsonFactory.getDefaultInstance();
    private static final String TOKENS_DIRECTORY_PATH = "tokens";
    private static final List<String> SCOPES =
            Collections.singletonList(SheetsScopes.SPREADSHEETS);
    private static final String CREDENTIALS_FILE_PATH = "/ignore/credentials.json";
    @Autowired
    EmailService emailService;

    private static Sheets getSheetsService(final NetHttpTransport HTTP_TRANSPORT) throws IOException, GeneralSecurityException {
        // Carrega os segredos do cliente
        InputStream in = SheetsQuickstart.class.getResourceAsStream(CREDENTIALS_FILE_PATH);
        if (in == null) {
            throw new FileNotFoundException("Resource not found: " + CREDENTIALS_FILE_PATH);
        }
        GoogleClientSecrets clientSecrets = GoogleClientSecrets.load(JSON_FACTORY, new InputStreamReader(in));

        // Configura o fluxo de autorização e a autorização do usuário
        GoogleAuthorizationCodeFlow flow = new GoogleAuthorizationCodeFlow.Builder(
                HTTP_TRANSPORT, JSON_FACTORY, clientSecrets, SCOPES)
                .setDataStoreFactory(new FileDataStoreFactory(new java.io.File(TOKENS_DIRECTORY_PATH)))
                .setAccessType("offline")
                .build();

        LocalServerReceiver receiver = new LocalServerReceiver.Builder().setPort(8888).build();
        Credential credential = new AuthorizationCodeInstalledApp(flow, receiver).authorize("user");

        // Retorna o serviço Sheets com a credencial obtida
        return new Sheets.Builder(HTTP_TRANSPORT, JSON_FACTORY, credential)
                .setApplicationName(APPLICATION_NAME)
                .build();
    }

    // Processa os dados da planilha, enviando e-mails quando necessário
    public List<List<Object>> processSheet(String sheetName, Sheets service, String spreadsheetId) throws IOException, MessagingException {
        String range = sheetName + "!A:U"; // Define o intervalo da planilha
        ValueRange response = service.spreadsheets().values().get(spreadsheetId, range).execute();
        List<List<Object>> values = response.getValues();

        if (values == null || values.isEmpty()) {
            System.out.println("Nenhum dado encontrado na planilha: " + sheetName);
        } else {
            for (int i = 0; i < values.size(); i++) {
                List<Object> row = values.get(i);

                // Verifica se a linha tem dados suficientes para processar
                if (row.size() >= 20) {
                    String rastreio = row.get(11).toString();  // Coluna 12 (Código de Rastreio)
                    String nome = row.get(19).toString();
                    String enviado = row.size() > 20 ? row.get(20).toString() : "";  // Coluna 22 (Status de Envio)
                    String cep = row.get(5).toString();
                    String sedex = row.get(1).toString();
                    String rastreioUrl = "";

                    // Define URL de rastreio
                    if ("FRENET_SEDEX_03220".equals(sedex)) {
                        rastreioUrl = "<a href='https://rastreio.frenet.com.br/COR'>https://rastreio.frenet.com.br/COR</a>";
                    } else {
                        rastreioUrl = "<a href='https://www.loggi.com/rastreador'>https://www.loggi.com/rastreador</a>";
                    }

                    // Verifica se o código de rastreio existe e se o e-mail já foi enviado
                    if (!rastreio.isEmpty() && (enviado == null || enviado.trim().isEmpty() || "confirmando".equals(enviado))) {
                        String email = row.get(9).toString();  // Coluna 10 (Email)

                        // Valida o e-mail antes de enviar
                        if (email != null && !email.trim().isEmpty() && email.contains("@") && email.contains(".")) {
                            String subject = "Seu código de rastreio";
                            String message = "<p>Olá!</p> " + nome + "<p>😊 Obrigado por escolher o DEPCARE! </p>" +
                                    "Aqui está o seu código de rastreio: " + rastreio +
                                    "<p>⚠ Por favor, verifique se as informações de entrega estão corretas!</p>" +
                                    "<p>CEP informado: <strong>" + cep + "</strong></p>" +
                                    "<p>🖥 Acesse o link de rastreamento: " + rastreioUrl + "</p>" +
                                    "<p>🔑 Insira o código de rastreio.</p>" +
                                    "<p>Entre em contato conosco pelo WhatsApp: <strong>(11)947062617</strong></p>";

                            // Envia o e-mail em formato HTML
                            emailService.EmailService(email, subject, message, true);

                            // Atualiza a célula de status para 'enviado'
                            List<List<Object>> updateValues = Collections.singletonList(
                                    Collections.singletonList("enviado")
                            );
                            ValueRange body = new ValueRange().setValues(updateValues);
                            String updateRange = sheetName + "!U" + (i + 1);
                            service.spreadsheets().values().update(spreadsheetId, updateRange, body)
                                    .setValueInputOption("RAW").execute();

                            System.out.println("E-mail enviado para: " + email + " | Código de Rastreio: " + rastreio);
                        } else {
                            System.out.println("E-mail inválido na linha " + (i + 2) + ":" + email);
                        }
                    } else {
                        String emailParaConfirmacao = row.get(9).toString();
                        if (emailParaConfirmacao != null && !emailParaConfirmacao.trim().isEmpty() &&
                                emailParaConfirmacao.contains("@") && emailParaConfirmacao.contains(".")) {
                            if (row.size() > 20) {
                                if (rastreio.isEmpty() && (enviado == null || enviado.trim().isEmpty())) {
                                    String apresentacao = "😊 Obrigado por escolher o DEPCARE!";
                                    String mensagemConfirmacao = "<p>Olá!</p>" + nome +
                                            "😊 Obrigado por escolher o DEPCARE!⚠Por favor, verifique se as informações de entrega estão corretas!\n Para confirmar o endereço completo e o nome do destinatário:\n" +
                                            "Acesse o link de rastreamento\n" +
                                            "https://www.loggi.com/rastreador/cep/[coluna L na aba MORICARE]\n" +
                                            "🔑Insira o CEP e CPF como senha\n"+"<p>Entre em contato conosco pelo WhatsApp caso queira alterar as informações de entrega. </p>" +
                                            "<p>Nosso WhatsApp: <strong>(11)947062617 ✨</strong></p>"+"Assista este vídeo para entender como usar!\n" +
                                            "Quando utilizar no rosto especialmente, faça um teste de sensibilidade em uma pequena área para verificar a reação da pele"+"LINK : https://www.instagram.com/p/C8P1B7mOGon/ ";

                                    System.out.println("Linha " + (i + 2) + " email de confirmação enviado para " + nome);
                                    emailService.EmailService(emailParaConfirmacao, apresentacao, mensagemConfirmacao, true);

                                    List<List<Object>> updateValues = Collections.singletonList(
                                            Collections.singletonList("confirmando")
                                    );
                                    ValueRange body = new ValueRange().setValues(updateValues);
                                    String updateRange = sheetName + "!W" + (i + 1);
                                    service.spreadsheets().values().update(spreadsheetId, updateRange, body)
                                            .setValueInputOption("RAW").execute();
                                }
                            } else {
                                System.out.println("A linha " + (i + 2) + " não possui a coluna 23.");
                            }
                        }
                    }
                } else {
                    System.out.println("Linha " + (i + 2) + " não possui colunas suficientes.");
                }
            }
        }
        return values;
    }


    public List<List<Object>> getSheetData() throws IOException, GeneralSecurityException, MessagingException {
        // Obter o serviço do Google Sheets
        NetHttpTransport HTTP_TRANSPORT = GoogleNetHttpTransport.newTrustedTransport(); // Criando o transport
        Sheets service = getSheetsService(HTTP_TRANSPORT); // Obter o serviço com o transporte HTTP

        // ID da planilha
        final String spreadsheetId = "1rwr8nh__pkIkzYN1d9iMzLrIodfWEMfLfTNnihLiw2M";

        // Perguntar ao usuário qual planilha ele quer processar
        Scanner scanner = new Scanner(System.in);
        System.out.println("Digite o nome da planilha que deseja processar (ex: yampi, yampi_mulher, etc.):");
        String sheetName = scanner.nextLine(); // Recebe o nome da planilha

        // Processar as planilhas
        return processSheet(sheetName, service, spreadsheetId); // Passa o serviço para processar a planilha
    }

    // Método que executa o agendamento para processar a planilha
    @Scheduled(fixedRate = 300000) // Intervalo de 5 minutos
    public void executarTarefa() {
        System.out.println("Tarefa executada em intervalos fixos de 5 minutos");
        try {
            getSheetData();  // Chama o método para processar e enviar os códigos de rastreio
        } catch (IOException | GeneralSecurityException | MessagingException e) {
            e.printStackTrace();
        }
    }



}
