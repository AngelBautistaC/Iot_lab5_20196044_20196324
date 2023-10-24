package com.example.clase7ws.repository;

import com.example.clase7ws.entity.Employee;
import com.example.clase7ws.entity.Projections.EmployeeWithId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.rest.core.annotation.RepositoryRestResource;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

@RepositoryRestResource(path = "employee", excerptProjection = EmployeeWithId.class)
public interface EmployeeRepository extends JpaRepository<Employee, Integer>{
    List<Employee> findByManagerId(Integer managerId);
    Optional<Employee> findById(Integer id);

    @Modifying
    @Transactional
    @Query("update Employee e set e.meeting = ?1 where e.managerId = ?2 and e.id = ?3")
    int updateMeetingDetails( int meeting, Integer managerId, Integer id);

    @Modifying
    @Transactional
    @Query("update Employee e set e.employeeFeedback = ?1 where e.managerId = ?2 and e.id = ?3")
    int updateEmployeeFeedback(String feedback, Integer managerId, Integer id);


}