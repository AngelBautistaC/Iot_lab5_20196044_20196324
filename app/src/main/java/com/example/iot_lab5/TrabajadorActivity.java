package com.example.iot_lab5;

import static com.example.iot_lab5.MainActivity.CHANNEL_ID;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.NotificationCompat;

import android.app.DownloadManager;
import android.app.NotificationManager;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;

import com.example.iot_lab5.entity.Employee;

import java.io.File;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Random;
import java.util.TimeZone;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class TrabajadorActivity extends AppCompatActivity {
    private static final String CHANNEL_ID = "employee_notifications";


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_trabajador);

        Intent intent = getIntent();
        Employee employeeData = (Employee) intent.getSerializableExtra("employeeData");

        if (employeeData != null) {
            Integer meeting = employeeData.getMeeting();
            String meetingDateStr = employeeData.getMeetingDate();

            Date meetingDate = null;
            if (meetingDateStr != null) {
                SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'");
                sdf.setTimeZone(TimeZone.getTimeZone("UTC"));
                try {
                    meetingDate = sdf.parse(meetingDateStr);
                } catch (ParseException e) {
                    e.printStackTrace();
                }
            }

            Date currentDate = new Date();

            // Manipulación de la visibilidad del botón
            Button feedbackButton = findViewById(R.id.feedbackButton);
            Button downloadScheduleButton = findViewById(R.id.downloadScheduleButton);
            LinearLayout feedback = findViewById(R.id.feedback);




            if (feedbackButton != null) {
                if (meeting == null || meeting != 1 || (meetingDate != null && meetingDate.after(currentDate))) {
                    feedbackButton.setVisibility(View.GONE);
                }
                else{

                    feedbackButton.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            feedback.setVisibility(View.VISIBLE);
                            Button sendCommentButton = findViewById(R.id.sendCommentButton);
                            EditText commentEditText = findViewById(R.id.commentEditText);

                            sendCommentButton.setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View view) {
                                    String comment = commentEditText.getText().toString();
                                    if (!comment.isEmpty()) {
                                        sendFeedbackToServer(comment, employeeData);
                                    } else {
                                        // Mostrar algún mensaje de error o feedback al usuario
                                    }
                                }
                            });


                        }
                    });

                }


            }

            if (downloadScheduleButton != null) {
                if (meeting == null || meeting != 1 || (meetingDate != null && meetingDate.before(currentDate))) {
                    downloadScheduleButton.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            showNotification("Tutorías", "No cuenta con tutorías pendientes");
                        }
                    });
                } else {
                    downloadScheduleButton.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            descargarConDownloadManager();
                        }
                    });
                }
            }
        }
    }

    public void descargarConDownloadManager() {
        String fileName = "imagen_descargada.jpg";
        String url = "https://i.pinimg.com/564x/4e/8e/a5/4e8ea537c896aa277e6449bdca6c45da.jpg";

        Uri downloadUri = Uri.parse(url);
        DownloadManager.Request request = new DownloadManager.Request(downloadUri);
        request.setAllowedNetworkTypes(DownloadManager.Request.NETWORK_WIFI | DownloadManager.Request.NETWORK_MOBILE);
        request.setAllowedOverRoaming(false);
        request.setTitle(fileName);
        request.setMimeType("image/jpeg");
        request.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED);
        request.setDestinationInExternalPublicDir(Environment.DIRECTORY_PICTURES, File.separator + fileName);

        DownloadManager dm = (DownloadManager) getSystemService(Context.DOWNLOAD_SERVICE);
        try {
            dm.enqueue(request);
        } catch (RuntimeException e) {
            e.printStackTrace();
        }
    }


    private void showNotification(String title, String message) {
        NotificationCompat.Builder builder = new NotificationCompat.Builder(this, CHANNEL_ID)
                .setSmallIcon(R.drawable.ic_notification)
                .setContentTitle(title)
                .setContentText(message)
                .setPriority(NotificationCompat.PRIORITY_HIGH);

        NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

        notificationManager.notify(new Random().nextInt(), builder.build());
    }

    private void sendFeedbackToServer(String comment, Employee employeeData) {
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl("http://10.0.2.2:8080/")
                .addConverterFactory(GsonConverterFactory.create())
                .build();

        EmployeeApi employeeApi = retrofit.create(EmployeeApi.class);

        Feedback feedback = new Feedback();
        feedback.setFeedback(comment);
        feedback.setManagerId(employeeData.getManagerId());
        feedback.setEmployeeId(Integer.parseInt(employeeData.getId()));

        Call<Void> call = employeeApi.sendFeedback(feedback);
        call.enqueue(new Callback<Void>() {
            @Override
            public void onResponse(Call<Void> call, Response<Void> response) {
                if (response.isSuccessful()) {
                    showNotification("Feedback", "Feedback enviado de manera exitosa");
                } else {
                    // Mostrar algún mensaje de error
                }
            }

            @Override
            public void onFailure(Call<Void> call, Throwable t) {
                // Mostrar algún mensaje de error
            }
        });
    }

}