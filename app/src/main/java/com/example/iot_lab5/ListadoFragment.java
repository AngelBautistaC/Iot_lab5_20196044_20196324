package com.example.iot_lab5;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.fragment.app.Fragment;

import android.os.ParcelFileDescriptor;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.example.iot_lab5.entity.Employee;

import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;


public class ListadoFragment extends Fragment {

    private EditText tutorCodeEditText;
    private Button downloadButton;
    private ActivityResultLauncher<Intent> saveFileLauncher;
    private List<Employee> employeesToSave;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_listado, container, false);

        tutorCodeEditText = view.findViewById(R.id.tutorCodeEditText);
        downloadButton = view.findViewById(R.id.downloadButton);

        saveFileLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == Activity.RESULT_OK) {
                        Intent data = result.getData();
                        if (data != null) {
                            Uri uri = data.getData();
                            try {
                                ParcelFileDescriptor pfd = getContext().getContentResolver().openFileDescriptor(uri, "w");
                                FileOutputStream fileOutputStream = new FileOutputStream(pfd.getFileDescriptor());
                                FileWriter fileWriter = new FileWriter(fileOutputStream.getFD());
                                for (Employee employee : employeesToSave) {
                                    fileWriter.write(employee.getFirstName() + " " + employee.getLastName() + "\n");
                                }
                                fileWriter.close();
                                fileOutputStream.getFD().sync();
                                fileOutputStream.close();
                                pfd.close();
                                Toast.makeText(getContext(), "Employee list downloaded successfully", Toast.LENGTH_SHORT).show();
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                        }
                    }
                }
        );

        downloadButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int managerId = Integer.parseInt(tutorCodeEditText.getText().toString());
                Retrofit retrofit = new Retrofit.Builder()
                        .baseUrl("http://10.0.2.2:8080/")
                        .addConverterFactory(GsonConverterFactory.create())
                        .build();

                EmployeeApi employeeApi = retrofit.create(EmployeeApi.class);
                Map<String, Integer> managerIdMap = new HashMap<>();
                managerIdMap.put("managerId", managerId);

                Call<List<Employee>> call = employeeApi.filterByManager(managerIdMap);

                call.enqueue(new Callback<List<Employee>>() {
                    @Override
                    public void onResponse(Call<List<Employee>> call, Response<List<Employee>> response) {
                        if (response.isSuccessful()) {
                            List<Employee> employees = response.body();
                            if (employees == null || employees.isEmpty()) {
                            } else {
                                employeesToSave = employees;
                                String fileName = "ListaEmpleados_ManagerID_" + managerId + ".txt";
                                Intent intent = new Intent(Intent.ACTION_CREATE_DOCUMENT);
                                intent.addCategory(Intent.CATEGORY_OPENABLE);
                                intent.setType("text/plain");
                                intent.putExtra(Intent.EXTRA_TITLE, fileName);
                                saveFileLauncher.launch(intent);
                            }
                        }else if (response.code() == 404) {
                            Toast.makeText(getContext(), "No existe dicho empleado", Toast.LENGTH_SHORT).show();
                        }
                    }

                    @Override
                    public void onFailure(Call<List<Employee>> call, Throwable t) {
                        Log.e("ListadoFragment", "Network error: " + t.getMessage());
                    }
                });
            }
        });

        return view;
    }
}