package com.alex.test.movies.network;

import com.alex.test.movies.entities.Page;
import com.alex.test.movies.entities.ReviewPage;
import com.alex.test.movies.entities.VideoPage;

import retrofit.Call;
import retrofit.http.GET;
import retrofit.http.Path;
import retrofit.http.Query;

public interface MovieDBService {
    @GET("/3/discover/movie?vote_count.gte=100")
    Call<Page> listMovies(@Query("sort_by") String sort_by,
                          @Query("api_key") String api_key,
                          @Query("primary_release_date.gte") String primary_release_date_gte);

    @GET("3/movie/{movie_id}/videos")
    Call<VideoPage> listVideos(@Path("movie_id") String movie_id,
                               @Query("api_key") String api_key,
                               @Query("primary_release_date.gte") String primary_release_date_gte);

    @GET("3/movie/{movie_id}/reviews")
    Call<ReviewPage> listReviews(@Path("movie_id") String movie_id,
                                 @Query("api_key") String api_key,
                                 @Query("primary_release_date.gte") String primary_release_date_gte);
}