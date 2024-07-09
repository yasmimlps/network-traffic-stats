package com.example.teste.view;

import android.Manifest;
import android.annotation.TargetApi;
import android.app.AppOpsManager;
import android.app.usage.NetworkStatsManager;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.view.MenuItem;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.AppCompatImageView;
import androidx.appcompat.widget.Toolbar;

import androidx.appcompat.app.AppCompatActivity;
import android.widget.ImageView;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.example.teste.R;
import com.example.teste.utils.NetworkStatsHelper;
import com.example.teste.utils.PackageManagerHelper;
import com.example.teste.utils.TrafficStatsHelper;


public class StatsActivity extends AppCompatActivity {
    private static final int READ_PHONE_STATE_REQUEST = 37;
    public static final String EXTRA_PACKAGE = "ExtraPackage";

    ImageView ivIcon;
    Toolbar toolbar;

    TextView trafficStatsAllRx;
    TextView trafficStatsAllTx;
    TextView trafficStatsPackageRx;
    TextView trafficStatsPackageTx;

    TextView networkStatsAllRx;
    TextView networkStatsAllTx;
    TextView networkStatsPackageRx;
    TextView networkStatsPackageTx;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_stats);
        toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        ivIcon = findViewById(R.id.avatar);
    }

    @Override
    protected void onStart() {
        super.onStart();
        requestPermissions();
    }

    @Override
    @TargetApi(Build.VERSION_CODES.M)
    protected void onResume() {
        super.onResume();
        if (!hasPermissions()) {
            return;
        }
        initTextViews();
        checkIntent();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void checkIntent() {
        Intent intent = getIntent();
        if (intent == null) {
            return;
        }
        Bundle extras = intent.getExtras();
        if (extras == null) {
            return;
        }
        String packageName = extras.getString(EXTRA_PACKAGE);
        if (packageName == null) {
            return;
        }
        try {
            ivIcon.setImageDrawable(getPackageManager().getApplicationIcon(packageName));
            toolbar.setTitle(getPackageManager().getApplicationLabel(
                    getPackageManager().getApplicationInfo(
                            packageName, PackageManager.GET_META_DATA)));
            toolbar.setSubtitle(packageName + ":" + PackageManagerHelper.getPackageUid(this, packageName));
            setSupportActionBar(toolbar);
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
        if (!PackageManagerHelper.isPackage(StatsActivity.this, packageName)) {
            return;
        }
        fillData(packageName);
    }

    private void requestPermissions() {
        if (!hasPermissionToReadNetworkHistory()) {
            return;
        }
        if (!hasPermissionToReadPhoneStats()) {
            requestPhoneStateStats();
        }
    }

    private boolean hasPermissions() {
        return hasPermissionToReadNetworkHistory() && hasPermissionToReadPhoneStats();
    }

    private void initTextViews() {
        trafficStatsAllRx = findViewById(R.id.traffic_stats_all_rx_value);
        trafficStatsAllTx = findViewById(R.id.traffic_stats_all_tx_value);
        trafficStatsPackageRx = findViewById(R.id.traffic_stats_package_rx_value);
        trafficStatsPackageTx = findViewById(R.id.traffic_stats_package_tx_value);
        networkStatsAllRx = findViewById(R.id.network_stats_all_rx_value);
        networkStatsAllTx = findViewById(R.id.network_stats_all_tx_value);
        networkStatsPackageRx = findViewById(R.id.network_stats_package_rx_value);
        networkStatsPackageTx = findViewById(R.id.network_stats_package_tx_value);
    }

    private void fillData(String packageName) {
        int uid = PackageManagerHelper.getPackageUid(this, packageName);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            NetworkStatsManager networkStatsManager = (NetworkStatsManager) getApplicationContext().getSystemService(Context.NETWORK_STATS_SERVICE);
            NetworkStatsHelper networkStatsHelper = new NetworkStatsHelper(networkStatsManager, uid);
            fillNetworkStatsAll(networkStatsHelper);
            fillNetworkStatsPackage(uid, networkStatsHelper);
        }
        fillTrafficStatsAll();
        fillTrafficStatsPackage(uid);
    }

    @TargetApi(Build.VERSION_CODES.M)
    private void fillNetworkStatsAll(NetworkStatsHelper networkStatsHelper) {
//        long WifiRx = networkStatsHelper.getAllRxBytesWifi() + networkStatsHelper.getAllRxBytesMobile(this);
        long WifiRx = networkStatsHelper.getAllRxBytesWifi();
//        long WifiRx = networkStatsHelper.getAllRxBytesMobile(this);
        networkStatsAllRx.setText((WifiRx/1048576) + " MB");
//        long WifiTx = networkStatsHelper.getAllTxBytesWifi() + networkStatsHelper.getAllTxBytesMobile(this);
        long WifiTx = networkStatsHelper.getAllTxBytesWifi();
//        long WifiTx = networkStatsHelper.getAllRxBytesMobile(this);
        networkStatsAllTx.setText((WifiTx/1048576) + " MB");
    }

    @TargetApi(Build.VERSION_CODES.M)
    private void fillNetworkStatsPackage(int uid, NetworkStatsHelper networkStatsHelper) {
//        long WifiRx = networkStatsHelper.getPackageRxBytesWifi() + networkStatsHelper.getPackageRxBytesMobile(this);
        long WifiRx = networkStatsHelper.getPackageRxBytesWifi();
//        long WifiRx = networkStatsHelper.getPackageRxBytesMobile(this);
        networkStatsPackageRx.setText((WifiRx/1048576) + " MB");
//        long WifiTx = networkStatsHelper.getPackageTxBytesWifi() + networkStatsHelper.getPackageTxBytesMobile(this);
        long WifiTx = networkStatsHelper.getPackageTxBytesWifi();
//        long WifiTx = networkStatsHelper.getPackageTxBytesMobile(this);
        networkStatsPackageTx.setText((WifiTx/1048576) + " MB");
    }

    private void fillTrafficStatsAll() {
        trafficStatsAllRx.setText(TrafficStatsHelper.getAllRxBytes() + " B");
        trafficStatsAllTx.setText(TrafficStatsHelper.getAllTxBytes() + " B");
    }

    private void fillTrafficStatsPackage(int uid) {
        trafficStatsPackageRx.setText(TrafficStatsHelper.getPackageRxBytes(uid) + " B");
        trafficStatsPackageTx.setText(TrafficStatsHelper.getPackageTxBytes(uid) + " B");
    }

    private boolean hasPermissionToReadPhoneStats() {
        return ContextCompat.checkSelfPermission(this, Manifest.permission.READ_PHONE_STATE) == PackageManager.PERMISSION_GRANTED;
    }

    private void requestPhoneStateStats() {
        ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.READ_PHONE_STATE}, READ_PHONE_STATE_REQUEST);
    }

    private boolean hasPermissionToReadNetworkHistory() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {
            return true;
        }
        final AppOpsManager appOps = (AppOpsManager) getSystemService(Context.APP_OPS_SERVICE);
        int mode = appOps.checkOpNoThrow(AppOpsManager.OPSTR_GET_USAGE_STATS,
                android.os.Process.myUid(), getPackageName());
        if (mode == AppOpsManager.MODE_ALLOWED) {
            return true;
        }
        appOps.startWatchingMode(AppOpsManager.OPSTR_GET_USAGE_STATS,
                getApplicationContext().getPackageName(),
                new AppOpsManager.OnOpChangedListener() {
                    @Override
                    @TargetApi(Build.VERSION_CODES.M)
                    public void onOpChanged(String op, String packageName) {
                        int mode = appOps.checkOpNoThrow(AppOpsManager.OPSTR_GET_USAGE_STATS,
                                android.os.Process.myUid(), getPackageName());
                        if (mode != AppOpsManager.MODE_ALLOWED) {
                            return;
                        }
                        appOps.stopWatchingMode(this);
                        Intent intent = new Intent(StatsActivity.this, StatsActivity.class);
                        if (getIntent().getExtras() != null) {
                            intent.putExtras(getIntent().getExtras());
                        }
                        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
                        getApplicationContext().startActivity(intent);
                    }
                });
        requestReadNetworkHistoryAccess();
        return false;
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    private void requestReadNetworkHistoryAccess() {
        Intent intent = new Intent(Settings.ACTION_USAGE_ACCESS_SETTINGS);
        startActivity(intent);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == READ_PHONE_STATE_REQUEST) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // Permission granted
                onResume();
            } else {
                // Permission denied, handle accordingly
            }
        }
    }
}
