package com.droideve.apps.nearbystores.parser.api_parser;


import android.util.Log;
import com.droideve.apps.nearbystores.utils.NSLog;

import com.droideve.apps.nearbystores.appconfig.AppConfig;
import com.droideve.apps.nearbystores.classes.Banner;
import com.droideve.apps.nearbystores.classes.Images;
import com.droideve.apps.nearbystores.parser.Parser;
import com.droideve.apps.nearbystores.parser.tags.Tags;

import org.json.JSONException;
import org.json.JSONObject;

import io.realm.RealmList;


public class BannerParser extends Parser {

    public BannerParser(JSONObject json) {
        super(json);
    }

    public BannerParser(Parser parser) {
        this.json = parser.json;
    }

    public RealmList<Banner> getBanners() {

        RealmList<Banner> list = new RealmList<Banner>();

        try {

            JSONObject json_array = json.getJSONObject(Tags.RESULT);
            if (AppConfig.APP_DEBUG) {
                NSLog.i("JSONBannerArray", json.toString());
            }

            for (int i = 0; i < json_array.length(); i++) {


                try {

                    JSONObject json_banner = json_array.getJSONObject(i + "");

                    if (AppConfig.APP_DEBUG) {
                        NSLog.i("BannerUD", json_banner + "");
                    }
                    Banner banner = new Banner();
                    banner.setId(json_banner.getInt("id"));
                    banner.setTitle(json_banner.getString("title"));
                    banner.setDescription(json_banner.getString("description"));
                    banner.setStatus(json_banner.getInt("status"));
                    banner.setModule(json_banner.getString("module"));
                    banner.setDate_end(json_banner.getString("date_start"));
                    banner.setDate_end(json_banner.getString("date_end"));
                    banner.setModule_id(json_banner.getString("module_id"));
                    if (!json_banner.isNull("is_can_expire"))
                        banner.setIs_can_expire(json_banner.getInt("is_can_expire"));


                    String jsonValues = "";
                    try {

                        if (!json_banner.isNull("image")) {
                            jsonValues = json_banner.getJSONObject("image").toString();
                            JSONObject jsonObject = new JSONObject(jsonValues);
                            ImagesParser imgp = new ImagesParser(jsonObject);
                            banner.setListImages(imgp.getImagesList());
                        } else {
                            banner.setListImages(new RealmList<Images>());
                        }

                    } catch (JSONException jex) {
                        banner.setListImages(new RealmList<Images>());
                    }

                    ImagesParser mImagesParser = new ImagesParser(
                            new JSONObject(json_banner.getString("image"))
                    );
                    banner.setImages(mImagesParser.getImage());


                    if (AppConfig.APP_DEBUG) {
                        NSLog.i("ParserBanner", banner.getId() + "  " + banner.getTitle());
                    }


                    list.add(banner);
                } catch (JSONException e) {
                    e.printStackTrace();
                }

            }

        } catch (JSONException e) {
            e.printStackTrace();
        }


        return list;
    }


}
