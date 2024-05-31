package com.droideve.apps.nearbystores.customView;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;

import androidx.appcompat.widget.Toolbar;
import androidx.core.content.ContextCompat;

import com.droideve.apps.nearbystores.R;
import com.droideve.apps.nearbystores.activities.GlobalActivity;
import com.droideve.apps.nearbystores.utils.NSToast;

import im.delight.android.webview.AdvancedWebView;

public class SimpleWebViewActivity extends GlobalActivity implements AdvancedWebView.Listener {

    private AdvancedWebView mWebView;
    private String link;
    private Toolbar toolbar;
    private TextView toolbar_title;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.advanced_web_view);


        initToolbar();

        mWebView = findViewById(R.id.webview);
        mWebView.setListener(this, this);

        if (getIntent().hasExtra("url")) {
            mWebView.loadUrl(getIntent().getExtras().getString("url"));
        } else {
            NSToast.show(( getString(R.string.error_try_later)));
            finish();
        }

        if (getIntent().hasExtra("title")) {
            toolbar_title.setText(getIntent().getExtras().getString("title"));
        }
    }


    public void initToolbar() {
        toolbar = findViewById(R.id.app_bar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowTitleEnabled(true);
        getSupportActionBar().setDisplayUseLogoEnabled(false);
        getSupportActionBar().setDisplayShowTitleEnabled(false);

        toolbar_title = toolbar.findViewById(R.id.toolbar_title);
        toolbar_title.setTextAlignment(View.TEXT_ALIGNMENT_CENTER);
        toolbar_title.setGravity(View.TEXT_ALIGNMENT_CENTER);
        toolbar_title.setTextColor(ContextCompat.getColor(this, R.color.colorAccent));
        toolbar_title.setText("--");


        toolbar.findViewById(R.id.toolbar_subtitle).setVisibility(View.GONE);


    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        if (android.R.id.home == item.getItemId()) {
            finish();
            
        }

        return super.onOptionsItemSelected(item);
    }


    @SuppressLint("NewApi")
    @Override
    protected void onResume() {
        super.onResume();
        mWebView.onResume();
        // ...
    }

    @SuppressLint("NewApi")
    @Override
    protected void onPause() {
        mWebView.onPause();
        // ...
        super.onPause();
    }

    @Override
    protected void onDestroy() {
        mWebView.onDestroy();
        // ...
        super.onDestroy();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent intent) {
        super.onActivityResult(requestCode, resultCode, intent);
        mWebView.onActivityResult(requestCode, resultCode, intent);
        // ...
    }

    @Override
    public void onBackPressed() {
        if (!mWebView.onBackPressed()) {
            return;
        }
        // ...
        super.onBackPressed();



    }

    @Override
    public void onPageStarted(String url, Bitmap favicon) {



    }


    @Override
    public void onPageFinished(String url) {

        //show loading progress
        toolbar.findViewById(R.id.progressLayout).setVisibility(View.GONE);

    }

    @Override
    public void onPageError(int errorCode, String description, String failingUrl) {
    }

    @Override
    public void onDownloadRequested(String url, String suggestedFilename, String mimeType, long contentLength, String contentDisposition, String userAgent) {
    }

    @Override
    public void onExternalPageRequest(String url) {
    }
}
