package com.droideve.apps.nearbystores.parser.api_parser;

import com.droideve.apps.nearbystores.classes.Review;
import com.droideve.apps.nearbystores.parser.Parser;
import com.droideve.apps.nearbystores.parser.tags.Tags;

import org.json.JSONException;
import org.json.JSONObject;

import io.realm.RealmList;


public class ReviewParser extends Parser {
    public ReviewParser(JSONObject json) {
        super(json);
    }
    public ReviewParser(Parser parser) {
        this.json = parser.json;
    }

    public RealmList<Review> getComments() {


        RealmList<Review> list = new RealmList<Review>();


        try {
            JSONObject json_array = json.getJSONObject(Tags.RESULT);

            for (int i = 0; i < json_array.length(); i++) {


                JSONObject json_user = json_array.getJSONObject(i + "");

                Review review = new Review();

                review.setId_rate(json_user.getInt("id_rate"));
                review.setStore_id(json_user.getInt("store_id"));
                review.setPseudo(json_user.getString("pseudo"));
                review.setRate(json_user.getDouble("rate"));
                review.setReview(json_user.getString("review"));
                review.setGuest_id(json_user.getInt("guest_id"));
                review.setImage(json_user.getString("image"));

                list.add(review);

            }


        } catch (JSONException e) {
            e.printStackTrace();
        }


        return list;
    }
}
