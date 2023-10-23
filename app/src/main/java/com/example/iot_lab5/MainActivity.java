package com.example.iot_lab5;

import static android.Manifest.permission.POST_NOTIFICATIONS;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

import android.app.AlertDialog;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.example.iot_lab5.entity.Employee;

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

public class MainActivity extends AppCompatActivity {

    private void showNotification(String title, String message) {
        // Crea un Intent para abrir TrabajadorActivity
        Intent intent = new Intent(this, TrabajadorActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, intent, PendingIntent.FLAG_IMMUTABLE);

        // Construye la notificación
        NotificationCompat.Builder builder = new NotificationCompat.Builder(this, CHANNEL_ID)
                .setSmallIcon(R.drawable.ic_notification) // Asegúrate de tener un drawable para la notificación
                .setContentTitle(title)
                .setContentText(message)
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setContentIntent(pendingIntent);


        NotificationManagerCompat notificationManager = NotificationManagerCompat.from(this);

        // Muestra la notificación
        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        notificationManager.notify(new Random().nextInt(), builder.build());
    }


    private int trabajadorId;
    private Employee employeeData;
    static final String CHANNEL_ID = "employee_notifications";







    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        createNotificationChannel();



        Button trabajadorButton = findViewById(R.id.trabajadorButton);
        Button tutorButton = findViewById(R.id.tutorButton);

        trabajadorButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showWorkerIdPrompt();
            }
        });

        tutorButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, TutorActivity.class);
                startActivity(intent);
            }
        });
    }

    public void pedirPermisos() {
        // TIRAMISU = 33
        if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
                ActivityCompat.checkSelfPermission(this, POST_NOTIFICATIONS) == PackageManager.PERMISSION_DENIED) {

            ActivityCompat.requestPermissions(MainActivity.this, new String[]{POST_NOTIFICATIONS}, 101);
        }
    }

    private void fetchEmployeeData(int id) {
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl("http://10.0.2.2:8080/")
                .addConverterFactory(GsonConverterFactory.create())
                .build();

        EmployeeApi employeeApi = retrofit.create(EmployeeApi.class);
        Call<Employee> call = employeeApi.getEmployeeById(id);

        call.enqueue(new Callback<Employee>() {
            @Override
            public void onResponse(Call<Employee> call, Response<Employee> response) {




                if (response.isSuccessful()) {
                    Employee employeeData = response.body();
                    if (employeeData != null) {

                        showNotification("Modo empleado", "Está entrando en modo Empleado");

                        if (employeeData.getMeeting() == 1) {
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
                            if ((meetingDate != null && meetingDate.after(currentDate))) {
                                showNotification("Tutoría agendada", "Tienes una tutoría agendada para " + meetingDate);

                            }


                        }

                        Intent intent = new Intent(MainActivity.this, TrabajadorActivity.class);
                        intent.putExtra("employeeData", employeeData);
                        startActivity(intent);

                    } else {
                        Toast.makeText(MainActivity.this, "Usuario no existe", Toast.LENGTH_SHORT).show();
                    }
                } else {
                    Toast.makeText(MainActivity.this, "Usuario no existe", Toast.LENGTH_SHORT).show();

                }


            }



            @Override
            public void onFailure(Call<Employee> call, Throwable t) {
                Log.d("msg-test", "Error de red: " + t.getMessage());
                Toast.makeText(MainActivity.this, "Error de red", Toast.LENGTH_SHORT).show();
            }
        });
    }


    private void showWorkerIdPrompt() {
        final EditText workerIdInput = new EditText(this);
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Introduce el ID del trabajador")
                .setView(workerIdInput)
                .setPositiveButton("Aceptar", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        String input = workerIdInput.getText().toString();
                        if (isValidWorkerId(input)) {
                            trabajadorId = Integer.parseInt(input);

                            fetchEmployeeData(trabajadorId);
                        } else {
                            Toast.makeText(MainActivity.this, "Código inválido", Toast.LENGTH_SHORT).show();
                        }
                    }
                })
                .setNegativeButton("Cancelar", null)
                .show();
    }

    private boolean isValidWorkerId(String input) {
        try {
            int id = Integer.parseInt(input);
            return id > 0;
        } catch (NumberFormatException e) {
            return false;
        }
    }

    private void createNotificationChannel() {
        pedirPermisos();
        CharSequence name = "Employee Notifications";
        String description = "Notifications for employees";
        int importance = NotificationManager.IMPORTANCE_HIGH;
        NotificationChannel channel = new NotificationChannel(CHANNEL_ID, name, importance);
        channel.setDescription(description);
        NotificationManager notificationManager = getSystemService(NotificationManager.class);
        notificationManager.createNotificationChannel(channel);
    }

}
