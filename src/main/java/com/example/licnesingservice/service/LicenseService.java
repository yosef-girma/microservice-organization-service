package com.example.licnesingservice.service;


import com.example.licnesingservice.config.ServiceConfig;
import com.example.licnesingservice.model.License;
import com.example.licnesingservice.repository.LicenseRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.stereotype.Service;


import java.util.Locale;
import java.util.Random;
import java.util.UUID;

@Service
public class LicenseService {
    @Autowired
    MessageSource messages;

    @Autowired
    private LicenseRepository licenseRepository;
    @Autowired
    ServiceConfig config;

    public License getLicense(String licenseId, String organizationId){

        License license = licenseRepository.findByOrganizationIdAndLicenseId(organizationId,licenseId);

        if(null == license){
            throw new IllegalArgumentException(String.format(messages.getMessage("license.search.error.message",null,null),licenseId,organizationId));

        }
        return license.withComent(config.getProperty());
    }

    public License createLicense(License license) {
        String responseMessage = null;
        license.setLicenseId(UUID.randomUUID().toString());
        licenseRepository.save(license);
        return license.withComent(config.getProperty());
    }

    public License updateLicense(License license) {
        licenseRepository.save(license);
        return license.withComent(config.getProperty());
    }

    public String deleteLicense(String licenseId) {
        String responseMessage = null;
        License license = new License();
        license.setLicenseId(licenseId);
        licenseRepository.delete(license);
        responseMessage = String.format("Deleting license with id %s", licenseId);
        return responseMessage;
    }
}
