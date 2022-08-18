package com.techsoft.callfeature;

import android.content.Context;
import android.database.Cursor;
import android.os.Bundle;
import android.os.Handler;
import android.provider.CallLog;
import android.util.Log;

import java.lang.reflect.Field;
import java.util.Date;

public class CallReceiver extends PhonecallReceiver {


    @Override
    protected void onIncomingCallReceived(Context ctx, String number, Date start, String name) {
        Log.d("PhoneCall", "Person name : " + name);
    }

    @Override
    protected void onIncomingCallAnswered(Context ctx, String number, Date start, String name) {
        Log.d("PhoneCall", "Person name : " + name);

    }

    @Override
    protected void onIncomingCallEnded(Context ctx, String number, Date start, Date end, String name) {
        Log.d("PhoneCall", "Person name : " + name);
    }


    @Override
    protected void onOutgoingCallStarted(Context ctx, String number, Date start, String name) {
        Log.d("PhoneCall", "Person name : " + name);

    }

    @Override
    protected void onOutgoingCallEnded(Context ctx, String number, Date start, Date end, String name) {
        Log.d("PhoneCallEnded", "Person name : " + name);
        startBroadCastReceiver(ctx);

    }

    @Override
    protected void onMissedCall(Context ctx, String number, Date start, String name) {
        Log.d("PhoneCall", "Person name : " + name);
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
