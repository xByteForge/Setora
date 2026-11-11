# Setora backend

REST API for Setora, built with Spring Boot 4.1.2, Java 25, Gradle and PostgreSQL.

## Api endpoints
When we have the rest controllers, supposedly, these should generate documentation automatically to be used by frontend... I think. 
- Swagger UI: `http://localhost:8080/swagger-ui.html`
- OpenAPI JSON: `http://localhost:8080/v3/api-docs`

## Requirements

- JDK 25
- PostgreSQL running on `localhost:5432`, with a database named `setoradb`
- Gradle is not needed, the project uses the Gradle wrapper (`gradlew`)

Create the database once:

```sql
CREATE DATABASE setoradb;
```

## Configuration

The database login is read from a `.env` file in this folder. The file is not in git, each developer has their own. Create it from the template:

```powershell
copy .env.example .env
```

| Variable      | Default    |
|---------------|------------|
| `DB_USER`     | `postgres` |
| `DB_PASSWORD` | `postgres` |

Change the values in `.env` if your local PostgreSQL uses another login. Without a `.env` file the defaults above are used. Real environment variables with the same names win over the file.

Start the app from this folder (`setora-backend`), otherwise `.env` is not found. Do not write real passwords in `application.properties` or in `.env.example`.

## Run

From this folder, in PowerShell (on Linux/macOS use `./gradlew`):

```powershell
.\gradlew.bat bootRun   # start the app
.\gradlew.bat test      # run the tests
.\gradlew.bat build     # tests + jar in build/libs
```

The app starts on `http://localhost:8080`.

- Swagger UI: `http://localhost:8080/swagger-ui.html`
- OpenAPI JSON: `http://localhost:8080/v3/api-docs`


## Database and test data

- `spring.jpa.hibernate.ddl-auto=create`: the tables are dropped and created again from the entity classes on every start. Anything you add by hand is lost on restart.
- `config/DataSeeder` inserts test data on every start. Add new test data there.


## Tests

Tests use JUnit (Jupiter). Besides JUnit, only `spring-boot-test` and `spring-test` are on the test classpath, so `@SpringBootTest` works.
Mockito, AssertJ and the other libraries from `spring-boot-starter-test` are not included, add them to `build.gradle` when needed.

### Data is not persistent between runs


## Structure

The code is grouped by feature, under `com.byteforge.setorabackend`:

| Package     | Content                                                         |
|-------------|-----------------------------------------------------------------|
| `auth`      | `User`                                                          |
| `business`  | `Business`                                                      |
| `offering`  | `ServiceItem`, a service offered by a business (table `services`) |
| `employees` | `Employee`, `WorkSchedule`                                      |
| `appointment`   | `Booking`, `BookingStatus`                                      |
| `common`    | `TimeInterval`, `DateTimeInterval` (both have `overlaps`)       |
| `config`    | `DataSeeder`                                                    |

Each entity has its repository next to it. I added it for testing, they are empty, change as needed.

Also, I used `ServiceItem` because service collides with `@Service` and probably it'd get confusing when we have services of ourselves.
Pretty sure we'll get ServiceItemService later on, but oh well... At least it's not Offerings (which apparently is a real marketing term).

## Conventions

- All ids are UUIDs.
- Clients and employees are users: an appointment points to the `User` who made it, an employee is linked to a `User`.
- The end of an interval is exclusive: 10:00-11:00 and 11:00-12:00 do not overlap.
- An appointment/booking has no business column, the business is the one of the booked service.
- Emails are saved in lower case.

## Dependencies

Some versions in `build.gradle` are set by hand (logback, Jackson) because the ones managed by Spring Boot 4.1.2 have known vulnerabilities. 
Do not remove them until Spring Boot is upgraded to a version that manages fixed releases - bcs apparently that's a thing we are dealing with now.
