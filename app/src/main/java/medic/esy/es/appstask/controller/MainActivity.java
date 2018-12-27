package medic.esy.es.appstask.controller;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.AbsListView;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import medic.esy.es.appstask.Adapter.repos_adapter;
import medic.esy.es.appstask.Api.client;
import medic.esy.es.appstask.Api.service;
import medic.esy.es.appstask.R;
import medic.esy.es.appstask.model.item;
import okhttp3.Cache;
import okhttp3.Interceptor;
import okhttp3.OkHttpClient;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class MainActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private item item;
    ProgressDialog pd;
    private SwipeRefreshLayout swipeContainer;
    boolean loading = true;
    boolean isscrolling = false;
    private LinearLayoutManager mLayoutManager;
    int currentItems, totalItems, scrollItems;
    ProgressBar progressBar;
    public List<item> items;
    public List<item>firt10Items;
    Boolean check;
    public static final String BASE_URL ="https://api.github.com/users/square/";
    public static Retrofit retrofit=null;
    repos_adapter repos_adapter;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        EditText searchBox =(EditText)findViewById(R.id.searchBox);
        searchBox.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
            }
            @Override
            public void afterTextChanged(Editable editable) {
                     filter(editable.toString());
            }
        });
        try {
            check =isConnected();
        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        mLayoutManager = new LinearLayoutManager(MainActivity.this);
        recyclerView = (RecyclerView) findViewById(R.id.mainRecycle);
        progressBar = (ProgressBar) findViewById(R.id.progressBar);
        initViews();
        swipeContainer = (SwipeRefreshLayout) findViewById(R.id.swipeContainer);
        swipeContainer.setColorSchemeResources(android.R.color.holo_orange_dark);
        swipeContainer.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                  /////first clear cache ////////////////////
                OkHttpClient client = new OkHttpClient.Builder()
                        .addNetworkInterceptor(REWRITE_CACHE_CONTROL_INTERCEPTOR)
                        .cache(null)
                        .build();
                /////////////load new Data//////////////////
                loadJSON();
                Toast.makeText(MainActivity.this, "Github Repository Refreshed", Toast.LENGTH_SHORT).show();
                //////////////////////////////////////////////////
                /////////////////////////////////////////////////
            }
        });
    }

    //filter data in recycleView method
    private void filter(String s) {
      List<item>filteredList=new ArrayList<>();
        for (int i = 0; i < items.size(); i++) {
            item= items.get(i);
            if (item.getName().toLowerCase().contains(s.toLowerCase())){
                      filteredList.add(item);
            }
        }
        repos_adapter.filterList(filteredList);

    }

    ////initViews method
    private void initViews()  {
        pd = new ProgressDialog(this);
        pd.setMessage("Fetching Github Repositories...");
        pd.setCancelable(false);
        pd.show();
        recyclerView.setLayoutManager(mLayoutManager);
        recyclerView.smoothScrollToPosition(0);
        loadJSON();
    }


    private void loadJSON() {
        try {
            //Initalization Retrofit instance //
            client Client = new client();
            if(retrofit==null){
                retrofit=new Retrofit.Builder()
                        .baseUrl(BASE_URL)
                        .addConverterFactory(GsonConverterFactory.create())
                        .client(getCahsing())
                        .build();
                internalNotification();
            }

        service apiService = retrofit.create(service.class);
        Call<List<item>> call = apiService.getItems();
        call.enqueue(new Callback<List<item>>() {
                @Override
                public void onResponse(Call<List<item>> call, Response<List<item>> response) {
                    items = response.body();
                    firt10Items=new ArrayList<>();
                    for (int i = 0; i < 10; i++) {
                      item= items.get(i);
                      firt10Items.add(item);
                    }
                    repos_adapter=new repos_adapter(getApplicationContext(), firt10Items);
                    recyclerView.setAdapter(repos_adapter);
                    recyclerView.smoothScrollToPosition(0);
                    recyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
                        @Override
                        public void onScrollStateChanged(@NonNull RecyclerView recyclerView, int newState) {
                            super.onScrollStateChanged(recyclerView, newState);
                            if (newState == AbsListView.OnScrollListener.SCROLL_STATE_TOUCH_SCROLL) {
                                isscrolling = true;
                            }

                        }
                        @Override
                        public void onScrolled(@NonNull RecyclerView recyclerView, int dx, int dy) {
                            super.onScrolled(recyclerView, dx, dy);
                            currentItems = mLayoutManager.getChildCount();
                            totalItems = mLayoutManager.getItemCount();
                            scrollItems = mLayoutManager.findFirstVisibleItemPosition();
                            if (isscrolling && currentItems + scrollItems >= totalItems) {
                                // data fetching
                                isscrolling = false;
                                fetchData();
                            }
                        }
                    });
                    swipeContainer.setRefreshing(false);
                    pd.hide();
                }
                @Override
                public void onFailure(Call<List<item>> call, Throwable t) {
                }
            });

        } catch (Exception e) {
            Log.d("Error", e.getMessage());
            Toast.makeText(this, e.toString(), Toast.LENGTH_SHORT).show();
        }
    }

/*   internalNotification method this method used to clear cache every one hour
   and send notification to user that the cache is refreshing now with new data
   */

    private void internalNotification() {



        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                OkHttpClient client = new OkHttpClient.Builder()
                        .addNetworkInterceptor(REWRITE_CACHE_CONTROL_INTERCEPTOR)
                        .cache(null)
                        .build();
                /////Every one Hour get Notification from system that cash is cleared.
                Intent i=new Intent(getApplicationContext(),NotificationReceiver.class);
                PendingIntent pendingIntent=PendingIntent.getBroadcast(getApplicationContext(),100,i,PendingIntent.FLAG_UPDATE_CURRENT);
                AlarmManager alarmManager= (AlarmManager) getSystemService(ALARM_SERVICE);
                alarmManager.setRepeating(AlarmManager.RTC_WAKEUP,System.currentTimeMillis(),1000*60*60*60,pendingIntent);
            }
        }, 1000*60*60*60);

    }
    /*

    this method using to fetch items collection by collection supported with
    progressBar
     */
    private void fetchData() {
        progressBar.setVisibility(View.VISIBLE);
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                for (int i = 10; i < items.size(); i++) {
                    item= items.get(i);
                    firt10Items.add(item);
                }
                repos_adapter.notifyDataSetChanged();
                progressBar.setVisibility(View.GONE);
            }
        }, 4000);


    }
    ///////////////////////////Caching Data//////////////////////////////////////
   // Check Internet Connection
    public boolean isConnected() throws InterruptedException, IOException {
        final String command = "ping -c 1 google.com";
        return Runtime.getRuntime().exec(command).waitFor() == 0;
    }

    // OKHttpClient

    public OkHttpClient getCahsing(){

        File httpCacheDirectory = new File(getCacheDir(), "responses");
        int cacheSize = 9*1024*1024;
        Cache cache = new Cache(httpCacheDirectory, cacheSize);


        OkHttpClient client = new OkHttpClient.Builder()
                .addNetworkInterceptor(REWRITE_CACHE_CONTROL_INTERCEPTOR)
                .cache(cache)
                .build();
        return client;
    }

    // Interceptor

    private Interceptor REWRITE_CACHE_CONTROL_INTERCEPTOR = new Interceptor() {
        @Override
        public okhttp3.Response intercept(Chain chain) throws IOException {
            okhttp3.Response originalResponse = chain.proceed(chain.request());
            if (check) {
                int maxAge = 60*24; // read from cache for 1 hour
                return originalResponse.newBuilder()
                        .header("Cache-Control", "public, max-age=" + maxAge)
                        .build();
            } else {
                int maxStale = 60 *24 ; // tolerate 4-weeks stale
                return originalResponse.newBuilder()
                        .header("Cache-Control", "public, only-if-cached, max-stale=" + maxStale)
                        .build();
            }
        }
    };
    ////////////////////////////////////////////////////////////////////////
                             // Ending Caching //
                  ////// Using Retrofit and OKHttp3///////
    ////////////////////////////////////////////////////////////////////////
}