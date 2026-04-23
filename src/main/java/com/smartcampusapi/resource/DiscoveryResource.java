/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.smartcampusapi.resource;

import java.util.HashMap;
import java.util.Map;
import javax.ws.rs.*;
import javax.ws.rs.core.*;

@Path("/")
@Produces(MediaType.APPLICATION_JSON)
public class DiscoveryResource {
    @GET
    public Response getApiInfo() {
        
        Map<String, Object> response = new HashMap<>();
        
        response.put("version", "v1");
        response.put("name", "Smart Campus Sensor & Room Management API");
        response.put("contact", "admin@smartcampus.com");
        response.put("description", "RESTful API for managing campus rooms and IoT sensors.");
        
        Map<String, String> resources = new HashMap<>();
        resources.put("rooms", "/api/v1/rooms");
        resources.put("sensors", "/api/v1/sensors");
        response.put("resources", resources);
        
        return Response.ok(response).build();
    }
}
