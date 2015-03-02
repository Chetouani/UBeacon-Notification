package be.uchrony.ubeacon.metier;

import java.util.List;

import retrofit.Callback;
import retrofit.client.Response;
import retrofit.http.Field;
import retrofit.http.FormUrlEncoded;
import retrofit.http.POST;

/**
 * Crée par Abdel le 28/02/2015.
 */
public interface WebServiceUchrony {

    @FormUrlEncoded
    @POST(InformationWebService.POST_BEACONS)
    public void getUbeacons(@Field("username") String username
                            ,@Field("password") String password
                            , Callback<List<UBeacon>> reponse);

    @FormUrlEncoded
    @POST(InformationWebService.POST_PRODUIT)
    public void getProduits(@Field("username") String username
                                    ,@Field("password") String password
                                    ,@Field("ibeacon_id") String ibeaconID
                                    , Callback<List<Produit>> reponse);

    @FormUrlEncoded
    @POST(InformationWebService.POST_NBR_VISITE)
    public void setNbrVisite(@Field("username") String username
                                    ,@Field("password") String password
                                    ,@Field("product_id") String produitId
                                    ,@Field("type_os") String typeOs
                                    ,Callback<Response> rep);

    @FormUrlEncoded
    @POST(InformationWebService.POST_NIV_BATTERIE)
    public void setNiveauBatterie(@Field("username") String username
                                    ,@Field("password") String password
                                    ,@Field("ibeacon_id") String UBeaconId
                                    ,@Field("level_battery") String niveauBatterie
                                    ,Callback<Response> rep);

}
