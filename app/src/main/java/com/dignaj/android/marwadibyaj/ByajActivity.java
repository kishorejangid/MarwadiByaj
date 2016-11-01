package com.dignaj.android.marwadibyaj;

import android.content.Context;
import android.support.v7.app.AppCompatActivity;


import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.MobileAds;
import com.google.android.gms.analytics.HitBuilders;
import com.google.android.gms.analytics.Tracker;

import java.math.BigDecimal;


/**
 * A login screen that offers login via email/password.
 */
public class ByajActivity extends AppCompatActivity {

    // UI references.
    private EditText mPrincipalView;
    private EditText mPeriodView;
    private EditText mRateView;

    private Tracker mTracker;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_byaj);

        // Obtain the shared Tracker instance.
        ByajApplication application = (ByajApplication) getApplication();
        mTracker = application.getDefaultTracker();

        MobileAds.initialize(getApplicationContext(), "ca-app-pub-3139166472959780~4403138368");


        AdView mAdView = (AdView) findViewById(R.id.adView);
        AdRequest adRequest = new AdRequest.Builder().build();
        mAdView.loadAd(adRequest);

        // Set up the login form.
        mPrincipalView = (EditText) findViewById(R.id.principal);
        mRateView = (EditText) findViewById(R.id.rate);
        mPeriodView = (EditText) findViewById(R.id.period);

        mPeriodView.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View view, boolean hasFocus) {
                if(!hasFocus){
                    hideKeyboard(view);
                }
            }
        });

        Button mCalculateButton = (Button) findViewById(R.id.calculate_button);
        mCalculateButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                //hideKeyboard(view);
                calculateInterest();

                mTracker.send(new HitBuilders.EventBuilder()
                        .setCategory("Action")
                        .setAction("Calculate Byaj")
                        .build());
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();

        mTracker.setScreenName("Screen~" + "Byaj");
        mTracker.send(new HitBuilders.ScreenViewBuilder().build());
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mTracker = null;
    }

    /*private void hideKeyboard(View view){
        InputMethodManager inputManager = (InputMethodManager)
                getSystemService(Context.INPUT_METHOD_SERVICE);

        if(view != null) {
            inputManager.hideSoftInputFromWindow(view.getWindowToken(),
                    InputMethodManager.HIDE_NOT_ALWAYS);
        }else{
            Log.d(getClass().getName(),"view is null.");
        }
    }*/

    /**
     * Hide keyboard on touch of UI
     */
    public void hideKeyboard(View view) {

        if (view instanceof ViewGroup) {

            for (int i = 0; i < ((ViewGroup) view).getChildCount(); i++) {

                View innerView = ((ViewGroup) view).getChildAt(i);

                hideKeyboard(innerView);
            }
        }
        if (!(view instanceof EditText)) {

            view.setOnTouchListener(new View.OnTouchListener() {
                @Override
                public boolean onTouch(View v, MotionEvent event) {
                    hideSoftKeyboard(v);
                    return false;
                }

            });
        }

    }

    /**
     * Hide keyboard while focus is moved
     */
    public void hideSoftKeyboard(View view) {
        if (view != null) {
            InputMethodManager inputManager = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
            if (inputManager != null) {
                if (android.os.Build.VERSION.SDK_INT < 11) {
                    inputManager.hideSoftInputFromWindow(view.getWindowToken(),
                            0);
                } else {
                    if (this.getCurrentFocus() != null) {
                        inputManager.hideSoftInputFromWindow(this
                                        .getCurrentFocus().getWindowToken(),
                                InputMethodManager.HIDE_NOT_ALWAYS);
                    }
                    view.clearFocus();
                }
                view.clearFocus();
            }
        }
    }


    /**
     * Attempts to sign in or register the account specified by the login form.
     * If there are form errors (invalid email, missing fields, etc.), the
     * errors are presented and no actual login attempt is made.
     */
    private void calculateInterest() {
        // Reset errors.
        mPrincipalView.setError(null);
        mRateView.setError(null);
        mPeriodView.setError(null);

        boolean cancel = false;
        View focusView = null;

        if (TextUtils.isEmpty(mPrincipalView.getText())) {
            mPrincipalView.setError(getString(R.string.error_principal_empty));
            focusView = mPrincipalView;
            cancel = true;
        }

        if (TextUtils.isEmpty(mRateView.getText())) {
            mRateView.setError(getString(R.string.error_rate_empty));
            focusView = mRateView;
            cancel = true;
        }

        if (TextUtils.isEmpty(mPeriodView.getText())) {
            mPeriodView.setError(getString(R.string.error_period_empty));
            focusView = mPeriodView;
            cancel = true;
        }

        if (cancel) {
            // There was an error; don't attempt login and focus the first
            // form field with an error.
            focusView.requestFocus();
        } else {

            BigDecimal principal = new BigDecimal(mPrincipalView.getText().toString());
            BigDecimal rate = new BigDecimal(mRateView.getText().toString());
            BigDecimal period = new BigDecimal(mPeriodView.getText().toString());

            updateResult(principal,calculateInterest(principal, rate,period));
        }
    }


    private BigDecimal calculateInterest(BigDecimal principal, BigDecimal rate,BigDecimal period)
    {
        return principal.multiply(rate).multiply(period).divide(new BigDecimal(3000),2,BigDecimal.ROUND_HALF_EVEN);
    }

    private void updateResult(BigDecimal principal,BigDecimal  result){
        TextView mInterestView = (TextView) findViewById(R.id.text_interest);
        mInterestView.setText(result.setScale(2,BigDecimal.ROUND_HALF_EVEN).toString());

        TextView mAccruedView = (TextView) findViewById(R.id.text_accrued);
        mAccruedView.setText(principal.add(result).setScale(2,BigDecimal.ROUND_HALF_EVEN).toString());

    }
}

