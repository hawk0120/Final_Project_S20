package com.example.final_project_s20;


import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;

import com.example.final_project_s20.BBC.BbcActivity;
import com.example.final_project_s20.BBC.FavourityActivity;
import com.google.android.material.navigation.NavigationView;
import com.google.android.material.snackbar.Snackbar;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Toast;


/**
 * the main page of the application. User can go to different Activities by using
 * images, buttons, icons in the Toolbar, NavigationDraw.
 */
public class MainActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {

    /**
     * @param savedInstanceState - reference to a Bundle object that is
     *                           passed into the onCreate method of {@link MainActivity}.
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        CheckBox chk = (CheckBox)findViewById(R.id.CheckBox);
        chk.setChecked(false);
        chk.setOnCheckedChangeListener( new CheckBox.OnCheckedChangeListener()
        {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if(isChecked==false)
                    Snackbar.make(buttonView, getResources().getString(R.string.BBC_CheckBoxNo),
                            Snackbar.LENGTH_LONG).setAction("Undo", click -> chk.setChecked(!isChecked)).show();
                else if(isChecked==true) {
                    Snackbar.make(buttonView, getResources().getString(R.string.BBC_CheckBoxYes),
                            Snackbar.LENGTH_LONG).setAction("Undo", click -> chk.setChecked(!isChecked)).show();
                }
            } } );

        ImageButton loginBtn1 = (ImageButton)findViewById(R.id.BBC_ImageButton);
        loginBtn1.setOnClickListener(click -> {
            Toast.makeText(MainActivity.this, getResources().getString(R.string.BBC_ButtonClick), Toast.LENGTH_LONG ).show();
            Intent goToProfile = null;
            if(chk.isChecked()==false) {
                goToProfile = new Intent(MainActivity.this, BbcActivity.class);
            }else if(chk.isChecked()==true){
                goToProfile = new Intent(MainActivity.this, FavourityActivity.class);
            }
            startActivity(goToProfile);
        });

        Button loginBtn2 = (Button)findViewById(R.id.BBC_Button);
        loginBtn2.setOnClickListener(click -> {
            Toast.makeText(MainActivity.this, getResources().getString(R.string.BBC_ButtonClick), Toast.LENGTH_LONG ).show();
            Intent goToProfile = new Intent(MainActivity.this, BbcActivity.class);
            startActivity(goToProfile);
        });

        //Toolbar and NavigationDraw
        //This gets the toolbar from the layout:
        Toolbar tBar = (Toolbar)findViewById(R.id.toolbar);

        //This loads the toolbar, which calls onCreateOptionsMenu below:
        setSupportActionBar(tBar);

        //For NavigationDrawer:
        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(this,
                drawer, tBar, R.string.BBC_open, R.string.BBC_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);


        SharedPreferences prefs=getSharedPreferences("versionInfo", Activity.MODE_PRIVATE);
        String versionNumber = prefs.getString("versionNumber","");

        EditText editText1 = findViewById(R.id.EditText1);
        editText1.setText(versionNumber);
    }

    /**
     * inflates the menu from XML layout
     * @param menu - {@link Menu} object that is applied by the activity
     * @return true
     */
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu items for use in the action bar
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu1, menu);

        return true;
    }

    /**
     * @param item - {@link MenuItem} object in the {@link Menu}
     * @return true
     */
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        String message = null;
        Intent goTo;
        switch(item.getItemId())
        {
            case R.id.goToBBC:
                message = getResources().getString(R.string.BBC_Toolbar_Option)
                        +" "+getResources().getString(R.string.BBC_News);
                goTo = new Intent(MainActivity.this, BbcActivity.class);
                startActivity(goTo);
                break;
            case R.id.bbc_help:
                message = getResources().getString(R.string.BBC_Toolbar_Option)
                        +" "+getResources().getString(R.string.BBC_help);
                new AlertDialog.Builder(MainActivity.this)
                        .setTitle(getString(R.string.BBC_AlertDialogTitle3))
                        .setMessage(getString(R.string.BBC_HelpMes1) + "\n" + getString(R.string.BBC_HelpMes2) + "\n"
                                + getString(R.string.BBC_HelpMes3) + "\n" + getString(R.string.BBC_HelpMes4) + "\n"
                                + getString(R.string.BBC_HelpMes5) + "\n")
                        .setNegativeButton(getString(R.string.BBC_Help_NegativeButton),null)
                        .setPositiveButton(getString(R.string.BBC_Help_PositiveButton), null).show();
                break;
        }
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
        return true;
    }

    /**
     * @param item - {@link MenuItem} object in the NavigationDrawer
     * @return false
     */
    @Override
    public boolean onNavigationItemSelected( MenuItem item) {

        String message = null;

        int id = item.getItemId();
        Intent goTo;

        if(id == R.id.BBC_Button){

            goTo = new Intent(MainActivity.this, BbcActivity.class);
            startActivity(goTo);

        }

        DrawerLayout drawerLayout = findViewById(R.id.drawer_layout);
        drawerLayout.closeDrawer(GravityCompat.START);
        return false;
    }

    /**
     * save version number to SharedPreferences
     */
    @Override
    public void onPause() {
        super.onPause();
        Toast.makeText(MainActivity.this, getResources().getString(R.string.OnPause), Toast.LENGTH_LONG ).show();
        EditText editText1 = findViewById(R.id.EditText1);
        String versionNumber = editText1.getText().toString();
        SharedPreferences prefs=getSharedPreferences("versionInfo", Context.MODE_PRIVATE);
        SharedPreferences.Editor edit=prefs.edit();
        edit.putString("versionNumber", versionNumber);
        edit.commit();

    }
}