package com.droideve.apps.nearbystores.parser.api_parser;

import com.droideve.apps.nearbystores.classes.Discussion;
import com.droideve.apps.nearbystores.classes.Message;
import com.droideve.apps.nearbystores.classes.User;
import com.droideve.apps.nearbystores.parser.Parser;
import com.droideve.apps.nearbystores.parser.tags.Tags;

import org.json.JSONException;
import org.json.JSONObject;

import io.realm.RealmList;

/**
 * Created by Droideve on 1/12/2016.
 */
public class DiscussionParser extends Parser {

    public DiscussionParser(JSONObject json) {
        super(json);
    }

    public DiscussionParser(Parser parser) {
        this.json = parser.json;
    }

    public RealmList<Discussion> getDiscussion() {

        RealmList<Discussion> list = new RealmList<Discussion>();

        try {

            JSONObject jsonResult = this.json.getJSONObject(Tags.RESULT);


            for (int i = 0; i < jsonResult.length(); i++) {

                JSONObject jsonRow = jsonResult.getJSONObject(String.valueOf(i));

                JSONObject json_user_sender = new JSONObject(jsonRow.getString(Tags.SENDER));
                UserParser mUserParserSender = new UserParser(json_user_sender);
                User sender = mUserParserSender.getUser().get(0);

                JSONObject json_mesg = new JSONObject(jsonRow.getString(Tags.MESSAGES));
                MessageParser mMessageParser = new MessageParser(json_mesg);
                RealmList<Message> messages = mMessageParser.getMessages();

                Discussion mDiscussion = new Discussion();
                mDiscussion.setDiscussionId(jsonRow.getInt("id_discussion"));
                mDiscussion.setSenderUser(sender);
                mDiscussion.setSystem(false);

                if(!jsonRow.isNull("nbrMessageNotSeen")){
                    mDiscussion.setNbrMessage(jsonRow.getInt("nbrMessageNotSeen"));
                }else{
                    mDiscussion.setNbrMessage(0);
                }

                mDiscussion.setCreatedAt(jsonRow.getString("created_at"));
                mDiscussion.setStatus(jsonRow.getInt("status"));


                mDiscussion.setMessages(messages);

                list.add(mDiscussion);

            }


        } catch (JSONException e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }


        return list;
    }


}
