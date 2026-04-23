# Smart Campus Sensor & Room Management API

A JAX-RS (Jersey) RESTful API built for the University’s "Smart Campus" approach. It manages campus Rooms and IoT Sensors with CRUD operations, sub-resource reading history, custom exception handling, and request/response logging, all backed by in-memory data structures.

## API Overview

The API follows REST architectural principles with a versioned base path of ‘/api/v1’. Three core resources model the Smart Campus domain:

•	Room: physical spaces with capacity and a list of deployed sensor IDs.

•	Sensor: IoT devices (Temperature, CO2, Occupancy, etc.) linked to a room.

•	SensorReading: timestamped measurement events recorded by a sensor, exposed as a sub-resource of Sensor.

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

