package com.techsoft.callfeature;



import android.annotation.SuppressLint;
import android.content.BroadcastReceiver;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.provider.ContactsContract;
import android.telephony.PhoneStateListener;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.widget.Toast;

import java.util.Date;

public abstract class PhonecallReceiver extends BroadcastReceiver {


    private static int lastState = TelephonyManager.CALL_STATE_IDLE;
    private static Date callStartTime;
    private static boolean isIncoming;
    private static String savedNumber;  //because the passed incoming is only valid in ringing
    String number, name;

    @Override
    public void onReceive(Context context, Intent intent) {

        if (intent.getAction().equals("android.intent.action.NEW_OUTGOING_CALL")) {
            savedNumber = intent.getExtras().getString("android.intent.extra.PHONE_NUMBER");
            Toast.makeText(context, savedNumber + "Outgoing Number", Toast.LENGTH_SHORT).show();

        } else {

            String stateStr = intent.getExtras().getString(TelephonyManager.EXTRA_STATE);
            int state = 0;
            if (stateStr.equals(TelephonyManager.EXTRA_STATE_IDLE)) {
                state = TelephonyManager.CALL_STATE_IDLE;
            } else if (stateStr.equals(TelephonyManager.EXTRA_STATE_OFFHOOK)) {
                state = TelephonyManager.CALL_STATE_OFFHOOK;
            } else if (stateStr.equals(TelephonyManager.EXTRA_STATE_RINGING)) {
                state = TelephonyManager.CALL_STATE_RINGING;
            }
            if (number == null) {
                TelephonyManager telephony = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
                telephony.listen(new PhoneStateListener() {
                    @Override
                    public void onCallStateChanged(int state, String incomingNumber) {
                        super.onCallStateChanged(state, incomingNumber);
                        name = VerifyMobileNumber(context, incomingNumber);
                        onCallStateChangedProcess(context, state, incomingNumber, name);
                        System.out.println("IncomingNumber : " + incomingNumber);
                    }
                }, PhoneStateListener.LISTEN_CALL_STATE);
            } else {
                name = VerifyMobileNumber(context, number);
                onCallStateChangedProcess(context, state, number, name);
            }

        }
    }


    protected abstract void onIncomingCallReceived(Context ctx, String number, Date start, String name);

    protected abstract void onIncomingCallAnswered(Context ctx, String number, Date start, String name);

    protected abstract void onIncomingCallEnded(Context ctx, String number, Date start, Date end, String name);

    protected abstract void onOutgoingCallStarted(Context ctx, String number, Date start, String name);

    protected abstract void onOutgoingCallEnded(Context ctx, String number, Date start, Date end, String name);

    protected abstract void onMissedCall(Context ctx, String number, Date start, String name);

    public void onCallStateChangedProcess(Context context, int state, String number, String name) {
        Log.d("PhoneState", "Last State : " + String.valueOf(lastState) + "New State : " + String.valueOf(state));
        if (lastState == state) {
            return;
        }
        switch (state) {
            case TelephonyManager.CALL_STATE_RINGING:
                isIncoming = true;
                callStartTime = new Date();
                savedNumber = number;
                onIncomingCallReceived(context, savedNumber, callStartTime, name);
                break;
            case TelephonyManager.CALL_STATE_OFFHOOK:
                if (lastState != TelephonyManager.CALL_STATE_RINGING) {
                    isIncoming = false;
                    callStartTime = new Date();
                    onOutgoingCallStarted(context, savedNumber, callStartTime, name);
                } else {
                    isIncoming = true;
                    callStartTime = new Date();
                    onIncomingCallAnswered(context, savedNumber, callStartTime, name);
                }

                break;
            case TelephonyManager.CALL_STATE_IDLE:
                //Went to idle-  this is the end of a call.  What type depends on previous state(s)
                if (lastState == TelephonyManager.CALL_STATE_RINGING) {
                    //Ring but no pickup-  a miss
                    onMissedCall(context, savedNumber, callStartTime, name);
                } else if (isIncoming) {
                    onIncomingCallEnded(context, savedNumber, callStartTime, new Date(), name);
                } else {
                    onOutgoingCallEnded(context, savedNumber, callStartTime, new Date(), name);
                }
                break;
        }
        lastState = state;
    }

    @SuppressLint("Range")
    public static String VerifyMobileNumber(Context ctx, String phoneNumber) {
        String res = null;
        try {
            ContentResolver resolver = ctx.getContentResolver();
            Uri uri = Uri.withAppendedPath(ContactsContract.PhoneLookup.CONTENT_FILTER_URI, Uri.encode(phoneNumber));
            Cursor c = resolver.query(uri, new String[]{ContactsContract.PhoneLookup.DISPLAY_NAME}, null, null, null);

            if (c != null) { // cursor not null means number is found contactsTable
                if (c.moveToFirst()) {   // so now find the contact Name
                    res = c.getString(c.getColumnIndex(ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME));
                }
                c.close();
            }
        } catch (Exception ex) {
            Log.d("PhoneCallError", ex.getMessage());
            /* Ignore */
        }
        return res;
    }
}