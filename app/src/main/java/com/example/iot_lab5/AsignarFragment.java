package com.example.iot_lab5;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import android.os.Build;
import android.os.Bundle;

import androidx.core.app.NotificationCompat;
import androidx.fragment.app.Fragment;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;

import com.example.iot_lab5.entity.Employee;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;


public class AsignarFragment extends Fragment {

    private EditText tutorCodeEditText, empleadoCodeEditText;
    private Button asignarButton;
    private static final String TUTOR_CHANNEL_ID = "tutor_notifications";

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_asignar, container, false);

        tutorCodeEditText = view.findViewById(R.id.tutorCode);
        empleadoCodeEditText = view.findViewById(R.id.empleadoCode);
        asignarButton = view.findViewById(R.id.downloadButton);

        asignarButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int managerId = Integer.parseInt(tutorCodeEditText.getText().toString());
                int employeeId = Integer.parseInt(empleadoCodeEditText.getText().toString());

                Retrofit retrofit = new Retrofit.Builder()
                        .baseUrl("http://10.0.2.2:8080/")
                        .addConverterFactory(GsonConverterFactory.create())
                        .build();

                EmployeeApi employeeApi = retrofit.create(EmployeeApi.class);


                Call<Employee> callGetEmployee = employeeApi.getEmployeeById(employeeId);

                callGetEmployee.enqueue(new Callback<Employee>() {
                    @Override
                    public void onResponse(Call<Employee> call, Response<Employee> response) {
                        if (response.isSuccessful() && response.body() != null) {
                            Employee employee = response.body();
                            if (employee.getManagerId() != managerId) {
                                showNotification("Usted no es el manager de este empleado.", TUTOR_CHANNEL_ID);
                                return;
                            }

                            if (employee.getMeeting() != null && employee.getMeeting() == 1) {
                                showNotification("El trabajador ya tiene una cita asignada. Elija otro trabajador", TUTOR_CHANNEL_ID);
                                return;
                            }

                            // Realizar la operación de asignación si todas las validaciones son correctas
                            Map<String, Integer> meetingDetails = new HashMap<>();
                            meetingDetails.put("managerId", managerId);
                            meetingDetails.put("employeeId", employeeId);
                            meetingDetails.put("meeting", 1);

                            Call<ResponseBody> callUpdateMeeting = employeeApi.updateMeetingDetails(meetingDetails);

                            callUpdateMeeting.enqueue(new Callback<ResponseBody>() {
                                @Override
                                public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                                    try {
                                        if (response.isSuccessful() && response.body() != null) {
                                            String responseBody = response.body().string();
                                            if ("Failed".equals(responseBody)) {
                                                showNotification("Operación fallida, verifique los IDs y la asignación", TUTOR_CHANNEL_ID);
                                            } else {
                                                showNotification("Asignación del trabajador correcta", TUTOR_CHANNEL_ID);
                                            }
                                        } else {
                                            showNotification("Operación fallida, código de estado HTTP: " + response.code(), TUTOR_CHANNEL_ID);
                                        }
                                    } catch (IOException e) {
                                        e.printStackTrace();
                                    }
                                }

                                @Override
                                public void onFailure(Call<ResponseBody> call, Throwable t) {
                                    showNotification("Error de red: " + t.getMessage(), TUTOR_CHANNEL_ID);
                                }
                            });

                        } else {
                            showNotification("Empleado no encontrado", TUTOR_CHANNEL_ID);
                        }
                    }

                    @Override
                    public void onFailure(Call<Employee> call, Throwable t) {
                        showNotification("Error al obtener información del empleado: " + t.getMessage(), TUTOR_CHANNEL_ID);
                    }
                });
            }
        });

        return view;
    }

    private void showNotification(String message, String channelId) {
        NotificationManager notificationManager = (NotificationManager) getContext().getSystemService(Context.NOTIFICATION_SERVICE);

        NotificationChannel channel = new NotificationChannel(channelId, "Tutor Notifications", NotificationManager.IMPORTANCE_DEFAULT);
        notificationManager.createNotificationChannel(channel);

        NotificationCompat.Builder builder = new NotificationCompat.Builder(getContext(), channelId)
                .setSmallIcon(R.drawable.ic_admin)
                .setContentTitle("Notificación de Tutor")
                .setContentText(message)
                .setPriority(NotificationCompat.PRIORITY_DEFAULT);

        notificationManager.notify(1, builder.build());
    }
}