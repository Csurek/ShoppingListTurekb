package org.projects.shoppinglist;

import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.DisplayMetrics;
import android.util.Log;
import android.util.SparseBooleanArray;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.firebase.client.AuthData;
import com.firebase.client.Firebase;
import com.firebase.client.FirebaseError;
import com.firebase.ui.FirebaseListAdapter;
import com.google.android.gms.appindexing.Action;
import com.google.android.gms.appindexing.AppIndex;
import com.google.android.gms.common.api.GoogleApiClient;

import java.util.ArrayList;
import java.util.Locale;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = "com.example.StateChange";
    //Declaring things here
    ListView listView;
    EditText bagInput;
    EditText bagQuantity;
    Spinner spinners;
    Locale myLocale;
    Firebase mref;
    FirebaseListAdapter<Product> fireAdapter;

    public FirebaseListAdapter<Product> getMyAdapter() {return fireAdapter;}
    public Product getItem (int index){
        return getMyAdapter().getItem(index);
    }

    private GoogleApiClient client;

    //Saving the last deleted product for Snackbar to restore
    Product lastDeletedProduct;
    int lastDeletedPosition;
    public void saveCopy()
    {
        lastDeletedPosition = listView.getCheckedItemPosition();
        Log.v("szar", String.valueOf(lastDeletedPosition));
        lastDeletedProduct = fireAdapter.getItem(lastDeletedPosition);
    }

    //Confirmation dialog for Clear all function
    public void showDialog() {
        //showing our dialog.
        ClearAllDialog dialog = new ClearAllDialog() {
            @Override
            protected void positiveClick() {
                //Here we override the methods and can now
                //do something
                Toast toast = Toast.makeText(getApplicationContext(),
                        "Shopping list cleared", Toast.LENGTH_LONG);
                mref.setValue(null);
                getMyAdapter().notifyDataSetChanged();
                toast.show();
            }

            @Override
            protected void negativeClick() {
                //Here we override the method and can now do something
                Toast toast = Toast.makeText(getApplicationContext(),
                        "negative button clicked", Toast.LENGTH_SHORT);
                toast.show();
            }
        };
        dialog.show(getFragmentManager(), "MyFragment");
    }

    //onCreate start
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        if (savedInstanceState != null) {
            //we don't use the bag anymore
        }
        Log.i(TAG, "onCreate");

        //Actionbar
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        //Just a greeting at app start
        SharedPreferences prefs = getSharedPreferences("my_prefs", MODE_PRIVATE);
        String email = prefs.getString("email", "");
        String passs = prefs.getString("passw", "");
        Toast.makeText(
                this,
                "Welcome back: " + email, Toast.LENGTH_SHORT).show();



        //Setting up the firebase
        mref = new Firebase("https://incandescent-heat-9274.firebaseio.com/items");
        //FirebaseListAdapter
        fireAdapter = new FirebaseListAdapter<Product>(this, Product
                .class, android.R.layout.simple_list_item_checked, mref){
            @Override
            protected void populateView(View v, Product product, int i){
                TextView text = (TextView) v.findViewById(android.R.id.text1);
                text.setText(product.toString());
            }
        };

        //Logging in a user
        Log.v("Authentication", email + passs);
        mref.authWithPassword(email, passs, new Firebase.AuthResultHandler() {
            @Override
            public void onAuthenticated(AuthData authData) {
                System.out.println("User ID: " + authData.getUid() + ", Provider: " + authData.getProvider());
                Log.v("User ID",authData.getUid());
            }
            @Override
            public void onAuthenticationError(FirebaseError firebaseError) {
                Log.v("ERROR", "There was an error with the authentication");
            }
        });
        //Authentication check
        mref.addAuthStateListener(new Firebase.AuthStateListener() {
            @Override
            public void onAuthStateChanged(AuthData authData) {
                if (authData != null) {
                    Log.v("User", "Successfully logged in");
                } else {
                    Log.v("ERROR", "No user");
                }
            }
        });

        //Setting up a listView with Firebase adapter
        listView = (ListView) findViewById(R.id.list);
        listView.setAdapter(fireAdapter);
        listView.setChoiceMode(ListView.CHOICE_MODE_SINGLE);

        //We add the Add button and the functions
        Button addButton = (Button) findViewById(R.id.addButton);
        addButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //Getting info from the input fields
                bagInput = (EditText) findViewById(R.id.inputBag);
                String bagText = bagInput.getText().toString();
                bagQuantity = (EditText) findViewById(R.id.inputQuantity);
                String bagNumber = bagQuantity.getText().toString();
                spinners = (Spinner) findViewById(R.id.spinner1);
                String volume = String.valueOf(spinners.getSelectedItem());
                final int bagQu = Integer.parseInt(bagNumber);

                //Combining the input fields and push it to Firebase
                Product g = new Product(bagQu, volume, bagText);
                Log.v("E_CHILD_ADDED", g.toString());
                mref.push().setValue(g);
                getMyAdapter().notifyDataSetChanged();
            }
        });

        //CLEAR function
        /*final Button clrButton = (Button) findViewById(R.id.clrButton);
        clrButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

            }
        });*/

        //DELETE function
        Button dltButton = (Button) findViewById(R.id.dltButton);
        dltButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //Saving the checked item first for Snackbar
                SparseBooleanArray checkedItems = listView.getCheckedItemPositions();
                saveCopy();
                for (int i = fireAdapter.getCount() - 1; i >= 0; i--) {
                    if (checkedItems.get(i)) {
                        // This item is checked and can be removed
                        getMyAdapter().getRef(i).setValue(null);
                    }
                }
                getMyAdapter().notifyDataSetChanged();

                //Creating Snackbar and retrieving the last deleted item
                final View parent = listView;
                Snackbar snackbar = Snackbar
                        .make(parent, "Item Deleted", Snackbar.LENGTH_LONG)
                        .setAction("UNDO", new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {
                                mref.push().setValue(lastDeletedProduct);
                                getMyAdapter().notifyDataSetChanged();
                                Snackbar snackbar = Snackbar.make(parent, "Item restored!", Snackbar.LENGTH_SHORT);
                                snackbar.show();
                            }
                        });
                snackbar.show();
                getMyAdapter().notifyDataSetChanged();
            }


        });


        // ATTENTION: This was auto-generated to implement the App Indexing API.
        // See https://g.co/AppIndexing/AndroidStudio for more information.
        client = new GoogleApiClient.Builder(this).addApi(AppIndex.API).build();

    }
    //Changing the language to the selected option
    public void setLocale(String lang) {
        myLocale = new Locale(lang);
        Resources res = getResources();
        DisplayMetrics dm = res.getDisplayMetrics();
        Configuration conf = res.getConfiguration();
        conf.locale = myLocale;
        res.updateConfiguration(conf, dm);
        Intent refresh = new Intent(this, MainActivity.class);
        startActivity(refresh);
    }

    //Converting list to pass it as a string when sharing
    public String convertListToString()
    {
        String result = "Here is the shopping list: ";
        for (int i = 0; i<fireAdapter.getCount();i++)
        {
            Product p = (Product) fireAdapter.getItem(i);
            result = result + "\n"+ p;
        }
        return result;
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode==1) //exited our preference screen
        {
            Toast toast =
                    Toast.makeText(getApplicationContext(), "back from preferences", Toast.LENGTH_LONG);
            toast.setText("back from our preferences");
            toast.show();

            SharedPreferences prefi = getSharedPreferences("my_prefs", MODE_PRIVATE);
            String langu = prefi.getString("langu", "");
            int posi = Integer.parseInt(langu);
            Log.v("Language",langu);
            if (posi == 1) {
                setLocale("en");
            } else if (posi == 2){
                setLocale("hu");
            }
            //here you could put code to do something.......
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    public void setPreferences() {
        Intent intent = new Intent(this, SettingsActivity.class);
        //we can use this, if we need to know when the user exists our preference screens
        startActivityForResult(intent, 1);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        //We we set that we want to use the xml file
        //under the menu directory in the resources and
        // that we want to use the specific file called "main.xml"
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    //Checking which options we selected and starting the corresponding action
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        switch (item.getItemId()) {

            case android.R.id.home:
                Toast.makeText(this, "Application icon clicked!",
                        Toast.LENGTH_SHORT).show();
                return true; //return true, means we have handled the event
            case R.id.item_settings:
                setPreferences();
                return true;
            case R.id.clear_all:
                showDialog();
                Toast.makeText(this, "Cleared!", Toast.LENGTH_SHORT)
                        .show();
                return true;
            case R.id.item_share:
                convertListToString();
                Intent intent = new Intent(Intent.ACTION_SEND);
                intent.setType("text/plain"); //MIME type
                intent.putExtra(Intent.EXTRA_TEXT, convertListToString());
                startActivity(intent);
                Toast.makeText(this, "Share it!", Toast.LENGTH_SHORT)
                        .show();
                return true;
        }
        return false; //we did not handle the event
    }

    //Changing the background color after coming back from settings
    @Override
    public void onResume() {
        super.onResume();
        LinearLayout rl = (LinearLayout) findViewById(R.id.container);
        SharedPreferences prefs = getSharedPreferences("my_prefs", MODE_PRIVATE);
        int bgc = Color.parseColor(prefs.getString("background_color", "#FFFFFF"));
        rl.setBackgroundColor(bgc);
    }

    @Override
    public void onSaveInstanceState(Bundle savedInstanceState) {
        //savedInstanceState.putParcelableArrayList("SaveList", bag);
        //We don't use bag anymore
    }

    @Override
    public void onStop() {
        super.onStop();
        // ATTENTION: This was auto-generated to implement the App Indexing API.
        // See https://g.co/AppIndexing/AndroidStudio for more information.
        Action viewAction = Action.newAction(
                Action.TYPE_VIEW,
                "Main Page",
                // make sure this auto-generated web page URL is correct.
                // Otherwise, set the URL to null.
                Uri.parse("http://host/path"),
                Uri.parse("android-app://org.projects.shoppinglist/http/host/path")
        );
        AppIndex.AppIndexApi.end(client, viewAction);
        client.disconnect();
    }
}
