/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.smartcampusapi.mapper;

import java.util.logging.*;
import javax.ws.rs.core.*;
import javax.ws.rs.ext.*;
import javax.ws.rs.*;

@Provider
public class GlobalExceptionMapper implements ExceptionMapper<Throwable> {
    
    private static final Logger LOGGER = Logger.getLogger(GlobalExceptionMapper.class.getName());
    
    @Override
    public Response toResponse(Throwable e) {
        
        if (e instanceof WebApplicationException) {
            return ((WebApplicationException) e).getResponse();
        }

        // Log the unexpected error
        LOGGER.log(Level.SEVERE, "Unexpected error occurred", e);

        // Only handle real server errors
        return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                .entity(new ErrorResponse(500, "Internal Server Error", "An unexpected error occurred."))
                .type(MediaType.APPLICATION_JSON) 
                .build();
    }
}
