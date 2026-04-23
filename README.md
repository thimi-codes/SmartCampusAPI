# Smart Campus Sensor & Room Management API

A JAX-RS (Jersey) RESTful API built for the University’s "Smart Campus" approach. It manages campus Rooms and IoT Sensors with CRUD operations, sub-resource reading history, custom exception handling, and request/response logging, all backed by in-memory data structures.

## API Overview

The API follows REST architectural principles with a versioned base path of ‘/api/v1’. Three core resources model the Smart Campus domain:

•	Room: physical spaces with capacity and a list of deployed sensor IDs.

•	Sensor: IoT devices (Temperature, CO2, Occupancy, etc.) linked to a room.

•	SensorReading: timestamped measurement events recorded by a sensor, exposed as a sub-resource of Sensor.

**Base URL:** `http://localhost:8080/smart-campus-api/api/v1` 

**Context path:** `/smart-campus-api` (as defined in `context.xml`)

## Technology Stack

•	Language: Java 11

•	REST Framework: JAX-RS 2.1 (Jersey 2.41)

•	JSON Serialisation: Jackson (via jersey-media-json-jackson)

•	Dependency Injection: HK2 (via jersey-hk2)

•	Build Tool: Maven 3.x

•	Server: Apache Tomcat 9.x (WAR deployment)

•	Data Storage: ConcurrentHashMap / ArrayList (in-memory)

## Project Structure
```
SmartCampusAPI/
├── pom.xml
└── src/main/java/com/smartcampusapi/
    ├── SmartCampusApplication.java          # @ApplicationPath("/api/v1")
    ├── store/
    │   └── DataStore.java                   # Singleton in-memory store
    ├── model/
    │   ├── Room.java
    │   ├── Sensor.java
    │   └── SensorReading.java
    ├── resource/
    │   ├── DiscoveryResource.java           # GET /api/v1
    │   ├── RoomResource.java                # /api/v1/rooms
    │   ├── SensorResource.java              # /api/v1/sensors
    │   └── SensorReadingResource.java       # /api/v1/sensors/{id}/readings
    ├── exception/
    │   ├── RoomNotEmptyException.java
    │   ├── LinkedResourceNotFoundException.java
    │   └── SensorUnavailableException.java
    ├── mapper/
    │   ├── ErrorResponse.java
    │   ├── RoomNotEmptyExceptionMapper.java      # 409
    │   ├── LinkedResourceNotFoundExceptionMapper.java  # 422
    │   ├── SensorUnavailableExceptionMapper.java # 403
    │   └── GlobalExceptionMapper.java            # 500 catch-all
    └── filter/
        └── LoggingFilter.java               # Request & Response logging
```

## Build & Run Instructions	

### Prerequisites
•	Java 11 or later (java -version) 

•	Maven 3.6+ (mvn -version) 

•	Apache Tomcat 9.x (download)


**Step 1:** Clone the repository
```
git clone https://github.com/thimi-codes/SmartCampusAPI.git
```

After cloning:  
```
cd SmartCampusAPI
```

**Step 2:** Build the WAR
```
mvn clean package
```
, or after opening the project in NetBeans, right-click the project and click “Clean and Build”. (This produces target/smart-campus-api.war.)

**Step 3:** Deploy to Tomcat

Copy the WAR into Tomcat’s ‘webapps’ directory:
```
cp target/smart-campus-api.war /path/to/tomcat/webapps/
```
**Step 4:** Start Tomcat
```
/path/to/tomcat/bin/startup.sh        #macOS / Linux

/path/to/tomcat/bin/startup.bat       #Windows    or 
```
on Netbeans; Windows -> Services -> Servers -> right-click the Apache Tomcat and select start.

**Step 5:** Verify

Open your browser or use curl:
```
http://localhost:8077/smart-campus-api/api/v1
```
You should see the Discovery JSON response.

Stopping the server; 
```
/path/to/tomcat/bin/shutdown.sh
```
or on Netbeans; Windows -> Services -> Servers -> right-click the Apache Tomcat and select stop.


## API Endpoints Reference
Method  |	Path                                |	Description                             |	Success Code
--------|-------------------------------------|-----------------------------------------|--------------
GET     |	/api/v1	                            | Discovery	                              | 200
GET	    | /api/v1/rooms                       |	List all rooms	                        | 200
POST	  | /api/v1/rooms	                      | Create a room	                          | 201
GET	    | /api/v1/rooms/{roomId}	            | Get room by ID	                        | 200
DELETE	| /api/v1/rooms/{roomId}	            | Delete room (blocked if sensors exist)	| 204
GET	    | /api/v1/sensors	                    | List all sensors (optional ?type=)	    | 200
GET	    | /api/v1/sensors/{sensorId}	        | Get sensor by ID	                      | 200
POST	  | /api/v1/sensors	                    | Register a new sensor	                  | 201
PUT	    | /api/v1/sensors/{sensorId}/status	  | Update sensor status	                  | 200
DELETE	| /api/v1/sensors/{sensorId}	        | Delete a sensor	                        | 204
GET	    | /api/v1/sensors/{sensorId}/readings	| Get reading history	                    | 200
POST	  | /api/v1/sensors/{sensorId}/readings	| Add a new reading	                      | 201

##  Sample curl Commands

Replace localhost:8077/smart-campus-api with your deployment URL if different. All POST/PUT requests require -H "Content-Type: application/json".

**1.	Discovering API**
```
curl -X GET http://localhost:8080/smart-campus-api/api/v1
```
Expected: 200 OK with JSON metadata including version, contact, and resource links.

**2.	Create a Room**
```
curl -X POST http://localhost:8080/smart-campus-api/api/v1/rooms \
  -H "Content-Type: application/json" \
  -d '{"id":"LAB-101","name":"Computer Lab","capacity":30}'
```
Expected: 201 Created with the created room object and a Location header.

**3.	Register a Sensor (link to an existing room)**
```
curl -X POST http://localhost:8080/smart-campus-api/api/v1/sensors \
  -H "Content-Type: application/json" \
  -d '{"id":"TEMP-001","type":"Temperature","roomId":"LAB-101"}'
```
Expected: 201 Created with the sensor object. The sensor is automatically set to ‘ACTIVE’ status.

**4.	Filter Sensors by Type**
```
curl -X GET http://localhost:8080/smart-campus-api/api/v1/sensors?type=Temperature
```
Expected: 200 OK with a JSON array containing only Temperature sensors.

**5.	Post a Sensor Reading**
```
curl -X POST http://localhost:8080/smart-campus-api/api/v1/sensors/TEMP-001/readings \
  -H "Content-Type: application/json" \
  -d '{"value":22.5}'
```
Expected: 201 Created with the new reading (auto-generated UUID and timestamp). The parent sensor's currentValue is updated to 22.5.

**6.	Attempt to delete a room that still has sensors (409 Conflict)**
```
curl -X DELETE http://localhost:8080/smart-campus-api/api/v1/rooms/LAB-101
```
Expected: 409 Conflict with JSON error body explaining the room still has active sensors.

**7.	Register a Sensor with a non-existent roomId (422)**
```
curl -X POST http://localhost:8080/smart-campus-api/api/v1/sensors \
  -H "Content-Type: application/json" \
  -d '{"id":"CO2-999","type":"CO2","roomId":"GHOST-999"}'
```
Expected: 422 Unprocessable Entity with JSON error body.

**8.	Post a reading to a MAINTENANCE sensor (403)**
```
#First set sensor to MAINTENANCE
curl -X PUT http://localhost:8080/smart-campus-api/api/v1/sensors/TEMP-001/status \
  -H "Content-Type: application/json" \
  -d '{"status":"MAINTENANCE"}'

#Then try to post a reading
curl -X POST http://localhost:8080/smart-campus-api/api/v1/sensors/TEMP-001/readings \
  -H "Content-Type: application/json" \
  -d '{"value":19.0}'
```
Expected: 403 Forbidden with JSON error body.

-------------------------------------------------------------------------------------------------------------------------------------------------------------------
# Report
## Part 1 : Service Architecture & Setup
### Question 1.1 : JAX-RS Resource Class Lifecycle: Request-scoped vs Singleton

By default, JAX-RS instantiates a new instance of each resource class for each incoming HTTP request (request-scoped lifecycle). It is the JAX-RS specification default, deliberate, and avoids state leakage between requests, as each thread has its own object with newly initialised instance variables.


This architectural implication on in-memory data management is very important: when every request is given a new resource instance, you cannot store shared data as plain instance fields (e.g:, private Map<String, Room> rooms = new HashMap<>()), since this map would be created and destroyed on a per-request basis, so that no data would ever be shared across calls.

To overcome this, the project implements a Singleton DataStore class DataStore).getInstance()). The data models ConcurrentHashMap<String, Room> , ConcurrentHashMap<String, Sensor>, and ConcurrentHashMap<String, List<SensorReading> > are in this singleton, and so all the request-scoped instances of the resource share these data models. ConcurrentHashMap is selected instead of plain HashMap to avoid race conditions: JAX-RS containers are multi-threaded (each request can be executed on a different thread at the same time), and therefore, any concurrent read/write operation on a non-thread-safe HashMap would result in the corruption of data and random errors of ConcurrentModificationException. ConcurrentHashMap divides its buckets in-house to enable safe concurrent access without blocking on the whole structure on each operation, delivering both thread safety and performance.


### Question 1.2: HATEOAS and Hypermedia-Driven API Design


The principle of embedding navigational links to related resources and available actions with the data, called HATEOAS (Hypermedia as the Engine of Application State), is that API responses must include these links. It is regarded as the utmost degree of REST maturity (Level 3 in the Richardson Maturity Model).


This is of great benefit to client developers: instead of relying on external documentation (which may become outdated), a HATEOAS-compliant API is self-documenting at runtime. A client that invokes GET /api/v1 receives not only metadata but also the specific URIs of rooms and sensors; that is, the client does not have to hardcode paths. Clients that follow embedded links will not need any code changes as the API evolves, e.g, when /api/v1/rooms becomes /api/v2/rooms. It helps reduce client-server connectivity, speeds onboarding for new developers, and enables interactive exploration of the API (e.g., in a browser or Postman).

## Part 2: Room Management

### Question 2.1: Returning IDs Only vs. Full Room Objects

In cases where a list endpoint only returns IDs (i.e. [LIB-301, LAB-101]) the payload is very small, reducing network bandwidth. Nonetheless, the client will need to make a separate GET /rooms/{id} request for each room it wishes to show; this is the N+1 request issue, which results in high latency in large datasets and unnecessarily loads the server.


List response (GET /api/v1/rooms) with full room objects will result in a slightly larger payload, eliminating the need for follow-up requests altogether. For a campus management dashboard that displays names and capacities in a table, it is much more efficient to receive all the data in a single response. The additional fields overhead (name, capacity, sensorIds) is insignificant as compared to the latency cost of hundreds of separate fetch requests. This project uses full objects, the industry-standard list API approach, at a moderate scale.


### Question 2.2: Idempotency of the DELETE Operation

This implementation is conditionally idempotent on the DELETE operation. HTTP idempotency means that a request can yield the same state on the server when repeated.


First, DELETE the existing empty room: the room is deleted, and a 204 No Content response is returned. Second DELETE (room no longer exists): The room is not present in the store, and service returns 404 Not Found. The server state is the same as that of the first call - the room is yet to be received. The response code is different (204 vs 404), but the resource state remains the same, which meets the idempotency requirement.


DELETE on a room with sensors: The request is blocked with a 409 Conflict response, and the room is retained. Making repeat requests of this nature keeps yielding 409 until the sensors are removed. The state of the server is the same in every such request, and this is also idempotent behavior.


Thus, the idempotence of DELETE in this implementation is literal: a call made repeatedly will never produce a different state, but may produce different response codes.

## Part 3: Sensor Operations & Filtering

### Question 3.1: Technical Consequences of @Consumes Media Type Mismatch

The @Consumes(MediaType.The annotation (APPLICATION_JSON) states that the POST endpoint only accepts request bodies that have a Content-Type: application/json header. When a client submits data with another content type, e.g., text/plain or application/xml, JAX-RS (Jersey) will reject the request before the method body is even called. The framework sends out an HTTP 415 Unsupported Media Type response immediately.


The implication is that there is no partial processing, no distorted data is sent to the deserialization layer, and the resource method is never called. This is a type of declarative input validation that is part of the framework itself and minimizes defensive boilerplate within business logic. Omitting @Consumes will cause JAX-RS to match any content type, which could cause Jackson to throw a JsonParseException or InvalidFormatException during deserialization, errors that would then have to be handled manually or via a global exception mapper.


### Question 3.2: @QueryParam vs Path Parameter for Filtering

Filtering by type (e.g., GET /api/v1/sensors?type=CO2) using @QueryParam(“type”) is semantically better than putting the filter in the path (e.g., /api/v1/sensors/type/CO2) due to several reasons:

•	Semantics: The route api/v1/sensors recognizes a set of resources. Filtering is not a new hierarchy of resources, but a modification of your query to that collection. Query parameters give instructions in the form of how to search, whereas path segments give instructions in the form of what resource to deal with.


•	Optionality: The query parameters are optional. GET /api/v1/sensors without a ?type= will give all sensors. A path parameter would require you to have a different endpoint to deal with the unfiltered case, duplicating logic.


•	Several filters: Query parameters are composed of clean query (?type=CO2&status=ACTIVE). Path parameters would be defined endpoint combinatorially.

•	Caching and bookmarkability: URLs containing query parameters are standardized to filtered views and work with HTTP caching systems and browser bookmarks.

•	REST convention: Query parameters for filtering, sorting, and pagination are strongly recommended in REST best practices and industry standards (Google API Design Guide, OpenAPI).

## Part 4 – Deep Nesting with Sub-Resources

### Question 4.1: Architectural Benefits of the Sub-Resource Locator Pattern

The sub-resource locator pattern (achieved through @Path("/{sensorId}/readings}) towards a new SensorReadingResource(sensorId) offers several architectural benefits as compared to monolithic controllers:


•	Separation of Concerns: SensorResource is limited to sensor-level operations. SensorReadingResource is the place of reading management logic. Every class has one, clearly defined task, which makes them easier to read, test and maintain.

•	Fewer Paths: Within a large API with dozens of nested paths, a placement of all the handlers in a single class results in a God object with hundreds of methods. The locator pattern makes every class concise and focused.

•	Contextual Injection: The parent resource constructs the sensorId, and the sub-resource and, therefore, the child class will always be in the appropriate sensor context without re-validating or re-reading it at the path.

•	Independent Evolution: SensorReadingResource can be changed or versioned or substituted without SensorResource. In a team, various developers may have various sub-resource classes and still not have to merge.

•	Testability: Individual resource classes can be unit tested, with a mocked DataStore, instead of having to run integration tests on an entire monolithic controller.

## Part 5 – Advanced Error Handling & Logging


### Question 5.2: Why HTTP 422 is More Semantically Accurate than 404
When a client POST a new sensor, whose roomId does not exist, the request URI itself (/api/v1/sensors)  is already valid, and the server identified it - so returning 404 Not Found would be a lie, as it would suggest that the endpoint has not been found.

The HTTP status code 422 Unprocessable Entity indicates that the request was syntactically correct (valid JSON, appropriate content type) and the appropriate endpoint was contacted, but the semantic content of the payload was invalid, specifically a reference to a resource (roomId) that does not exist. A broken foreign key reference within the request body is the issue, rather than a missing URL.

404 is used when the resource being addressed (the URL) is not found. A payload validation failure is a validation failure; therefore, using it as a payload validation failure mixes two entirely different types of errors, confusing clients and complicating error-handling logic. 422 provides the client with the exact amount of information: "your JSON was valid, but the data inside it doesn't satisfy business rules."

### Question 5.4: Cybersecurity Risks of Exposing Java Stack Traces

Exposure of raw Java stack traces to external API consumers presents a number of real security risks:

•	Internal path disclosure: Stack traces show the precise file system paths of compiled classes (e.g., /home/deploy/app/WEB-INF/classes/com/smartcampus/...), revealing server directory structure that can be used to target attacks.

•	Library and framework fingerprinting: Stack traces contain fully qualified class names and package paths of third-party libraries (e.g., org.glassfish.jersey.server.internal...), exposing precisely the frameworks and their version numbers. Those versions can then be looked up by attackers to get known CVEs to create exploits.

•	Disclosure of internal logic: Stack traces demonstrate the call sequence of methods, business logic, internal API patterns, and class hierarchies. This greatly lowers the effort required to comprehend the architecture of the application to exploit it.

•	Injection attack instructions: Trace contents (e.g. SQL exceptions, deserialization errors) can be used to confirm that injection attempts are being successfully made and to inform attackers on effective payloads.

In this project, the GlobalExceptionMapper <Throwable> captures all unhandled errors, captures the entire stack trace on the server-side (where it can be of use to the developer), and sends to the client only a generic message “An unexpected error occurred”, which will avoid all of the above risks but still allow the developer to debug.

### Question 5.5: Why JAX-RS Filters Are Superior to Manual Logging Statements

An example of managing a cross-cutting concern is using a filter annotated with @Provider (implementing both ContainerRequestFilter and ContainerResponseFilter) for logging, which applies to all endpoints, irrespective of business domain.

The manual method of adding Logger.info() calls in each resource method suffers a number of weaknesses: it causes extensive code duplication (all resource methods must include the same boilerplate), it is susceptible to error (coders need to remember to add it to new methods), and it makes the code more difficult to maintain (when changing the log format, all resource methods must be changed).

All these problems are solved by filters. The LoggingFilter is automatically applied to each request and response and does not require any action on the part of resource methods. Zero further logging code is needed to add a new endpoint. All the definitions of the log format are in a single place. This is what AOP (Aspect-Oriented Programming) is all about: centralise cross-cutting behaviour and keep business logic clean. Authentication, CORS headers, rate limiting, request ID injection are other cross-cutting concerns that filters manage well.


