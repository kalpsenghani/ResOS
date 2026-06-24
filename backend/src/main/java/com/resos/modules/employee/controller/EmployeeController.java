package com.resos.modules.employee.controller;

import com.resos.modules.employee.domain.EmployeeStatus;
import com.resos.modules.employee.dto.*;
import com.resos.modules.employee.service.EmployeeService;
import com.resos.shared.api.ApiResponse;
import com.resos.shared.security.UserPrincipal;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/employees")
@RequiredArgsConstructor
public class EmployeeController {

    private final EmployeeService employeeService;

    @PutMapping("/schedules/{scheduleId}")
    @PreAuthorize("hasAuthority('employees:write')")
    public ResponseEntity<ApiResponse<ScheduleResponse>> updateSchedule(
            @PathVariable UUID scheduleId,
            @Valid @RequestBody UpdateScheduleRequest request,
            @AuthenticationPrincipal UserPrincipal principal) {
        return ResponseEntity.ok(ApiResponse.of(employeeService.updateSchedule(scheduleId, request, principal)));
    }

    @DeleteMapping("/schedules/{scheduleId}")
    @PreAuthorize("hasAuthority('employees:write')")
    public ResponseEntity<Void> deleteSchedule(
            @PathVariable UUID scheduleId,
            @AuthenticationPrincipal UserPrincipal principal) {
        employeeService.deleteSchedule(scheduleId, principal);
        return ResponseEntity.noContent().build();
    }

    @GetMapping
    @PreAuthorize("hasAuthority('employees:read')")
    public ResponseEntity<ApiResponse<List<EmployeeResponse>>> list(
            @RequestParam(required = false) UUID restaurantId,
            @RequestParam(required = false) EmployeeStatus status,
            @RequestParam(required = false) String position,
            @RequestParam(required = false) String search,
            @org.springframework.data.web.PageableDefault(size = 20) org.springframework.data.domain.Pageable pageable) {
        return ResponseEntity.ok(employeeService.listEmployees(restaurantId, status, position, search, pageable));
    }

    @PostMapping
    @PreAuthorize("hasAuthority('employees:write')")
    public ResponseEntity<ApiResponse<EmployeeResponse>> create(
            @Valid @RequestBody CreateEmployeeRequest request,
            @AuthenticationPrincipal UserPrincipal principal) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.of(employeeService.createEmployee(request, principal)));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority('employees:read')")
    public ResponseEntity<ApiResponse<EmployeeResponse>> get(@PathVariable UUID id) {
        return ResponseEntity.ok(ApiResponse.of(employeeService.getEmployee(id)));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority('employees:write')")
    public ResponseEntity<ApiResponse<EmployeeResponse>> update(
            @PathVariable UUID id,
            @Valid @RequestBody UpdateEmployeeRequest request,
            @RequestHeader(value = "If-Match", required = false) Integer ifMatch,
            @AuthenticationPrincipal UserPrincipal principal) {
        return ResponseEntity.ok(ApiResponse.of(employeeService.updateEmployee(id, request, ifMatch, principal)));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('employees:write')")
    public ResponseEntity<Void> delete(
            @PathVariable UUID id,
            @AuthenticationPrincipal UserPrincipal principal) {
        employeeService.deleteEmployee(id, principal);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/{id}/schedules")
    @PreAuthorize("hasAuthority('employees:read')")
    public ResponseEntity<ApiResponse<List<ScheduleResponse>>> listSchedules(
            @PathVariable UUID id,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        return ResponseEntity.ok(ApiResponse.of(employeeService.listSchedules(id, startDate, endDate)));
    }

    @PostMapping("/{id}/schedules")
    @PreAuthorize("hasAuthority('employees:write')")
    public ResponseEntity<ApiResponse<ScheduleResponse>> createSchedule(
            @PathVariable UUID id,
            @Valid @RequestBody CreateScheduleRequest request,
            @AuthenticationPrincipal UserPrincipal principal) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.of(employeeService.createSchedule(id, request, principal)));
    }
}
