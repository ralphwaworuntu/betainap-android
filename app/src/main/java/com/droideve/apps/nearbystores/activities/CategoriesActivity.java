package com.droideve.apps.nearbystores.activities;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import com.droideve.apps.nearbystores.utils.NSLog;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.droideve.apps.nearbystores.R;
import com.droideve.apps.nearbystores.adapter.lists.CategoriesListAdapter;
import com.droideve.apps.nearbystores.appconfig.Constances;
import com.droideve.apps.nearbystores.classes.Category;
import com.droideve.apps.nearbystores.controllers.categories.CategoryController;
import com.droideve.apps.nearbystores.network.ServiceHandler;
import com.droideve.apps.nearbystores.network.VolleySingleton;
import com.droideve.apps.nearbystores.network.api_request.SimpleRequest;
import com.droideve.apps.nearbystores.parser.api_parser.CategoryParser;
import com.droideve.apps.nearbystores.parser.tags.Tags;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;
import com.droideve.apps.nearbystores.utils.NSLog;

import io.realm.RealmList;

import static com.droideve.apps.nearbystores.appconfig.AppConfig.APP_DEBUG;


public class CategoriesActivity extends AppCompatActivity implements CategoriesListAdapter.ClickListener, SwipeRefreshLayout.OnRefreshListener {

    RecyclerView lisyView;
    CategoriesListAdapter adapter;
    //GET CATEGORIES FROM  DATABASE
    Toolbar toolbar;
    private TextView APP_TITLE_VIEW = null;
    private TextView APP_DESC_VIEW = null;
    private SwipeRefreshLayout mSwipeRefreshLayout;
    private RequestQueue queue;

    @SuppressLint("WrongConstant")
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_generic_list);


        initToolbar();

        APP_TITLE_VIEW.setText(R.string.categories);

        lisyView = findViewById(R.id.list);
        lisyView.setVisibility(View.VISIBLE);

        adapter = new CategoriesListAdapter(this, getData(), false, null, 0, 0, false);


        lisyView.setHasFixedSize(true);
        LinearLayoutManager mLayoutManager = new LinearLayoutManager(this);
        mLayoutManager.setOrientation(LinearLayoutManager.VERTICAL);
        lisyView.setLayoutManager(mLayoutManager);
        lisyView.setAdapter(adapter);

        adapter.setClickListener(this);


        mSwipeRefreshLayout = findViewById(R.id.refresh);

        mSwipeRefreshLayout.setOnRefreshListener(this);


        mSwipeRefreshLayout.setColorSchemeResources(
                R.color.colorPrimary,
                R.color.colorPrimary,
                R.color.colorPrimary,
                R.color.colorPrimary
        );


    }

    public List<Category> getData() {

        List<Category> results = new ArrayList<>();

        RealmList<Category> listCats = CategoryController.list();

        for (Category cat : listCats) {
            if (cat.getNumCat() > 0)
                results.add(cat);
        }


        return results;
    }

    @Override
    public void itemClicked(View view, final int position) {

        Intent intent = new Intent(this, ListStoresActivity.class);
        intent.putExtra("category", adapter.getItem(position).getNumCat());
        
        startActivity(intent);

    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        if (android.R.id.home == item.getItemId()) {
            finish();
            
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();

        

    }


    public void initToolbar() {

        toolbar = findViewById(R.id.app_bar);
        setSupportActionBar(toolbar);
        // getSupportActionBar().setSubtitle("E-shop");
        //getSupportActionBar().setHomeButtonEnabled(true);
        //getSupportActionBar().setHomeAsUpIndicator(R.drawable.ic_menu_white_36dp);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        //getSupportActionBar().setDisplayShowCustomEnabled(true);
        getSupportActionBar().setDisplayShowTitleEnabled(true);
        getSupportActionBar().setDisplayUseLogoEnabled(false);

        getSupportActionBar().setDisplayShowTitleEnabled(false);
        APP_TITLE_VIEW = toolbar.findViewById(R.id.toolbar_title);
        APP_DESC_VIEW = toolbar.findViewById(R.id.toolbar_subtitle);

        APP_DESC_VIEW.setVisibility(View.GONE);

    }


    @Override
    public void onRefresh() {

        load();

    }


    private void load() {

        if (!ServiceHandler.isNetworkAvailable(this)) {
            if (CategoryController.list().size() == 0) {
                //database.insertCats(Loader.parseCategoriesFromAssets(this));
            }
        }

        queue = VolleySingleton.getInstance(this).getRequestQueue();

        SimpleRequest request = new SimpleRequest(Request.Method.GET,
                Constances.API.API_USER_GET_CATEGORY, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {

                mSwipeRefreshLayout.setRefreshing(false);

                try {

                    if (APP_DEBUG) {
                        NSLog.e("catsResponse", response);
                    }

                    JSONObject jsonObject = new JSONObject(response);
                    // NSLog.e("response", jsonObject.toString());
                    final CategoryParser mCategoryParser = new CategoryParser(jsonObject);
                    int success = Integer.parseInt(mCategoryParser.getStringAttr(Tags.SUCCESS));

                    if (success == 1) {

                        RealmList<Category> list = mCategoryParser.getCategories();

                        adapter.clear();

                        for (int i = 0; i < list.size(); i++) {
                            adapter.addItem(list.get(i));
                        }

                        //CategoryController.removeAll();
                        CategoryController.insertCategories(list);

                    }

                } catch (JSONException e) {
                    //send a rapport to support
                    e.printStackTrace();

                }


            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                if (APP_DEBUG) {
                    NSLog.e("ERROR", error.toString());
                }

                mSwipeRefreshLayout.setRefreshing(false);

            }


        }) {


        };

        request.setRetryPolicy(new DefaultRetryPolicy(SimpleRequest.TIME_OUT,
                DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
                DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));

        queue.add(request);


    }


}
