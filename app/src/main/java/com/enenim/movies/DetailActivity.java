package com.enenim.movies;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.enenim.movies.config.Config;
import com.enenim.movies.model.Movie;
import com.squareup.picasso.Picasso;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Created by enenim on 4/13/17.
 */

public class DetailActivity extends AppCompatActivity {
    private static final String TAG = DetailActivity.class.getSimpleName();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detail);

        View includePrimaryMovieInfo = findViewById(R.id.primary_info);
        View includeExtraMovieInfo = findViewById(R.id.extra_details);

        final Movie movie = (Movie)getIntent().getSerializableExtra(Config.MOVIE_KEY);
        final int appLabel = getIntent().getIntExtra(Config.APP_LABEL_KEY, R.string.popular_movie_label);
        this.setTitle(appLabel);
        Log.d(TAG, movie.getPosterPath());

        TextView tvOverview = (TextView)includePrimaryMovieInfo.findViewById(R.id.tv_overview);
        TextView tvReleaseDate = (TextView)includePrimaryMovieInfo.findViewById(R.id.tv_release_date);
        TextView tvTitle = (TextView)includePrimaryMovieInfo.findViewById(R.id.tv_movie_title);
        TextView tvRating = (TextView)includePrimaryMovieInfo.findViewById(R.id.tv_rating);
        ImageView tvMoviePoster = (ImageView)includePrimaryMovieInfo.findViewById(R.id.im_movie_poster);
        String rating = movie.getVoteAverage() + "";

        tvOverview.setText(movie.getOverview());
        //tvReleaseDate.setText(movie.getReleaseDate());
        tvReleaseDate.setText(R.string.overview_label);
        tvTitle.setText(movie.getTitle());
        tvRating.setText(rating);

        //For Movie extra detail
        TextView tv_ExtraRating = (TextView)includeExtraMovieInfo.findViewById(R.id.user_rating_value);
        //TextView tv_ExtraTitle = (TextView)includeExtraMovieInfo.findViewById(R.id.title_value);
        TextView tv_ExtraReleaseDate = (TextView)includeExtraMovieInfo.findViewById(R.id.release_date_value);

        tv_ExtraRating.setText(rating);
        //tv_ExtraReleaseDate.setText(date.toString());
        tv_ExtraReleaseDate.setText(movie.getReleaseDate());
        //tv_ExtraTitle.setText(movie.getTitle());

        String imageFullPath = Config.IMAGE_BASE_URL + movie.getPosterPath();

        Log.d(TAG, imageFullPath);

        Picasso.with(this)
                .load(imageFullPath)
                .into(tvMoviePoster);

    }
}
