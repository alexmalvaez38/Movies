package com.alex.test.movies;

import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.design.widget.NavigationView;
import android.support.v4.app.FragmentManager;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.ViewGroup;

import com.alex.test.movies.entities.Result;
import com.alex.test.movies.fragments.AboutFragment;
import com.alex.test.movies.fragments.MainFragment;
import com.alex.test.movies.fragments.PlaceholderFragment;
import com.alex.test.movies.providers.FavoritesProvider;
import com.alex.test.movies.tasks.RetrieveMoviesTask;
import com.alex.test.movies.utils.Constants;
import com.alex.test.movies.utils.NetworkMagic;
import com.alex.test.movies.utils.ShareAppMagic;
import com.alex.test.movies.utils.SnackbarMagic;

import java.util.List;

import butterknife.Bind;
import butterknife.ButterKnife;

public class MainActivity extends AppCompatActivity implements MainFragment.ActivityCallback {
    private static final String TAG = "MainActivityTAG_";
    private static final String ABOUT_FRAGMENT_KEY = "about_fragment_key";

    private static final String POPULARITY_STRING = "popularity";
    private static final String VOTE_AVERAGE_STRING = "vote_average";

    private MainFragment mMainFragment;
    private PlaceholderFragment mPlaceholderFragment;

    private ActionBar mActionBar;
    private Result mResult;

    private String mOrderType;

    @Bind(R.id.mainFrame)
    ViewGroup mMainFrame;
    @Bind(R.id.mainActivityToolbar)
    Toolbar mToolbar;
    @Bind(R.id.mainDrawerLayout)
    DrawerLayout mDrawer;
    @Bind(R.id.mainNavigationView)
    NavigationView mNavigationView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // TODO: 6/22/16 Add support to RecyclerView inside ScrollView
        // TODO: 6/24/16 Remove Constants file
        // TODO: 6/24/16 Fix camelcase resource names
        // TODO: 6/24/16 Remove color links reviews
        // TODO: 6/24/16 Add preferences Activity
        // TODO: 6/24/16 Add push notifications
        // TODO: 6/24/16 Implement Dagger
        // TODO: 6/24/16 Create unit tests

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        ButterKnife.bind(this);

        mOrderType = "popularity";
        setSupportActionBar(mToolbar);
        mActionBar = getSupportActionBar();

        mMainFragment = (MainFragment) getSupportFragmentManager().findFragmentById(R.id.mainFragment);
        mPlaceholderFragment = (PlaceholderFragment) getSupportFragmentManager().findFragmentById(R.id.placeholderFragment);

        mMainFragment.setRetainInstance(true);

        if (savedInstanceState != null) {
            mOrderType = savedInstanceState.getString(Constants.ORDER_TYPE_KEY, "popularity");
            if (savedInstanceState.containsKey(Constants.RESULT_TEMP_KEY)) {
                mResult = savedInstanceState.getParcelable(Constants.RESULT_TEMP_KEY);
                refreshPlaceholderFragment(mResult);
            }
        } else {
            queryMovieAPI(mOrderType);
        }

        refreshActionBar();
        setupDrawer();
    }

    @Override
    public void onBackPressed() {
        if (mDrawer.isDrawerOpen(GravityCompat.START)) {
            mDrawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        if (!mOrderType.equals("popularity")) {
            menu.findItem(R.id.menu_toggle).setTitle(R.string.menuTogglePopularity);
        } else {
            menu.findItem(R.id.menu_toggle).setTitle(R.string.menuToggleRating);
        }
        return super.onPrepareOptionsMenu(menu);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.menu_toggle:
                toggleOrderType(item);
                return true;
            case R.id.menu_favs:
                if (checkHasFavorites()) {
                    Intent intent = new Intent(this, FavoritesActivity.class);
                    startActivity(intent);
                } else {
                    SnackbarMagic.showSnackbar(mMainFrame, R.string.noFavoritesYet);
                }
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private void setupDrawer() {
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, mDrawer, mToolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        mDrawer.setDrawerListener(toggle);
        toggle.syncState();

        mNavigationView.setNavigationItemSelectedListener(new NavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(MenuItem item) {
                int id = item.getItemId();

                switch (item.getItemId()) {
                    case R.id.nav_about:
                        showAbout();
                        break;
                    case R.id.nav_rate:
                        ShareAppMagic.rateApp(getApplicationContext());
                        break;
                    case R.id.nav_like:
                        ShareAppMagic.likeApp(getApplicationContext());
                        break;
                    case R.id.nav_more:
                        ShareAppMagic.openMoreApps(getApplicationContext());
                        break;
                    case R.id.nav_share:
                        ShareAppMagic.shareApp(getApplicationContext());
                        break;
                    default:
                    case R.id.nav_home:
                }

                mDrawer.closeDrawer(GravityCompat.START);
                return true;
            }
        });
    }

    private void showAbout() {
        AboutFragment aboutFragment = new AboutFragment();
        FragmentManager fragmentManager = getSupportFragmentManager();

        aboutFragment.show(fragmentManager, ABOUT_FRAGMENT_KEY);
    }

    private boolean checkHasFavorites() {
        Uri uri = Uri.withAppendedPath(FavoritesProvider.PROVIDER_URI, "count");
        boolean hasFavorites;

        Cursor cursor = getContentResolver().query(uri, null, null, null, null);
        if (cursor != null) {
            cursor.moveToFirst();
            hasFavorites = cursor.getInt(0) > 0;
            cursor.close();
        } else {
            return false;
        }

        return hasFavorites;
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        if (mOrderType != null) {
            outState.putString(Constants.ORDER_TYPE_KEY, mOrderType);
        }
        if (mResult != null) {
            outState.putParcelable(Constants.RESULT_TEMP_KEY, mResult);
        }
        super.onSaveInstanceState(outState);
    }

    @Override
    public void onEmptyResults() {
        queryMovieAPI(mOrderType);
    }

    private void toggleOrderType(MenuItem item) {
        if (!NetworkMagic.isNetworkAvailable(getApplicationContext())) {
            SnackbarMagic.showSnackbar(mMainFrame, R.string.internetNotAvailableMessage);
            return;
        }

        if (item.getTitle().equals(getString(R.string.menuTogglePopularity))) {
            item.setTitle(getString(R.string.menuToggleRating));
            mOrderType = POPULARITY_STRING;
        } else {
            item.setTitle(getString(R.string.menuTogglePopularity));
            mOrderType = VOTE_AVERAGE_STRING;
        }

        queryMovieAPI(mOrderType);
        refreshActionBar();
    }

    private void refreshActionBar() {
        if (mActionBar != null) {
            mActionBar.setSubtitle(getString(R.string.subtitleOrderedPrefix) +
                    (mOrderType.equals("popularity")
                            ? getString(R.string.subtitlePopularity)
                            : getString(R.string.subtitleRating)));
            mActionBar.invalidateOptionsMenu();
        }
    }

    public void queryMovieAPI(String arg) {
        if (mMainFragment != null){
            mMainFragment.showProgress();
        }
        if (NetworkMagic.isNetworkAvailable(getApplicationContext())) {
            arg = (arg == null) ? "popularity" : arg;
            new RetrieveMoviesTask(this).execute(arg);
        } else {
            SnackbarMagic.showSnackbar(mMainFrame, R.string.internetNotAvailableMessage);
        }
    }

    public void setResults(List<Result> results) {
        if (mMainFragment != null) {
            mMainFragment.refreshRecycler(results);
        }
        if (results != null && results.size() > 0) {
            if (isTabletLayout()) {
                refreshDetails(results.get(0));
            } else {
                mResult = results.get(0);
            }
        }
    }

    private boolean isTabletLayout() {
        return mPlaceholderFragment != null && mPlaceholderFragment.isAdded();
    }

    public void refreshDetails(Result result) {
        if (result == null) {
            return;
        }

        mResult = result;

        if (isTabletLayout()) {
            refreshPlaceholderFragment(mResult);
        } else {
            Intent intent = new Intent(this, DetailsActivity.class);
            Bundle b = new Bundle();

            b.putParcelable(DetailsActivity.MOVIE_PARCELABLE_TAG, mResult);
            intent.putExtras(b);
            startActivity(intent);
        }
    }

    private void refreshPlaceholderFragment(Result result) {
        if (mPlaceholderFragment != null) {
            mPlaceholderFragment.refreshContent(result);
        }
    }
}
