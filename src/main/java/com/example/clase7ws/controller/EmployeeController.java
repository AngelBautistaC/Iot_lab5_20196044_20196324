package com.example.clase7ws.controller;

import com.example.clase7ws.entity.Employee;
import com.example.clase7ws.repository.EmployeeRepository;
import lombok.Getter;
import lombok.Setter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.text.SimpleDateFormat;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Date;
import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api")
public class EmployeeController {

    @Autowired
    private EmployeeRepository employeeRepository;


    @GetMapping("/getEmployeeById")
    public ResponseEntity<Employee> getEmployeeById(@RequestParam Integer id) {
        Optional<Employee> employee = employeeRepository.findById(id);
        return employee.map(value -> new ResponseEntity<>(value, HttpStatus.OK)).orElseGet(() -> new ResponseEntity<>(HttpStatus.NOT_FOUND));
    }

    @PostMapping("/filterByManager")
    public List<Employee> filterByManager(@RequestBody ManagerIdRequest managerIdRequest) {
        return employeeRepository.findByManagerId(managerIdRequest.getManagerId());
    }

    @Getter
    public static class ManagerIdRequest {
        private Integer managerId;

        public void setManagerId(Integer managerId) {
            this.managerId = managerId;
        }
    }

    @PostMapping("/updateEmployeeFeedback")
    public String updateEmployeeFeedback(@RequestBody UpdateFeedbackRequest updateFeedbackRequest) {
        int updatedRows = employeeRepository.updateEmployeeFeedback(
                updateFeedbackRequest.getFeedback(),
                updateFeedbackRequest.getManagerId(),
                updateFeedbackRequest.getEmployeeId()
        );

        if (updatedRows > 0) {
            return "Success";
        } else {
            return "Failed";
        }
    }

    @Getter
    @Setter
    public static class UpdateFeedbackRequest {
        private Integer managerId;
        private Integer employeeId;
        private String feedback;
    }



    @PostMapping("/updateMeetingDetails")
    public String updateMeetingDetails(@RequestBody UpdateMeetingRequest updateMeetingRequest) {

        int updatedRows = employeeRepository.updateMeetingDetails(

                updateMeetingRequest.getMeeting(),
                updateMeetingRequest.getManagerId(),
                updateMeetingRequest.getEmployeeId()
        );

        if (updatedRows > 0) {
            return "Success";
        } else {
            return "Failed";
        }
    }



    @Getter
    public static class UpdateMeetingRequest {
        private Integer managerId;
        private Integer employeeId;
        private int meeting;

        // Getters y setters
        public void setManagerId(Integer managerId) {
            this.managerId = managerId;
        }

        public void setEmployeeId(Integer employeeId) {
            this.employeeId = employeeId;
        }

        public void setMeeting(int meeting) {
            this.meeting = meeting;
        }
    }



    }
