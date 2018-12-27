package medic.esy.es.appstask.Api;

import android.content.Context;

import medic.esy.es.appstask.controller.MainActivity;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class client {

    public static final String BASE_URL ="https://api.github.com/users/square/";
    public static Retrofit retrofit=null;



    public static Retrofit getClient(){

        MainActivity cashing=new MainActivity();

        if(retrofit==null){
            retrofit=new Retrofit.Builder()
                    .baseUrl(BASE_URL)
                    .addConverterFactory(GsonConverterFactory.create())
                    .client(cashing.getCahsing())
                    .build();
        }
        return retrofit;
    }


}
