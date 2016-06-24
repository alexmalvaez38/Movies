package com.alex.test.movies.tasks;

import android.os.AsyncTask;

import com.alex.test.movies.MainActivity;
import com.alex.test.movies.entities.Page;
import com.alex.test.movies.entities.Result;
import com.alex.test.movies.network.MoviesRetrofit;

import java.util.List;

public class RetrieveMoviesTask extends AsyncTask<String, Void, List<Result>> {
    // TODO: 6/24/16 Change to Retrofit enqueue

    private MainActivity mActivity;

    public RetrieveMoviesTask(MainActivity activity) {
        mActivity = activity;
    }

    @Override
    protected List<Result> doInBackground(String... params) {
        String order = (params.length < 1) ? "popularity.desc" : params[0] + ".desc";

        MoviesRetrofit moviesRetrofit = new MoviesRetrofit();

        Page results = moviesRetrofit.getMovies(order);

        return (results != null) ? results.getResults() : null;
    }

    @Override
    protected void onPostExecute(List<Result> results) {
        super.onPostExecute(results);

        if (!isCancelled() && mActivity != null) {
            mActivity.setResults(results);
        }

        clearReferences();
    }

    private void clearReferences() {
        mActivity = null;
    }
}
