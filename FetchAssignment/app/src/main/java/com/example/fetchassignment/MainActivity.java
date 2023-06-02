package com.example.fetchassignment;

import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class MainActivity extends AppCompatActivity {
    private RecyclerView recyclerView;
    private DataAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Initialize RecyclerView
        recyclerView = findViewById(R.id.recycler_view);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        // Create Retrofit instance
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl("https://fetch-hiring.s3.amazonaws.com/")
                .addConverterFactory(GsonConverterFactory.create())
                .build();

        // Create FetchApi service
        FetchApi service = retrofit.create(FetchApi.class);
        Call<List<Data>> call = service.GetData();

        // Make API call
        call.enqueue(new Callback<List<Data>>() {
            @Override
            public void onResponse(Call<List<Data>> call, Response<List<Data>> response) {
                if (response.isSuccessful()) {
                    // Retrieve the data from the response
                    List<Data> dataList = response.body();

                    // Filter out items with blank or null names
                    List<Data> filteredList = dataList.stream()
                            .filter(data -> data.getName() != null && !data.getName().isEmpty())
                            .collect(Collectors.toList());

                    // Sort the filtered list by listId and then by name
                    Collections.sort(filteredList, new Comparator<Data>() {
                        @Override
                        public int compare(Data data1, Data data2) {
                            int compareListId = Integer.compare(data1.getListId(), data2.getListId());
                            if (compareListId != 0) {
                                return compareListId;
                            }
                            return data1.getName().compareTo(data2.getName());
                        }
                    });

                    // Group the sorted list by listId
                    Map<Integer, List<Data>> groupedData = filteredList.stream()
                            .collect(Collectors.groupingBy(Data::getListId));

                    // Flatten the grouped data into a single list
                    List<Data> groupedAndSortedData = groupedData.values().stream()
                            .flatMap(List::stream)
                            .collect(Collectors.toList());

                    // Set up the RecyclerView adapter
                    adapter = new DataAdapter(groupedAndSortedData);
                    recyclerView.setAdapter(adapter);
                }
            }

            @Override
            public void onFailure(Call<List<Data>> call, Throwable t) {
                t.printStackTrace();
            }
        });
    }
}
