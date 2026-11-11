# Setora backend

REST API for Setora, built with Spring Boot 4.1.2, Java 25, Maven and PostgreSQL.

## Requirements

- JDK 25
- PostgreSQL running on `localhost:5432`, with a database named `setoradb`
- Maven is not needed, the project uses the Maven wrapper (`mvnw`)

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

The app starts on `http://localhost:8080`.

- Swagger UI: `http://localhost:8080/swagger-ui.html`
- OpenAPI JSON: `http://localhost:8080/v3/api-docs`


## Database and test data

- `spring.jpa.hibernate.ddl-auto=create`: the tables are dropped and created again from the entity classes on every start. Anything you add by hand is lost on restart.
- `config/DataSeeder` inserts test data on every start. Add new test data there.


TEST DEPENDENCIES need further configuration.

### Data is not persistent between runs


## Structure

The code is grouped by feature, under `com.byteforge.setorabackend`:

| Package     | Content                                                         |
|-------------|-----------------------------------------------------------------|
| `auth`      | `User`                                                          |
| `business`  | `Business`                                                      |
| `offering`  | `ServiceItem`, a service offered by a business (table `services`) |
| `employees` | `Employee`, `WorkSchedule`                                      |
| `booking`   | `Booking`, `BookingStatus`                                      |
| `common`    | `TimeInterval`, `DateTimeInterval` (both have `overlaps`)       |
| `config`    | `DataSeeder`                                                    |

Each entity has its repository next to it. I added it for testing, they are empty, change as needed.

Also, I used `ServiceItem` because service collides with `@Service` and probably it'd get confusing when we have services of ourselves.
Pretty sure we'll get ServiceItemService later on, but oh well... At least it's not Offerings (which apparently is a real marketing term).

## Conventions

- All ids are UUIDs.
- Clients and employees are users: a booking points to the `User` who made it, an employee is linked to a `User`.
- The end of an interval is exclusive: 10:00-11:00 and 11:00-12:00 do not overlap.
- A booking has no business column, the business is the one of the booked service.
- Emails are saved in lower case.

## Dependencies

Some versions in `pom.xml` are set by hand (logback, Jackson, json-smart, objenesis, org.json) because the ones managed by Spring Boot 4.1.2 have known vulnerabilities. 
Do not remove them until Spring Boot is upgraded to a version that manages fixed releases - bcs apparently that's a thing we are dealing with now.
