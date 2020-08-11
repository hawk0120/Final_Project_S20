package com.example.final_project_s20.BBC;

import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.final_project_s20.R;
import com.google.android.material.navigation.NavigationView;

import java.util.ArrayList;


public class FavourityActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {

    private ListView myList;
    private MyListAdapter myAdapter;
    private ArrayList<News> favourite = new ArrayList<>();

    SQLiteDatabase db;

    public static final String NEWS_TITLE = "TITLE";
    public static final String NEWS_DESCRIPTION = "DESCRIPTION";
    public static final String NEWS_LINK = "LINK";
    public static final String NEWS_DATE = "DATE";
    public static final String NEWS_POSITION = "POSITION";
    public static final String NEWS_ID = "ID";

    DetailsFragment dFragment;

    /**
     * @param savedInstanceState - the Bundle object that is passed into the onCreate method
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_favourity);

        loadDataFromDatabase();

        //Load data from BBC News into ArrayList
        //Create list view
        myList = findViewById(R.id.favouriteListView);
        myList.setAdapter( myAdapter = new MyListAdapter());

        boolean isTablet = findViewById(R.id.frameLayout) != null;

        //User can use long click to add news to favourite list
        myList.setOnItemLongClickListener((parent, view, position, id) -> {
            new AlertDialog.Builder(FavourityActivity.this)
                    .setTitle(getString(R.string.BBC_AlertDialogTitle2))
                    .setMessage(getString(R.string.BBC_rowNum) + " " + position + "\n" + getString(R.string.BBC_databaseId) + " " + id)
                    .setNegativeButton(getString(R.string.BBC_Favourite_NegativeButton),null)
                    .setPositiveButton(getString(R.string.BBC_Favourite_PositiveButton), new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            deleteMessage( favourite.get(position));
                            favourite.remove(position);
                            myAdapter.notifyDataSetChanged();
                        }}).show();
            return true;   });

        //create the fragment of news
        myList.setOnItemClickListener((list, item, position, id) -> {
            //Create a bundle to pass data to the new fragment
            Bundle dataToPass = new Bundle();
            dataToPass.putString(NEWS_TITLE, favourite.get(position).getTitle() );
            dataToPass.putString(NEWS_DESCRIPTION, favourite.get(position).getDescription() );
            dataToPass.putString(NEWS_LINK, favourite.get(position).getLink() );
            dataToPass.putString(NEWS_DATE, favourite.get(position).getDate() );
            dataToPass.putInt(NEWS_POSITION, position);
            dataToPass.putLong(NEWS_ID, id);

            if(isTablet)
            {
                dFragment = new DetailsFragment(); //add a DetailFragment
                dFragment.setArguments( dataToPass ); //pass it a bundle for information
                getSupportFragmentManager()
                        .beginTransaction()
                        .replace(R.id.frameLayout, dFragment) //Add the fragment in FrameLayout
                        .commit(); //actually load the fragment.
            }
            else //isPhone
            {
                Intent nextActivity = new Intent(FavourityActivity.this, EmptyActivity.class);
                nextActivity.putExtras(dataToPass); //send data to next activity
                startActivity(nextActivity); //make the transition
            }});

        //Toolbar and NavigationDraw
        Toolbar tBar = (Toolbar)findViewById(R.id.toolbar);
        setSupportActionBar(tBar);

        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(this,
                drawer, tBar, R.string.BBC_open, R.string.BBC_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);
    }


    /**
     * load data from database
     */
    private void loadDataFromDatabase()
    {
        MyOpener dbOpener = new MyOpener(this);
        db = dbOpener.getWritableDatabase();
        String [] columns = {MyOpener.COL_ID, MyOpener.COL_TITLE, MyOpener.COL_DESCRIPTION, MyOpener.COL_LINK, MyOpener.COL_DATE};
        Cursor myCursor = db.query(false, MyOpener.TABLE_NAME, columns, null, null, null, null, null, null);

        int titleColumnIndex = myCursor.getColumnIndex(MyOpener.COL_TITLE);
        int descriptionColumnIndex = myCursor.getColumnIndex(MyOpener.COL_DESCRIPTION);
        int linkColumnIndex = myCursor.getColumnIndex(MyOpener.COL_LINK);
        int dateColumnIndex = myCursor.getColumnIndex(MyOpener.COL_DATE);
        int idColIndex = myCursor.getColumnIndex(MyOpener.COL_ID);

        while(myCursor.moveToNext())
        {
            String title = myCursor.getString(titleColumnIndex);
            String description = myCursor.getString(descriptionColumnIndex);
            String link = myCursor.getString(linkColumnIndex);
            String date = myCursor.getString(dateColumnIndex);
            long id = myCursor.getLong(idColIndex);
            //add the news to the array list:
            favourite.add(new News(title, description, link, date));
        }
    }

    /**
     * @param news - the news that will be deleted from the database and listView
     */
    protected void deleteMessage(News news)
    {
        db.delete(MyOpener.TABLE_NAME, MyOpener.COL_TITLE + "= ?", new String[] {news.getTitle()});
    }

    /**
     * the adapter inner class that provide data for the listView
     */
    private class MyListAdapter extends BaseAdapter {

        /**
         * @return the number of items
         */
        public int getCount() { return favourite.size();}

        /**
         * @param position - the row position of a listView content
         * @return the object to show at row position
         */
        public Object getItem(int position) { return  favourite.get(position).getTitle() ; }

        /**
         * @param position - the row position of a listView content
         * @return database id of the item at the position
         */
        public long getItemId(int position) { return (long) position; }

        /**
         * @param position - the row position of a listView content
         * @param old - the previous view at the position
         * @param parent - contains other views, describes the layout of the Views in the group
         * @return a View object to go in a row of the ListView
         */
        public View getView(int position, View old, ViewGroup parent)
        {
            View newView = null;
            LayoutInflater inflater = getLayoutInflater();
            newView = inflater.inflate(R.layout.row_layout, parent, false);
            TextView tView = newView.findViewById(R.id.newsTitle);
            tView.setText(getItem(position).toString());
            return newView;
        }
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

        switch(item.getItemId())
        {
            case R.id.bbc_pic:
                message = getResources().getString(R.string.BBC_Toolbar_Option)
                        +" "+getResources().getString(R.string.BBC_News);
                Intent goToChatRoom = new Intent(FavourityActivity.this, BbcActivity.class);
                startActivity(goToChatRoom);
                break;

            case R.id.bbc_help:
                message = getResources().getString(R.string.BBC_Toolbar_Option)
                        +" "+getResources().getString(R.string.BBC_help);
                new AlertDialog.Builder(FavourityActivity.this)
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

        if(id == R.id.goToFav){

            goTo = new Intent(FavourityActivity.this, BbcActivity.class);
            startActivity(goTo);
        }

        DrawerLayout drawerLayout = findViewById(R.id.drawer_layout);
        drawerLayout.closeDrawer(GravityCompat.START);
        return false;
    }
}