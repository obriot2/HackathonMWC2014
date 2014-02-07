/**
 * RetailAPI
 * Copyright (C) 2014 dejamobile.
 *
 */
package com.dejamobile.retailapi.sample;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import com.dejamobile.retailapi.core.RetailAPIManager;
import com.dejamobile.retailapi.core.Session;
import com.dejamobile.retailapi.model.BasketItem;
import com.dejamobile.retailapi.model.CustomerId;
import com.dejamobile.retailapi.model.Error;
import com.dejamobile.retailapi.model.MEHolderId;
import com.dejamobile.retailapi.model.MyBasket;
import com.dejamobile.retailapi.model.MyLoyalty;
import com.dejamobile.retailapi.model.ServiceState;

/**
 *
 * @author Olivierr Briot
 *         Date: 08/01/14
 */
public class MainActivity extends Activity implements RetailAPIManager.onMyLoyaltyListener, RetailAPIManager.onMyBasketListener, RetailAPIManager.onCustomerIdListener, RetailAPIManager.onMEHolderListener {

    private RetailAPIManager retailManager;
    private final static String retailID = "ORI1";
    public static final String TAG = "Hackathon";
    private Session s;
    private TextView tLog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        tLog = (TextView)findViewById(R.id.log);
    }

    @Override
    protected void onStart() {
        super.onStart();
        retailManager = RetailAPIManager.getInstance(this);
    }

    @Override
    protected void onResume() {
        super.onResume();

        OpenOrReopenSession();

        if (getIntent().getAction().equals("com.dejamobile.PUSH"+retailID))
        {
            byte b = getIntent().getByteExtra("com.dejamobile.ACTION",(byte)0xFF);
            switch (b) {
                case 0 :
                    tLog.setText("Basket successfully read and flushed");
                    break;
                case 1 :
                    tLog.setText("Empty Basket, please set basket");
                    break;
                default:
                    tLog.setText("You know who I am. See you later");
            }

            setIntent(new Intent("android.intent.action.MAIN"));
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        CloseSession();

    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);

        setIntent(intent);
    }

    private void OpenOrReopenSession()
    {
        try {
            retailManager.setServiceStateListener(new RetailAPIManager.onServiceStateListener() {
                @Override
                public void onServiceStateAvailable(ServiceState state) {
                    if (state.getState() == ServiceState.SUBSCRIBED)
                    {
                        retailManager.setSessionListener(new RetailAPIManager.onSessionListener() {
                            @Override
                            public void onSessionOpened(Session session) {
                                s = session;
                                tLog.setText("Session successfully started for retailer " + retailID);
                            }

                            @Override
                            public void onSessionError(com.dejamobile.retailapi.model.Error e) {
                                tLog.setText("bad retailerId");
                            }
                        });
                        try {
                            retailManager.doOpenSession(retailID);
                        } catch (Exception e) {
                            tLog.setText("Exception : " + e.getClass().getName());
                        }
                    }
                    else if (state.getState() == ServiceState.UNSUBSCRIBED)
                    {
                        tLog.setText("RetailAPI " + retailID + " is not activated");
                    }
                }

                @Override
                public void onServiceStateError(com.dejamobile.retailapi.model.Error e) {
                    tLog.setText("onServiceStateError : " + e.getReason());
                }
            });
            retailManager.doGetServiceState(retailID);
        } catch (Exception e) {
            tLog.setText("Exception : " + e.getClass().getName());
        }
    }

    private void CloseSession()
    {
        if ((retailManager != null) && (s != null))
        {
            try {
                retailManager.closeSession(s);
            } catch (Exception e) {
                tLog.setText("Exception : " + e.getClass().getName());
            }
        }
    }

    public void setBasket(View v)
    {
        MyBasket b = new MyBasket();

        b.add(new BasketItem((short) 1,"1234567891323",100));
        b.add(new BasketItem((short) 1,"1234987891323",200));
        b.add(new BasketItem((short) 1,"1234567891593",299));

        if (s != null)
        {
            s.setBasketListener(this);
            s.doSetBasket(b);
        }
    }

    public void getBasket(View v)
    {
        if (s != null)
        {
            s.setBasketListener(this);
            s.doGetBasket();
        }
    }

    public void getLoyaltyData(View v)
    {
        if (s != null)
        {
            s.setLoyaltyCardIdListener(this);
            s.doGetLoyaltyCardId();
        }
        else
        {
            tLog.setText("No session");
        }
    }

    public void setMyLoyalty(View v)
    {
        MyLoyalty m = new MyLoyalty(new byte[] {(byte)0x01, (byte)0x02});
        if (s != null)
        {
            s.setLoyaltyCardIdListener(this);
            s.doSetLoyaltyCardId(m);
        }
    }

    public void getCustomerId(View v)
    {
        if (s != null)
        {
            s.setCustomerIdListener(this);
            s.doGetCustomerId();
        }
        else
        {
            tLog.setText("No session");
        }
    }

    public void setCustomerId(View v)
    {
        CustomerId c = new CustomerId(new byte[] {(byte)0x1f, (byte)0x2f});
        if (s != null)
        {
            s.setCustomerIdListener(this);
            s.doSetCustomerId(c);
        }
    }

    public void getMEHolderId(View v)
    {
        if (s != null)
        {
            s.setMEHolderIdListener(this);
            s.doGetMEHolderId();
        }
        else
        {
            tLog.setText("No session");
        }
    }

    @Override
    public void onMyBasketRead(MyBasket basket) {
        Log.d(TAG, "onMyBasketWritten");
        tLog.setText("Basket contains " + basket.getItems().size() + " items");
    }

    @Override
    public void onMyBasketWritten() {
        Log.d(TAG, "onMyBasketWritten");
        tLog.setText("Done, Please tap your handset");
    }

    @Override
    public void onMyBasketError(com.dejamobile.retailapi.model.Error e) {
        tLog.setText("onMyBasketError : " + e.getReason());
    }

    @Override
    public void onMyLoyaltyRead(MyLoyalty loyaltyCard) {
        Log.d(TAG,"onMyLoyaltyRead : " + loyaltyCard.toString());
        tLog.setText(loyaltyCard.toString());
    }

    @Override
    public void onMyLoyaltyWritten() {
        Log.d(TAG,"onMyLoyaltyWritten");
        tLog.setText("Loyalty card Id has been registered");
    }

    @Override
    public void onMyLoyaltyError(Error e) {
        tLog.setText("onMyLoyaltyError : " + e.getReason());
    }

    @Override
    public void onCustomerIdRead(CustomerId customerId) {
        Log.d(TAG,"onCustomerId : " + customerId.toString());
        tLog.setText(customerId.toString());
    }

    @Override
    public void onCustomerIdWritten() {
        Log.d(TAG,"onCustomerIdWritten");
        tLog.setText("CustomerId has been registered");
    }

    @Override
    public void onCustomerIdError(Error error) {
        tLog.setText("onCustomerIdError : " + error.getReason());
    }

    @Override
    public void onMEHolderRead(MEHolderId meHolderId) {
        Log.d(TAG,"onMEHolderId : " + meHolderId.toString());
        tLog.setText(meHolderId.toString());
    }

    @Override
    public void onMEHolderError(Error error) {

    }



    public void popup(String message, String title)
    {
        // 1. Instantiate an AlertDialog.Builder with its constructor
        AlertDialog.Builder builder = new AlertDialog.Builder(this);

        // 2. Chain together various setter methods to set the dialog characteristics
        builder.setMessage(message)
                .setTitle(title);

        builder.setNeutralButton("Ok", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                // User cancelled the dialog
            }
        });

        // 3. Get the AlertDialog from create()
        AlertDialog dialog = builder.create();
        dialog.show();
    }


}
