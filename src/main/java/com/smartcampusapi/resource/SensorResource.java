/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.smartcampusapi.resource;

import com.smartcampusapi.exception.LinkedResourceNotFoundException;
import com.smartcampusapi.mapper.ErrorResponse;
import com.smartcampusapi.model.Room;
import com.smartcampusapi.model.Sensor;
import com.smartcampusapi.store.DataStore;
import java.net.URI;
import java.util.*;
import java.util.stream.Collectors;
import javax.ws.rs.core.*;
import javax.ws.rs.*;

@Path("/sensors")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class SensorResource {
    private DataStore store = DataStore.getInstance();

    // GET /api/v1/sensors (with optional ?type= filter)
    @GET
    public Response getSensors(@QueryParam("type") String type) {
        List<Sensor> result = new ArrayList<>(store.getSensors().values());
        if (type != null && !type.isEmpty()) {
            result = result.stream()
                .filter(s -> s.getType().equalsIgnoreCase(type))
                .collect(Collectors.toList());
        }
        return Response.ok(result).build();
    }
    
    // GET /api/v1/sensors/{sensorId}
    @GET
    @Path("/{sensorId}")
    public Response getSensorById(@PathParam("sensorId") String sensorId) {
        Sensor sensor = store.getSensors().get(sensorId);
        if (sensor == null) {
            return Response.status(404)
                .entity(new ErrorResponse(404, "Not Found", "Sensor " + sensorId + " not found."))
                .build();
    }
        return Response.ok(sensor).build();
}

    // POST /api/v1/sensors
    @POST
    public Response createSensor(Sensor sensor) {
        if (sensor.getRoomId() == null || sensor.getRoomId().isEmpty()) {
            return Response.status(400)
                .entity(new ErrorResponse(400, "Bad Request", "roomId is required."))
                .build();
        }
        
        if (sensor.getType() == null || sensor.getType().isEmpty()) {
            return Response.status(400)
                .entity(new ErrorResponse(400, "Bad Request", "Sensor type is required."))
                .build();
        }
        
        if (!store.getRooms().containsKey(sensor.getRoomId())) {
            throw new LinkedResourceNotFoundException(
                "Room with ID '" + sensor.getRoomId() + "' does not exist.");
        }
        
        if (sensor.getId() == null || sensor.getId().isEmpty()) {
            sensor.setId("SENSOR-" + System.currentTimeMillis());
        }
        
        sensor.setStatus("ACTIVE"); // default status
        store.getSensors().put(sensor.getId(), sensor);
        // Add sensor ID to the parent room's sensorIds list
        store.getRooms().get(sensor.getRoomId()).getSensorIds().add(sensor.getId());
        store.getReadings().put(sensor.getId(), new ArrayList<>());
        URI location = URI.create("/api/v1/sensors/" + sensor.getId());
        return Response.created(location).entity(sensor).build();
    }

    // Sub-resource locator (to SensorReadingResource)
    @Path("/{sensorId}/readings")
    public SensorReadingResource getReadingResource(@PathParam("sensorId") String sensorId) {
        return new SensorReadingResource(sensorId);
    }
    
    // PUT /api/v1/sensors/{sensorId}/status
    @PUT
    @Path("/{sensorId}/status")
    public Response updateSensorStatus(@PathParam("sensorId") String sensorId, Sensor body) {
        Sensor sensor = store.getSensors().get(sensorId);
        if (sensor == null) {
            return Response.status(404)
                .entity(new ErrorResponse(404, "Not Found", "Sensor not found."))
                .build();
    }
        sensor.setStatus(body.getStatus());
        return Response.ok(sensor).build();
}
    
    @DELETE
    @Path("/{sensorId}")
    public Response deleteSensor(@PathParam("sensorId") String sensorId) {
        Sensor sensor = store.getSensors().get(sensorId);
        if (sensor == null) {
            return Response.status(404)
                .entity(new ErrorResponse(404, "Not Found", "Sensor not found."))
                .build();
    }
    // Remove from parent room's sensorIds
        Room room = store.getRooms().get(sensor.getRoomId());
        if (room != null) {
            room.getSensorIds().remove(sensor.getId());
    }
        store.getSensors().remove(sensorId);
        store.getReadings().remove(sensorId);
        return Response.noContent().build();
}
}
