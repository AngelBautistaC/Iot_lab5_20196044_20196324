package com.example.iot_lab5;

import com.example.iot_lab5.entity.Employee;

import java.util.List;
import java.util.Map;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.Query;

public interface EmployeeApi {
    @GET("api/getEmployeeById")
    Call<Employee> getEmployeeById(@Query("id") int id);

    @POST("api/updateEmployeeFeedback")
    Call<Void> sendFeedback(@Body Feedback feedback);

    @POST("api/filterByManager")
    Call<List<Employee>> filterByManager(@Body Map<String, Integer> managerId);

    @POST("api/updateMeetingDetails")
    Call<ResponseBody> updateMeetingDetails(@Body Map<String, Integer> meetingDetails);


}
