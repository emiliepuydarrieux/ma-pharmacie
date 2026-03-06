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

    @Value("${mailgun.api-key}")
    private String apiKey;

    @Value("${mailgun.domain}")
    private String domain;

    @Value("${mailgun.from-email}")
    private String fromEmail;

    public void sendEmail(String to, String subject, String text) {
        try {
            String mailgunUrl = "https://api.mailgun.net/v3/" + domain + "/messages";

            // Encodage des paramètres pour le format x-www-form-urlencoded
            String postData = "from=" + URLEncoder.encode(fromEmail, "UTF-8")
                    + "&to=" + URLEncoder.encode(to, "UTF-8")
                    + "&subject=" + URLEncoder.encode(subject, "UTF-8")
                    + "&text=" + URLEncoder.encode(text, "UTF-8");

            // On appelle la nouvelle méthode qui n'utilise PLUS curl
            sendViaHttp(mailgunUrl, postData);

        } catch (Exception e) {
            logger.error("Erreur lors de la préparation de l'email pour {}", to, e);
        }
    }

    private void sendViaHttp(String mailgunUrl, String postData) {
        try {
            URL url = new URL(mailgunUrl);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();

            // Configuration de l'authentification "Basic" (api:CLE_API)
            String auth = "api:" + apiKey.trim(); // Le .trim() enlève les espaces invisibles
String encodedAuth = Base64.getEncoder().encodeToString(auth.getBytes(StandardCharsets.UTF_8)).replaceAll("\\s", "");
            
            conn.setRequestMethod("POST");
            conn.setDoOutput(true);
            conn.setRequestProperty("Authorization", "Basic " + encodedAuth);
            conn.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");

            // Envoi des données
            try (OutputStream os = conn.getOutputStream()) {
                byte[] input = postData.getBytes(StandardCharsets.UTF_8);
                os.write(input, 0, input.length);
            }

            int responseCode = conn.getResponseCode();
            if (responseCode >= 200 && responseCode < 300) {
                logger.info("Email envoyé avec succès via l'API Mailgun (Code: {})", responseCode);
            } else {
                logger.warn("Mailgun a refusé l'envoi. Code HTTP: {}", responseCode);
            }

        } catch (Exception e) {
            logger.error("Erreur fatale lors de l'envoi HTTP vers Mailgun", e);
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
}
