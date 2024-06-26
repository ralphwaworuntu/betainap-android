package com.droideve.apps.nearbystores.dtmessenger;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;
import com.droideve.apps.nearbystores.utils.NSLog;

import com.droideve.apps.nearbystores.appconfig.AppContext;
import com.droideve.apps.nearbystores.utils.NSLog;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by Abderrahim El imame on 29/01/2016.
 * Email : abderrahim.elimame@gmail.com
 */
public abstract class MessagesReceiverBroadcast extends BroadcastReceiver {
    @Override
    public void onReceive(Context mContext, Intent intent) {

        String action = intent.getAction();

        NSLog.e(MessagesReceiverBroadcast.class.getName(), action);

        switch (action) {

            case "new_user_message_notification":


                String message = intent.getExtras().getString("message");

                try {
                    MessageReceived(mContext, new JSONObject(message));
                } catch (JSONException e) {
                    e.printStackTrace();
                }

                break;
        }


    }

    protected abstract void MessageReceived(Context context, JSONObject data);
}
