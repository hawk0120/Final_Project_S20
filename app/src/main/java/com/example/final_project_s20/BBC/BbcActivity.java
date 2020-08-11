package com.example.final_project_s20.BBC;

import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;

import android.app.AlertDialog;
import android.content.ContentValues;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;


import com.example.final_project_s20.R;
import com.google.android.material.navigation.NavigationView;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserFactory;

import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;


public class BbcActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {

    private ArrayList<News> elements = new ArrayList<>();
    private ListView myList;
    private MyListAdapter myAdapter;
    public static final String ACTIVITY_NAME = "BBC_ACTIVITY";
    private ArrayList<News> tempFavourite = new ArrayList<>();
    //Load content
    private ProgressBar progressBar;
    private String title;
    private String description;
    private String link;
    private String date;
    SQLiteDatabase db;
    MyOpener dbOpener;
    //fragment
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
        setContentView(R.layout.activity_bbc);

        //Load data from BBC News into ArrayList
        progressBar = (ProgressBar)findViewById(R.id.bbcProgressBar);
        progressBar.setVisibility(View.VISIBLE);

        dbOpener = new MyOpener(this);
        db = dbOpener.getWritableDatabase();

        String [] urlArray = {"http://feeds.bbci.co.uk/news/world/us_and_canada/rss.xml"};
        BbcQuery req1 = new BbcQuery();
        req1.execute(urlArray);

        //Create list view
        myList = findViewById(R.id.newsListView);
        myList.setAdapter( myAdapter = new MyListAdapter());

        boolean isTablet = findViewById(R.id.frameLayout) != null;

        //User can use long click to add news to favourite list
        myList.setOnItemLongClickListener((parent, view, position, id) -> {
            new AlertDialog.Builder(BbcActivity.this)
                    .setTitle(getString(R.string.BBC_AlertDialogTitle))
                    .setMessage(getString(R.string.BBC_rowNum) + " " + position + "\n" + getString(R.string.BBC_databaseId) + " " + id)
                    .setNegativeButton(getString(R.string.BBC_Favourite_NegativeButton),null)
                    .setPositiveButton(getString(R.string.BBC_Favourite_PositiveButton), (click, arg) -> {

                        //if confirmed, save the news to database as well
                        ContentValues newRowValues = new ContentValues();
                        newRowValues.put(MyOpener.COL_TITLE, elements.get(position).getTitle());
                        newRowValues.put(MyOpener.COL_DESCRIPTION, elements.get(position).getDescription());
                        newRowValues.put(MyOpener.COL_LINK, elements.get(position).getLink());
                        newRowValues.put(MyOpener.COL_DATE, elements.get(position).getDate());

                        //Check if the news already exist in the Favourite List
                        //Load favourite list from database into tempFavourite
                        loadDataFromDatabase();
                        boolean isExist = false;
                        for(News tempNews:tempFavourite){
                            if(elements.get(position).getTitle().equals(tempNews.getTitle())) isExist=true;
                        }
                        if(isExist == false) {
                            long newId = db.insert(MyOpener.TABLE_NAME, null, newRowValues);
                        }else{
                            Toast.makeText(this, "Cannot add, this news already exist in the favourite list", Toast.LENGTH_SHORT).show();
                        }
                        myAdapter.notifyDataSetChanged();
                    }).create().show();
            return true;   });

        Button favouriteButton = (Button)findViewById(R.id.GoToFavourite);
        favouriteButton.setOnClickListener( new View.OnClickListener()
        {  public void onClick(View v){
            Intent goToChatRoom = new Intent(BbcActivity.this, FavourityActivity.class);
            startActivity(goToChatRoom);
        } });


        //create the fragment of news
        myList.setOnItemClickListener((list, item, position, id) -> {
            //Create a bundle to pass data to the new fragment
            Bundle dataToPass = new Bundle();
            dataToPass.putString(NEWS_TITLE, elements.get(position).getTitle() );
            dataToPass.putString(NEWS_DESCRIPTION, elements.get(position).getDescription() );
            dataToPass.putString(NEWS_LINK, elements.get(position).getLink() );
            dataToPass.putString(NEWS_DATE, elements.get(position).getDate() );

            dataToPass.putInt(NEWS_POSITION, position);
            dataToPass.putLong(NEWS_ID, id);

            if(isTablet)
            {
                dFragment = new DetailsFragment();
                dFragment.setArguments( dataToPass );
                getSupportFragmentManager()
                        .beginTransaction()
                        .replace(R.id.frameLayout, dFragment)
                        .commit();
            }
            else //isPhone
            {
                Intent nextActivity = new Intent(BbcActivity.this, EmptyActivity.class);
                nextActivity.putExtras(dataToPass); //send data to next activity
                startActivity(nextActivity); //make the transition
            }
        });

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
        dbOpener = new MyOpener(this);
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
            tempFavourite.add(new News(title, description, link, date));
        }
    }

    /**
     * inner class that deals with thread synchronization
     */
    private class BbcQuery extends AsyncTask<String, Integer, String>
    {
        /**
         * @param args - the argument that passed by the BbcQuery.execute(urlArray) method
         * @return result object
         */
        public String doInBackground(String ... args)
        {
            try {
                URL url = new URL(args[0]);
                HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();
                InputStream response = urlConnection.getInputStream();

                XmlPullParserFactory factory = XmlPullParserFactory.newInstance();
                factory.setNamespaceAware(false);
                XmlPullParser xpp = factory.newPullParser();
                xpp.setInput( response  , "UTF-8");
                int eventType = xpp.getEventType();
                //The boolean variable is used to check if all elements of a news have been catched
                boolean titleCount= false;
                boolean desCount = false;
                boolean linkCount = false;
                boolean dateCount = false;
                while(eventType != XmlPullParser.END_DOCUMENT)
                {
                    if(eventType == XmlPullParser.START_TAG)
                    {
                        if(xpp.getName().equals("title")||xpp.getName().equals("description")||xpp.getName().equals("link")||xpp.getName().equals("pubDate"))
                        {
                            if(xpp.getName().equals("title"))
                            {
                                xpp.next();
                                title = xpp.getText();
                                titleCount=true;
                                publishProgress(25);
                            }
                            else if(xpp.getName().equals("description")) {
                                xpp.next();
                                description = xpp.getText();
                                desCount=true;
                                publishProgress(50);
                            }
                            else if(xpp.getName().equals("link")) {
                                xpp.next();
                                link = xpp.getText();
                                linkCount=true;
                                publishProgress(75);
                            }
                            else if(xpp.getName().equals("pubDate")) {
                                xpp.next();
                                date = xpp.getText();
                                dateCount=true;
                            }
                            //If all elements of a news have been catched, then create the news,
                            //add to the Arraylist and reset all boolean variables to check next news.
                            if(titleCount==true&&desCount==true&&linkCount==true&&dateCount==true) {
                                elements.add(new News(title, description, link, date));
                                titleCount = false;desCount = false;linkCount = false;dateCount = false;
                            }
                        }
                    }
                    eventType = xpp.next(); //move to the next xml event and store it in a variable
                }
            }
            catch (Exception e) {
                Log.e("Error", e.getMessage());
            }
            return "Done";
        }

        /**
         * @param args - the argument that used to update the progressBar
         */
        public void onProgressUpdate(Integer ... args)
        {
            progressBar.setVisibility(View.VISIBLE);
            progressBar.setProgress(args[0]);
        }

        /**
         * @param fromDoInBackground - the result object passed from doInBackground()
         */
        public void onPostExecute(String fromDoInBackground)
        {
            Log.i("HTTP", fromDoInBackground);
            progressBar.setVisibility(View.INVISIBLE );
        }
    }

    /**
     * the adapter inner class that provide data for the listView
     */
    private class MyListAdapter extends BaseAdapter {

        /**
         * @return the number of items
         */
        public int getCount() { return elements.size();}

        /**
         * @param position - the row position of a listView content
         * @return the object to show at row position
         */
        public Object getItem(int position) { return  elements.get(position).getTitle() ; }

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
        inflater.inflate(R.menu.menu3, menu);

        return true;
    }

    /**
     * @param item - {@link MenuItem} object in the {@link Menu}
     * @return true
     */
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        String message = null;
        //Look at your menu XML file. Put a case for every id in that file:
        switch(item.getItemId())
        {
            //what to do when the menu item is selected:
            case R.id.bbc_fav_pic:
                message = getResources().getString(R.string.BBC_Toolbar_Option)
                        +" "+getResources().getString(R.string.BBC_Favourite);
                Intent goToChatRoom = new Intent(BbcActivity.this, FavourityActivity.class);
                startActivity(goToChatRoom);
                break;

            case R.id.bbc_help:
                message = getResources().getString(R.string.BBC_Toolbar_Option)
                        +" "+getResources().getString(R.string.BBC_help);
                new AlertDialog.Builder(BbcActivity.this)
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

            goTo = new Intent(BbcActivity.this, FavourityActivity.class);
            startActivity(goTo);

        }
        DrawerLayout drawerLayout = findViewById(R.id.drawer_layout);
        drawerLayout.closeDrawer(GravityCompat.START);
        return false;
    }
}
