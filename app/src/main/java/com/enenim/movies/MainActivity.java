package com.enenim.movies;

import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.os.Bundle;
import android.provider.Settings;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.ProgressBar;

import com.enenim.movies.config.Config;
import com.enenim.movies.model.Movie;
import com.enenim.movies.model.MoviesResponse;
import com.enenim.movies.rest.ApiClient;
import com.enenim.movies.rest.ApiInterface;
import com.enenim.movies.util.InternetUtil;

import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class MainActivity extends AppCompatActivity implements MovieAdapter.MovieAdapterOnClickHandler  {
    private static final String TAG = MainActivity.class.getSimpleName();

    private RecyclerView mRecyclerView;

    private MovieAdapter mMovieAdapter;

    private List<Movie> movies;

    private ProgressBar mLoadingIndicator;

    private FloatingActionButton fab_forward;
    private FloatingActionButton fab_backward;
    private String current_page_info;
    private MenuItem menuInfo;

    private int defaultVal = R.string.most_popular; //default, popular movies

    private int app_label = R.string.popular_movie_label;

    private int page = 1; //default page
    private int id;
    private int maxPage = 10;
    private int firstPage = 1;

    private  boolean connected = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        ColorStateList csl = new ColorStateList(new int[][] { new int[0] }, new int[]{Color.parseColor("#000080") });

        fab_forward = (FloatingActionButton) findViewById(R.id.fab_forward);
        fab_forward.setVisibility(View.INVISIBLE);

        fab_forward.setBackgroundTintList(csl);
        fab_forward.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(page < maxPage){
                    page = page + 1;
                    execute(defaultVal);
                }
            }
        });

        fab_backward = (FloatingActionButton) findViewById(R.id.fab_backward);
        fab_backward.setVisibility(View.INVISIBLE);
        fab_backward.setBackgroundTintList(csl);
        fab_backward.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(page > firstPage){
                    page = page - 1;
                    execute(defaultVal);
                }
            }
        });

        if (Config.API_KEY.isEmpty()) {
            //Toast.makeText(getApplicationContext(), R.string.no_api_key_message, Toast.LENGTH_LONG).show();

            InternetUtil.showDialog(this, android.R.drawable.ic_dialog_alert, R.string.no_api_key_message)
                   .show();

            return;
        }

        connected = InternetUtil.isNetworkConnected(this);

        if (connected) {
            mRecyclerView = (RecyclerView) findViewById(R.id.recyclerview_movie);

            GridLayoutManager layoutManager
                    = new GridLayoutManager(getApplicationContext(), 2);

            mRecyclerView.setLayoutManager(layoutManager);

            mRecyclerView.setHasFixedSize(true);

            mMovieAdapter = new MovieAdapter(this, getApplicationContext());

            mLoadingIndicator = (ProgressBar) findViewById(R.id.pb_loading_indicator);

            execute(defaultVal);

        } else {
            InternetUtil.showDialog(this, android.R.drawable.ic_dialog_alert, R.string.no_network_message)
                    .setPositiveButton(R.string.action_settings, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            Intent intent = new Intent(Settings.ACTION_SETTINGS);

                            if (intent.resolveActivity(getPackageManager()) != null) {
                                startActivity(intent);
                            }else{
                                InternetUtil.showDialog(MainActivity.this, android.R.drawable.ic_dialog_alert, R.string.no_setting_app)
                                        .show();
                            }
                        }
                    })
                    .show();

            //Toast.makeText(MainActivity.this, "No Internet Connection", Toast.LENGTH_LONG).show();
        }
    }

    @Override
    public void onClick(Movie movie) {
        Intent intent = new Intent(MainActivity.this, DetailActivity.class);
        Bundle bundle = new Bundle();

        bundle.putSerializable(Config.MOVIE_KEY, movie);
        bundle.putInt(Config.APP_LABEL_KEY, app_label);
        intent.putExtras(bundle);

        startActivity(intent);
    }

    private void showMovieDataView() {
        mRecyclerView.setVisibility(View.VISIBLE);
    }


    private void showErrorMessage() {
        /* First, hide the currently visible data */
        mRecyclerView.setVisibility(View.INVISIBLE);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        /* Use AppCompatActivity's method getMenuInflater to get a handle on the menu inflater */
        MenuInflater inflater = getMenuInflater();
        /* Use the inflater's inflate method to inflate our menu layout to this menu */
        inflater.inflate(R.menu.menu_main, menu);
        /* Return true so that the menu is displayed in the Toolbar */

        /* Show menu items only if internet connection is available */
        return connected;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem menuItem) {

        id = menuItem.getItemId();
        page = firstPage;
        if(defaultVal != id){
            if(id == R.id.action_most_popular){
                //Toast.makeText(MainActivity.this, "Load Most Popular Movies", Toast.LENGTH_LONG).show();
                execute(id);

                return true;
            }
            if(id == R.id.action_top_rated){
                //Toast.makeText(MainActivity.this, "Load Top Rated Movies ", Toast.LENGTH_LONG).show();
                execute(id);

                return true;
            }
        }

        return  super.onOptionsItemSelected(menuItem);
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        super.onPrepareOptionsMenu(menu);
        menuInfo = menu.findItem(R.id.current_page_info);
        menuInfo.setTitle(this.current_page_info);
        //menuInfo.setEnabled(false);
        return true;
    }


    public void execute(int selectedMenuType){
        defaultVal = selectedMenuType;

        mLoadingIndicator.setVisibility(View.VISIBLE);
        mMovieAdapter.setMovies(null);

        if(!(page == firstPage)){
            fab_backward.setVisibility(View.VISIBLE);
        }else {
            fab_backward.setVisibility(View.INVISIBLE);
        }

        if(!(page == maxPage)){
            fab_forward.setVisibility(View.VISIBLE);
        }else {
            fab_forward.setVisibility(View.INVISIBLE);
        }

        ApiInterface apiService = ApiClient.getClient().create(ApiInterface.class);

        Call<MoviesResponse> call;
        if(selectedMenuType == R.id.action_most_popular){
            app_label = R.string.popular_movie_label;
            call = apiService.getPopularMovies(Config.API_KEY, page);
        }else if (selectedMenuType == R.id.action_top_rated) {
            app_label = R.string.top_movie_label;
            call = apiService.getTopRatedMovies(Config.API_KEY, page);
        }else {
            app_label = R.string.popular_movie_label;
            call = apiService.getPopularMovies(Config.API_KEY, page);
        }

        //current_page_info = R.string.current_page + " ( " + page + " " + R.string.current_of + " " + maxPage + " ) ";

        this.current_page_info = page + " " + "of" + " " + maxPage;

        call.enqueue(new Callback<MoviesResponse>() {
            @Override
            public void onResponse(Call<MoviesResponse>call, Response<MoviesResponse> response) {
                MainActivity.this.setTitle(app_label);

                //Call to refresh menu in order to update menuInfo in onPrepareOptionsMenu(Menu menu)
                invalidateOptionsMenu();

                movies = response.body().getResults();

                mLoadingIndicator.setVisibility(View.INVISIBLE);

                if (movies != null) {
                    showMovieDataView();
                    mMovieAdapter.setMovies(movies);

                    /* Setting the adapter attaches it to the RecyclerView in our layout. */
                    mRecyclerView.setAdapter(mMovieAdapter);
                } else {
                    showErrorMessage();
                }

                Log.d(TAG, "Number of movies received: " + movies.size());
            }

            @Override
            public void onFailure(Call<MoviesResponse>call, Throwable t) {
                // Log error here since request failed
                Log.e(TAG, t.toString());
            }
        });
    }
}
