package com.zaytoona.youtube.safe.gui.activities;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.support.annotation.NonNull;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.afollestad.materialdialogs.DialogAction;
import com.afollestad.materialdialogs.MaterialDialog;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.zaytoona.youtube.safe.R;
import com.zaytoona.youtube.safe.businessobjects.YouTube.POJOs.YouTubePublicPlayList;
import com.zaytoona.youtube.safe.businessobjects.categories.SafetoonsCategory;
import com.zaytoona.youtube.safe.businessobjects.db.PublicPlayListsDb;
import com.zaytoona.youtube.safe.businessobjects.db.SafetoonsCategoriesDb;
import com.zaytoona.youtube.safe.businessobjects.firebase.MultiCheckCategory;
import com.zaytoona.youtube.safe.businessobjects.firebase.MultiCheckPlayList;
import com.zaytoona.youtube.safe.common.Constants;
import com.zaytoona.youtube.safe.gui.businessobjects.ExpandableCheckRecyclerViewPreferenceDialog;
import com.zaytoona.youtube.safe.gui.businessobjects.adapters.MultiCheckFirebaseAdapter;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class PublicPlayListsImportActivity extends AppCompatActivity {

    private MultiCheckFirebaseAdapter listAdapter = null;
    private RecyclerView recycler = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_public_play_lists_import);

        // setup the toolbar / actionbar

        final ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
            actionBar.setHomeButtonEnabled(true);
        }

        setTitle(R.string.download_public_play_lists);

        DatabaseReference database = FirebaseDatabase.getInstance().getReference();
        final DatabaseReference publicPlayLists = database.child(Constants.FIREBASE_DATABASE_TABLE_PUBLIC_PLAY_LISTS);

        final List<MultiCheckCategory> categories = new ArrayList<>();

        final ProgressDialog progressDialog = new ProgressDialog(this);

        recycler = findViewById(R.id.recycler_multi_check_expandable_1);

        recycler.setLayoutManager(new LinearLayoutManager(this));

        //recycler.setAdapter(listAdapter);

        Button selectAllButton = findViewById(R.id.select_all_button);
        selectAllButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(listAdapter != null) {
                    listAdapter.selectAll();
                }
            }
        });

        Button selectNoneButton = findViewById(R.id.select_none_button);
        selectNoneButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(listAdapter != null) {
                    listAdapter.selectNone();
                }
            }
        });

        progressDialog.setTitle(this.getString(R.string.downloading));
        progressDialog.setMessage(null);

        // reading data from Firebase
        progressDialog.show();

        publicPlayLists.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override

            public void onDataChange(DataSnapshot dataSnapshot) {
                progressDialog.dismiss();

                for (final DataSnapshot snapshot : dataSnapshot.getChildren()) {

                    List<MultiCheckPlayList> playLists = new ArrayList<>();

                    String parentKey = snapshot.getKey();

                    // Category Id, first two numerical characters
                    String categoryId = parentKey.substring(0, 2);

                    PublicPlayListsDb.getPublicPlayListsDb().getPublicPlayListsInCategory(categoryId);

                    String description = null;

                    if (snapshot.child("description").getValue() != null) {
                        description = snapshot.child("description").getValue().toString();
                    }

                    String avatarURL = null;

                    if(snapshot.child("avatarURL").getValue() != null) {
                        avatarURL = snapshot.child("avatarURL").getValue().toString();
                    }


                    for (DataSnapshot playListsSnapshot : snapshot.child(Constants.FIREBASE_DATABASE_TABLE_PUBLIC_PLAY_LISTS_PUBLIC_LISTS).getChildren()) {

                        String childValue = playListsSnapshot.child("title").getValue().toString();
                        String playListKey = playListsSnapshot.getKey();

                        playLists.add(new MultiCheckPlayList(childValue, playListKey));
                    }

                    if(playLists.size() > 0) {

                        // Sort by list number
                        Collections.sort(playLists, new Comparator<MultiCheckPlayList>() {
                            @Override
                            public int compare(MultiCheckPlayList lhs, MultiCheckPlayList rhs) {

                                String first = "1000000";
                                String second = "1000000";

                                if(lhs.getName().length() > 2) {
                                    first = lhs.getName().substring(0, 2);
                                }

                                if(rhs.getName().length() > 2) {
                                    second = rhs.getName().substring(0, 2);
                                }

                                if(isInteger(first) && isInteger(second)) {
                                    return Integer.parseInt(first) - Integer.parseInt(second);
                                }

                                return -1;
                            }
                        } );

                        MultiCheckCategory category = new MultiCheckCategory(parentKey, playLists, 0, description, avatarURL);

                        for (int i = 0; i < playLists.size(); i++) {

                            if (PublicPlayListsDb.getPublicPlayListsDb().isPublicPlayListAdded(categoryId, playLists.get(i).getKey())) {
                                category.checkChild(i);
                            }
                        }

                        categories.add(category);
                    }
                }

                if(categories.size() > 0) {
                    setCategories(categories);

                    /*
                    // display a dialog which allows the user to select the channels to import
                    new ExpandableCheckRecyclerViewPreferenceDialog(PublicPlayListsImportActivity.this, categories)
                            .title(R.string.download_public_play_lists)
                            .positiveText(R.string.download_public_play_lists_button)
                            .onPositive(new MaterialDialog.SingleButtonCallback() {
                                @Override
                                public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {

                                    for (MultiCheckCategory category : categories) {

                                        List<MultiCheckPlayList> playLists = category.getItems();

                                        boolean[] selected = category.selectedChildren;
                                        Object[] array = playLists.toArray();

                                        for (int i = 0; i < selected.length; i++) {

                                            MultiCheckPlayList multiCheckPlayList = (MultiCheckPlayList) array[i];

                                            if (selected[i] == true) {

                                                YouTubePublicPlayList publicPlayList = new YouTubePublicPlayList(multiCheckPlayList.getKey(), multiCheckPlayList.getName());

                                                PublicPlayListsDb.getPublicPlayListsDb().addOrUpdate(publicPlayList);
                                            }
                                            else {
                                                PublicPlayListsDb.getPublicPlayListsDb().remove(multiCheckPlayList.getKey());
                                            }

                                        }
                                    }

                                    dialog.dismiss();

                                    Toast.makeText(PublicPlayListsImportActivity.this, R.string.pref_public_play_lists_updated,Toast.LENGTH_SHORT).show();
                                }
                            })
                            .negativeText(R.string.cancel)
                            .onNegative(new MaterialDialog.SingleButtonCallback() {
                                @Override
                                public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                                    dialog.dismiss();
                                }
                            })
                            .build()
                            .show();
                            */
                }
                else {
                    new AlertDialog.Builder(PublicPlayListsImportActivity.this)
                            .setMessage(R.string.no_public_play_lists_found)
                            .setNeutralButton(R.string.ok, null)
                            .show();
                }

            };


            @Override
            public void onCancelled(DatabaseError databaseError) {
                progressDialog.dismiss();

                Toast.makeText(PublicPlayListsImportActivity.this, databaseError.getMessage(), Toast.LENGTH_LONG).show();
            }

        });

        Button importButton = findViewById(R.id.public_play_list_import_ok_button);
        importButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                List<YouTubePublicPlayList> validLists = new ArrayList<>();

                for (MultiCheckCategory category : categories) {

                    List<MultiCheckPlayList> playLists = category.getItems();

                    boolean[] selected = category.selectedChildren;
                    Object[] array = playLists.toArray();

                    String categoryTitle = category.getTitle();

                    // Category Id, first two numerical characters
                    String categoryId = categoryTitle.substring(0, 2);

                    // Category title after removing the Id above
                    categoryTitle = categoryTitle.substring(5);

                    boolean bCategoryCreated = false;

                    for (int i = 0; i < selected.length; i++) {

                        MultiCheckPlayList multiCheckPlayList = (MultiCheckPlayList) array[i];

                        if (selected[i] == true) {

                            if(bCategoryCreated == false) {

                                // Create the category if not exists
                                SafetoonsCategoriesDb.getSafetoonsCategoriesDb().addOrUpdate(new SafetoonsCategory(categoryId, categoryTitle, category.getDescription(), category.getAvatarURL()));

                                bCategoryCreated = true;
                            }

                            String name = multiCheckPlayList.getName();

                            int orderIdInt = Integer.MAX_VALUE;

                            if(name.length() > 5) {

                                // List orderId, first two numerical characters
                                String orderId = name.substring(0, 2);

                                try {
                                    orderIdInt = Integer.parseInt(orderId);
                                    // List name after removing the orderId above
                                    name = name.substring(5);
                                } catch (NumberFormatException | NullPointerException nfe) {
                                }
                            }

                            YouTubePublicPlayList publicPlayList = new YouTubePublicPlayList(multiCheckPlayList.getKey(), categoryId, name, orderIdInt);

                            validLists.add(publicPlayList);

                            PublicPlayListsDb.getPublicPlayListsDb().addOrUpdate(publicPlayList);
                        }
                        else {
                            PublicPlayListsDb.getPublicPlayListsDb().remove(categoryId, multiCheckPlayList.getKey());
                        }

                    }
                }

                // Remove lists not in the server
                PublicPlayListsDb.getPublicPlayListsDb().removeNotInList(validLists);

                // Remove empty Categorise
                SafetoonsCategoriesDb.getSafetoonsCategoriesDb().clearEmptySafetoonsCategories();

                finish();

                Toast.makeText(PublicPlayListsImportActivity.this, R.string.pref_public_play_lists_updated,Toast.LENGTH_SHORT).show();
            }
        });

        Button cancelButton = findViewById(R.id.public_play_list_import_cancel_button);
        cancelButton.setOnClickListener(new View.OnClickListener() {


            @Override
            public void onClick(View v) {
                finish();
            }
        });

    }

    public static boolean isInteger(String s) {
        try {
            Integer.parseInt(s);
        } catch(NumberFormatException e) {
            return false;
        } catch(NullPointerException e) {
            return false;
        }
        // only got here if we didn't return false
        return true;
    }

    /**
     * Set Categories\Items to be displayed in this dialog.
     *
     * @param categories A list of items to be displayed.
     */
    public void setCategories(List<MultiCheckCategory> categories) {
        listAdapter = new MultiCheckFirebaseAdapter(categories);

        recycler.setAdapter(listAdapter);

        listAdapter.notifyDataSetChanged();
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            // close this activity when the user clicks on the back button (action bar)
            case android.R.id.home:
                finish();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public void onBackPressed() {
        //super.onBackPressed();

        finish();
    }
}
