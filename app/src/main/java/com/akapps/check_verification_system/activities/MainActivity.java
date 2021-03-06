package com.akapps.check_verification_system.activities;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.core.widget.NestedScrollView;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.nfc.NfcAdapter;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.SearchView;
import android.widget.TextView;
import com.akapps.check_verification_system.R;
import com.akapps.check_verification_system.classes.Animation;
import com.akapps.check_verification_system.classes.BottomSheetHelper;
import com.akapps.check_verification_system.classes.Customer;
import com.akapps.check_verification_system.classes.FirestoreDatabase;
import com.akapps.check_verification_system.classes.Helper;
import com.akapps.check_verification_system.classes.NFC;
import com.akapps.check_verification_system.recyclerview.customers_search_recyclerview;
import com.google.android.material.card.MaterialCardView;
import com.google.firebase.storage.StorageReference;
import com.irfaan008.irbottomnavigation.SpaceItem;
import com.irfaan008.irbottomnavigation.SpaceNavigationView;
import com.irfaan008.irbottomnavigation.SpaceOnClickListener;
import java.util.ArrayList;
import java.util.stream.Collectors;
import www.sanju.motiontoast.MotionToast;

public class MainActivity extends AppCompatActivity {

    // NFC
    private NFC nfc;
    private NfcAdapter nfcAdapter;
    private PendingIntent pendingIntent;

    // activity
    private Context context;

    // bottom navigation
    private SpaceNavigationView spaceNavigationView;

    // animation
    private Animation animation;

    // bottom sheets helper
    public BottomSheetHelper bottomSheetHelper;

    // layout
    private NestedScrollView scrollView;
    private TextView date;
    private LinearLayout dash;
    private SearchView searchView;
    private TextView closeSearch;
    private ImageView nfcStatus;
    private ImageView settings;
    private RecyclerView customerRecyclerview;
    private RecyclerView.Adapter adapterCustomers;
    private LinearLayout searchLayout;
    private TextView totalInSystemText;
    private MaterialCardView totalInSystemCardView;
    private TextView addedTodayText;
    private MaterialCardView addedTodayCardView;
    private TextView verifiedTodayText;
    private MaterialCardView verifiedTodayCardView;
    private TextView viewedTodayText;
    private MaterialCardView viewedTodayCardView;
    private TextView emptyRecyclerviewMessage;
    private MaterialCardView tapToRefresh;

    // variables
    private final int gridSpan = 2;
    private final int mediumDuration = 1000;
    private boolean writeNfcMode = false;
    private boolean readNfcMode = false;
    private String writeString = "";
    private boolean showKeyboard = true;
    ////////////////////////////////////
    private boolean isViewingTotalInSystem;
    private boolean isViewingAddedToday;
    private boolean isViewingVerifiedToday;
    private boolean isViewingViewedToday;
    private boolean isSearching;
    private boolean isViewingDashboard;

    // firestore database
    public FirestoreDatabase firestoreDatabase;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        context = this;

        initializeLayout(savedInstanceState);

        // ensures sheets only have one instance
        bottomSheetHelper = new BottomSheetHelper(this);

        initializeNFC();

        // initialize animation
        animation = new Animation(scrollView, searchView, closeSearch, nfcStatus, searchLayout,
                customerRecyclerview, emptyRecyclerviewMessage, nfcAdapter, settings);

        // initialize Database
        firestoreDatabase = new FirestoreDatabase(MainActivity.this, context);
        firestoreDatabase.loadCustomerData(false);
    }

    @Override
    public void onBackPressed() {
        // if search bar is open, close it on back press
        if (searchView.getVisibility() == View.VISIBLE)
            closeSearch();
    }

    @Override
    protected void onResume() {
        super.onResume();
        // re-enables nfc and checks to see if nfc status has been updated by user
        if (nfcAdapter != null) {
            nfc.checkNfcStatus();
            nfcAdapter.enableForegroundDispatch(this, pendingIntent, null, null);
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        // closes nfc
        if (nfcAdapter != null)
            nfcAdapter.disableForegroundDispatch(this);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (bottomSheetHelper != null)
            bottomSheetHelper.closeAllSheets();
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        // catches nfc tap by user when app is open
        // reads nfc card to open customer
        if (nfc != null && !writeNfcMode && !readNfcMode) {
            nfc.readNFCTag(intent, false, "");
            bottomSheetHelper.closeNfcSheet();
        }
        // writes to nfc card
        else if (nfc != null && writeNfcMode) {
            nfc.readNFCTag(intent, true, writeString);
            writeNfcMode = false;
        }
        // read card to output card data to user (used in settings sheet)
        else if (nfc != null && readNfcMode) {
            nfc.readNFCTag(intent, false, getString(R.string.output_data));
            readNfcMode = false;
            bottomSheetHelper.closeNfcSheet();
        }
    }

    private void initializeNFC() {
        nfcAdapter = NfcAdapter.getDefaultAdapter(this);
        if (nfcAdapter == null) {
            return; // does not initialize if NFC is not available for device
        }
        pendingIntent = PendingIntent.getActivity(context, 0, new Intent(this,
                getClass()).addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP), PendingIntent.FLAG_IMMUTABLE);
        nfc = new NFC(this, context, nfcAdapter, pendingIntent, nfcStatus, bottomSheetHelper);
    }

    // this method determines whether to read or write to next card tap via NFC
    public void showNfcPrompt(String customerId, boolean readNfcMode,
                              StorageReference profileStoragePath, String customerName) {
        String titleMessage;
        if (readNfcMode) {
            this.readNfcMode = true;
            titleMessage = "Read";
        }
        else {
            writeNfcMode = true;
            writeString = customerId;
            if(customerId.isEmpty())
                titleMessage = "Reset";
            else
                titleMessage = "";
        }
        if (nfc != null)
            if(writeNfcMode)
                nfc.showNfcPrompt(titleMessage, profileStoragePath, customerName);
            else
                nfc.showNfcPrompt(titleMessage);
    }

    private void initializeLayout(Bundle savedInstanceState) {
        scrollView = findViewById(R.id.main_scrollview);
        date = findViewById(R.id.date);
        dash = findViewById(R.id.dashboard);
        settings = findViewById(R.id.settings);
        totalInSystemText = findViewById(R.id.num_total);
        totalInSystemCardView = findViewById(R.id.total_layout);
        addedTodayText = findViewById(R.id.num_added);
        addedTodayCardView = findViewById(R.id.added_layout);
        verifiedTodayText = findViewById(R.id.num_verified);
        verifiedTodayCardView = findViewById(R.id.verified_layout);
        viewedTodayText = findViewById(R.id.num_viewed);
        viewedTodayCardView = findViewById(R.id.viewed_layout);
        searchLayout = findViewById(R.id.search_layout_main);
        searchView = findViewById(R.id.searchview);
        spaceNavigationView = findViewById(R.id.space);
        closeSearch = findViewById(R.id.close_search);
        nfcStatus = findViewById(R.id.nfc_icon);
        customerRecyclerview = findViewById(R.id.customers_recyclerview);
        emptyRecyclerviewMessage = findViewById(R.id.recyclerview_empty_text);
        tapToRefresh = findViewById(R.id.tap_to_refresh);

        // setting up
        customerRecyclerview.setHasFixedSize(true);
        customerRecyclerview.setLayoutManager(new GridLayoutManager(context, gridSpan));
        spaceNavigationView.initWithSaveInstanceState(savedInstanceState);
        spaceNavigationView.addSpaceItem(new SpaceItem(getString(R.string.bottom_nav_home_text), R.drawable.home_icon));
        spaceNavigationView.addSpaceItem(new SpaceItem(getString(R.string.bottom_nav_search_text), R.drawable.search_icon));
        spaceNavigationView.setCentreButtonIconColorFilterEnabled(false);
        spaceNavigationView.setCentreButtonRippleColor(ContextCompat.getColor(this, R.color.cerulean_blue));
        spaceNavigationView.showIconOnly();
        searchView.setFocusable(false);
        searchView.setIconified(false);
        searchView.clearFocus();
        date.setText(Helper.getTodaysDate());
        isViewingDashboard = true;

        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                return false;
            }

            @Override
            public boolean onQueryTextChange(String typingQuery) {
                if (typingQuery.length() > 0)
                    firestoreDatabase.searchForCustomer(typingQuery);
                else if (isSearching)
                    populateRecyclerview(new ArrayList<>());
                return false;
            }
        });

        spaceNavigationView.setSpaceOnClickListener(new SpaceOnClickListener() {
            @Override
            public void onCentreButtonClick() {
                // opens bottom sheet so user can add a new customer
                bottomSheetHelper.openCustomerSheet(MainActivity.this, null);
            }

            @Override
            public void onItemClick(int itemIndex, String itemName) {
                // search is selected
                if (itemIndex == 1) {
                    // if there are customers, then open
                    if (firestoreDatabase.getCustomers().size() > 0) {
                        // if search is selected and nothing else is
                        // currently selected, then current view is isSearching
                        if (isViewingTotalInSystem || isViewingAddedToday || isViewingVerifiedToday || isViewingViewedToday)
                            isSearching = isViewingDashboard = false;
                        else {
                            isSearching = true;
                            isViewingDashboard = isViewingTotalInSystem = isViewingAddedToday =
                                    isViewingVerifiedToday = isViewingViewedToday = false;
                        }
                        // animation to open search view
                        animation.slideUp(tapToRefresh, date, searchLayout, dash, showKeyboard);
                        // resets boolean
                        showKeyboard = true;
                    }
                    else {
                        Helper.showMessage(MainActivity.this, context.getString(R.string.no_customers_title),
                                context.getString(R.string.no_customers_message),
                                MotionToast.TOAST_ERROR);
                        new Handler().postDelayed(() -> closeSearch(), mediumDuration);
                    }
                }
                // home is selected
                else if (itemIndex == 0) {
                    // if home is selected, that means that search/recyclerview was closed
                    // thus, we are back to dashboard and not viewing anything
                    isSearching = isViewingTotalInSystem = isViewingAddedToday =
                            isViewingVerifiedToday = isViewingViewedToday = false;
                    isViewingDashboard = true;
                    // animation to close search view
                    animation.slideDown(tapToRefresh, date, searchLayout, dash);
                }
            }

            @Override
            public void onItemReselected(int itemIndex, String itemName) {
            }
        });

        // tap to update data and recyclerview
        tapToRefresh.setOnClickListener(view -> {
            // checks to see if NFC status changed
            if (nfcAdapter != null)
                nfc.checkNfcStatus();
            // retrieves data from database to see if there is any updates
            firestoreDatabase.loadCustomerData(customerRecyclerview.getAdapter() != null ? true : false);
        });

        closeSearch.setOnClickListener(v -> closeSearch());

        settings.setOnClickListener(v -> bottomSheetHelper.openSettingsSheet(""));

        // checks to see if NFC is not enabled and opens NFC settings
        nfcStatus.setOnClickListener(v -> {
            if(nfc != null) {
                // if device is NFC capable and it's off, nfc settings screen is opened
                if (!nfc.checkNfcStatus())
                    nfc.isNfcDisabled();
            }
            else
                Helper.showMessage(this, context.getString(R.string.no_nfc_text),
                        context.getString(R.string.no_nfc_message),
                        MotionToast.TOAST_WARNING);
        });

        // displays all customers
        totalInSystemCardView.setOnClickListener(v -> {
            // prevents invisible dashboard from being clicked
            if(!isSearching) {
                isViewingTotalInSystem = true;
                isViewingViewedToday = isViewingVerifiedToday = isViewingAddedToday = false;
                showFilterResults(firestoreDatabase.getCustomers());
            }
        });

        // displays customers added today
        addedTodayCardView.setOnClickListener(v -> {
            if(!isSearching) {
                isViewingAddedToday = true;
                isViewingViewedToday = isViewingVerifiedToday = isViewingTotalInSystem = false;
                showFilterResults(firestoreDatabase.getAddedTodayList());
            }
        });

        // displays customers verified today (a.k.a they used an NFC card today)
        verifiedTodayCardView.setOnClickListener(v -> {
            if(!isSearching) {
                isViewingVerifiedToday = true;
                isViewingViewedToday = isViewingAddedToday = isViewingTotalInSystem = false;
                showFilterResults(firestoreDatabase.getVerifiedTodayList());
            }
        });

        // displays customers viewed today (those clicked to view their information)
        viewedTodayCardView.setOnClickListener(v -> {
            if(!isSearching) {
                isViewingViewedToday = true;
                isViewingVerifiedToday = isViewingAddedToday = isViewingTotalInSystem = false;
                showFilterResults(firestoreDatabase.getViewedTodayList());
            }
        });
    }

    private void showFilterResults(ArrayList<Customer> list){
        // this will be always be false when filtering
        showKeyboard = isViewingDashboard = isSearching = false;
        if(list.size() > 0) {
            spaceNavigationView.changeCurrentItem(1);
            new Handler().postDelayed(() -> populateRecyclerview(list), mediumDuration);
        }
    }

    // populates recyclerview and if empty, shows an empty message
    public void populateRecyclerview(ArrayList<Customer> customers){
        if(customers.size() == 0) {
            emptyRecyclerviewMessage.setVisibility(View.VISIBLE);
            customerRecyclerview.setAdapter(null);
        }
        else {
            emptyRecyclerviewMessage.setVisibility(View.GONE);
            adapterCustomers = new customers_search_recyclerview(customers, MainActivity.this, context);
            customerRecyclerview.setAdapter(adapterCustomers);
        }
    }

    // finds customer and opens their information via bottom sheet
    public void findCustomer(String customerId){
        ArrayList<Customer> result = (ArrayList<Customer>) firestoreDatabase.getCustomers().stream().filter(customer -> customer.getCustomerUniqueId().contains(customerId)).collect(Collectors.toList());

        if(customerId.isEmpty())
            Helper.showMessage(this, context.getString(R.string.read_nfc_title), context.getString(R.string.read_nfc_empty_message),
                    MotionToast.TOAST_ERROR);
        else if(result.size() != 1) {
            Helper.showMessage(this, context.getString(R.string.read_nfc_title), context.getString(R.string.read_nfc_message),
                    MotionToast.TOAST_ERROR);
        }
        else{
            // customer has been found using NFC card, so open a bottom sheet so their data can be viewed
            bottomSheetHelper.openCustomerSheet(MainActivity.this, result.get(0));
            // customer has tapped card, so it is logged in their history
            // also updates customer verification date to today to reflect in dashboard
            firestoreDatabase.addHistoryAndDateVerified(result.get(0));
        }
    }

    // updates customers to be up-to-date and updates dashboard data
    public void updateLayoutData(ArrayList<Customer> updatedCustomers, boolean updateRecyclerview){
        String total = "" + updatedCustomers.size();
        String added = "" + updatedCustomers.stream().filter(customer ->
                customer.getDateAdded().contains(Helper.getDatabaseDate())).collect(Collectors.toList()).size();
        String verified = "" + updatedCustomers.stream().filter(customer ->
                customer.getDateVerified().contains(Helper.getDatabaseDate())).collect(Collectors.toList()).size();
        String viewed = "" + updatedCustomers.stream().filter(customer ->
                customer.getDateViewed().contains(Helper.getDatabaseDate())).collect(Collectors.toList()).size();

        totalInSystemText.setText(total);
        addedTodayText.setText(added);
        verifiedTodayText.setText(verified);
        viewedTodayText.setText(viewed);
        // after getting updated data from database, this ensures that the
        // respective list is updated with the new data via the current view
        if(updateRecyclerview) {
            if(isSearching)
                firestoreDatabase.searchForCustomer(searchView.getQuery().toString());
            else if(isViewingAddedToday)
                showFilterResults(firestoreDatabase.getAddedTodayList());
            else if(isViewingVerifiedToday)
                showFilterResults(firestoreDatabase.getVerifiedTodayList());
            else if(isViewingTotalInSystem)
                populateRecyclerview(updatedCustomers);
        }
    }

    public void logOut(){
        bottomSheetHelper.closeAllSheets();
        Intent main = new Intent(this, PinLoginActivity.class);
        startActivity(main);
        finish();
    }

    // closes search by "pressing on" home icon at position 0
    // this will run the spaceNavigationView OnItemClick Method
    // which does the slide down animation for search bar
    private void closeSearch(){
        spaceNavigationView.changeCurrentItem(0);
    }
}