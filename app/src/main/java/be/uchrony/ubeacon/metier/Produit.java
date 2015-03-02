package be.uchrony.ubeacon.metier;

/**
 * Crée par Abdel le 28/02/2015.
 */
public class Produit {

    private String product_id;
    private String product_title;
    private String product_description;
    private String ibeacon_id;

    public Produit(String product_id, String product_title, String product_description
                                                            ,String ibeacon_id) {
        this.product_id = product_id;
        this.product_title = product_title;
        this.product_description = product_description;
        this.ibeacon_id = ibeacon_id;
    }

    public String getId() {
        return product_id;
    }

    public String getDescription() {
        return product_description;
    }

    public String getTitre() {
        return product_title;
    }

    public String getUBeaconId() {
        return ibeacon_id;
    }

    @Override
    public String toString() {
        return "Produit{ " +
                "Id =  " + product_id  +
                ", Titre = '" + product_title  +
                ", Description=' " + product_description +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Produit produit = (Produit) o;

        return product_id.equals(produit.product_id);

    }

    @Override
    public int hashCode() {
        return product_id.hashCode();
    }
}
