package com.droideve.apps.nearbystores.activities;

import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.FragmentManager;

import com.droideve.apps.nearbystores.R;
import com.droideve.apps.nearbystores.appconfig.Constances;
import com.droideve.apps.nearbystores.classes.Event;
import com.droideve.apps.nearbystores.classes.Offer;
import com.droideve.apps.nearbystores.classes.Store;
import com.droideve.apps.nearbystores.controllers.events.EventController;
import com.droideve.apps.nearbystores.controllers.stores.OffersController;
import com.droideve.apps.nearbystores.controllers.stores.StoreController;
import com.droideve.apps.nearbystores.fragments.GalleryFragment;

import butterknife.BindView;
import butterknife.ButterKnife;

public class GalleryActivity extends AppCompatActivity {


    @BindView(R.id.toolbar_title)
    TextView APP_TITLE_VIEW;
    @BindView(R.id.toolbar_subtitle)
    TextView APP_DESC_VIEW;
    @BindView(R.id.app_bar)
    Toolbar toolbar;


    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_gallery);
        ButterKnife.bind(this);
        initToolbar();


        int int_id = 0;
        String type = "";
        try {

            int_id = getIntent().getExtras().getInt("int_id", 0);
            type = getIntent().getExtras().getString("type", Constances.ModulesConfig.STORE_MODULE);

            if (int_id == 0)
                finish();

            String iname = "";
            if (type.equals(Constances.ModulesConfig.STORE_MODULE)) {
                Store store = StoreController.findStoreById(int_id);
                iname = store.getName();
            } else if (type.equals(Constances.ModulesConfig.OFFER_MODULE)) {
                Offer offer = OffersController.getOffer(int_id);
                iname = offer.getName();
            } else if (type.equals(Constances.ModulesConfig.EVENT_MODULE)) {
                Event event = EventController.getEvent(int_id);
                iname = event.getName();
            } else {
                finish();
            }

            APP_TITLE_VIEW.setText(getResources().getString(R.string.gallery));
            APP_DESC_VIEW.setText(iname);
            APP_TITLE_VIEW.setVisibility(View.VISIBLE);
            APP_DESC_VIEW.setVisibility(View.VISIBLE);

        } catch (Exception e) {
            e.printStackTrace();
            finish();
            return;
        }

        GalleryFragment frag = new GalleryFragment();
        frag.setParent_width(MainActivity.width);

        Bundle b = new Bundle();
        b.putInt("int_id", int_id);
        b.putString("type", type);
        frag.setArguments(b);
        FragmentManager fm = getSupportFragmentManager();
        fm.beginTransaction().replace(R.id.frame, frag).commit();


    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish();
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
    }

    public void initToolbar() {

        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowTitleEnabled(true);
        getSupportActionBar().setDisplayUseLogoEnabled(false);
        getSupportActionBar().setDisplayShowTitleEnabled(false);
        APP_DESC_VIEW.setVisibility(View.GONE);


        APP_TITLE_VIEW.setText(R.string.gallery);
        APP_DESC_VIEW.setVisibility(View.GONE);

    }


}
