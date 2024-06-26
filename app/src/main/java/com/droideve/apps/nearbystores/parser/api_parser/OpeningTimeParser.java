package com.droideve.apps.nearbystores.parser.api_parser;


import android.util.Log;
import com.droideve.apps.nearbystores.utils.NSLog;

import com.droideve.apps.nearbystores.appconfig.AppConfig;
import com.droideve.apps.nearbystores.classes.OpeningTime;
import com.droideve.apps.nearbystores.parser.Parser;

import org.json.JSONException;
import org.json.JSONObject;


public class OpeningTimeParser extends Parser {

    public OpeningTimeParser(JSONObject json) {
        super(json);
    }
    public OpeningTimeParser(Parser parser) {
        this.json = parser.json;
    }
    public OpeningTime getOpeningTimes() {


        if (AppConfig.APP_DEBUG) {
            NSLog.i("JSONOpeningTimeArray", json.toString());
        }


        try {

            JSONObject json_ot = json.getJSONObject(0 + "");

            if (AppConfig.APP_DEBUG) {
                NSLog.i("OpeningTimeUD", json_ot + "");
            }
            OpeningTime mOpeningTime = new OpeningTime();
            mOpeningTime.setId(json_ot.getInt("id"));
            mOpeningTime.setStore_id(json_ot.getInt("store_id"));
            mOpeningTime.setDay(json_ot.getString("day"));
            mOpeningTime.setClosing(json_ot.getString("closing"));
            mOpeningTime.setOpening(json_ot.getString("opening"));
            mOpeningTime.setTimezone(json_ot.getString("timezone"));
            mOpeningTime.setEnabled(json_ot.getInt("enabled"));

            if (AppConfig.APP_DEBUG) {
                NSLog.i("ParserOpeningTime", mOpeningTime.getId() + "  " + mOpeningTime.getDay());
            }
            return mOpeningTime;


        } catch (JSONException e) {
            e.printStackTrace();
        }


        return null;
    }


}
