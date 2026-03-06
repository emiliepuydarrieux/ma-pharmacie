package pharmacie.rest;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import lombok.extern.slf4j.Slf4j;
import pharmacie.dao.MedicamentRepository;
import pharmacie.entity.ApprovisionnementService;

@RestController
@RequestMapping(path = "/api/approvisionnement")
@Slf4j
public class ApprovisionnementController {

    @Autowired
    private ApprovisionnementService approvisionnementService;

    @Autowired
    private MedicamentRepository medicamentRepository;

    /**
     * Déclenche la vérification du stock et envoie des alertes 
     * aux fournisseurs pour les médicaments en rupture
     * 
     * @return Un message de confirmation
     */
    @GetMapping(path = "/check-stock")
    public String checkStock() {
        log.info("Vérification du stock et envoi des alertes aux fournisseurs...");
        
        // Récupérer tous les médicaments
        List<pharmacie.entity.Medicament> tousLesMedics = medicamentRepository.findAll();
        
        // Lancer la vérification et l'envoi des emails
        approvisionnementService.simulerApprovisionnement(tousLesMedics);
        
        log.info("Vérification du stock terminée");
        return "Vérification du stock effectuée et emails envoyés aux fournisseurs pour les médicaments en rupture";
    }
}
