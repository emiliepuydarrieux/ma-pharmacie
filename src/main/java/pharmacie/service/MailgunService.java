package pharmacie.service;

import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class MailgunService {

    private static final Logger logger = LoggerFactory.getLogger(MailgunService.class);

    @Value("${mailgun.api-key:no-key}")
    private String apiKey;

    @Value("${mailgun.domain:no-domain}")
    private String domain;

    @Value("${mailgun.from-email:no-email}")
    private String fromEmail;

   public void sendEmail(String to, String subject, String text) {
    try {
        // 1. On s'assure que la clé n'est pas vide et on enlève les espaces
        if (apiKey == null || apiKey.equals("no-key")) {
            logger.error("LA CLÉ API EST ABSENTE DES VARIABLES RENDER !");
            return;
        }
        
        String cleanKey = apiKey.trim().replaceAll("\\s", "");
        
        // 2. Configuration (Vérifie bien si c'est api.eu.mailgun.net ou api.mailgun.net)
        String testDomain = "sandbox0921dcaf1....mailgun.org"; // <--- METS TON DOMAINE COMPLET ICI
        String testFrom = "postmaster@" + testDomain;
        String mailgunUrl = "https://api.mailgun.net/v3/" + testDomain + "/messages";

        // 3. Encodage Base64 strict
        String auth = "api:" + cleanKey;
        byte[] authBytes = auth.getBytes(StandardCharsets.UTF_8);
        String encodedAuth = Base64.getEncoder().encodeToString(authBytes);

        URL url = new URL(mailgunUrl);
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setRequestMethod("POST");
        conn.setDoOutput(true);
        
        // IMPORTANT : On enlève tout espace ou retour à la ligne du Base64
        conn.setRequestProperty("Authorization", "Basic " + encodedAuth.trim());
        conn.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");

        // 4. Préparation des données
        String postData = "from=" + URLEncoder.encode(testFrom, "UTF-8")
                + "&to=" + URLEncoder.encode("emiliepuydarrieux@gmail.com", "UTF-8")
                + "&subject=" + URLEncoder.encode(subject, "UTF-8")
                + "&text=" + URLEncoder.encode(text, "UTF-8");

        try (OutputStream os = conn.getOutputStream()) {
            os.write(postData.getBytes(StandardCharsets.UTF_8));
        }

        int responseCode = conn.getResponseCode();
        logger.info("TENTATIVE MAILGUN - Code: {} - URL: {}", responseCode, mailgunUrl);

    } catch (Exception e) {
        logger.error("Erreur technique: {}", e.getMessage());
    }
}
    
    public void sendAledStockEmail(String fournisseurEmail, String nomMedicament, int unitesEnStock, int niveauDeReappro) {
        String subject = "Alerte Stock";
        String text = "Le medicament " + nomMedicament + " est bas (" + unitesEnStock + ").";
        sendEmail(fournisseurEmail, subject, text);
    }
}
