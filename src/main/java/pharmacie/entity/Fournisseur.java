package pharmacie.entity;

import java.util.ArrayList;
import java.util.List;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.JoinTable;
import jakarta.persistence.ManyToMany;
import lombok.Getter;
import lombok.Setter;

@Entity
@Getter @Setter
public class Fournisseur {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;
    private String nom;
    private String email;

    @Setter @Getter
    @ManyToMany
    @JoinTable(
        name = "fournisseur_categorie", // Nom de la table de jointure
        joinColumns = @JoinColumn(name = "fournisseur_id"),
        inverseJoinColumns = @JoinColumn(name = "categorie_code")
    )
    private List<Categorie> categories = new ArrayList<>();

    public Fournisseur() {}

    public Fournisseur(String nom, String email) {
        this.nom = nom;
        this.email = email;
    }

    public String getEmail() {
        return email;
    };

}