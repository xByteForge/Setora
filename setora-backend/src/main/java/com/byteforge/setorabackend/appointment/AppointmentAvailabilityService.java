package com.byteforge.setorabackend.appointment;

import com.byteforge.setorabackend.business.Business;
import com.byteforge.setorabackend.common.TimeInterval;
import com.byteforge.setorabackend.employees.model.AvailabilityDtos;
import com.byteforge.setorabackend.employees.model.Employee;
import com.byteforge.setorabackend.employees.model.WorkSchedule;
import com.byteforge.setorabackend.employees.service.EmployeeService;
import com.byteforge.setorabackend.serviceItem.ServiceItem;
import com.byteforge.setorabackend.serviceItem.ServiceItemService;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AppointmentAvailabilityService {
    private final ServiceItemService serviceItemService;
    private final EmployeeService employeeService;
    private final AppointmentService appointmentService;

    /**
     * Retrieves all available starting times for booking a service on a given date.
     * For each eligible employee, checks their working schedule, unavailable
     * slots, and existing non-cancelled appointments. Only returns starting
     * times for which the entire service duration fits within working hours
     * and does not overlap with any unavailable slot or existing appointment.
     * The returned times are sorted and contain no duplicates.
     *
     * @param businessId ID of the business offering the service
     * @param date date for which availability is requested
     * @param serviceId ID of the service to be booked
     * @param employeeId optional ID of the employee; null considers all eligible employees
     * @return sorted list of unique available appointment starting times
     * @throws RuntimeException if the service is inactive, does not belong to the
     *         specified business, or the specified employee is not eligible
     */
    @Transactional(readOnly = true)
    public List<LocalTime> getAllAvailableAppointmentTimesForToday(
            int businessId, LocalDate date, int serviceId, Integer employeeId) {
        // check business and service match
        ServiceItem serviceItem = serviceItemService.findById(serviceId);
        Business business = serviceItem.getBusiness();
        if (business.getId() != businessId) {
            throw new RuntimeException(
                    "The service provided does not belong to the business provided"); // aka we needn't receive the
            // businessId
        }
        if (!serviceItem.isActive()) {
            throw new RuntimeException("This service is not active");
        }

        // check if Employee is available for that service;
        // should've been checked beforehand, when showing the user
        List<Employee> emps = new ArrayList<>();

        if (employeeId != null) {
            Employee employee = employeeService.findById(employeeId);
            if (employee.getBusiness().getId() != businessId
                    || !employee.isActive()
                    || !employee.getServiceItems().contains(serviceItem)) {
                throw new RuntimeException("The employee selected does not perform this service.");
            }
            emps.add(employee);
        } else {
            emps = employeeService.findAllEmployeesByServiceId(serviceId);
        }

        Set<LocalTime> availableTimes = new TreeSet<>();

        for (Employee emp : emps) {
            // find working schedule for given day
            Optional<WorkSchedule> workSchedule =
                    employeeService.findWorkScheduleByEmployeeId(emp.getId(), date.getDayOfWeek());
            if (workSchedule.isEmpty()) continue;

            // All intervals for my given date where employee is 'on leave'.
            List<AvailabilityDtos.UnavailableOccurrence> unavailableSlotsOfEmployee = employeeService
                    .getUnavailableSlots(emp.getId(), date, null, null)
                    .slots();

            // All appointments of the employee on this date
            List<Appointment> existingAppointmentsForEmployee =
                    appointmentService.findByEmployeeIdOnDate(emp.getId(), date);

            // Generate all possible start times, compute end times and check if it intersects unavailableOfEmployee or
            // existingAppointmentsForEmployee
            LocalTime workingStart = workSchedule.get().getInterval().getStart();

            LocalTime workingEnd = workSchedule.get().getInterval().getEnd();

            for (LocalTime candidateStart = workingStart;
                    !candidateStart
                            .plusMinutes(serviceItem.getDurationMinutes())
                            .isAfter(workingEnd);
                    candidateStart = candidateStart.plusMinutes(business.getTimeSlice())) {

                LocalTime candidateEnd = candidateStart.plusMinutes(serviceItem.getDurationMinutes());

                // Check unavailable slots
                TimeInterval currentInterval = new TimeInterval(candidateStart, candidateEnd);

                boolean overlapsUnavailable = unavailableSlotsOfEmployee.stream()
                        .map(slot -> new TimeInterval(slot.start(), slot.end()))
                        .anyMatch(interval -> interval.overlaps(currentInterval));

                if (overlapsUnavailable) continue;

                // Check existing appointments
                boolean overlapsAppointment = existingAppointmentsForEmployee.stream()
                        .filter(app -> app.getStatus() != AppointmentStatus.CANCELLED)
                        .map(app -> new TimeInterval(
                                app.getInterval()
                                        .getStart()
                                        .atZone(business.getTimezone())
                                        .toLocalTime(),
                                app.getInterval()
                                        .getEnd()
                                        .atZone(business.getTimezone())
                                        .toLocalTime()))
                        .anyMatch(interval -> interval.overlaps(currentInterval));

                if (overlapsAppointment) continue;

                // 7. This starting time is available for this employee
                availableTimes.add(candidateStart);
            }
        }
        return new ArrayList<>(availableTimes);
    }
}
