package com.akapps.check_vertification_system_v1.classes;

import android.animation.ObjectAnimator;
import android.nfc.NfcAdapter;
import android.os.Handler;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.SearchView;
import android.widget.TextView;
import androidx.recyclerview.widget.RecyclerView;

public class Animation {

    // layout
    private SearchView searchView;
    private TextView closeSearch;
    private ImageView nfcStatus;
    private LinearLayout searchLayout;
    private RecyclerView customerRecyclerview;
    private TextView emptyRecyclerviewMessage;
    private ImageView settings;

    // variables
    private final int longAnimationDuration = 1000;
    private final int mediumAnimationDuration = 500;
    private final int smallDelay = 100;
    private NfcAdapter nfcAdapter;

    public Animation(SearchView searchView, TextView closeSearch, ImageView nfcStatus,
                     LinearLayout searchLayout, RecyclerView customerRecyclerview,
                     TextView emptyRecyclerviewMessage, NfcAdapter nfcAdapter, ImageView settings) {
        this.searchView = searchView;
        this.closeSearch = closeSearch;
        this.nfcStatus = nfcStatus;
        this.searchLayout = searchLayout;
        this.customerRecyclerview = customerRecyclerview;
        this.emptyRecyclerviewMessage = emptyRecyclerviewMessage;
        this.nfcAdapter = nfcAdapter;
        this.settings = settings;
        if(nfcAdapter == null) {
            nfcStatus.setVisibility(View.GONE);
            settings.setVisibility(View.GONE);
        }
    }

    // slides up the search bar to be on the top and sets whatever on top to be invisible
    public void slideUp(View move, View other, boolean showKeyboard){
        new Handler().postDelayed(() -> {
            if(nfcAdapter != null) {
                nfcStatus.animate().alpha(0.0f).setDuration(mediumAnimationDuration).withEndAction(() -> nfcStatus.setVisibility(View.GONE));
                settings.animate().alpha(0.0f).setDuration(mediumAnimationDuration).withEndAction(() -> settings.setVisibility(View.GONE));
            }
            closeSearch.animate().alpha(1.0f).setDuration(mediumAnimationDuration).withStartAction(() -> closeSearch.setVisibility(View.VISIBLE));
            emptyRecyclerviewMessage.animate().alpha(1.0f).setDuration(mediumAnimationDuration).withStartAction(() -> emptyRecyclerviewMessage.setVisibility(View.VISIBLE));
            move.animate().alpha(1.0f).setDuration(mediumAnimationDuration).withStartAction(() -> move.setVisibility(View.VISIBLE));
            other.animate().alpha(0.0f).setDuration(mediumAnimationDuration);
            ObjectAnimator searchAnimation = ObjectAnimator.ofFloat(searchLayout, "translationY",(-1 * (move.getY() - other.getY())) + 40);
            searchAnimation.setDuration(longAnimationDuration);
            searchAnimation.start();
            if(showKeyboard) {
                // focuses on search and opens keyboard
                searchView.setIconified(true);
                searchView.setIconified(false);
            }
            // clear recyclerview data
            customerRecyclerview.setAdapter(null);
        }, smallDelay);
    }

    // slides down the search bar to its default position and
    public void slideDown(View move,View other){
        new Handler().postDelayed(() -> {
            if(nfcAdapter != null) {
                nfcStatus.animate().alpha(1.0f).setDuration(mediumAnimationDuration).withEndAction(() -> nfcStatus.setVisibility(View.VISIBLE));
                settings.animate().alpha(1.0f).setDuration(mediumAnimationDuration).withEndAction(() -> settings.setVisibility(View.VISIBLE));
            }
            closeSearch.animate().alpha(0.0f).setDuration(mediumAnimationDuration).withEndAction(() -> closeSearch.setVisibility(View.INVISIBLE));
            move.animate().alpha(0.0f).setDuration(mediumAnimationDuration).withEndAction(() -> move.setVisibility(View.GONE));
            other.animate().alpha(1.0f).setDuration(mediumAnimationDuration);
            ObjectAnimator animation = ObjectAnimator.ofFloat(move, "translationY",   0f);
            animation.setDuration(mediumAnimationDuration);
            animation.start();
            // clears recyclerview data
            customerRecyclerview.setAdapter(null);
        }, smallDelay);
    }
}