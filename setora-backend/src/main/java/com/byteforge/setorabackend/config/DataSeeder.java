package com.byteforge.setorabackend.config;

import com.byteforge.setorabackend.auth.User;
import com.byteforge.setorabackend.auth.UserRepository;
import com.byteforge.setorabackend.booking.Booking;
import com.byteforge.setorabackend.booking.BookingRepository;
import com.byteforge.setorabackend.booking.BookingStatus;
import com.byteforge.setorabackend.business.Business;
import com.byteforge.setorabackend.business.BusinessRepository;
import com.byteforge.setorabackend.common.DateTimeInterval;
import com.byteforge.setorabackend.common.TimeInterval;
import com.byteforge.setorabackend.employees.Employee;
import com.byteforge.setorabackend.employees.EmployeeRepository;
import com.byteforge.setorabackend.employees.WorkSchedule;
import com.byteforge.setorabackend.employees.WorkScheduleRepository;
import com.byteforge.setorabackend.offering.ServiceItem;
import com.byteforge.setorabackend.offering.ServiceItemRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.DayOfWeek;
import java.time.Duration;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.temporal.TemporalAdjusters;
import java.util.List;
import java.util.Set;

@Slf4j
@Component
@RequiredArgsConstructor
public class DataSeeder implements CommandLineRunner {
    //add testing data directly on each build and run

    private static final ZoneId ZONE = ZoneId.of("Europe/Bucharest");
    private static final List<DayOfWeek> WORK_DAYS = List.of(
            DayOfWeek.MONDAY, DayOfWeek.TUESDAY, DayOfWeek.WEDNESDAY, DayOfWeek.THURSDAY, DayOfWeek.FRIDAY);

    private final UserRepository userRepository;
    private final BusinessRepository businessRepository;
    private final ServiceItemRepository serviceItemRepository;
    private final EmployeeRepository employeeRepository;
    private final WorkScheduleRepository workScheduleRepository;
    private final BookingRepository bookingRepository;

    @Override
    @Transactional
    public void run(String... args) {
        if (userRepository.count() > 0) {
            return;
        }

        User owner = seedUser("Pavel", "Emma", "emma.pavel@setora.test", "0700000001");
        User client = seedUser("Pop", "Ana", "ana.pop@setora.test", "0700000002");

        Business business = seedBusiness(owner);

        ServiceItem haircut = seedService(business, "Tuns", "Tuns si aranjat", "80.00", 45);
        ServiceItem coloring = seedService(business, "Vopsit", "Vopsit par, o singura culoare", "250.00", 120);
        ServiceItem manicure = seedService(business, "Manichiura", "Manichiura clasica", "100.00", 60);

        Employee maria = seedEmployee(business,
                seedUser("Ionescu", "Maria", "maria.ionescu@setora.test", "0700000003"),
                Set.of(haircut, coloring), LocalTime.of(9, 0), LocalTime.of(17, 0));
        Employee andrei = seedEmployee(business,
                seedUser("Dumitru", "Andrei", "andrei.dumitru@setora.test", "0700000004"),
                Set.of(haircut, manicure), LocalTime.of(12, 0), LocalTime.of(20, 0));

        seedBooking(client, maria, haircut, LocalTime.of(10, 0));
        seedBooking(client, andrei, manicure, LocalTime.of(14, 0));

        log.info("Seeded business {} with id {}", business.getName(), business.getId());
    }

    private User seedUser(String surname, String firstname, String email, String phoneNumber) {
        User user = new User();
        user.setSurname(surname);
        user.setFirstname(firstname);
        user.setEmail(email);
        user.setPasswordHash("not-a-real-hash");
        user.setPhoneNumber(phoneNumber);
        return userRepository.save(user);
    }

    private Business seedBusiness(User owner) {
        Business business = new Business();
        business.setOwner(owner);
        business.setName("Studio Coafura");
        business.setSlug("studio-coafura");
        business.setDescription("Salon de infrumusetare");
        business.setAddress("Str. Memorandumului 10, Cluj-Napoca");
        business.setLatitude(46.7694);
        business.setLongitude(23.5899);
        business.setPhone("0264000000");
        business.setTimezone(ZONE);
        return businessRepository.save(business);
    }

    private ServiceItem seedService(Business business, String name, String description, String price, int durationMinutes) {
        ServiceItem serviceItem = new ServiceItem();
        serviceItem.setBusiness(business);
        serviceItem.setName(name);
        serviceItem.setDescription(description);
        serviceItem.setPrice(new BigDecimal(price));
        serviceItem.setDurationMinutes(durationMinutes);
        return serviceItemRepository.save(serviceItem);
    }

    //the employee works monday to friday with the same hours --for now
    private Employee seedEmployee(Business business, User user, Set<ServiceItem> serviceItems, LocalTime from, LocalTime to) {
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

    //booked for next monday, so it is always in the future
    private void seedBooking(User client, Employee employee, ServiceItem serviceItem, LocalTime at) {
        Instant start = LocalDate.now(ZONE)
                .with(TemporalAdjusters.next(DayOfWeek.MONDAY))
                .atTime(at)
                .atZone(ZONE)
                .toInstant();
        Instant end = start.plus(Duration.ofMinutes(serviceItem.getDurationMinutes()));

        Booking booking = new Booking();
        booking.setClient(client);
        booking.setEmployee(employee);
        booking.setServiceItem(serviceItem);
        booking.setInterval(new DateTimeInterval(start, end));
        booking.setStatus(BookingStatus.CONFIRMED);
        booking.setPriceAtBooking(serviceItem.getPrice());
        bookingRepository.save(booking);
    }
}
