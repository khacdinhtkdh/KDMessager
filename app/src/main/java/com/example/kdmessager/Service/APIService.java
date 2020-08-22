package com.example.kdmessager.Service;

import com.example.kdmessager.ModelClasses.MyResponse;
import com.example.kdmessager.ModelClasses.Sender;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.Headers;
import retrofit2.http.POST;

import static com.example.kdmessager.Ultilities.ConstantsKt.TOKEN_KEY;

public interface APIService {
    @Headers({
            "Content-Type:application/json",
            "Authorization:key=AAAAPbTdCsY:APA91bE4QxMc8o4A3M5p-Y-W3jLjj-D5KtKXcRB8ZcxqpbldVjlw8gF16OpHtMG9PhsuHaBr_tcTPmM-v9LhDujuyoKTQDXhynEytc3PSNaX1zB1KG_gzfWSwdXmDBcgLaMQc4cgacEW"
    })
    @POST("fcm/send")
    Call<MyResponse> sendNotification(@Body Sender body);
}
