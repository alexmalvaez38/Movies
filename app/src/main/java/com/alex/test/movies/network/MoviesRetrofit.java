package com.alex.test.movies.network;

import android.util.Log;

import com.alex.test.movies.entities.Page;
import com.alex.test.movies.entities.ReviewPage;
import com.alex.test.movies.entities.VideoPage;
import com.alex.test.movies.utils.Constants;
import com.alex.test.movies.utils.Keys;

import retrofit.Call;
import retrofit.GsonConverterFactory;
import retrofit.Retrofit;

public class MoviesRetrofit {
    // TODO: 2/26/16 Firebase the DATE_GTE

    private static final String TAG = "MoviesRetrofit";
    private static final String DATE_GTE = "2016-01-01";

    private MovieDBService buildMoviesService() {
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(Constants.BASE_API_URL)
                .addConverterFactory(GsonConverterFactory.create())
                .build();
        return retrofit.create(MovieDBService.class);
    }

    public Page getMovies(String order) {
        Call<Page> listCall = buildMoviesService().listMovies(order, Keys.MDB_API_KEY, DATE_GTE);

        Page results = null;

        try {
            results = listCall.execute().body();
        } catch (Exception e) {
            Log.e(TAG, "Error: " + e.toString());
        }

        return results;
    }

    public VideoPage getVideos(String movieId){
        Call<VideoPage> listCall = buildMoviesService().listVideos(movieId, Keys.MDB_API_KEY, DATE_GTE);

        VideoPage results = null;

        try {
            results = listCall.execute().body();
        } catch (Exception e) {
            Log.e(TAG, "Error: " + e.toString());
        }

        return results;
    }

    public ReviewPage getReviews(String movieId){
        Call<ReviewPage> listCall = buildMoviesService().listReviews(movieId, Keys.MDB_API_KEY, DATE_GTE);

        ReviewPage results = null;

        try {
            results = listCall.execute().body();
        } catch (Exception e) {
            Log.e(TAG, "Error: " + e.toString());
        }

        return results;
    }

}
