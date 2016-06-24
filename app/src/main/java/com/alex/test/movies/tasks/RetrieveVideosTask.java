package com.alex.test.movies.tasks;

import android.os.AsyncTask;

import com.alex.test.movies.entities.VideoPage;
import com.alex.test.movies.entities.VideoResult;
import com.alex.test.movies.fragments.DetailsFragment;
import com.alex.test.movies.network.MoviesRetrofit;

import java.util.List;

public class RetrieveVideosTask extends AsyncTask<Integer, Void, List<VideoResult>> {
    // TODO: 6/24/16 Change to Retrofit enqueue

    DetailsFragment mDetailsFragment;

    public RetrieveVideosTask(DetailsFragment detailsFragment) {
        mDetailsFragment = detailsFragment;
    }

    @Override
    protected List<VideoResult> doInBackground(Integer... params) {
        String movieId = (params.length > 0) ? String.valueOf(params[0]) : "293660";

        MoviesRetrofit moviesRetrofit = new MoviesRetrofit();

        VideoPage videoPage = moviesRetrofit.getVideos(movieId);

        return (videoPage!= null) ? videoPage.getVideoResults() : null;
    }

    @Override
    protected void onPostExecute(List<VideoResult> videoResults) {
        super.onPostExecute(videoResults);

        if (!isCancelled() && mDetailsFragment != null){
            mDetailsFragment.refreshVideos(videoResults);
        }

        clearReferences();
    }

    private void clearReferences() {
        mDetailsFragment = null;
    }
}
