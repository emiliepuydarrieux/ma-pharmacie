package pharmacie.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.rest.core.annotation.HandleAfterCreate;
import org.springframework.data.rest.core.annotation.HandleBeforeSave;
import org.springframework.data.rest.core.annotation.RepositoryEventHandler;
import org.springframework.stereotype.Component;

import pharmacie.entity.Medicament;

@Component
@RepositoryEventHandler(Medicament.class)
public class MedicamentEventHandler {

    @Autowired
    private MailgunService mailgunService;

    // 1. Déclenché lors de l'AJOUT d'un nouveau produit
    @HandleAfterCreate
    public void notificationNouveauMedicament(Medicament m) {
        System.out.println("LOG : Nouveau médicament détecté : " + m.getNom());
        envoyerMail("Nouveau produit ajouté", "Le médicament " + m.getNom() + " est maintenant en stock.");
    }

    // 2. Déclenché lors de la MODIFICATION (boutons + et - ou formulaire)
    @HandleBeforeSave
    public void handleMedicamentUpdate(Medicament m) {
        System.out.println("LOG : Mise à jour du stock pour : " + m.getNom() + " (Quantité : " + m.getUnitesEnStock() + ")");

        // Seuil d'alerte : si le stock descend en dessous de 5
        if (m.getUnitesEnStock() < 5) {
            System.out.println("LOG : Alerte stock bas ! Envoi du mail en cours...");
            envoyerMail(
                "Alerte Stock Bas : " + m.getNom(),
                "Attention, il ne reste plus que " + m.getUnitesEnStock() + " unités de " + m.getNom() + " en stock."
            );
        }
    }

    // Petite méthode utilitaire pour éviter de répéter ton adresse mail partout
    private void envoyerMail(String sujet, String corps) {
        try {
            mailgunService.sendEmail(
                "ton-email-perso@gmail.com", // REMPLACE PAR TON VRAI GMAIL VALIDÉ
                "Pharmacie : " + sujet,
                corps
            );
            System.out.println("LOG : Mail envoyé avec succès.");
        } catch (Exception e) {
            System.err.println("LOG ERREUR : Échec de l'envoi du mail : " + e.getMessage());
        }
    }
}