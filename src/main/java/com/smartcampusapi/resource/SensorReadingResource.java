/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.smartcampusapi.resource;

import com.smartcampusapi.exception.SensorUnavailableException;
import com.smartcampusapi.mapper.ErrorResponse;
import com.smartcampusapi.model.Sensor;
import com.smartcampusapi.model.SensorReading;
import com.smartcampusapi.store.DataStore;
import java.net.URI;
import java.util.*;
import javax.ws.rs.*;
import javax.ws.rs.core.*;


public class SensorReadingResource {
    private final String sensorId;
    private DataStore store = DataStore.getInstance();

    public SensorReadingResource(String sensorId) {
        this.sensorId = sensorId;
    }

    // GET /api/v1/sensors/{sensorId}/readings
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public Response getReadings() {
        if (!store.getSensors().containsKey(sensorId)) {
            return Response.status(404)
                .entity(new ErrorResponse(404, "Not Found", "Sensor not found."))
                .build();
        }
        List<SensorReading> history = store.getReadings()
            .computeIfAbsent(sensorId, k -> new ArrayList<>());
        return Response.ok(history).build();
    }

    // POST /api/v1/sensors/{sensorId}/readings
    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response addReading(SensorReading reading) {
        Sensor sensor = store.getSensors().get(sensorId);
        if (sensor == null) {
            return Response.status(404)
                .entity(new ErrorResponse(404, "Not Found", "Sensor not found."))
                .build();
        }
        // 403 if sensor is under maintenance
        if ("MAINTENANCE".equalsIgnoreCase(sensor.getStatus()) || "OFFLINE".equalsIgnoreCase(sensor.getStatus())) {
            throw new SensorUnavailableException(sensorId);
        }
        reading.setId(UUID.randomUUID().toString());
        reading.setTimestamp(System.currentTimeMillis());
        
        store.getReadings().computeIfAbsent(sensorId, k -> new ArrayList<>()).add(reading);
        
        // update parent sensor's currentValue
        sensor.setCurrentValue(reading.getValue());
        
        URI location = URI.create("/api/v1/sensors/" + sensorId + "/readings/" + reading.getId());
        return Response.created(location).entity(reading).build();
    }
}
