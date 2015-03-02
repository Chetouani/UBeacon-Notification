package be.uchrony.ubeacon.metier;

import android.util.Log;

import java.util.List;

/**
 * Crée par Abdel le 28/02/2015.
 */
public class Catalogue {

    private static String TAG_DEBUG = "TAG_DEBUG_Catalogue";

    public static Produit getProduitLierAuUBeacon(Ibeacon ibeacon,List<UBeacon> listeUbeacons
                                                    ,List<Produit> listeProduits) {
        boolean trouverBeacon = false;
        boolean trouverProduit = false;
        Produit produit = null;
        int i = 0;
        int j = 0;
        String uuid = null;

        while (!trouverBeacon && i < listeUbeacons.size()) {
            if (estEgal(listeUbeacons.get(i),ibeacon)) {
                Log.d(TAG_DEBUG, "beacon egale" + listeUbeacons.get(i).getUbeaconTitre());
                trouverBeacon = true;
                uuid = listeUbeacons.get(i).getUbeaconID();
            } else {
                i++;
            }
        }

        if (trouverBeacon) {
            while (!trouverProduit && j < listeProduits.size()) {
                if (listeProduits.get(j).getUBeaconId().equals(uuid)) {
                    trouverProduit = true;
                    produit = listeProduits.get(j);
                } else {
                    j++;
                }
            }
        }

        return produit;
    }


    private static boolean estEgal(UBeacon ubeacon,Ibeacon ibeacon) {
        return ubeacon.getUbeaconUuid().toUpperCase().equals(ibeacon.getId1().toUuidString().toUpperCase())
                && Integer.parseInt(ubeacon.getUbeaconMajor()) == ibeacon.getId2().toInt()
                && Integer.parseInt(ubeacon.getUbeaconMinor()) == ibeacon.getId3().toInt();
    }
}
