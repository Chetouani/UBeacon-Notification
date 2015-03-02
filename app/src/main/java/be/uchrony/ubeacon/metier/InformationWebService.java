package be.uchrony.ubeacon.metier;

/**
 * Crée par Abdel le 28/02/2015.
 */
public class InformationWebService {

    public final static String NOM_DOMAINE = "http://ubeacondemo.uchrony.net";
    public final static String USERNAME = "ibeacon";
    public final static String PASSWORD = "18Eac0N";
    public final static String POST_PRODUIT = "/endpoint/ibeacon/rest_getproductByIbeaconId";
    public final static String POST_BEACONS = "/endpoint/ibeacon/rest_getibeacons";
    public final static String POST_NBR_VISITE = "/endpoint/ibeacon/rest_setview";
    public final static String POST_NIV_BATTERIE = "/endpoint/ibeacon/rest_setbattery";
}
