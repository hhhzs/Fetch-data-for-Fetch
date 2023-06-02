package com.example.fetchassignment;

import retrofit2.Call;
import retrofit2.http.GET;
import java.util.List;

public interface FetchApi {
    @GET("hiring.json")
    Call<List<Data>> GetData();
}
