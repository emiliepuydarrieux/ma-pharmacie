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
        if ("no-key".equals(apiKey) || "no-domain".equals(domain)) {
            logger.error("Mailgun non configuré. Vérifiez les variables sur Render.");
            return;
        }

        try {
            String mailgunUrl = "https://api.mailgun.net/v3/" + domain.trim() + "/messages";
            URL url = new URL(mailgunUrl);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();

            String auth = "api:" + apiKey.trim();
            String encodedAuth = Base64.getEncoder().encodeToString(auth.getBytes(StandardCharsets.UTF_8));

            conn.setRequestMethod("POST");
            conn.setDoOutput(true);
            conn.setRequestProperty("Authorization", "Basic " + encodedAuth);
            conn.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");

            String postData = "from=" + URLEncoder.encode(fromEmail, "UTF-8")
                    + "&to=" + URLEncoder.encode(to, "UTF-8")
                    + "&subject=" + URLEncoder.encode(subject, "UTF-8")
                    + "&text=" + URLEncoder.encode(text, "UTF-8");

            try (OutputStream os = conn.getOutputStream()) {
                os.write(postData.getBytes(StandardCharsets.UTF_8));
            }

            logger.info("Mailgun Response Code: {}", conn.getResponseCode());
        } catch (Exception e) {
            logger.error("Erreur Mailgun: {}", e.getMessage());
        }
    }

    public void sendAledStockEmail(String fournisseurEmail, String nomMedicament, int unitesEnStock, int niveauDeReappro) {
        String subject = "Alerte Stock";
        String text = "Le medicament " + nomMedicament + " est bas (" + unitesEnStock + ").";
        sendEmail(fournisseurEmail, subject, text);
    }
}
