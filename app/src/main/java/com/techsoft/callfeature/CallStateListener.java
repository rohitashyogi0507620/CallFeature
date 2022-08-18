package com.techsoft.callfeature;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.os.Handler;
import android.provider.CallLog;
import android.telephony.PhoneStateListener;
import android.telephony.TelephonyManager;
import android.util.Log;

import java.util.Date;

public class CallStateListener extends BroadcastReceiver {

    private static int lastState = TelephonyManager.CALL_STATE_IDLE;
    private static boolean isIncoming;

    @Override
    public void onReceive(Context context, Intent intent) {
        Log.d("CALLTEST", "onReceive");

        TelephonyManager telephony = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
        telephony.listen(new PhoneStateListener() {

            @Override
            public void onCallStateChanged(int state, String incomingNumber) {
                super.onCallStateChanged(state, incomingNumber);
                onCallStateChangedProcess(context, state, incomingNumber);
                Log.d("CALLTEST", "STATECHNAGE " + state);
            }
        }, PhoneStateListener.LISTEN_CALL_STATE);

//        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.LOLLIPOP_MR1) {
//            SubscriptionManager subscriptionManager = (SubscriptionManager) context.getSystemService(TELEPHONY_SUBSCRIPTION_SERVICE);
//
//            @SuppressLint("MissingPermission") Integer subscriptionIdOfSimCard2 = subscriptionManager.getActiveSubscriptionInfoForSimSlotIndex(1).getSubscriptionId();
//            Log.d("CALLTEST", "ID : " + String.valueOf(subscriptionIdOfSimCard2));
//
//            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.N) {
//                TelephonyManager telephony2 = (TelephonyManager) ((TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE)).createForSubscriptionId(subscriptionIdOfSimCard2);
//                telephony2.listen(new PhoneStateListener() {
//
//                    @Override
//                    public void onCallStateChanged(int state, String incomingNumber) {
//                        super.onCallStateChanged(state, incomingNumber);
//                        onCallStateChangedProcess(context, state, incomingNumber);
//                        Log.d("CALLTEST", "STATECHNAGE " + state);
//                    }
//                }, PhoneStateListener.LISTEN_CALL_STATE);
//            }
//
//        }

    }

    public void onCallStateChangedProcess(Context context, int state, String savedNumber) {
        if (lastState == state) {
            Log.d("CALLTEST", "CASE0");
            onOutgoingCallEnded(context);
            return;
        }
        switch (state) {
            case TelephonyManager.CALL_STATE_RINGING:
                isIncoming = true;
                Log.d("CALLTEST", "CASE1");
                break;
            case TelephonyManager.CALL_STATE_OFFHOOK:
                if (lastState != TelephonyManager.CALL_STATE_RINGING) {
                    isIncoming = false;
                    Log.d("CALLTEST", "CASE2");

                } else {
                    isIncoming = true;
                    Log.d("CALLTEST", "CASE3");

                }

                break;
            case TelephonyManager.CALL_STATE_IDLE:
                //Went to idle-  this is the end of a call.  What type depends on previous state(s)
                if (lastState == TelephonyManager.CALL_STATE_RINGING) {
                } else if (isIncoming) {
                    Log.d("CALLTEST", "InComing Answer");
                    Log.d("CALLTEST", "CAS4");
                } else {
                    //On Incoming Answer
                    Log.d("CALLTEST", "InComing Answer");
                    Log.d("CALLTEST", "CASE5");
                    onOutgoingCallEnded(context);
                }
                break;
        }
        lastState = state;
    }

    public void onOutgoingCallStarted(Context ctx, String number) {
        //startBroadCastReceiver(ctx);
        Log.d("CALLTEST", "onOutgoingCallStarted");

    }

    private void onOutgoingCallEnded(Context ctx) {
        Log.d("CALLTEST", "onOutgoingCallEnded");

        startBroadCastReceiver(ctx);
    }

    private void startBroadCastReceiver(Context ctx) {

        Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
            public void run() {
                Bundle extras = getCallDetails(ctx);
                String phonenumber = extras.getString("Number");
                String duration = extras.getString("Duration");
                String type = extras.getString("Type");
                String time = extras.getString("Time");
                SessionManager sessionManager = new SessionManager();
                sessionManager.setContext("RMCALLDATA", ctx);
                sessionManager.saveData("Number", phonenumber);
                sessionManager.saveData("Duration", duration);
                sessionManager.saveData("Type", type);
                sessionManager.saveData("Time", time);
                Log.d("CALLTEST", "DATASAVED");

            }
        }, 2000);


    }

    private Bundle getCallDetails(Context context) {
        Bundle callData = new Bundle();

        try {
            Cursor managedCursor = context.getContentResolver().query(CallLog.Calls.CONTENT_URI, null, null, null, CallLog.Calls.DATE + " DESC");
            int number = managedCursor.getColumnIndex(CallLog.Calls.NUMBER);
            int type = managedCursor.getColumnIndex(CallLog.Calls.TYPE);
            int date = managedCursor.getColumnIndex(CallLog.Calls.DATE);
            int duration = managedCursor.getColumnIndex(CallLog.Calls.DURATION);
            int simcard = managedCursor.getColumnIndex(CallLog.Calls.PHONE_ACCOUNT_ID);

            while (managedCursor.moveToNext()) {
                String phNumber = managedCursor.getString(number); // mobile number
                String callType = managedCursor.getString(type); // call type
                String callDate = managedCursor.getString(date); // call date
                Date callDayTime = new Date(Long.valueOf(callDate));
                String callDuration = managedCursor.getString(duration);
                String callSim = managedCursor.getString(simcard);
                String dir = null;
                int dircode = Integer.parseInt(callType);
                switch (dircode) {
                    case CallLog.Calls.OUTGOING_TYPE:
                        dir = "OUTGOING";
                        break;

                    case CallLog.Calls.INCOMING_TYPE:
                        dir = "INCOMING";
                        break;

                    case CallLog.Calls.MISSED_TYPE:
                        dir = "MISSED";
                        break;
                }

                callData.putString("Number", phNumber);
                callData.putString("Duration", callDuration);
                callData.putString("Type", dir);
                callData.putString("Time", callDayTime.toString());
                Log.d("MobileSIM", callSim);
                break;


            }
        } catch (Exception e) {
        }

        return callData;
    }

}
