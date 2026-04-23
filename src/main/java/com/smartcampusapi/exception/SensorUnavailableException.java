/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.smartcampusapi.exception;

public class SensorUnavailableException extends RuntimeException {
    
    public SensorUnavailableException(String sensorId) {
        super("Sensor " + sensorId + " is under Maintenance and cannot accept readings.");
    }
    
}
