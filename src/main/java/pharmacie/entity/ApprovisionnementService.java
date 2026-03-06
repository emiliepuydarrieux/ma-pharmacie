package pharmacie.entity;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import pharmacie.service.MailgunService;

@Service
public class ApprovisionnementService {

    @Autowired
    private MailgunService mailgunService;

    /**
     * Vérifie le stock de tous les médicaments et envoie une alerte 
     * aux fournisseurs si le stock tombe en dessous du niveau de réapprovisionnement
     * @param tousLesMedics Liste de tous les médicaments à vérifier
     */
    public void simulerApprovisionnement(List<Medicament> tousLesMedics) {
        
        for (Medicament m : tousLesMedics) {
            // 1. Condition de réappro
            if (m.getUnitesEnStock() < m.getNiveauDeReappro()) {
                
                System.out.println("--- ALERTE STOCK : " + m.getNom() + " ---");
                
                // 2. Trouver les fournisseurs via la catégorie
                for (Fournisseur f : m.getCategorie().getFournisseurs()) {
                    
                    // 3. Envoyer le mail via Mailgun
                    System.out.println("Envoi d'un email à : " + f.getEmail());
                    
                    try {
                        mailgunService.sendAledStockEmail(
                            f.getEmail(),
                            m.getNom(),
                            m.getUnitesEnStock(),
                            m.getNiveauDeReappro()
                        );
                        System.out.println("✓ Email envoyé avec succès à : " + f.getEmail());
                    } catch (Exception e) {
                        System.out.println("✗ Erreur lors de l'envoi d'email à : " + f.getEmail());
                        e.printStackTrace();
                    }
                }
            }
        }
    }
}




/*package pharmacie.entity;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import pharmacie.dao.MedicamentRepository;

@Service
public class ApprovisionnementService {

    @Autowired
    private MedicamentRepository repo; // Pour lire la base de données
    
    @Autowired
    private JavaMailSender emailOutil; // Pour envoyer le mail

    public void verifierEtCommander() {
        
        // 1. On récupère TOUS les médicaments
        List<Medicament> tousLesMedocs = repo.findAll();

        // 2. On regarde qui est en rupture
        for (Medicament m : tousLesMedocs) {
            
            if (m.getUnitesEnStock() < m.getNiveauDeReappro()) {
                
                // 3. On trouve qui peut nous vendre ce médicament
                // (On va chercher les fournisseurs de la catégorie du médicament)
                List<Fournisseur> lesVendeurs = m.getCategorie().getFournisseurs();

                for (Fournisseur f : lesVendeurs) {
                    // 4. On envoie un mail au vendeur
                    envoyerUnMailSimple(f.getEmail(), m.getNom());
                }
            }
        }
    }

    // Une petite méthode pour envoyer le mail
    private void envoyerUnMailSimple(String adresse, String nomMedic) {
        SimpleMailMessage mail = new SimpleMailMessage();
        mail.setTo(adresse);
        mail.setSubject("Besoin de stock !");
        mail.setText("Bonjour, on a besoin de : " + nomMedic);
        emailOutil.send(mail);
    }
}
    */