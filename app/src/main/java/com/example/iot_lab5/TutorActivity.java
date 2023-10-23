package com.example.iot_lab5;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatButton;
import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import java.util.Random;

public class TutorActivity extends AppCompatActivity {

    AppCompatButton listadoButton;
    AppCompatButton trabajadorButton;
    AppCompatButton tutoriaButton;
    TextView titulo;

    private static final String TUTOR_CHANNEL_ID = "tutor_notifications";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_tutor);

        titulo = findViewById(R.id.titleTextView);



        listadoButton = findViewById(R.id.listadoButton);
        trabajadorButton = findViewById(R.id.trabajadorButton);
        tutoriaButton = findViewById(R.id.tutoriaButton);
        listadoButton.setEnabled(false);
        titulo.setText("Listar Trabajadores");
        listadoButton.setAlpha(0.5f);


        createNotificationChannel();
        showNotification("Modo Tutor", "Está entrando en modo Tutor");



        loadFragment(new ListadoFragment());

        trabajadorButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // Cambiar el título y el estado del botón
                titulo.setText("Buscar Trabajador");
                updateButtonStates(trabajadorButton);

                // Cargar el fragmento Buscar
                loadFragment(new BuscarFragment());
            }
        });

        listadoButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // Cambiar el título y el estado del botón
                titulo.setText("Listar Trabajadores");
                updateButtonStates(listadoButton);

                // Cargar el fragmento Listado
                loadFragment(new ListadoFragment());
            }
        });

        tutoriaButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // Cambiar el título y el estado del botón
                titulo.setText("Asignar Tutoría");
                updateButtonStates(tutoriaButton);

                // Cargar el fragmento Asignar
                loadFragment(new AsignarFragment());
            }
        });

    }

    private void createNotificationChannel() {
        CharSequence name = "Tutor Notifications";
        String description = "Notifications for tutors";
        int importance = NotificationManager.IMPORTANCE_HIGH;
        NotificationChannel channel = new NotificationChannel(TUTOR_CHANNEL_ID, name, importance);
        channel.setDescription(description);

        NotificationManager notificationManager = getSystemService(NotificationManager.class);
        if (notificationManager != null) {
            notificationManager.createNotificationChannel(channel);
        }
    }

    private void showNotification(String title, String message) {
        NotificationCompat.Builder builder = new NotificationCompat.Builder(this, TUTOR_CHANNEL_ID)
                .setSmallIcon(R.drawable.ic_admin) // Asegúrate de tener un drawable para la notificación
                .setContentTitle(title)
                .setContentText(message)
                .setPriority(NotificationCompat.PRIORITY_HIGH);

        NotificationManagerCompat notificationManager = NotificationManagerCompat.from(this);

        // Muestra la notificación
        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {

            return;
        }
        notificationManager.notify(new Random().nextInt(), builder.build());
    }


    private void loadFragment(Fragment fragment) {
        // Cargar el fragmento en el contenedor
        FragmentManager fragmentManager = getSupportFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        fragmentTransaction.replace(R.id.fragmentContainerView, fragment);
        fragmentTransaction.commit();
    }

    private void updateButtonStates(AppCompatButton activatedButton) {
        // Restablecer todos los botones a estado habilitado y opacidad completa
        listadoButton.setEnabled(true);
        listadoButton.setAlpha(1f);
        trabajadorButton.setEnabled(true);
        trabajadorButton.setAlpha(1f);
        tutoriaButton.setEnabled(true);
        tutoriaButton.setAlpha(1f);

        // Desactivar y hacer menos opaco el botón activado
        activatedButton.setEnabled(false);
        activatedButton.setAlpha(0.5f);
    }


}