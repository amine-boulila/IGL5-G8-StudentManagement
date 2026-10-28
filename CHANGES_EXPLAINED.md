# SonarQube changes explained

## 1. DTOs and mappers in Student and Enrollment controllers

**a) The problem:** Sonar objected to controllers accepting and returning database entities directly. A DTO (data transfer object) is a small class that defines which fields are allowed in an API request or response. Binding a request to an entity can allow a client to set fields the API did not intend to expose.

**b) Why it matters:** Before the change, a client could try sending an ID and other protected fields along with a normal student update:

```json
{"idStudent":1,"firstName":"Mallory","lastName":"X","email":"x@example.com","enrollments":[]}
```

Because the whole entity was accepted, the server could attempt to save values the client should not control, such as the student ID. A mapper is a class that copies allowed fields between the request/response DTO and the database entity.

**c) What was changed:** Created `StudentRequestDTO`, `StudentResponseDTO`, `EnrollmentRequestDTO`, and `EnrollmentResponseDTO` in `src/main/java/tn/esprit/studentmanagement/dto/`; created `StudentMapper` and `EnrollmentMapper` in `src/main/java/tn/esprit/studentmanagement/mappers/`; modified `StudentController.java` and `EnrollmentController.java`; added `spring-boot-starter-validation` in `pom.xml`.

```java
// BEFORE
public Student createStudent(@RequestBody Student student) {
    return studentService.saveStudent(student);
}

// AFTER
public StudentResponseDTO createStudent(@Valid @RequestBody StudentRequestDTO dto) {
    Student student = new Student();
    studentMapper.applyToEntity(dto, student);
    return studentMapper.toResponse(studentService.saveStudent(student));
}
```

**d) How it works now:** Angular sends JSON; the controller validates and receives a request DTO; the mapper resolves related IDs (such as `departmentId`) and copies allowed values to a new or existing entity; the service saves it; the mapper makes a response DTO, which Spring returns as JSON.

**e) What changed for the outside world:** Student JSON now uses `departmentId` and omits `idStudent` and `enrollments`. Enrollment JSON uses `studentId` and `courseId`, not nested objects, and omits `idEnrollment` from requests. Both updates now require an ID in the URL: `PUT /students/updateStudent/{id}` and `PUT /Enrollment/updateEnrollment/{id}`. Angular must send those IDs in the URL and use the new flat JSON fields. GET and create/delete paths stay the same.

## 2. Constructor injection and safe Optional handling in services

**a) The problem:** Sonar found repositories injected directly into service fields and calls to `Optional.get()` without checking whether a row existed. An Optional is a wrapper that can contain a value or be empty.

**b) Why it matters:** Calling `getDepartmentById(404)` when there is no department used to throw a generic `NoSuchElementException`, often appearing to the client as a server error. Constructor injection makes required dependencies visible when a service is created; tests can pass mock repositories through the constructor instead of filling fields after creation.

**c) What was changed:** Modified `DepartmentService.java`, `EnrollmentService.java`, and `StudentService.java` to use `private final` repository fields and Lombok `@AllArgsConstructor`. Department and Enrollment lookups now throw `ResponseStatusException` with HTTP 404 when missing. `StudentService.getStudentById` keeps its existing `orElse(null)` behavior. Updated `DepartmentServiceImplTest.java`, `DepartmentServiceMockitoTest.java`, and `EnrollmentServiceMockitoTest.java` to expect the 404 response.

```java
// BEFORE
@Autowired
private DepartmentRepository departmentRepository;
Department department = departmentRepository.findById(id).get();

// AFTER
private final DepartmentRepository departmentRepository;
Department department = departmentRepository.findById(id)
    .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,
        "Department not found with id " + id));
```

**d) How it works now:** Spring creates each service and supplies its repository through the generated constructor. For a department or enrollment lookup, the service returns the found entity; if the repository returns an empty Optional, the service raises a 404 instead. Student lookup still returns `null` when missing.

**e) What changed for the outside world:** Service method signatures and successful requests are unchanged. A missing department or enrollment ID now has a clear 404 result; missing student IDs retain the old null behavior.

## 3. Tests and JaCoCo coverage setup

**a) The problem:** Sonar's coverage quality gate checks what share of executable source lines are run by tests. The measured overall coverage was low, and the controllers and new mappers had little or no test coverage.

**b) Why it matters:** A coverage percentage is a measure of executed lines, not a guarantee that a program has no bugs. If a test never runs the update path, a mistake there can go unnoticed even when create works.

**c) What was changed:** `pom.xml` now points Sonar at `target/site/jacoco/jacoco.xml` and excludes entities, DTOs, application bootstrap, and config paths. Added `StudentControllerTest.java`, `DepartmentControllerTest.java`, and `EnrollmentControllerTest.java`; added `StudentMapperTest.java`, `EnrollmentMapperTest.java`, and `DepartmentMapperTest.java`; expanded `StudentServiceMockitoTest.java`, `DepartmentServiceMockitoTest.java`, and `EnrollmentServiceMockitoTest.java`. JaCoCo 0.8.12 was already configured to collect coverage and make the report during `verify`.

```xml
<sonar.coverage.jacoco.xmlReportPaths>target/site/jacoco/jacoco.xml</sonar.coverage.jacoco.xmlReportPaths>
<sonar.coverage.exclusions>**/entities/**,**/dto/**,**/*Application.java,**/config/**</sonar.coverage.exclusions>
```

**d) How it works now:** Controller tests send web requests through Spring's test web layer and check the response status, including a Student request with missing required fields. Mapper tests check copied values, ID resolution, missing related IDs, and null relations. Service tests check repository calls and error handling. Running `mvn clean verify` runs the tests and writes JaCoCo's XML report for Sonar.

**e) What changed for the outside world:** No URLs or JSON formats changed in this coverage work. Sonar now reads the XML report. Note: the `**/dto/**` exclusion also skips `DepartmentMapper` because that mapper currently lives in the `dto` package, even though `DepartmentMapperTest` exists; Student and Enrollment mappers remain included.

## Short project-defense summary

“We stopped accepting database entities directly in the Student and Enrollment API by using request and response DTOs, so clients can only send the fields we chose. The mappers translate those simple fields and related IDs to and from database entities. In services, dependencies now come through constructors and missing department or enrollment IDs produce a clear 404. Finally, we added tests for endpoints, mappers, and repository error cases, and JaCoCo writes the coverage report Sonar uses.”

## Questions a teacher may ask

1. **What is the difference between an entity and a DTO?**  An entity represents data stored in the database. A DTO is the selected set of fields that the API accepts or returns.

2. **Why are IDs placed in the URL for updates?**  The URL identifies which record is being changed, so the request body cannot choose a different record by supplying its own ID. For example, Angular now calls `PUT /students/updateStudent/12` to update student 12.

3. **What does the mapper do?**  It copies allowed values between a DTO and an entity. It also looks up related records using IDs such as `departmentId` and reports a bad request if that ID does not exist.

4. **What happens when a department ID is not found?**  The service returns an HTTP 404 response with a message that includes the missing ID. Student lookup still returns `null` for a missing ID, preserving its previous behavior.

5. **Does 100% coverage prove the code is bug-free?**  No; it only means the measured lines ran during tests. The tests still need meaningful assertions to check that those lines produced the right behavior.
