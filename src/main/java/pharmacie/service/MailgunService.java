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

    // Vérifie bien que sur Render la variable s'appelle MAILGUN_API_KEY
    @Value("${mailgun.api-key}")
    private String apiKey;

    @Value("${mailgun.domain}")
    private String domain;

    @Value("${mailgun.from-email}")
    private String fromEmail;

    public void sendEmail(String to, String subject, String text) {
        try {
            // Construction de l'URL propre
            String mailgunUrl = "https://api.mailgun.net/v3/" + domain.trim() + "/messages";

            String postData = "from=" + URLEncoder.encode(fromEmail, "UTF-8")
                    + "&to=" + URLEncoder.encode(to, "UTF-8")
                    + "&subject=" + URLEncoder.encode(subject, "UTF-8")
                    + "&text=" + URLEncoder.encode(text, "UTF-8");

            sendViaHttp(mailgunUrl, postData);

        } catch (Exception e) {
            logger.error("Erreur lors de la préparation de l'email pour {}", to, e);
        }
    }

    private void sendViaHttp(String mailgunUrl, String postData) {
    try {
        // 1. Sécurité : si la clé est nulle, on arrête tout proprement
        if (apiKey == null || apiKey.isEmpty()) {
            logger.error("ERREUR : La clé API Mailgun est vide ! Vérifiez vos variables d'environnement sur Render.");
            return;
        }

        URL url = new URL(mailgunUrl);
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();

        // 2. Nettoyage radical de la clé
        String cleanKey = apiKey.trim();
        
        // 3. Construction de l'auth Basic
        String auth = "api:" + cleanKey;
        String encodedAuth = Base64.getEncoder().encodeToString(auth.getBytes(StandardCharsets.UTF_8));
        
        conn.setRequestMethod("POST");
        conn.setDoOutput(true);
        conn.setRequestProperty("Authorization", "Basic " + encodedAuth);
        conn.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");

        try (OutputStream os = conn.getOutputStream()) {
            os.write(postData.getBytes(StandardCharsets.UTF_8));
        }

        int responseCode = conn.getResponseCode();
        if (responseCode >= 200 && responseCode < 300) {
            logger.info("Email envoyé ! Code: {}", responseCode);
        } else {
            logger.warn("Mailgun a refusé l'envoi. Code: {}", responseCode);
        }

    } catch (Exception e) {
        logger.error("Erreur lors de l'envoi Mailgun : {}", e.getMessage());
    }
}

    public void sendAledStockEmail(String fournisseurEmail, String nomMedicament, 
                                    int unitesEnStock, int niveauDeReappro) {
        String subject = "Alerte Stock - Réapprovisionnement nécessaire";
        
        String text = String.format(
                "Bonjour,\n\n" +
                "Le médicament '%s' nécessite un réapprovisionnement immédiat.\n\n" +
                "Détails :\n" +
                "- Unités en stock : %d\n" +
                "- Seuil d'alerte : %d\n\n" +
                "Merci de nous envoyer un devis.\n\n" +
                "Cordialement,\n" +
                "Pharmacie Centrale",
                nomMedicament, unitesEnStock, niveauDeReappro
        );
        
        sendEmail(fournisseurEmail, subject, text);
    }
    logger.info("Tentative avec le domaine : '{}'", domain);
logger.info("La clé commence par : '{}' et finit par : '{}'", 
    apiKey.substring(0, Math.min(apiKey.length(), 4)), 
    apiKey.substring(Math.max(0, apiKey.length() - 2)));
}
