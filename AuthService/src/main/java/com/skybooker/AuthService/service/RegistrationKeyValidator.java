package com.skybooker.AuthService.service;

import com.skybooker.AuthService.entity.Role;
import com.skybooker.AuthService.exception.InvalidRegistrationKeyException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class RegistrationKeyValidator {

    @Value("${registration.secrets.airline-staff}")
    private String staffSecret;

    @Value("${registration.secrets.admin}")
    private String adminSecret;

    public Role resolveRole(String registrationKey) {
        if (registrationKey == null || registrationKey.isBlank()) {
            return Role.PASSENGER; // no key → always regular user
        }
        if (registrationKey.equals(adminSecret)) {
            return Role.ADMIN;
        }
        if (registrationKey.equals(staffSecret)) {
            return Role.AIRLINE_STAFF;
        }
        // key was provided but didn't match anything → reject
        throw new InvalidRegistrationKeyException("Invalid registration key");
    }
}