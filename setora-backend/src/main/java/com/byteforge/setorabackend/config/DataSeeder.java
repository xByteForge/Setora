/*
 * DEVELOPMENT DATABASE SEEDER
 *
 * Generates a sample dataset for testing the Setora application.
 *
 * Dataset:
 * - 5 categories.
 * - 20 service types (4 per category).
 * - 100 businesses (20 per location).
 * - 400 services (4 per business).
 * - 400 employees (4 per business).
 * - 510 users (10 clients, 100 business owners, 400 employees).
 * - 2,000 work schedules (5 weekdays per employee).
 * - 1,100 appointments (11 per business).
 * - 2,400 unavailable slots (5 recurring breaks and 1 specific
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
import com.byteforge.setorabackend.business.model.Business;
import com.byteforge.setorabackend.business.model.Category;
import com.byteforge.setorabackend.business.model.Location;
import com.byteforge.setorabackend.business.repository.BusinessRepository;
import com.byteforge.setorabackend.business.repository.CategoryRepository;
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
import com.byteforge.setorabackend.serviceItem.ServiceType;
import com.byteforge.setorabackend.serviceItem.ServiceTypeRepository;
import java.math.BigDecimal;
import java.time.DayOfWeek;
import java.time.Duration;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.temporal.TemporalAdjusters;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Component
@RequiredArgsConstructor
public class DataSeeder implements CommandLineRunner {

    private static final ZoneId ZONE = ZoneId.of("Europe/Bucharest");

    private static final int BUSINESSES_PER_LOCATION = 20;

    private static final int TOTAL_BUSINESSES = BUSINESSES_PER_LOCATION * 5;

    private static final List<DayOfWeek> WORK_DAYS =
            List.of(DayOfWeek.MONDAY, DayOfWeek.TUESDAY, DayOfWeek.WEDNESDAY, DayOfWeek.THURSDAY, DayOfWeek.FRIDAY);

    private static final Map<String, List<String>> CATEGORY_SERVICE_TYPES = Map.of(
            "Barbershop",
            List.of("Men's Haircut", "Beard Trim", "Hot Towel Shave", "Fade & Styling"),
            "Hair Salon",
            List.of("Women's Haircut", "Hair Coloring", "Balayage", "Blowout & Styling"),
            "Nail Salon",
            List.of("Gel Manicure", "Classic Pedicure", "Nail Extensions", "Nail Art"),
            "Beauty Salon",
            List.of("Facial Treatment", "Eyebrow Shaping", "Eyelash Extensions", "Makeup"),
            "Spa & Wellness",
            List.of("Swedish Massage", "Deep Tissue Massage", "Aromatherapy", "Body Scrub"));

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
            "Bloom Beauty",
            "Luna Beauty",
            "Bella Salon",
            "The Style Loft",
            "Pure Elegance",
            "Golden Touch",
            "Mirror Studio",
            "Serenity Spa",
            "Chic Corner",
            "The Beauty Lab",
            "Prime Look");

    private static final List<String> BUSINESS_CATEGORIES = List.of(
            "Hair Salon",
            "Beauty Salon",
            "Barbershop",
            "Nail Salon",
            "Beauty Salon",
            "Nail Salon",
            "Barbershop",
            "Spa & Wellness",
            "Hair Salon",
            "Spa & Wellness");

    private static final List<String> LOCATIONS = List.of("Cluj-Napoca", "Bucuresti", "Brasov", "Sibiu", "Timisoara");

    private static final List<String> ADDRESSES = List.of(
            "Str. Memorandumului", "Calea Victoriei", "Str. Republicii", "Str. Nicolae Balcescu", "Piata Unirii");

    private static final double[][] COORDINATES = {
        {46.7694, 23.5899},
        {44.4268, 26.1025},
        {45.6579, 25.6012},
        {45.7983, 24.1256},
        {45.7489, 21.2087}
    };

    private static final List<String> BUSINESS_DESCRIPTIONS = List.of(
            "A modern salon offering premium hair styling and professional treatments.",
            "Your destination for beauty treatments, skincare and relaxation.",
            "Professional grooming services with experienced barbers.",
            "Creative nail artistry and premium manicure services.",
            "A welcoming beauty studio focused on quality and customer experience.",
            "Relaxation and wellness treatments in a comfortable environment.",
            "Professional beauty services tailored to your individual needs.",
            "A contemporary studio combining quality products and experienced specialists.",
            "Personalized treatments delivered by a passionate team.",
            "Premium wellness experiences designed to help you unwind.");

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

    private static final BigDecimal[] BASE_PRICES = {
        new BigDecimal("70.00"), new BigDecimal("120.00"), new BigDecimal("200.00"), new BigDecimal("55.00")
    };

    private static final int[][] SERVICE_DURATIONS = {
        {30, 45, 60, 30},
        {45, 60, 90, 60},
        {60, 75, 120, 30},
        {30, 60, 90, 60},
        {45, 90, 120, 45}
    };

    private static final List<String> SERVICE_DESCRIPTIONS = List.of(
            "A professional treatment performed by an experienced specialist.",
            "A premium service using carefully selected professional products.",
            "A personalized experience tailored to your preferences.",
            "An express treatment designed for convenience and quality.");

    private final UserRepository userRepository;
    private final CategoryRepository categoryRepository;
    private final BusinessRepository businessRepository;
    private final ServiceTypeRepository serviceTypeRepository;
    private final ServiceItemRepository serviceItemRepository;
    private final EmployeeRepository employeeRepository;
    private final WorkScheduleRepository workScheduleRepository;
    private final AppointmentRepository appointmentRepository;
    private final UnavailableSlotRepository unavailableSlotRepository;
    private final JdbcTemplate jdbcTemplate;

    private int userCounter = 0;

    @Override
    @Transactional
    public void run(String... args) {

        // Enable PostgreSQL accent-insensitive search.
        jdbcTemplate.execute("CREATE EXTENSION IF NOT EXISTS unaccent SCHEMA public");

        // Do not duplicate seed data on subsequent application starts.
        if (businessRepository.count() > 0) {
            log.info("Businesses already exist. Skipping data seeding.");
            return;
        }

        log.info("Starting development data seeding...");

        Map<String, Category> categoriesByName = seedCategories();

        Map<String, List<ServiceType>> serviceTypesByCategory = seedServiceTypes(categoriesByName);

        List<User> clients = seedClients(10);

        List<Employee> allEmployees = new ArrayList<>();

        LocalDate nextMonday = LocalDate.now(ZONE).with(TemporalAdjusters.next(DayOfWeek.MONDAY));

        LocalDate nextTuesday = nextMonday.plusDays(1);

        for (int i = 0; i < TOTAL_BUSINESSES; i++) {

            // Create business owner.
            User owner = seedUser("Owner", "Business" + (i + 1), "owner" + (i + 1) + "@setora.test");

            // Assign businesses evenly across locations.
            String categoryName = BUSINESS_CATEGORIES.get(i % BUSINESS_CATEGORIES.size());

            Business business = seedBusiness(owner, categoriesByName.get(categoryName), i);

            // Create four services.
            List<ServiceItem> services = seedServices(business, serviceTypesByCategory.get(categoryName), i);

            // Create four employees with different service assignments.
            List<Employee> employees = List.of(
                    seedEmployee(
                            business,
                            seedEmployeeUser(i * 4),
                            Set.of(services.get(0), services.get(1)),
                            LocalTime.of(9, 0),
                            LocalTime.of(17, 0),
                            0),
                    seedEmployee(
                            business,
                            seedEmployeeUser(i * 4 + 1),
                            Set.of(services.get(1), services.get(2)),
                            LocalTime.of(10, 0),
                            LocalTime.of(18, 0),
                            1),
                    seedEmployee(
                            business,
                            seedEmployeeUser(i * 4 + 2),
                            Set.of(services.get(0), services.get(2), services.get(3)),
                            LocalTime.of(8, 0),
                            LocalTime.of(16, 0),
                            2),
                    seedEmployee(
                            business,
                            seedEmployeeUser(i * 4 + 3),
                            Set.of(services.get(1), services.get(3)),
                            LocalTime.of(12, 0),
                            LocalTime.of(20, 0),
                            3));

            allEmployees.addAll(employees);

            // ==========================================
            // MONDAY - 6 APPOINTMENTS
            // ==========================================

            seedAppointment(
                    clients.get(i % clients.size()),
                    employees.get(0),
                    services.get(0),
                    nextMonday,
                    appointmentTime(LocalTime.of(9, 0), i),
                    AppointmentStatus.CONFIRMED);

            seedAppointment(
                    clients.get((i + 1) % clients.size()),
                    employees.get(1),
                    services.get(1),
                    nextMonday,
                    appointmentTime(LocalTime.of(12, 0), i),
                    AppointmentStatus.CONFIRMED);

            seedAppointment(
                    clients.get((i + 2) % clients.size()),
                    employees.get(2),
                    services.get(2),
                    nextMonday,
                    appointmentTime(LocalTime.of(10, 0), i),
                    AppointmentStatus.CANCELLED);

            seedAppointment(
                    clients.get((i + 3) % clients.size()),
                    employees.get(3),
                    services.get(3),
                    nextMonday,
                    appointmentTime(LocalTime.of(15, 0), i),
                    AppointmentStatus.CONFIRMED);

            seedAppointment(
                    clients.get((i + 4) % clients.size()),
                    employees.get(0),
                    services.get(1),
                    nextMonday,
                    appointmentTime(LocalTime.of(14, 0), i),
                    AppointmentStatus.CONFIRMED);

            seedAppointment(
                    clients.get((i + 5) % clients.size()),
                    employees.get(2),
                    services.get(0),
                    nextMonday,
                    appointmentTime(LocalTime.of(15, 0), i),
                    AppointmentStatus.CANCELLED);

            // ==========================================
            // TUESDAY - 5 APPOINTMENTS
            // ==========================================

            seedAppointment(
                    clients.get((i + 6) % clients.size()),
                    employees.get(0),
                    services.get(0),
                    nextTuesday,
                    appointmentTime(LocalTime.of(9, 0), i),
                    AppointmentStatus.CONFIRMED);

            seedAppointment(
                    clients.get((i + 7) % clients.size()),
                    employees.get(1),
                    services.get(1),
                    nextTuesday,
                    appointmentTime(LocalTime.of(10, 0), i),
                    AppointmentStatus.CONFIRMED);

            seedAppointment(
                    clients.get((i + 8) % clients.size()),
                    employees.get(2),
                    services.get(2),
                    nextTuesday,
                    appointmentTime(LocalTime.of(10, 0), i),
                    AppointmentStatus.CONFIRMED);

            seedAppointment(
                    clients.get((i + 9) % clients.size()),
                    employees.get(3),
                    services.get(3),
                    nextTuesday,
                    appointmentTime(LocalTime.of(14, 0), i),
                    AppointmentStatus.CANCELLED);

            seedAppointment(
                    clients.get((i + 5) % clients.size()),
                    employees.get(0),
                    services.get(1),
                    nextTuesday,
                    appointmentTime(LocalTime.of(15, 0), i),
                    AppointmentStatus.CONFIRMED);
        }

        // Create recurring breaks and specific unavailable slots.
        seedUnavailableSlots(allEmployees);

        log.info("Development data seeding completed.");
        log.info("Categories: {}", categoryRepository.count());
        log.info("Service types: {}", serviceTypeRepository.count());
        log.info("Businesses: {}", businessRepository.count());
        log.info("Services: {}", serviceItemRepository.count());
        log.info("Employees: {}", employeeRepository.count());
        log.info("Appointments: {}", appointmentRepository.count());
        log.info("Work schedules: {}", workScheduleRepository.count());
        log.info("Unavailable slots: {}", unavailableSlotRepository.count());
        log.info("Users: {}", userRepository.count());
    }

    private Map<String, Category> seedCategories() {

        Map<String, Category> categories = new LinkedHashMap<>();

        for (String name : CATEGORY_SERVICE_TYPES.keySet()) {
            Category category = new Category();
            category.setName(name);
            categories.put(name, categoryRepository.save(category));
        }

        return categories;
    }

    private Map<String, List<ServiceType>> seedServiceTypes(Map<String, Category> categoriesByName) {

        Map<String, List<ServiceType>> serviceTypesByCategory = new LinkedHashMap<>();

        for (Map.Entry<String, List<String>> entry : CATEGORY_SERVICE_TYPES.entrySet()) {

            Category category = categoriesByName.get(entry.getKey());

            List<ServiceType> types = new ArrayList<>();

            for (String typeName : entry.getValue()) {
                ServiceType serviceType = new ServiceType();
                serviceType.setCategory(category);
                serviceType.setName(typeName);
                types.add(serviceTypeRepository.save(serviceType));
            }

            serviceTypesByCategory.put(entry.getKey(), types);
        }

        return serviceTypesByCategory;
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

    private Business seedBusiness(User owner, Category category, int index) {

        Business business = new Business();

        int locationIndex = index / BUSINESSES_PER_LOCATION;
        int businessNumber = index + 1;

        String baseName = BUSINESS_NAMES.get(index % BUSINESS_NAMES.size());
        String locationName = LOCATIONS.get(locationIndex);
        String name = BUSINESS_NAMES.get(index % BUSINESSES_PER_LOCATION) + " " + locationName;

        String slug = name.toLowerCase().replace(" ", "-");

        // Give businesses different street numbers within each city.
        int streetNumber = 10 + (index % BUSINESSES_PER_LOCATION) * 3;

        business.setOwner(owner);
        business.setCategory(category);
        business.setName(name);
        business.setEmail("contact" + businessNumber + "@setora.test");
        business.setSlug(slug);
        business.setDescription(BUSINESS_DESCRIPTIONS.get(index % BUSINESS_DESCRIPTIONS.size()));

        business.setLocation(new Location(
                LOCATIONS.get(locationIndex),
                ADDRESSES.get(locationIndex) + " " + streetNumber,
                COORDINATES[locationIndex][0],
                COORDINATES[locationIndex][1]));

        business.setPhone(String.format("0264%06d", 100000 + index));
        business.setTimezone(ZONE);

        return businessRepository.save(business);
    }

    private List<ServiceItem> seedServices(Business business, List<ServiceType> serviceTypes, int businessIndex) {

        int locationIndex = businessIndex / BUSINESSES_PER_LOCATION;
        int businessNumberInLocation = businessIndex % BUSINESSES_PER_LOCATION;

        // Different prices for every business within the same location.
        // Each city also has a different base price level.
        BigDecimal priceOffset = BigDecimal.valueOf(locationIndex * 25L + businessNumberInLocation * 8L);

        int[] durations = SERVICE_DURATIONS[businessIndex % SERVICE_DURATIONS.length];

        List<ServiceItem> services = new ArrayList<>();

        for (int i = 0; i < serviceTypes.size(); i++) {

            ServiceItem service = seedService(
                    business,
                    serviceTypes.get(i),
                    serviceTypes.get(i).getName(),
                    SERVICE_DESCRIPTIONS.get((i + businessIndex) % SERVICE_DESCRIPTIONS.size()),
                    BASE_PRICES[i].add(priceOffset),
                    durations[i]);

            services.add(service);
        }

        return services;
    }

    private ServiceItem seedService(
            Business business,
            ServiceType serviceType,
            String name,
            String description,
            BigDecimal price,
            int durationMinutes) {

        ServiceItem service = new ServiceItem();

        service.setBusiness(business);
        service.setServiceType(serviceType);
        service.setName(name);
        service.setDescription(description);
        service.setPrice(price);
        service.setDurationMinutes(durationMinutes);

        return serviceItemRepository.save(service);
    }

    private Employee seedEmployee(
            Business business,
            User user,
            Set<ServiceItem> serviceItems,
            LocalTime from,
            LocalTime to,
            int employeeIndex) {

        Employee employee = new Employee();

        employee.setBusiness(business);
        employee.setUser(user);
        employee.getServiceItems().addAll(serviceItems);

        employee = employeeRepository.save(employee);

        for (DayOfWeek day : WORK_DAYS) {

            WorkSchedule schedule = new WorkSchedule();

            int offsetMinutes = (day.getValue() + employeeIndex) % 2 == 0 ? 30 : 0;

            schedule.setEmployee(employee);
            schedule.setDayOfWeek(day);
            schedule.setInterval(new TimeInterval(from.plusMinutes(offsetMinutes), to.plusMinutes(offsetMinutes)));

            workScheduleRepository.save(schedule);
        }

        return employee;
    }

    private LocalTime appointmentTime(LocalTime baseTime, int businessIndex) {

        return baseTime.plusMinutes((businessIndex % 4) * 15L);
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
