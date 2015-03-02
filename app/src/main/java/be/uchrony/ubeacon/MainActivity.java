package be.uchrony.ubeacon;

import android.app.Dialog;
import android.bluetooth.BluetoothAdapter;
import android.content.Context;
import android.content.DialogInterface;
import android.content.pm.PackageManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.wifi.WifiManager;
import android.os.RemoteException;
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
import android.widget.TextView;
import android.widget.Toast;

import org.altbeacon.beacon.Beacon;
import org.altbeacon.beacon.BeaconConsumer;
import org.altbeacon.beacon.BeaconManager;
import org.altbeacon.beacon.BeaconParser;
import org.altbeacon.beacon.Identifier;
import org.altbeacon.beacon.RangeNotifier;
import org.altbeacon.beacon.Region;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import be.uchrony.ubeacon.metier.Catalogue;
import be.uchrony.ubeacon.metier.Ibeacon;
import be.uchrony.ubeacon.metier.InformationWebService;
import be.uchrony.ubeacon.metier.Produit;
import be.uchrony.ubeacon.metier.UBeacon;
import be.uchrony.ubeacon.metier.WebServiceUchrony;
import retrofit.Callback;
import retrofit.RestAdapter;
import retrofit.RetrofitError;
import retrofit.client.Response;


/**
 * Crée par Abdel le 28/02/2015.
 */

public class MainActivity extends ActionBarActivity implements BeaconConsumer{

    private String TAG_DEBUG = "TAG_DEBUG_MainActivity";
    private List<UBeacon> listeUBeacons = new ArrayList<>();
    private List<Produit> listeProduits = new ArrayList<>();
    private List<Ibeacon> listeIbeaconsScanner = new ArrayList<>();
    private Produit dernierProduit = null;
    private TextView tvInfo;
    private BeaconManager beaconManager;
    private Region regionUchrony;
    private boolean enCoursDeScan = false;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // initialisation des boutons et autre de la fenetre principale
        initElements();
        //TODO Décomenter
        verificationBluetooth();
        verificationConnectionInternet();
        // TODO soucis si on est pas en ligne le temps d'activer le net
        // TODO on ne sera pas en ligne en gros faut attendre le while regle le souci

        while (!estEnLigne());
        recuperationUBeaconWebService();
        // initialisation la configuration des beacons
        initBeacon();
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
        beaconManager.unbind(this);
        if (BluetoothAdapter.getDefaultAdapter().isEnabled()) {
            BluetoothAdapter.getDefaultAdapter().disable();
        }
    }

    @Override
    protected void onRestart() {
        super.onRestart();
        if (enCoursDeScan) {
            startScan();
        }
    }

    /*----------------------------------------------------------------------------------------*/
    /*------------------------------------       ANDROID      --------------------------------*/
    /*----------------------------------------------------------------------------------------*/

    private void lancerPopUp(Produit produit) {
        final Dialog dialog = new Dialog(MainActivity.this);
        LayoutInflater inflater = getLayoutInflater();
        View popUpPub = inflater.inflate(R.layout.popup_pub
                ,(ViewGroup) findViewById(R.id.popup_pub_id));
        WebView webVue = (WebView) popUpPub.findViewById(R.id.web_vue_pub);
        webVue.loadData(produit.getDescription(), "text/html", "utf-8");
        dialog.setTitle(produit.getTitre());
        dialog.setContentView(popUpPub);
        dialog.setOnDismissListener(new DialogInterface.OnDismissListener() {
            @Override
            public void onDismiss(DialogInterface dialog) {
                if (!enCoursDeScan)
                    startScan();
            }
        });
        dialog.show();

    }

    /*----------------------------------------------------------------------------------------*/
    /*----------------------------------  Web Service  ---------------------------------------*/
    /*----------------------------------------------------------------------------------------*/

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
                tvInfo.append(listeUBeacons.toString());
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
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
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
                            tvInfo.append(produits.toString());
                            listeProduits.addAll(produits);
                        }

                        @Override
                        public void failure(RetrofitError retrofitError) {
                            Log.d(TAG_DEBUG, "failure recuperationProduitWebService" + retrofitError.getMessage());
                            MainActivity.this.recuperationProduitWebService();
                        }
                    });
                }
            }
        });
    }

    /*----------------------------------------------------------------------------------------*/
    /*----------------------------------  Wifi Bluetooth  ------------------------------------*/
    /*----------------------------------------------------------------------------------------*/

    private void verificationConnectionInternet() {
        if (!estEnLigne()) {
            Toast.makeText(this, "Vous n\'êtes pas connecté à internet", Toast.LENGTH_LONG).show();
            Toast.makeText(this, "Activation du Wifi", Toast.LENGTH_LONG).show();
            WifiManager wifi = (WifiManager) getSystemService(Context.WIFI_SERVICE);
            wifi.setWifiEnabled(true);
        } else {
            //TODO
        }
    }

    private boolean estEnLigne() {
        ConnectivityManager cm = (ConnectivityManager) getSystemService(CONNECTIVITY_SERVICE);
        NetworkInfo ni = cm.getActiveNetworkInfo();
        return ni != null && ni.isConnectedOrConnecting();
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

    /*----------------------------------------------------------------------------------------*/
    /*----------------------------------    BEACON    ----------------------------------------*/
    /*----------------------------------------------------------------------------------------*/

    @Override
    public void onBeaconServiceConnect() {
        beaconManager.setRangeNotifier(new RangeNotifier() {
            @Override
            public void didRangeBeaconsInRegion(final Collection<Beacon> beacons, Region region) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        listeIbeaconsScanner.clear();
                        if ( beacons.size() > 1) {
                            Log.d(TAG_DEBUG, "entrer Region");
                            for (Beacon unB : beacons) {
                                listeIbeaconsScanner.add(new Ibeacon(unB));
                            }
                            Collections.sort(listeIbeaconsScanner);
                            tvInfo.setText("");
                            for (Ibeacon unB : listeIbeaconsScanner) {
                                tvInfo.append(unB.getId2().toInt() + " - " + unB.getDistance() + "\n");
                            }

                            Produit p = Catalogue.getProduitLierAuUBeacon(listeIbeaconsScanner.get(0)
                                    , listeUBeacons, listeProduits);

                            if (p != null && (dernierProduit == null || !dernierProduit.equals(p))) {
                                stopScan();
                                lancerPopUp(p);
                                dernierProduit = p;
                            }
                        }
                    }
                });
            }
        });
    }

    private void initBeacon() {
        regionUchrony = new Region("regionId", Identifier.parse("F7826DA6-4FA2-4E98-8024-BC5B71E0893E"),null,null);
        beaconManager = BeaconManager.getInstanceForApplication(this);
        beaconManager.getBeaconParsers().add(new BeaconParser().
                setBeaconLayout("m:2-3=0215,i:4-19,i:20-21,i:22-23,p:24-24"));
        beaconManager.setBackgroundBetweenScanPeriod(0);
        beaconManager.setForegroundBetweenScanPeriod(0);
        beaconManager.setBackgroundScanPeriod(1000);
        beaconManager.setForegroundScanPeriod(1000);
        beaconManager.bind(this);
    }

    private void initElements() {
        Button btTest = (Button) findViewById(R.id.bt_test);
        tvInfo = (TextView) findViewById(R.id.tv_info);
        btTest.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startScan();
            }
        });
    }

    private void startScan() {
        try {
            beaconManager.startRangingBeaconsInRegion(regionUchrony);
            enCoursDeScan = true;
            Log.d(TAG_DEBUG,"start scan");
        } catch (RemoteException e) {
            Log.d(TAG_DEBUG,e.getMessage());
        }
    }

    private void stopScan() {
        try {
            beaconManager.stopRangingBeaconsInRegion(regionUchrony);
            enCoursDeScan = false;
            Log.d(TAG_DEBUG,"stop scan");
        } catch (RemoteException e) {
            Log.d(TAG_DEBUG,e.getMessage());
        }
    }
}
