/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.smartcampusapi.resource;

import com.smartcampusapi.exception.RoomNotEmptyException;
import com.smartcampusapi.mapper.ErrorResponse;
import com.smartcampusapi.model.Room;
import com.smartcampusapi.store.DataStore;
import java.net.URI;
import java.util.ArrayList;
import javax.ws.rs.*;
import javax.ws.rs.core.*;

@Path("/rooms")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class RoomResource {
    private DataStore store = DataStore.getInstance();

    // GET /api/v1/rooms
    @GET
    public Response getAllRooms() {
        return Response.ok(new ArrayList<>(store.getRooms().values())).build();
    }

    // POST /api/v1/rooms
    @POST
    public Response createRoom(Room room) {
        if (room == null) {
            return Response.status(400)
                .entity(new ErrorResponse(400, "Bad Request", 
                    "Request body is missing or not valid JSON."))
                .build();
        }
        
        if (room.getId() == null || room.getId().isEmpty()) {
            room.setId("ROOM-" + System.currentTimeMillis());
        }
        
        if (room.getName() == null || room.getName().isEmpty()) {
            return Response.status(400)
                .entity(new ErrorResponse(400, "Bad Request", "Room name is required."))
                .build();
        }
        
        if (store.getRooms().containsKey(room.getId())) {
            return Response.status(409)
                .entity(new ErrorResponse(409, "Conflict", "Room ID already exists."))
                .build();
        }
        
        store.getRooms().put(room.getId(), room);
        // 201 Created with Location header 
        URI location = URI.create("/api/v1/rooms/" + room.getId());
        return Response.created(location).entity(room).build();
    }

    // GET /api/v1/rooms/{roomId}
    @GET
    @Path("/{roomId}")
    public Response getRoomById(@PathParam("roomId") String roomId) {
        Room room = store.getRooms().get(roomId);
        if (room == null) {
            return Response.status(404)
                .entity(new ErrorResponse(404, "Not Found", "Room " + roomId + " not found."))
                .build();
        }
        return Response.ok(room).build();
    }

    // DELETE /api/v1/rooms/{roomId}
    @DELETE
    @Path("/{roomId}")
    public Response deleteRoom(@PathParam("roomId") String roomId) {
        Room room = store.getRooms().get(roomId);
        if (room == null) {
            // Idempotent: second call on non-existent room still returns 404
            return Response.status(404)
                .entity(new ErrorResponse(404, "Not Found", "Room not found."))
                .build();
        }
        if (!room.getSensorIds().isEmpty()) {
            throw new RoomNotEmptyException(roomId); // triggers 409 mapper
        }
        store.getRooms().remove(roomId);
        return Response.noContent().build(); // 204 No Content
    }
}