package com.example.iot_lab5;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.ParcelFileDescriptor;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.fragment.app.Fragment;

import com.example.iot_lab5.entity.Employee;

import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class BuscarFragment extends Fragment {

    private EditText tutorCodeEditText;
    private Button downloadButton;
    private ActivityResultLauncher<Intent> saveFileLauncher;
    private Employee employeeToSave;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_buscar, container, false);

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

                                String employeeInfo = "ID: " + employeeToSave.getId() + "\n" +
                                        "First Name: " + employeeToSave.getFirstName() + "\n" +
                                        "Last Name: " + employeeToSave.getLastName() + "\n" +
                                        "Email: " + employeeToSave.getEmail() + "\n" +
                                        "Phone Number: " + employeeToSave.getPhoneNumber() + "\n" +
                                        "Hire Date: " + employeeToSave.getHireDate() + "\n" +
                                        "Manager ID: " + employeeToSave.getManagerId() + "\n" +
                                        "Meeting: " + employeeToSave.getMeeting() + "\n" +
                                        "Employee Feedback: " + employeeToSave.getEmployeeFeedback() + "\n" +
                                        "Meeting Date: " + employeeToSave.getMeetingDate();

                                fileWriter.write(employeeInfo);

                                fileWriter.close();
                                fileOutputStream.getFD().sync();
                                fileOutputStream.close();
                                pfd.close();
                                Toast.makeText(getContext(), "Employee information downloaded successfully", Toast.LENGTH_SHORT).show();
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
                int employeeId = Integer.parseInt(tutorCodeEditText.getText().toString());
                Retrofit retrofit = new Retrofit.Builder()
                        .baseUrl("http://10.0.2.2:8080/")
                        .addConverterFactory(GsonConverterFactory.create())
                        .build();

                EmployeeApi employeeApi = retrofit.create(EmployeeApi.class);

                Call<Employee> call = employeeApi.getEmployeeById(employeeId);

                call.enqueue(new Callback<Employee>() {
                    @Override
                    public void onResponse(Call<Employee> call, Response<Employee> response) {
                        if (response.isSuccessful()) {
                            Employee employee = response.body();
                            if (employee == null) {
                                Log.d("BuscarFragment", "No employee found for this ID");
                                Toast.makeText(getContext(), "Sin información para dicho código", Toast.LENGTH_SHORT).show();
                            } else {
                                employeeToSave = employee;
                                String fileName = "informacionDe" + employee.getId() + ".txt";
                                Intent intent = new Intent(Intent.ACTION_CREATE_DOCUMENT);
                                intent.addCategory(Intent.CATEGORY_OPENABLE);
                                intent.setType("text/plain");
                                intent.putExtra(Intent.EXTRA_TITLE, fileName);
                                saveFileLauncher.launch(intent);
                            }
                        }
                    }

                    @Override
                    public void onFailure(Call<Employee> call, Throwable t) {
                        Toast.makeText(getContext(), "Sin información para dicho código", Toast.LENGTH_SHORT).show();
                    }
                });
            }
        });

        return view;
    }
}
