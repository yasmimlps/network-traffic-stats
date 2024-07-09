package com.example.teste.view;

import android.Manifest;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Bundle;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.teste.R;
import com.example.teste.model.Package;
import com.example.teste.utils.OnPackageClickListener;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;



/**
 * Created by Robert Zagórski on 2016-12-14
 */

public class ListActivity extends AppCompatActivity implements OnPackageClickListener {

    RecyclerView recyclerView;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_list);
        setSupportActionBar((Toolbar) findViewById(R.id.toolbar));
        List<Package> packageList = getPackagesData();
        recyclerView = (RecyclerView) findViewById(R.id.recycler_view);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(new PackageAdapter(packageList, this));
    }

    private List<Package> getPackagesData() {
        PackageManager packageManager = getPackageManager();
        List<PackageInfo> packageInfoList = packageManager.getInstalledPackages(PackageManager.GET_META_DATA);
        Collections.sort(packageInfoList, new Comparator<PackageInfo>() {
            @Override
            public int compare(PackageInfo o1, PackageInfo o2) {
                return (int) ((o2.lastUpdateTime - o1.lastUpdateTime) / 10);
            }
        });
        List<Package> packageList = new ArrayList<>(packageInfoList.size());
        for (PackageInfo packageInfo : packageInfoList) {
//            if (packageManager.checkPermission(Manifest.permission.INTERNET,
//                    packageInfo.packageName) == PackageManager.PERMISSION_DENIED) {
//                continue;
//            }
            Package packageItem = new Package();
            packageItem.setVersion(packageInfo.versionName);
            packageItem.setPackageName(packageInfo.packageName);
            packageList.add(packageItem);
            ApplicationInfo ai = null;
            try {
                ai = packageManager.getApplicationInfo(packageInfo.packageName, PackageManager.GET_META_DATA);
            } catch (PackageManager.NameNotFoundException e) {
                e.printStackTrace();
            }
            if (ai == null) {
                continue;
            }
            CharSequence appName = packageManager.getApplicationLabel(ai);
            if (appName != null) {
                packageItem.setName(appName.toString());
            }
        }
        return packageList;
    }

    @Override
    public void onClick(Package packageItem) {
        Intent intent = new Intent(ListActivity.this, StatsActivity.class);
        intent.putExtra(StatsActivity.EXTRA_PACKAGE, packageItem.getPackageName());
        startActivity(intent);
    }

}
