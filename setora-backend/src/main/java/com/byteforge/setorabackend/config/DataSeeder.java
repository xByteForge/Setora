/*
 * DEVELOPMENT DATABASE SEEDER
 *
 * Generates a sample dataset for testing the Setora application.
 *
 * Dataset:
 * - 10 businesses, each with a unique owner and location.
 * - 40 services (4 per business).
 * - 40 employees (4 per business).
 * - 60 users (10 clients, 10 business owners, 40 employees).
 * - 200 work schedules (5 weekdays per employee).
 * - 110 appointments (11 per business).
 *   - 6 appointments next Monday.
 *   - 5 appointments next Tuesday.
 * - 240 unavailable slots (5 recurring breaks and 1 specific
 *   unavailable slot per employee).
 *
 * All businesses use the Europe/Bucharest timezone.
 *
 * The dataset is generated only when no businesses exist.
 * This seeder is intended exclusively for development and testing.
 */
package com.byteforge.setorabackend.config;

import com.byteforge.setorabackend.appointment.*;
import com.byteforge.setorabackend.auth.User;
import com.byteforge.setorabackend.auth.UserRepository;
import com.byteforge.setorabackend.business.Business;
import com.byteforge.setorabackend.business.BusinessRepository;
import com.byteforge.setorabackend.business.Location;
import com.byteforge.setorabackend.common.DateTimeInterval;
import com.byteforge.setorabackend.common.TimeInterval;
import com.byteforge.setorabackend.employees.model.Employee;
import com.byteforge.setorabackend.employees.model.UnavailableSlot;
import com.byteforge.setorabackend.employees.model.UnavailableType;
import com.byteforge.setorabackend.employees.model.WorkSchedule;
import com.byteforge.setorabackend.employees.repository.EmployeeRepository;
import com.byteforge.setorabackend.employees.repository.UnavailableSlotRepository;
import com.byteforge.setorabackend.employees.repository.WorkScheduleRepository;
import com.byteforge.setorabackend.serviceItem.ServiceItem;
import com.byteforge.setorabackend.serviceItem.ServiceItemRepository;
import java.math.BigDecimal;
import java.time.DayOfWeek;
import java.time.Duration;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.temporal.TemporalAdjusters;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Component
@RequiredArgsConstructor
public class DataSeeder implements CommandLineRunner {

    private static final ZoneId ZONE = ZoneId.of("Europe/Bucharest");

    private static final List<DayOfWeek> WORK_DAYS =
            List.of(DayOfWeek.MONDAY, DayOfWeek.TUESDAY, DayOfWeek.WEDNESDAY, DayOfWeek.THURSDAY, DayOfWeek.FRIDAY);

    private static final List<String> BUSINESS_NAMES = List.of(
            "Studio Armonia",
            "Beauty House",
            "Urban Barber",
            "The Nail Room",
            "Glow Studio",
            "Velvet Beauty",
            "Classic Cuts",
            "Aura Wellness",
            "Elite Studio",
            "Bloom Beauty");

    private static final List<String> FIRST_NAMES = List.of(
            "Maria",
            "Andrei",
            "Elena",
            "Alexandru",
            "Ioana",
            "Mihai",
            "Ana",
            "David",
            "Sofia",
            "Gabriel",
            "Irina",
            "Vlad",
            "Diana",
            "Matei",
            "Bianca",
            "Radu",
            "Cristina",
            "Paul",
            "Laura",
            "Stefan");

    private static final List<String> SURNAMES = List.of(
            "Popescu",
            "Ionescu",
            "Dumitru",
            "Stan",
            "Radu",
            "Marin",
            "Dobre",
            "Munteanu",
            "Constantin",
            "Georgescu",
            "Stoica",
            "Ilie",
            "Preda",
            "Tudor",
            "Nistor",
            "Florea",
            "Petrescu",
            "Moldovan",
            "Enache",
            "Lazar");

    private final UserRepository userRepository;
    private final BusinessRepository businessRepository;
    private final ServiceItemRepository serviceItemRepository;
    private final EmployeeRepository employeeRepository;
    private final WorkScheduleRepository workScheduleRepository;
    private final AppointmentRepository appointmentRepository;
    private final UnavailableSlotRepository unavailableSlotRepository;

    private int userCounter = 0;

    @Override
    @Transactional
    public void run(String... args) {

        // Do not duplicate seed data on subsequent application starts.
        if (businessRepository.count() > 0) {
            log.info("Businesses already exist. Skipping data seeding.");
            return;
        }

        log.info("Starting development data seeding...");

        List<User> clients = seedClients(10);

        List<Employee> allEmployees = new ArrayList<>();

        LocalDate nextMonday = LocalDate.now(ZONE).with(TemporalAdjusters.next(DayOfWeek.MONDAY));

        LocalDate nextTuesday = nextMonday.plusDays(1);

        for (int i = 0; i < BUSINESS_NAMES.size(); i++) {

            // Create business owner.
            User owner = seedUser("Owner", "Business" + (i + 1), "owner" + (i + 1) + "@setora.test");

            // Create business.
            Business business = seedBusiness(owner, i);

            // Create four services.
            List<ServiceItem> services = seedServices(business);

            // Create four employees with different service assignments.
            List<Employee> employees = List.of(
                    seedEmployee(
                            business,
                            seedEmployeeUser(i * 4),
                            Set.of(services.get(0), services.get(1)),
                            LocalTime.of(9, 0),
                            LocalTime.of(17, 0)),
                    seedEmployee(
                            business,
                            seedEmployeeUser(i * 4 + 1),
                            Set.of(services.get(1), services.get(2)),
                            LocalTime.of(10, 0),
                            LocalTime.of(18, 0)),
                    seedEmployee(
                            business,
                            seedEmployeeUser(i * 4 + 2),
                            Set.of(services.get(0), services.get(2), services.get(3)),
                            LocalTime.of(8, 0),
                            LocalTime.of(16, 0)),
                    seedEmployee(
                            business,
                            seedEmployeeUser(i * 4 + 3),
                            Set.of(services.get(1), services.get(3)),
                            LocalTime.of(12, 0),
                            LocalTime.of(20, 0)));

            allEmployees.addAll(employees);

            // ==========================================
            // MONDAY - 6 APPOINTMENTS
            // ==========================================

            seedAppointment(
                    clients.get(i % clients.size()),
                    employees.get(0),
                    services.get(0),
                    nextMonday,
                    LocalTime.of(9, 0),
                    AppointmentStatus.CONFIRMED);

            seedAppointment(
                    clients.get((i + 1) % clients.size()),
                    employees.get(1),
                    services.get(1),
                    nextMonday,
                    LocalTime.of(12, 0),
                    AppointmentStatus.CONFIRMED);

            seedAppointment(
                    clients.get((i + 2) % clients.size()),
                    employees.get(2),
                    services.get(2),
                    nextMonday,
                    LocalTime.of(10, 0),
                    AppointmentStatus.CANCELLED);

            seedAppointment(
                    clients.get((i + 3) % clients.size()),
                    employees.get(3),
                    services.get(3),
                    nextMonday,
                    LocalTime.of(15, 0),
                    AppointmentStatus.CONFIRMED);

            seedAppointment(
                    clients.get((i + 4) % clients.size()),
                    employees.get(0),
                    services.get(1),
                    nextMonday,
                    LocalTime.of(14, 0),
                    AppointmentStatus.CONFIRMED);

            seedAppointment(
                    clients.get((i + 5) % clients.size()),
                    employees.get(2),
                    services.get(0),
                    nextMonday,
                    LocalTime.of(15, 0),
                    AppointmentStatus.CANCELLED);

            // ==========================================
            // TUESDAY - 5 APPOINTMENTS
            // ==========================================

            seedAppointment(
                    clients.get((i + 6) % clients.size()),
                    employees.get(0),
                    services.get(0),
                    nextTuesday,
                    LocalTime.of(9, 0),
                    AppointmentStatus.CONFIRMED);

            seedAppointment(
                    clients.get((i + 7) % clients.size()),
                    employees.get(1),
                    services.get(1),
                    nextTuesday,
                    LocalTime.of(10, 0),
                    AppointmentStatus.CONFIRMED);

            seedAppointment(
                    clients.get((i + 8) % clients.size()),
                    employees.get(2),
                    services.get(2),
                    nextTuesday,
                    LocalTime.of(10, 0),
                    AppointmentStatus.CONFIRMED);

            seedAppointment(
                    clients.get((i + 9) % clients.size()),
                    employees.get(3),
                    services.get(3),
                    nextTuesday,
                    LocalTime.of(14, 0),
                    AppointmentStatus.CANCELLED);

            seedAppointment(
                    clients.get((i + 5) % clients.size()),
                    employees.get(0),
                    services.get(1),
                    nextTuesday,
                    LocalTime.of(15, 0),
                    AppointmentStatus.CONFIRMED);
        }

        // Create recurring breaks and specific unavailable slots.
        seedUnavailableSlots(allEmployees);

        log.info("Development data seeding completed.");
        log.info("Businesses: {}", businessRepository.count());
        log.info("Services: {}", serviceItemRepository.count());
        log.info("Employees: {}", employeeRepository.count());
        log.info("Appointments: {}", appointmentRepository.count());
        log.info("Work schedules: {}", workScheduleRepository.count());
        log.info("Unavailable slots: {}", unavailableSlotRepository.count());
        log.info("Users: {}", userRepository.count());
    }

    private User seedUser(String surname, String firstname, String email) {

        userCounter++;

        User user = new User(surname, firstname, email, String.format("070000%04d", userCounter), "not-a-real-hash");

        return userRepository.save(user);
    }

    private User seedEmployeeUser(int index) {

        return seedUser(
                SURNAMES.get(index % SURNAMES.size()),
                FIRST_NAMES.get(index % FIRST_NAMES.size()),
                "employee" + (index + 1) + "@setora.test");
    }

    private List<User> seedClients(int count) {

        List<User> clients = new ArrayList<>();

        for (int i = 0; i < count; i++) {

            clients.add(seedUser(
                    SURNAMES.get((i + 5) % SURNAMES.size()),
                    FIRST_NAMES.get((i + 5) % FIRST_NAMES.size()),
                    "client" + (i + 1) + "@setora.test"));
        }

        return clients;
    }

    private Business seedBusiness(User owner, int index) {

        Business business = new Business();

        String name = BUSINESS_NAMES.get(index);
        String slug = name.toLowerCase().replace(" ", "-");

        business.setOwner(owner);
        business.setName(name);
        business.setEmail("contact" + (index + 1) + "@setora.test");
        business.setSlug(slug);
        business.setDescription("Professional beauty and wellness services.");
        business.setLocation(
                new Location("Cluj-Napoca", "Str. Memorandumului " + (10 + index) + ", Cluj-Napoca", 46.7694, 23.5899));
        business.setPhone("0264000000");
        business.setTimezone(ZONE);

        return businessRepository.save(business);
    }

    private List<ServiceItem> seedServices(Business business) {

        List<ServiceItem> services = new ArrayList<>();

        services.add(seedService(business, "Standard Service", "A standard appointment service.", "80.00", 45));

        services.add(seedService(business, "Premium Service", "A premium appointment service.", "150.00", 60));

        services.add(
                seedService(business, "Deluxe Service", "An extended premium appointment service.", "250.00", 120));

        services.add(
                seedService(business, "Express Service", "A shorter appointment for quick treatments.", "50.00", 30));

        return services;
    }

    private ServiceItem seedService(
            Business business, String name, String description, String price, int durationMinutes) {

        ServiceItem service = new ServiceItem();

        service.setBusiness(business);
        service.setName(name);
        service.setDescription(description);
        service.setPrice(new BigDecimal(price));
        service.setDurationMinutes(durationMinutes);

        return serviceItemRepository.save(service);
    }

    private Employee seedEmployee(
            Business business, User user, Set<ServiceItem> serviceItems, LocalTime from, LocalTime to) {

        Employee employee = new Employee();

        employee.setBusiness(business);
        employee.setUser(user);
        employee.getServiceItems().addAll(serviceItems);

        employee = employeeRepository.save(employee);

        for (DayOfWeek day : WORK_DAYS) {

            WorkSchedule schedule = new WorkSchedule();

            schedule.setEmployee(employee);
            schedule.setDayOfWeek(day);
            schedule.setInterval(new TimeInterval(from, to));

            workScheduleRepository.save(schedule);
        }

        return employee;
    }

    private void seedAppointment(
            User client,
            Employee employee,
            ServiceItem serviceItem,
            LocalDate date,
            LocalTime at,
            AppointmentStatus status) {

        Instant start = date.atTime(at).atZone(ZONE).toInstant();

        Instant end = start.plus(Duration.ofMinutes(serviceItem.getDurationMinutes()));

        Appointment appointment = new Appointment();

        appointment.setClient(client);
        appointment.setEmployee(employee);
        appointment.setServiceItem(serviceItem);
        appointment.setInterval(new DateTimeInterval(start, end));
        appointment.setStatus(status);
        appointment.setPriceAtAppointment(serviceItem.getPrice());

        appointmentRepository.save(appointment);
    }

    private void seedUnavailableSlots(List<Employee> employees) {

        LocalDate nextWednesday = LocalDate.now(ZONE).with(TemporalAdjusters.next(DayOfWeek.WEDNESDAY));

        for (Employee employee : employees) {

            // Recurring lunch breaks, Monday through Friday.
            for (DayOfWeek day : WORK_DAYS) {

                seedUnavailableSlot(
                        employee,
                        UnavailableType.BREAK,
                        "Lunch break",
                        null,
                        day,
                        LocalTime.of(13, 0),
                        LocalTime.of(14, 0));
            }

            // One specific unavailable slot.
            seedUnavailableSlot(
                    employee,
                    UnavailableType.BLOCKED,
                    "Personal appointment",
                    nextWednesday,
                    null,
                    LocalTime.of(15, 0),
                    LocalTime.of(16, 0));
        }
    }

    private void seedUnavailableSlot(
            Employee employee,
            UnavailableType type,
            String reason,
            LocalDate date,
            DayOfWeek dayOfWeek,
            LocalTime from,
            LocalTime to) {

        UnavailableSlot slot = new UnavailableSlot();

        slot.setEmployee(employee);
        slot.setType(type);
        slot.setReason(reason);
        slot.setSpecificDate(date);
        slot.setDayOfWeek(dayOfWeek);
        slot.setInterval(new TimeInterval(from, to));

        unavailableSlotRepository.save(slot);
    }
}
