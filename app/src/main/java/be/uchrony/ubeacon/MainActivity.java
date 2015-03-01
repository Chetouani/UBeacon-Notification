package be.uchrony.ubeacon;

import android.app.Dialog;
import android.bluetooth.BluetoothAdapter;
import android.content.Context;
import android.content.pm.PackageManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.wifi.WifiManager;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebView;
import android.widget.Button;
import android.widget.PopupWindow;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;

import be.uchrony.ubeacon.metier.InformationWebService;
import be.uchrony.ubeacon.metier.Produit;
import be.uchrony.ubeacon.metier.UBeacon;
import be.uchrony.ubeacon.metier.WebServiceUchrony;
import retrofit.Callback;
import retrofit.RestAdapter;
import retrofit.RetrofitError;
import retrofit.client.Response;


public class MainActivity extends ActionBarActivity {

    private String TAG_DEBUG = "TAG_DEBUG_MainActivity";
    private List<UBeacon> listeUBeacons = new ArrayList<>();
    private List<Produit> listeProduits = new ArrayList<>();
    private TextView tvInfo;
    private int cpt=0;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Button btTest = (Button) findViewById(R.id.bt_test);
        tvInfo = (TextView) findViewById(R.id.tv_info);
        btTest.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                lancerPopUp();
            }
        });

    }

    @Override
    protected void onStart() {
        super.onStart();
        //TODO Decomenter
        //   verificationBluetooth();
        //  verificationConnectionInternet();
        if (estEnLigne())
            recuperationUBeaconWebService();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (BluetoothAdapter.getDefaultAdapter().isEnabled()) {
            BluetoothAdapter.getDefaultAdapter().disable();
        }
    }

    /*----------------------------------------------------------------------------------------*/
    /*----------------------------------------------------------------------------------------*/
    /*----------------------------------------------------------------------------------------*/

    private void lancerPopUp() {
        final Dialog dialog = new Dialog(MainActivity.this);
        LayoutInflater inflater = getLayoutInflater();
        View popUpPub = inflater.inflate(R.layout.popup_pub
                ,(ViewGroup) findViewById(R.id.popup_pub_id));
        WebView webVue = (WebView) popUpPub.findViewById(R.id.web_vue_pub);
        webVue.loadData(listeProduits.get(cpt).getDescription(), "text/html", "utf-8");
        dialog.setTitle(listeProduits.get(cpt++).getTitre());
        dialog.setContentView(popUpPub);
        dialog.show();
    }

    private void miseAJourNbrVisite() {
        RestAdapter ad = new RestAdapter.Builder()
                .setEndpoint(InformationWebService.NOM_DOMAINE)
                .build();

        WebServiceUchrony wsu = ad.create(WebServiceUchrony.class);

        wsu.setNbrVisite(InformationWebService.USERNAME
                , InformationWebService.PASSWORD
                , "198", "ANDROID", new Callback<Response>() {
            @Override
            public void success(Response response, Response response2) {
                Log.d(TAG_DEBUG, "success miseAJourNbrVisite" );
            }

            @Override
            public void failure(RetrofitError retrofitError) {
                Log.d(TAG_DEBUG, "failure miseAJourNbrVisite");
                MainActivity.this.miseAJourNbrVisite();
            }
        });
    }

    private void recuperationUBeaconWebService() {
        RestAdapter ad = new RestAdapter.Builder()
                .setEndpoint(InformationWebService.NOM_DOMAINE)
                .build();

        WebServiceUchrony wsu = ad.create(WebServiceUchrony.class);

        wsu.getUbeacons(InformationWebService.USERNAME
                , InformationWebService.PASSWORD
                , new Callback<List<UBeacon>>() {
            @Override
            public void success(List<UBeacon> ubeacons, Response response) {
                Log.d(TAG_DEBUG, "success recuperationUBeaconWebService" + response.getStatus());
                listeUBeacons = ubeacons;
                recuperationProduitWebService();
            }

            @Override
            public void failure(RetrofitError retrofitError) {
                Log.d(TAG_DEBUG, "failure recuperationUBeaconWebService");
                MainActivity.this.recuperationUBeaconWebService();
            }
        });

    }

    private void recuperationProduitWebService() {
        RestAdapter ad = new RestAdapter.Builder()
                .setEndpoint(InformationWebService.NOM_DOMAINE)
                .build();

        WebServiceUchrony wsu = ad.create(WebServiceUchrony.class);

        for (UBeacon unBeacon : listeUBeacons) {

            wsu.getProduits(InformationWebService.USERNAME
                    , InformationWebService.PASSWORD
                    , unBeacon.getUbeaconID()
                    , new Callback<List<Produit>>() {
                @Override
                public void success(List<Produit> produits, Response response) {
                    Log.d(TAG_DEBUG, "success recuperationProduitWebService " + response.getReason());
                    listeProduits.addAll(produits);
                    tvInfo.setText(listeProduits.toString());
                }

                @Override
                public void failure(RetrofitError retrofitError) {
                    Log.d(TAG_DEBUG, "failure recuperationProduitWebService" + retrofitError.getMessage());
                    MainActivity.this.recuperationProduitWebService();
                }
            });
        }

    }

    private void verificationConnectionInternet() {
        if (!estEnLigne()) {
            Toast.makeText(this, "Vous n\'êtes pas connecté à internet", Toast.LENGTH_LONG).show();
            WifiManager wifi = (WifiManager) getSystemService(Context.WIFI_SERVICE);
            wifi.setWifiEnabled(true);
        } else {
            //TODO
        }
    }

    private boolean estEnLigne() {
        ConnectivityManager cm = (ConnectivityManager) getSystemService(CONNECTIVITY_SERVICE);
        NetworkInfo ni = cm.getActiveNetworkInfo();
        if (ni != null && ni.isConnectedOrConnecting()) {
            return true;
        } else {
            return false;
        }
    }

    private void verificationBluetooth() {
        // Verifie que on posséde le bluetooth LE
        if (!getPackageManager().hasSystemFeature(PackageManager.FEATURE_BLUETOOTH_LE)) {
            Toast.makeText(this, "Votre gsm n'a pas le bluetooth LE", Toast.LENGTH_SHORT).show();
            //TODO quitter l'application si pas de BLE
        }
        // Verifie que le bluetooth est activé
        if (!BluetoothAdapter.getDefaultAdapter().isEnabled()) {
            if (BluetoothAdapter.getDefaultAdapter().enable())
                Toast.makeText(this, "Le bluetooth à été activer", Toast.LENGTH_SHORT).show();
            else
                Toast.makeText(this, "Echec d'activation du bluetooth", Toast.LENGTH_SHORT).show();
        }
    }

}
