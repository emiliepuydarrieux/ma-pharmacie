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

    // Ces valeurs seront lues depuis Render
    @Value("${mailgun.api-key:no-key}")
    private String apiKey;

    @Value("${mailgun.domain:no-domain}")
    private String domain;

    @Value("${mailgun.from-email:no-email}")
    private String fromEmail;

    public void sendEmail(String to, String subject, String text) {
        try {
            // 1. Nettoyage de la clé et du domaine
            String cleanKey = apiKey.trim().replaceAll("\\s", "");
            String cleanDomain = domain.trim().replaceAll("\\s", "");
            
            // 2. URL de l'API (On garde l'URL EU qui semble être la tienne)
            String mailgunUrl = "https://api.eu.mailgun.net/v3/" + cleanDomain + "/messages";

            // 3. Préparation de l'authentification
            String auth = "api:" + cleanKey;
            String encodedAuth = Base64.getEncoder().encodeToString(auth.getBytes(StandardCharsets.UTF_8));

            URL url = new URL(mailgunUrl);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("POST");
            conn.setDoOutput(true);
            conn.setRequestProperty("Authorization", "Basic " + encodedAuth);
            conn.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");

            // 4. Construction du message
            String postData = "from=" + URLEncoder.encode(fromEmail, "UTF-8")
                    + "&to=" + URLEncoder.encode(to, "UTF-8")
                    + "&subject=" + URLEncoder.encode(subject, "UTF-8")
                    + "&text=" + URLEncoder.encode(text, "UTF-8");

            try (OutputStream os = conn.getOutputStream()) {
                os.write(postData.getBytes(StandardCharsets.UTF_8));
            }

            // 5. Vérification du résultat
            int responseCode = conn.getResponseCode();
            if (responseCode >= 200 && responseCode < 300) {
                logger.info("✅ SUCCÈS : Email envoyé ! (Code: {})", responseCode);
            } else {
                logger.error("❌ ÉCHEC : Mailgun a renvoyé le code {}. Vérifiez la région (EU/US) et la clé.", responseCode);
            }

        } catch (Exception e) {
            logger.error("Erreur technique lors de l'envoi : {}", e.getMessage());
        }
    }

    // Cette méthode est celle appelée par ton contrôleur ou ton gestionnaire de stock
    public void sendAledStockEmail(String fournisseurEmail, String nomMedicament, int unitesEnStock, int niveauDeReappro) {
        String subject = "Alerte Stock - " + nomMedicament;
        String text = String.format(
            "Bonjour,\n\nLe stock du médicament '%s' est bas (%d unités restantes).\nSeuil d'alerte : %d.\n\nMerci de prévoir un réapprovisionnement.",
            nomMedicament, unitesEnStock, niveauDeReappro
        );
        
        // On envoie à l'adresse du fournisseur (qui doit être ton mail validé pour l'instant)
        sendEmail(fournisseurEmail, subject, text);
    }
}
