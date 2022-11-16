package com.example.newsgateway;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.viewpager2.widget.ViewPager2;

import android.annotation.SuppressLint;
import android.content.res.Configuration;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.SubMenu;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

public class MainActivity extends AppCompatActivity {

  private static final String TAG = "MainActivity";

  private Source currSource;
  private boolean topicFilter;
  private boolean countryFilter;
  private boolean languageFilter;
  private String currTopic;
  private String currCountry;
  private String currLanguage;

  // Menu variables
  private Menu menu;
  private final String[] FILTERS = {"Topics", "Countries", "Languages"};
  private final HashMap<String, ArrayList<Source>> topicsMenu = new HashMap<>();
  private final HashMap<String, ArrayList<Source>> countriesMenu = new HashMap<>();
  private final HashMap<String, ArrayList<Source>> languagesMenu = new HashMap<>();

  // DrawerLayout variables
  private DrawerLayout mDrawerLayout;
  private ListView mDrawerList;
  private ActionBarDrawerToggle mDrawerToggle;
  private Set<Source> drawerSources = new HashSet<>();
  private final ArrayList<Source> listSources = new ArrayList<>();
  private ArrayAdapter<Source> arrayAdapter;

  // ViewPager variables
  private ArticleAdapter articleAdapter;
  ViewPager2 viewPager;
  private final ArrayList<Article> currArticles = new ArrayList<>();



  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_main);



    // View pager
    viewPager = findViewById(R.id.viewPager);
    articleAdapter = new ArticleAdapter(this, currArticles);
    viewPager.setAdapter(articleAdapter);
    viewPager.setOrientation(ViewPager2.ORIENTATION_HORIZONTAL);

    // Drawer layout
    mDrawerLayout = findViewById(R.id.drawerLayout);
    mDrawerList = findViewById(R.id.drawerList);

    mDrawerList.setOnItemClickListener(
        (parent, view, position, id) ->{
          displayArticle(position);
          mDrawerLayout.closeDrawer(mDrawerList);
        });

    mDrawerToggle = new ActionBarDrawerToggle(
        this,
        mDrawerLayout,
        R.string.drawer_open,
        R.string.drawer_closed
    );
    if (getSupportActionBar() != null){
      getSupportActionBar().setDisplayHomeAsUpEnabled(true);
      getSupportActionBar().setHomeButtonEnabled(true);
    }

    // Display all articles

    topicFilter = false;
    countryFilter = false;
    languageFilter = false;


    // Get sources
    new Thread(new SourceLoaderRunnable(this)).start();
  }

  @Override
  protected void onPostCreate(@Nullable Bundle savedInstanceState) {
    super.onPostCreate(savedInstanceState);
    mDrawerToggle.syncState();
  }

  @Override
  public void onConfigurationChanged(@NonNull Configuration newConfig) {
    super.onConfigurationChanged(newConfig);
    mDrawerToggle.onConfigurationChanged(newConfig);
  }

  @Override
  public boolean onCreateOptionsMenu(Menu menu) {
    this.menu = menu;
    return super.onCreateOptionsMenu(menu);
  }

  @Override
  public boolean onOptionsItemSelected(@NonNull MenuItem item) {
    if(mDrawerToggle.onOptionsItemSelected(item)){
      return true;
    }

    if (item.hasSubMenu())
      return true;

    int parentSubmenu = item.getGroupId();
    String menuItem = (String) item.getTitle();

    if (parentSubmenu == 0){
      topicFilter = !menuItem.equals("all");
      currTopic = menuItem;
      updateDrawer();
    }
    else if (parentSubmenu == 1){
      countryFilter = !menuItem.equals("all");
      currCountry = menuItem;
      updateDrawer();
    }
    else if (parentSubmenu == 2){
      languageFilter = !menuItem.equals("all");
      currLanguage = menuItem;
      updateDrawer();
    }
    arrayAdapter.notifyDataSetChanged();

    if (listSources.size() == 0){
      AlertDialog.Builder builder = new AlertDialog.Builder(this);
      String message = "There are no sources that are";

      builder.setPositiveButton("OK", (dialog, id) -> {});

      if (languageFilter)
        message += " in " + currLanguage;

      if (topicFilter)
        message += " about " + currTopic;

      if (countryFilter)
        message += " from " + currCountry;

      builder.setMessage(message);
      builder.setTitle("No sources found");

      AlertDialog dialog = builder.create();
      dialog.show();
    }
    return super.onOptionsItemSelected(item);
  }


  public void downloadFailed() {
    Log.d(TAG, "downloadFailed: ");
    Toast.makeText(this, "Unable to download data.", Toast.LENGTH_LONG).show();
  }

  public void getArticles(ArrayList<Article> list, int position){
    ArrayList<Source> first = new ArrayList<>(listSources.subList(0, position));
    ArrayList<Source> second = new ArrayList<>(listSources.subList(position+1, listSources.size()));
    Source source = listSources.get(position);
    source.setArticles(list);
    listSources.clear();
    listSources.addAll(first);
    listSources.add(source);
    listSources.addAll(second);

    for (ArrayList<Source> temp : topicsMenu.values()) {
      for (int i = 0; i < temp.size(); i++){
        if (temp.get(i).equals(source)){
          temp.set(i, source);
        }
      }
    }

    for (ArrayList<Source> temp : countriesMenu.values()) {
      for (int i = 0; i < temp.size(); i++){
        if (temp.get(i).equals(source)){
          temp.set(i, source);
        }
      }
    }

    for (ArrayList<Source> temp : languagesMenu.values()) {
      for (int i = 0; i < temp.size(); i++){
        if (temp.get(i).equals(source)){
          temp.set(i, source);
        }
      }
    }

    displayArticle(position);
  }

  @SuppressLint("NotifyDataSetChanged")
  public void displayArticle(int position){
    currArticles.clear();

    if (listSources.get(position).getArticles().size() == 0){
      new Thread(new ArticleLoaderRunnable(this, listSources.get(position), position)).start();
    }

    currSource = listSources.get(position);
    ArrayList<Article> articles = new ArrayList<>(currSource.getArticles());
    currArticles.addAll(articles);
    articleAdapter.notifyDataSetChanged();
    viewPager.setCurrentItem(0);
    setTitle(currSource.getName());
  }

  public void updateData(ArrayList<Source> list) {
    // Putting all the sources in their respective hashmaps
    for (Source source : list){
      String topic = source.getCategory();
      String lang = source.getLang();
      String country = source.getCountry();

      if (!topicsMenu.containsKey(topic))
        topicsMenu.put(topic, new ArrayList<>());
      Objects.requireNonNull(topicsMenu.get(topic)).add(source);

      if (!languagesMenu.containsKey(lang))
        languagesMenu.put(lang, new ArrayList<>());
      Objects.requireNonNull(languagesMenu.get(lang)).add(source);

      if (!countriesMenu.containsKey(country))
        countriesMenu.put(country, new ArrayList<>());
      Objects.requireNonNull(countriesMenu.get(country)).add(source);
    }

    // Populating topics submenu
    ArrayList<String> topicTemp = new ArrayList<>(topicsMenu.keySet());
    Collections.sort(topicTemp);
    SubMenu topicSubMenu = menu.addSubMenu(FILTERS[0]);
    topicSubMenu.add(0, 0, 0, "all");
    for (int i = 0; i < topicTemp.size(); i++)
      topicSubMenu.add(0, i+1, i+1, topicTemp.get(i));

    // Populating countries submenu
    ArrayList<String> countryTemp = new ArrayList<>(countriesMenu.keySet());
    Collections.sort(countryTemp);
    SubMenu countrySubMenu = menu.addSubMenu(FILTERS[1]);
    countrySubMenu.add(1, 0, 0, "all");
    for (int i = 0; i < countryTemp.size(); i++)
      countrySubMenu.add(1, i+1, i+1, countryTemp.get(i));

    // Populating languages submenu
    ArrayList<String> languageTemp = new ArrayList<>(languagesMenu.keySet());
    Collections.sort(languageTemp);
    SubMenu languageSubMenu = menu.addSubMenu(FILTERS[2]);
    languageSubMenu.add(2,0,0,"all");
    for (int i = 0; i < languagesMenu.size(); i++)
      languageSubMenu.add(2, i+1, i+1, languageTemp.get(i));

    updateDrawer();

    arrayAdapter = new ArrayAdapter<>(this, R.layout.drawer_item, listSources);
    mDrawerList.setAdapter(arrayAdapter);
    findViewById(R.id.progressBar).setVisibility(View.GONE);

  }

  @SuppressLint("DefaultLocale")
  public void updateDrawer(){
    drawerSources.clear();
    listSources.clear();

    if (!topicFilter && !languageFilter && !countryFilter){
      for (ArrayList<Source> temp : topicsMenu.values())
        drawerSources.addAll(temp);

      for (ArrayList<Source> temp : languagesMenu.values())
        drawerSources.addAll(temp);

      for (ArrayList<Source> temp : countriesMenu.values())
        drawerSources.addAll(temp);
    }
    if (topicFilter && !countryFilter && !languageFilter){
      ArrayList<Source> temp = topicsMenu.get(currTopic);
      drawerSources.addAll(temp);
    }
    if (countryFilter && !topicFilter && !languageFilter){
      ArrayList<Source> temp = countriesMenu.get(currCountry);
      drawerSources.addAll(temp);
    }
    if (languageFilter && !topicFilter && !countryFilter){
      ArrayList<Source> temp = languagesMenu.get(currLanguage);
      drawerSources.addAll(temp);
    }

    if (topicFilter && countryFilter && !languageFilter){
      ArrayList<Source> tTopic = topicsMenu.get(currTopic);
      ArrayList<Source> tCountry = countriesMenu.get(currCountry);

      for(Source t : tTopic){
        if (tCountry.contains(t)){
          drawerSources.add(t);
        }
      }
    }

    if (topicFilter && languageFilter && !countryFilter){
      ArrayList<Source> tTopic = topicsMenu.get(currTopic);
      ArrayList<Source> tLang = languagesMenu.get(currLanguage);

      for (Source t : tTopic){
        if (tLang.contains(t)){
          drawerSources.add(t);
        }
      }
    }

    if (languageFilter && countryFilter && !topicFilter){
      ArrayList<Source> tLang = languagesMenu.get(currLanguage);
      ArrayList<Source> tCountry = countriesMenu.get(currCountry);

      for (Source t : tLang){
        if (tCountry.contains(t)){
          drawerSources.add(t);
        }
      }
    }

    if (languageFilter && countryFilter && topicFilter){
      ArrayList<Source> tLang = languagesMenu.get(currLanguage);
      ArrayList<Source> tCountry = countriesMenu.get(currCountry);
      ArrayList<Source> tTopic = topicsMenu.get(currTopic);
      ArrayList<Source> temp = new ArrayList<>();

      for (Source t : tLang){
        if (tCountry.contains(t)){
          temp.add(t);
        }
      }

      for (Source t: temp){
        if (tTopic.contains(t)){
          drawerSources.add(t);
        }
      }
    }

    listSources.addAll(drawerSources);
    Collections.sort(listSources);
    setTitle(String.format("News Gateway (%d)", listSources.size()));
  }
}