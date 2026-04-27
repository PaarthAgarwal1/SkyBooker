package com.skybooker.PassengerService.dto.request;

import lombok.Data;

@Data
public class UpdatePassengerRequest {
    private String firstName;
    private String lastName;
    private String gender;
    private String nationality;
}
