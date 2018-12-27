package medic.esy.es.appstask.Api;

import java.util.List;

import medic.esy.es.appstask.model.item;
import retrofit2.Call;
import retrofit2.http.GET;

public interface service {
    @GET("repos")
    Call<List<item>>getItems();


}
