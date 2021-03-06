package com.alex.test.movies.fragments;

import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.alex.test.movies.FavoritesActivity;
import com.alex.test.movies.R;
import com.alex.test.movies.adapters.ReviewsAdapter;
import com.alex.test.movies.adapters.VideosAdapter;
import com.alex.test.movies.database.MoviesContract.FavoriteEntry;
import com.alex.test.movies.decorators.FavSpacesItemDecoration;
import com.alex.test.movies.entities.Result;
import com.alex.test.movies.entities.ReviewResult;
import com.alex.test.movies.entities.VideoResult;
import com.alex.test.movies.providers.FavoritesProvider;
import com.alex.test.movies.tasks.RetrieveReviewsTask;
import com.alex.test.movies.tasks.RetrieveVideosTask;
import com.alex.test.movies.utils.Constants;
import com.alex.test.movies.utils.NetworkMagic;
import com.alex.test.movies.utils.SnackbarMagic;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.List;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;


public class DetailsFragment extends Fragment implements View.OnClickListener {

    private static final String TAG = "DetailsFragmentTAG_";

    @Bind(R.id.detailsFragmentTitleTxt)
    TextView mTextViewTitle;
    @Bind(R.id.detailsFragmentReleaseTxt)
    TextView mTextViewRelease;
    @Bind(R.id.detailsFragmentVote)
    TextView mTextViewVote;
    @Bind(R.id.detailsFragmentPlot)
    TextView mTextViewPlot;
    @Bind(R.id.detailsIsFavoriteIcon)
    ImageView mImageIcon;
    @Bind(R.id.detailsFragmentPoster)
    ImageView mImageView;
    @Bind(R.id.detailsFragmentRecyclerReviews)
    RecyclerView mRecyclerReviews;
    @Bind(R.id.detailsFragmentRecyclerVideos)
    RecyclerView mRecyclerVideos;

    private ActivityCallback mCallback;

    private Result mMovie;
    private String mTrailer;
    private boolean isFavorite = false;

    private ArrayList<VideoResult> mVideos;
    private VideosAdapter mAdapterVideos;

    private ArrayList<ReviewResult> mReviews;
    private ReviewsAdapter mAdapterReviews;

    public DetailsFragment() {

    }

    public interface ActivityCallback {
        void onModifiedFavorites();
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (getActivity() instanceof FavoritesActivity) {
            mCallback = (ActivityCallback) context;
        }
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mVideos = new ArrayList<>();
        mReviews = new ArrayList<>();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_details, container, false);
        ButterKnife.bind(this, view);
        return view;
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        setupRecyclerVideos();
        setupRecyclerReviews();
    }

    @Override
    public void onResume() {
        super.onResume();
        checkIfFavorite();
        modifyUIFavorite();
    }

    @OnClick(R.id.detailsShareIcon)
    public void onShareBtnClick() {
        Intent intent = new Intent();
        intent.setAction(Intent.ACTION_SEND);
        intent.putExtra(Intent.EXTRA_TEXT, buildShareMessage());
        intent.setType("text/plain");
        getActivity().startActivity(intent);
    }

    @OnClick(R.id.detailsIsFavoriteIcon)
    public void onAddToFavoritesBtnClick() {
        if (getView() != null) {
            if (isFavorite) {
                removeFromFavorites();
            } else {
                addToFavorites();
            }
        }

        showSnackBar();
        checkIfFavorite();
        modifyUIFavorite();
    }

    @Override
    public void onClick(View v) {
        onAddToFavoritesBtnClick();
    }

    private void setupRecyclerReviews() {
        mAdapterReviews = new ReviewsAdapter(getContext(), mReviews);
        mRecyclerReviews.setAdapter(mAdapterReviews);
        mRecyclerReviews.setLayoutManager(new LinearLayoutManager(getContext()));
        mRecyclerReviews.addItemDecoration(new FavSpacesItemDecoration(10));
    }

    private void setupRecyclerVideos() {
        mAdapterVideos = new VideosAdapter(getContext(), mVideos);
        mRecyclerVideos.setAdapter(mAdapterVideos);
        mRecyclerVideos.setLayoutManager(new LinearLayoutManager(getContext()));
        mRecyclerVideos.addItemDecoration(new FavSpacesItemDecoration(10));
    }

    private void removeFromFavorites() {
        final ContentResolver contentResolver = getActivity().getContentResolver();
        final String[] selectionArgs = {String.valueOf(mMovie.getId())};
        final String columnMovieId = FavoriteEntry.COLUMN_MOVIE_ID + " = ?";

        int nRows = contentResolver.delete(FavoritesProvider.PROVIDER_URI, columnMovieId, selectionArgs);
        if (nRows > 0 && mCallback != null) {
            mCallback.onModifiedFavorites();
        }
    }

    private void addToFavorites() {
        final ContentResolver contentResolver = getActivity().getContentResolver();
        final ContentValues values = FavoriteEntry.resolveMovie(mMovie);

        Uri uri = contentResolver.insert(FavoritesProvider.PROVIDER_URI, values);
        if (uri != null && mCallback != null) {
            mCallback.onModifiedFavorites();
        }
    }

    public String buildShareMessage() {
        StringBuilder message = new StringBuilder();
        message.append("You should check this movie! ");
        if (mMovie != null) {
            message.append(mMovie.getTitle());
        } else {
            message.append("Yes");
        }
        message.append("! ");
        if (mTrailer != null) {
            message.append("https://youtu.be/");
            message.append(mTrailer);
        }
        return message.toString();
    }

    public void refreshDetails(Result movie) {
        if (movie == null) {
            if (getView() != null) {
                SnackbarMagic.showSnackbar(getView().getRootView(), R.string.retrieveMovieFailedMessage);
            }
            getActivity().getSupportFragmentManager().beginTransaction().remove(this).commit();
        } else {
            mMovie = movie;

            checkIfFavorite();
            modifyUIFavorite();

            refreshUI(movie);
            startRefreshingVideos();
            startRefreshingReviews();
        }
    }

    public void refreshReviews(List<ReviewResult> reviewResults) {
        if (mReviews != null && mAdapterReviews != null && reviewResults != null) {
            mReviews.clear();
            mReviews.addAll(reviewResults);
            mAdapterReviews.notifyDataSetChanged();
        }
    }

    public void refreshVideos(List<VideoResult> videoResults) {
        if (videoResults != null && videoResults.size() > 0) {
            mTrailer = videoResults.get(0).getKey();
        }
        if (videoResults != null && mVideos != null && mAdapterVideos != null) {
            mVideos.clear();
            mVideos.addAll(videoResults);
            mAdapterVideos.notifyDataSetChanged();
        }
    }

    private void modifyUIFavorite() {
        if (isFavorite) {
            mImageIcon.setImageResource(R.drawable.ic_favorite_white_24dp);
        } else {
            mImageIcon.setImageResource(R.drawable.ic_favorite_border_white_24dp);
        }
    }

    public void refreshUI(Result movie) {
        Picasso.with(getActivity().getApplicationContext())
                .load(Constants.BASE_IMG_URL + movie.getBackdropPath())
                .placeholder(R.drawable.large_placeholder)
                .into(mImageView);

        mTextViewTitle.setText(movie.getTitle());
        mTextViewRelease.setText(movie.getReleaseDate());
        mTextViewVote.setText(String.format("%.02f", movie.getVoteAverage()));
        mTextViewPlot.setText(movie.getOverview());
    }

    private void startRefreshingReviews() {
        refreshReviews(new ArrayList<ReviewResult>());
        if (NetworkMagic.isNetworkAvailable(getContext())) {
            new RetrieveReviewsTask(this).execute(mMovie.getId());
        }
    }

    private void showSnackBar() {
        int message = (!isFavorite) ? R.string.addedToFavoritesSuccess : R.string.removedFromFavoritesSuccess;
        View view = getView();
        if (view != null) {
            Snackbar snackbar = Snackbar.make(getView(), message, Snackbar.LENGTH_SHORT);
            snackbar.setAction(R.string.undoFavorites, this);
            snackbar.show();
        }
    }

    private void startRefreshingVideos() {
        refreshVideos(new ArrayList<VideoResult>());
        if (NetworkMagic.isNetworkAvailable(getContext())) {
            new RetrieveVideosTask(this).execute(mMovie.getId());
        }
    }

    private void checkIfFavorite() {
        if (mMovie == null) {
            return;
        }
        Uri uri = Uri.withAppendedPath(FavoritesProvider.PROVIDER_URI, String.valueOf(mMovie.getId()));
        Cursor cursor = getActivity().getContentResolver().query(uri, null, null, null, null);

        if (cursor != null) {
            isFavorite = cursor.getCount() > 0;
            cursor.close();
        }
    }

}
