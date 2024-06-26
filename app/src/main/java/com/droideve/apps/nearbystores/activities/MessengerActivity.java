package com.droideve.apps.nearbystores.activities;

import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Handler;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import com.droideve.apps.nearbystores.utils.NSLog;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.core.content.res.ResourcesCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.droideve.apps.nearbystores.AppController;
import com.droideve.apps.nearbystores.R;
import com.droideve.apps.nearbystores.Services.BusStation;
import com.droideve.apps.nearbystores.Services.Pusher;
import com.droideve.apps.nearbystores.adapter.messenger.ListMessageAdapter;
import com.droideve.apps.nearbystores.appconfig.AppConfig;
import com.droideve.apps.nearbystores.appconfig.AppContext;
import com.droideve.apps.nearbystores.appconfig.Constances;
import com.droideve.apps.nearbystores.classes.Message;
import com.droideve.apps.nearbystores.classes.User;
import com.droideve.apps.nearbystores.controllers.messenger.MessengerController;
import com.droideve.apps.nearbystores.controllers.sessions.SessionsController;
import com.droideve.apps.nearbystores.dtmessenger.DCMBroadcastReceiver;
import com.droideve.apps.nearbystores.dtmessenger.MessengerHelper;
import com.droideve.apps.nearbystores.load_manager.ViewManager;
import com.droideve.apps.nearbystores.network.VolleySingleton;
import com.droideve.apps.nearbystores.network.api_request.SimpleRequest;
import com.droideve.apps.nearbystores.parser.Parser;
import com.droideve.apps.nearbystores.parser.api_parser.MessageParser;
import com.droideve.apps.nearbystores.parser.api_parser.UserParser;
import com.droideve.apps.nearbystores.parser.tags.Tags;
import com.droideve.apps.nearbystores.utils.Translator;
import com.mikepenz.community_material_typeface_library.CommunityMaterial;
import com.mikepenz.iconics.IconicsDrawable;

import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import com.droideve.apps.nearbystores.utils.NSLog;
import java.util.Map;

import io.realm.Realm;

import static com.droideve.apps.nearbystores.appconfig.AppConfig.APP_DEBUG;


/**
 * Created by Droideve on 6/26/2016.
 */
public class MessengerActivity extends AppCompatActivity implements ListMessageAdapter.ClickListener,
        ListMessageAdapter.LoadEarlierMessages,
        ViewManager.CustomView, DCMBroadcastReceiver.NetworkStateReceiverListener {


    public static boolean inbox_opend = false;
    public static Map<Integer, List<Message>> listMessagesOnSaves = new HashMap<>();
    public static Map<Integer, Integer> listCountOnSaves = new HashMap<>();
    public static Map<Integer, Integer> listPageOnSaves = new HashMap<>();
    public static Map<Integer, String> listDateOnSaves = new HashMap<>();
    private static int PAGE = 1;
    public ViewManager mViewManager;
    Toolbar toolbar;
    private User mUserSession = null;
    private User mUserClient = null;
    private ListMessageAdapter adapter;
    private RecyclerView listDiscussion;
    private LinearLayoutManager mLayoutManager;
    private EditText msgInput;

    /*
        SAVE MESSAGE INSTANCE
     */
    private ImageButton send;
    private RequestQueue queue;
    private Realm mRealm = Realm.getDefaultInstance();
    private DCMBroadcastReceiver mDCMBroadcastReceiver;
    private String currentDateAndTime;
    private Drawable onlineDrawable;
    private Drawable offlineDrawable;
    private int discussionId = 0;

    /*
       END MESSAGE INSTANCE
    */
    private int userId = 0;
    private TextView APP_TITLE_VIEW = null;
    private TextView APP_DESC_VIEW = null;
    private int COUNT = 0;
    private Menu mMenu;


    public static void saveListMessages(int key, List<Message> list) {
        if (listMessagesOnSaves == null)
            listMessagesOnSaves = new HashMap<>();
        listMessagesOnSaves.put(key, list);
    }

    public static void saveMessage(int key, Message message) {

        if (listMessagesOnSaves == null)
            listMessagesOnSaves = new HashMap<>();

        if (listMessagesOnSaves.containsKey(key)) {

            if (!checkMessageIsExist(key, message)) {
                listMessagesOnSaves.get(key).add(message);
                int count = listCountOnSaves.get(key);
                count++;
                listCountOnSaves.put(key, count);
            }

        }
    }

    public static boolean checkMessageIsExist(int key, Message message) {

        for (int i = 0; i < listMessagesOnSaves.get(key).size(); i++) {
            if (listMessagesOnSaves.get(key).get(i).getMessageid().equals(message.getMessageid())) {
                return true;
            }
        }

        return false;
    }

    public static void saveCountMessages(int key, int count) {
        if (listCountOnSaves == null)
            listCountOnSaves = new HashMap<>();
        listCountOnSaves.put(key, count);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_messenger);
        setupToolbar();


        try {
            discussionId = getIntent().getExtras().getInt("discussionId");
        } catch (Exception e) {
        }


        onlineDrawable = new IconicsDrawable(this)
                .icon(CommunityMaterial.Icon.cmd_checkbox_blank_circle)
                .color(Color.parseColor("#8BC34A"))
                .sizeDp(12);

        offlineDrawable = new IconicsDrawable(this)
                .icon(CommunityMaterial.Icon.cmd_checkbox_blank_circle_outline)
                .color(Color.parseColor("#ffffff"))
                .sizeDp(12);

        APP_DESC_VIEW.setTextSize(12);


        if (!SessionsController.isLogged()) {
            ActivityCompat.finishAffinity(this);
            startActivity(new Intent(this, SplashActivity.class));
        }

        //init date time
        SimpleDateFormat inputPattern = new SimpleDateFormat("yyyy-MM-dd H:m:s");
        currentDateAndTime = inputPattern.format(new Date());

        PAGE = 1;

        mViewManager = new ViewManager(getApplicationContext());
        mViewManager.setLoadingView(findViewById(R.id.loading));
        mViewManager.setContentView(findViewById(R.id.no_loading));
        mViewManager.setErrorView(findViewById(R.id.error));
        mViewManager.setEmptyView(findViewById(R.id.empty));
        mViewManager.showLoading();
        mViewManager.setCustumizeView(this);


        queue = VolleySingleton.getInstance(this).getRequestQueue();

        loadUserData();


        mUserSession = Realm.getDefaultInstance().copyFromRealm(SessionsController.getSession().getUser());

        msgInput = findViewById(R.id.message_input);
        send = findViewById(R.id.send_button);


        msgInput.setSingleLine(false);
        send.setImageDrawable(new IconicsDrawable(this)
                .icon(CommunityMaterial.Icon2.cmd_send)
                .color(Color.WHITE)
                .sizeDp(20));

        if (AppController.isRTL()) {
            send.setRotation(180);
        }

        send.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                String msg = msgInput.getText().toString().trim();
                if (!msg.equals("")) {

                    //generate temp message Id
                    int randomNum = 1 + (int) (Math.random() * 90000);
                    long unixTime = System.currentTimeMillis() / 1000L;
                    String tempMessageId = unixTime + ":" + randomNum;


                    final Message message = new Message();
                    message.setMessage(msg);

                    message.setType(Message.SENDER_VIEW);
                    message.setStatus(Message.NO_SENT);

                    SimpleDateFormat inputPattern = new SimpleDateFormat("yyyy-MM-dd H:m:s");
                    String currentDateAndTime = inputPattern.format(new Date());
                    message.setDate(currentDateAndTime);
                    message.setMessageid(tempMessageId);
                    adapter.sendMessage(message);

                    final int idelay = adapter.getItemCount() - 1;

                    (new Handler()).postDelayed(new Runnable() {
                        @Override
                        public void run() {

                            adapter.getItem(idelay).setType(Message.SENDER_VIEW);
                            adapter.getItem(idelay).setStatus(Message.SENT);
                            adapter.notifyDataSetChanged();

                        }
                    }, 30000);


                    sendMessageToServer(message, tempMessageId, idelay);
                    msgInput.setText("");


                    scrollToBottom();

                }
            }
        });


        msgInput.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                scrollToBottom();
            }
        });

        msgInput.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

                scrollToBottom();
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {


            }
        });


        adapter = new ListMessageAdapter(this, new ArrayList<Message>());
        listDiscussion = findViewById(R.id.listdiscussion);

        listDiscussion.getItemAnimator().setAddDuration(0);
        listDiscussion.getItemAnimator().setRemoveDuration(500);
        listDiscussion.getItemAnimator().setMoveDuration(0);
        listDiscussion.getItemAnimator().setChangeDuration(0);


        listDiscussion.setHasFixedSize(true);
        mLayoutManager = new LinearLayoutManager(this);

        mLayoutManager.setOrientation(LinearLayoutManager.VERTICAL);

        listDiscussion.setLayoutManager(mLayoutManager);
        listDiscussion.setAdapter(adapter);
        adapter.setClickListener(this);
        scrollToBottom();

        Message messengerLoadingView = new Message();

        messengerLoadingView.setMessageid("-1");
        messengerLoadingView.setType(Message.LOADING_VIEW);

        adapter.addMessage(0, messengerLoadingView);
        adapter.setLoadEarlierMsgs(false);
        adapter.notifyDataSetChanged();

        adapter.setmLoadEarlierMessagesListener(this);

        mDCMBroadcastReceiver = new DCMBroadcastReceiver();
        //filter.addAction("android.net.wifi.WIFI_STATE_CHANGED");
        mDCMBroadcastReceiver.addListener(this);

        APP_TITLE_VIEW.setVisibility(View.VISIBLE);


    }

    //MAKE MESSAGE AS DELIVERED
    private void messageDelivered(final JSONObject message) {

        NSLog.e("messageDelivered", message.toString());

        //parse from json to message object
        MessageParser mMessageParser = new MessageParser(message);

        //get success state
        int success = Integer.parseInt(mMessageParser.getStringAttr(Tags.SUCCESS));

        //get temp message ID
        String tempMessageId = mMessageParser.getStringAttr("messageId");

        if (AppConfig.APP_DEBUG) {
            NSLog.e("tempMessageId", tempMessageId);
        }

        if (success == 1) {

            try {

                List<Message> list = mMessageParser.getMessages();
                if (list.size() > 0) {

                    //create object
                    Message message1 = list.get(0);
                    message1.setType(Message.SENDER_VIEW);
                    message1.setStatus(Message.SENT);

                    //save instance of message
                    saveMessage(userId, message1);

                    if (AppConfig.APP_DEBUG)
                        NSLog.e("__messenger__", "Save ===> " + userId + " " + message1);

                    //update messenger list
                    MessengerHelper.changeStateMessagerAdapter(adapter, message1, tempMessageId);
                    //Change color of message

                }
            } catch (Exception e) {
                e.printStackTrace();
            }

        }

    }

    //marke user as online
    private void userIsOnline(final int id) {

        if (AppContext.DEBUG)
            Toast.makeText(MessengerActivity.this, userId + " is online", Toast.LENGTH_LONG).show();

        if (id == userId) {

            (new Handler()).postDelayed(new Runnable() {
                @Override
                public void run() {

                    if (Translator.print("Online").equals(APP_DESC_VIEW.getText().toString())) {

                    } else {

                        APP_DESC_VIEW.setVisibility(View.VISIBLE);
                        APP_DESC_VIEW.setText(Translator.print("Online"));
                        APP_DESC_VIEW.setCompoundDrawables(onlineDrawable, null, null, null);
                        APP_DESC_VIEW.setCompoundDrawablePadding(10);

                    }

                }
            }, 1000);
        }

    }

    //marke user as offline
    private void userIsOffline(final int id) {

        if (id == userId) {

            if (AppContext.DEBUG)
                Toast.makeText(MessengerActivity.this, userId + " is offline", Toast.LENGTH_LONG).show();

            APP_DESC_VIEW.setVisibility(View.VISIBLE);
            APP_DESC_VIEW.setText(Translator.print("Offline"));
            APP_DESC_VIEW.setCompoundDrawables(offlineDrawable, null, null, null);
            APP_DESC_VIEW.setCompoundDrawablePadding(10);

        }

    }

    private void userStopTyping(final int id) {

        if (id == userId) {
            userIsOnline(id);
        }

    }

    /*
     * RECIEVE NEW MESSAGE FROM THE BACKGROUND
     */
    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onMessageReceived(final Message message) {

        NSLog.e("onMessageReceived", message.getMessage());

        if (message != null) {

            final Message mesageData = message;

            if (!MessengerHelper.messageExists(adapter, mesageData)) {

                if (userId == message.getSenderId()) {

                    mesageData.setType(Message.RECEIVER_VIEW);
                    mesageData.setStatus(Message.SENT);

                    adapter.addMessage(mesageData);

                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            scrollToBottom();
                        }
                    });

                    (new Handler()).postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            //MessengerController.loadMessages(mUserSession);
                            // inboxLoadedChangeStatus(mUserSession, message);
                        }
                    }, 1000);
                } else {
                    MessengerHelper.updateInbox(mesageData.getSenderId(), message);
                }
            }

        }
    }

    //RECIEVE ANY OUTPUT FROM BACKGROUND (User connection, Message delivered states ...)
    @Subscribe
    public void onReceive(final Pusher pusher) {

        runOnUiThread(new Runnable() {
            @Override
            public void run() {

                if (AppContext.DEBUG)
                    NSLog.e("onReceive", pusher.toString());

                if (pusher.getType() == Pusher.MESSAGE_DELIVERED) {

                    try {
                        messageDelivered(new JSONObject(pusher.getMessage()));
                    } catch (JSONException e) {

                        if (AppContext.DEBUG)
                            e.printStackTrace();
                    }

                } else if (pusher.getType() == Pusher.USER_CONNECTED) {
                    try {
                        JSONObject json = new JSONObject(pusher.getMessage());
                        final int userId = json.getInt("userId");

                        if (AppContext.DEBUG)
                            Toast.makeText(MessengerActivity.this, "Your " + userId + " is connetced", Toast.LENGTH_LONG);


                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                } else if (pusher.getType() == Pusher.ONLINE) {


                    try {

                        JSONObject json = new JSONObject(pusher.getMessage());
                        int userId = json.getInt("userId");
                        userIsOnline(userId);

                    } catch (JSONException e) {
                        e.printStackTrace();
                    }

                } else if (pusher.getType() == Pusher.OFFLINE) {
                    try {
                        JSONObject json = new JSONObject(pusher.getMessage());
                        int userId = json.getInt("userId");
                        userIsOffline(userId);

                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                } else if (pusher.getType() == Pusher.USER_TYPING) {
                    try {
                        JSONObject json = new JSONObject(pusher.getMessage());
                        int userId = json.getInt("senderId");
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                } else if (pusher.getType() == Pusher.USER_STOP_TYPING) {
                    try {
                        JSONObject json = new JSONObject(pusher.getMessage());
                        int userId = json.getInt("senderId");
                        userStopTyping(userId);
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }

            }
        });


    }

    @Override
    protected void onResume() {
        super.onResume();
        BusStation.getBus().register(this);
        inbox_opend = true;
    }

    @Override
    protected void onPause() {
        super.onPause();
        BusStation.getBus().unregister(this);
        inbox_opend = false;

        /*
         *   MARK ALL MESSAGES AS SEEN
         */
        MessengerController.inboxMarkAsSeen(mUserSession, discussionId);
    }

    @Override
    protected void onStart() {
        super.onStart();

        fetchSaves();

        IntentFilter filter = new IntentFilter();
        filter.addAction("android.net.conn.CONNECTIVITY_CHANGE");
        this.registerReceiver(mDCMBroadcastReceiver, filter);

        /*
         *   MARK ALL MESSAGES AS SEEN
         */
        MessengerController.inboxMarkAsSeen(mUserSession, discussionId);

    }

    @Override
    protected void onStop() {
        super.onStop();

        this.unregisterReceiver(mDCMBroadcastReceiver);

    }

    private void loadUserData() {

        try {
            userId = getIntent().getExtras().getInt("userId", 0);

            mUserClient = mRealm.where(User.class).equalTo("id", userId).findFirst();

            if (mUserClient == null)
                getUserInfo(userId);

            (new Handler()).postDelayed(new Runnable() {
                @Override
                public void run() {
                    if (mUserClient != null) {

                        if (mUserClient.isBlocked()) {
                            msgInput.setEnabled(false);
                            // msgInput.setBackgroundColor(ResourcesCompat.getColor(getResources(),R.color.gray,null));
                            send.setBackgroundColor(ResourcesCompat.getColor(getResources(), R.color.gray, null));
                            send.setEnabled(false);
                        }


                        if (AppConfig.APP_DEBUG)
                            NSLog.e("userOfStore", mUserClient.getUsername());

                        APP_TITLE_VIEW.setText(mUserClient.getName());

                        if (!mUserClient.getAuth().equals("manager")) {
                            APP_DESC_VIEW.setText("@" + mUserClient.getUsername());
                            APP_DESC_VIEW.setVisibility(View.VISIBLE);
                        } else {
                            APP_DESC_VIEW.setVisibility(View.GONE);
                        }

                    }
                }
            }, 2500);


        } catch (Exception e) {

            if (AppConfig.APP_DEBUG)
                e.printStackTrace();

            Toast.makeText(MessengerActivity.this, getString(R.string.error), Toast.LENGTH_LONG).show();
            finish();

        }

    }

    private void scrollToBottom() {
        listDiscussion.scrollToPosition((adapter.getItemCount() - 1));
    }

    @Override
    public void itemClicked(View view, int position) {

    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        int id = item.getItemId();
        if (id == android.R.id.home) {
            finish();
        } else if (id == R.id.nearby_stores) {

            Intent intent = new Intent(this, ListStoresActivity.class);
            intent.putExtra("user_id", mUserClient.getId());
            startActivity(intent);

        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public boolean onCreateOptionsMenu(final Menu menu) {
        mMenu = menu;

        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.messenger_menu, menu);

        mMenu.findItem(R.id.nearby_stores).setVisible(false);

        return super.onCreateOptionsMenu(menu);
    }

    public void setupToolbar() {

        toolbar = findViewById(R.id.app_bar);
        toolbar.setContentInsetsAbsolute(0, 0);
        setSupportActionBar(toolbar);

        getSupportActionBar().setTitle("");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowTitleEnabled(false);
        getSupportActionBar().setDisplayUseLogoEnabled(false);


        getSupportActionBar().setDisplayShowTitleEnabled(false);
        APP_TITLE_VIEW = toolbar.findViewById(R.id.toolbar_title);
        APP_DESC_VIEW = toolbar.findViewById(R.id.toolbar_subtitle);
        //


        //typeface(APP_TITLE_VIEW);

        APP_DESC_VIEW.setVisibility(View.GONE);

    }

    @Override
    public void customErrorView(View v) {

    }

    @Override
    public void customLoadingView(View v) {

    }

    @Override
    public void customEmptyView(View v) {

    }

    @Override
    public void networkAvailable() {

        MessengerController.loadMessages(mUserSession);

    }

    @Override
    public void networkUnavailable() {

    }

    private void fetchSaves() {
        //get list instance
        if (listPageOnSaves != null && listPageOnSaves.containsKey(userId)) {

            PAGE = listPageOnSaves.get(userId);

        } else {
            listPageOnSaves.put(userId, 1);
            PAGE = 1;
        }


        if (listDateOnSaves != null
                && listDateOnSaves.containsKey(userId))
            currentDateAndTime = listDateOnSaves.get(userId);

        if (listMessagesOnSaves != null
                && listMessagesOnSaves.containsKey(userId)
                && listCountOnSaves.containsKey(userId)) {


            List<Message> listMessages = listMessagesOnSaves.get(userId);

            if (listMessages.size() > 0) {

                for (int i = 0; i < listMessages.size(); i++) {
                    adapter.addMessage(listMessages.get(i));
                    if (AppConfig.APP_DEBUG)
                        NSLog.e("__messenger__", listMessages.get(i).getMessage() + " - " + listMessages.get(i).getMessageid());
                }

                COUNT = listCountOnSaves.get(userId);

                //show result layout
                mViewManager.showContent();
                scrollToBottom();

                //display load more btn
                displayBtnLoading();

                //load new Messages
                MessengerController.loadMessages(mUserSession);

                return;
            }

            return;
        }


        loadMessages(PAGE);
    }

    private void displayBtnLoading() {

        if ((adapter.getItemCount() - 1) < COUNT) {
            adapter.setLoadEarlierMsgs(true);
        } else {
            adapter.setLoadEarlierMsgs(false);
        }
        adapter.notifyDataSetChanged();


        for (int i = 0; i < adapter.getData().size(); i++) {
            if (adapter.getData().get(i).getType() == Message.LOADING_VIEW)
                NSLog.e("Loading", "showLoading pos:"
                        + i + " count:" + COUNT + " adapterCount:" + adapter.getItemCount() + " Page:" + PAGE);
        }

    }


    public void getUserInfo(final int _userId) {


        SimpleRequest request = new SimpleRequest(Request.Method.POST,
                Constances.API.API_GET_USERS, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {

                try {

                    JSONObject jsonObject = new JSONObject(response);
                    if (APP_DEBUG) {
                        NSLog.e("response", jsonObject.toString());
                    }

                    final UserParser mUsersParser = new UserParser(jsonObject);
                    if (mUsersParser.getUser().size() > 0)
                        mUserClient = mUsersParser.getUser().get(0);

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

                mViewManager.showError();
            }
        }) {

            @Override
            protected Map<String, String> getParams() {
                Map<String, String> params = new HashMap<String, String>();

                params.put("limit", "1");
                params.put("page", "1");
                params.put("uid", String.valueOf(_userId));

                return params;
            }

        };


        request.setRetryPolicy(new DefaultRetryPolicy(SimpleRequest.TIME_OUT,
                DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
                DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));

        queue.add(request);


    }

    private void loadMessages(final int currentPage) {

        //load from database

        final int senderid = mUserSession.getId();
        final int receiverid = userId;

        if (currentPage == 1) {
            mViewManager.showLoading();
        }


        SimpleRequest request = new SimpleRequest(Request.Method.POST,
                Constances.API.API_LOAD_MESSAGES, new Response.Listener<String>() {
            @Override
            public void onResponse(final String response) {

                mViewManager.showContent();


                try {

                    if (AppContext.DEBUG) {
                        NSLog.e("response", response);
                        NSLog.e("post", response);
                        NSLog.e("url", Constances.API.API_LOAD_MESSAGES);
                    }

                    JSONObject js = new JSONObject(response);
                    final Parser mParser = new Parser(js);
                    int success = Integer.parseInt(mParser.getStringAttr(Tags.SUCCESS));

                    if (success == 1) {


                        MessageParser mMessageParser = new MessageParser(new JSONObject(response));
                        COUNT = mMessageParser.getIntArg(Tags.COUNT);
                        final List<Message> list = mMessageParser.getMessages();


                        if (list.size() > 0) {


                            if (currentPage == 1) {

                                SimpleDateFormat inputPattern = new SimpleDateFormat("yyyy-MM-dd H:m:s");
                                currentDateAndTime = inputPattern.format(new Date());

                                listDateOnSaves.put(userId, currentDateAndTime);


                                for (int i = list.size() - 1; i >= 0; i--) {

                                    if (list.get(i).getSenderId() == mUserSession.getId())
                                        list.get(i).setType(Message.SENDER_VIEW);
                                    else {
                                        list.get(i).setType(Message.RECEIVER_VIEW);
                                    }

                                    list.get(i).setStatus(Message.SENT);
                                    adapter.addMessage(list.get(i));

                                }


                                scrollToBottom();

                            } else {


                                int pos = 1;
                                for (int i = 0; i < list.size(); i++) {

                                    if (list.get(i).getSenderId() == mUserSession.getId())
                                        list.get(i).setType(Message.SENDER_VIEW);
                                    else {
                                        list.get(i).setType(Message.RECEIVER_VIEW);
                                    }

                                    list.get(i).setStatus(Message.SENT);
                                    adapter.addMessage(1, list.get(i));
                                    pos++;

                                }

                                listDiscussion.scrollToPosition(pos);

                            }


                            if ((adapter.getItemCount() - 1) < COUNT) {
                                PAGE++;
                                listPageOnSaves.put(userId, PAGE);
                            }

                            //save temp message

                            List<Message> lm = new ArrayList<>();
                            for (int i = 0; i < adapter.getData().size(); i++) {
                                if (adapter.getItem(i).getType() != Message.LOADING_VIEW)
                                    lm.add(adapter.getItem(i));
                            }

                            saveListMessages(userId, lm);
                            saveCountMessages(userId, COUNT);

                            displayBtnLoading();

                            mViewManager.showContent();

                        } else {
                            mViewManager.getEmpty();
                        }

                        // adapter.
                    } else {
                        mViewManager.showError();
                    }


                } catch (JSONException e) {
                    e.printStackTrace();
                    mViewManager.showError();
                } catch (Exception e) {
                    e.printStackTrace();
                    mViewManager.showError();
                }

            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                NSLog.e("ERROR", error.toString());

                mViewManager.showContent();
                Toast.makeText(MessengerActivity.this, Translator.print("Error Loading"), Toast.LENGTH_LONG).show();

                displayBtnLoading();
                error.printStackTrace();
            }
        }) {

            @Override
            protected Map<String, String> getParams() {
                Map<String, String> params = new HashMap<String, String>();

                params.put("sender_id", String.valueOf(senderid));
                params.put("user_id", String.valueOf(senderid));
                params.put("receiver_id", String.valueOf(receiverid));
                params.put("page", String.valueOf(currentPage));
                params.put("status", "0");
                params.put("date", currentDateAndTime);

                String lastMessageId = "0";
                try {
                    if (listMessagesOnSaves.size() > 0 && listMessagesOnSaves.containsKey(userId)) {
                        if (listMessagesOnSaves.get(userId).size() > 0) {
                            lastMessageId = listMessagesOnSaves.get(userId).get(0).getMessageid();
                        }
                    }
                } catch (Exception e) {

                    if (AppContext.DEBUG)
                        e.printStackTrace();
                }

                params.put("last_id", String.valueOf(lastMessageId));
                return params;
            }

        };


        request.setRetryPolicy(new DefaultRetryPolicy(SimpleRequest.TIME_OUT,
                DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
                DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));

        queue.add(request);


    }


    @Override
    public void onLoadEarlierMessages(ProgressBar mProgressBar, Button mButton) {


        mProgressBar.setVisibility(View.VISIBLE);
        mButton.setVisibility(View.GONE);

        loadMessages(PAGE);

    }


    private void sendMessageToServer(final Message message, String tempMessageId, final int position) {

        final int senderid = mUserSession.getId();
        final int receiverid = userId;
        final String content = message.getMessage();

        Map<String, String> params = new HashMap<String, String>();

        params.put("sender_id", String.valueOf(senderid));
        params.put("receiver_id", String.valueOf(receiverid));
        params.put("content", content);
        params.put("messageId", tempMessageId);

        if (AppConfig.CHAT_WITH_FIREBASE) { //send with WebSockets

            if (AppContext.DEBUG)
                NSLog.e("__messeneger_request", params.toString());

            sendBasicMessageUsingWebService(params, tempMessageId);
        }

    }


    private void sendBasicMessageUsingWebService(final Map<String, String> params, final String tempMessageId) {

        SimpleRequest request = new SimpleRequest(Request.Method.POST,
                Constances.API.API_SEND_MESSAGE, new Response.Listener<String>() {
            @Override
            public void onResponse(final String response) {

                try {//recieve sent successful

                    if (AppContext.DEBUG) {
                        NSLog.e("__response", response);
                    }

                    //sendMessageToClient(response);

                    JSONObject js = new JSONObject(response);

                    messageDelivered(js);

                } catch (JSONException e) {
                    e.printStackTrace();

                    //show loadToast with showError
                } catch (Exception e) {
                    e.printStackTrace();
                }

            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {

                if (AppContext.DEBUG)
                    NSLog.e("ERROR", error.toString());

                if (AppConfig.APP_DEBUG) {
                    Toast.makeText(MessengerActivity.this, Translator.print("Error sending"), Toast.LENGTH_LONG).show();
                    error.printStackTrace();
                }

            }
        }) {

            @Override
            protected Map<String, String> getParams() {

                return params;
            }

        };


        request.setRetryPolicy(new DefaultRetryPolicy(SimpleRequest.TIME_OUT,
                DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
                DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));

        queue.add(request);

    }

}
