package com.alex.test.movies.tasks;

import android.os.AsyncTask;

import com.alex.test.movies.entities.ReviewPage;
import com.alex.test.movies.entities.ReviewResult;
import com.alex.test.movies.fragments.DetailsFragment;
import com.alex.test.movies.network.MoviesRetrofit;

import java.util.List;

public class RetrieveReviewsTask extends AsyncTask<Integer, Void, List<ReviewResult>> {
    // TODO: 6/24/16 Change to Retrofit enqueue

    DetailsFragment mDetailsFragment;

    public RetrieveReviewsTask(DetailsFragment detailsFragment) {
        mDetailsFragment = detailsFragment;
    }

    @Override
    protected List<ReviewResult> doInBackground(Integer... params) {
        String movieId = (params.length > 0) ? String.valueOf(params[0]) : "293660";

        MoviesRetrofit moviesRetrofit = new MoviesRetrofit();

        ReviewPage reviewPage = moviesRetrofit.getReviews(movieId);

        return (reviewPage != null) ? reviewPage.getReviewResults() : null;
    }

    @Override
    protected void onPostExecute(List<ReviewResult> reviewResults) {
        super.onPostExecute(reviewResults);

        if (!isCancelled() && mDetailsFragment != null){
            mDetailsFragment.refreshReviews(reviewResults);
        }

        clearReferences();
    }

    private void clearReferences() {
        mDetailsFragment = null;
    }
}
