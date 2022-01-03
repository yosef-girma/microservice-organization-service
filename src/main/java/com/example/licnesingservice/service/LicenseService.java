package com.example.licnesingservice.service;


import com.example.licnesingservice.config.ServiceConfig;
import com.example.licnesingservice.model.License;
import com.example.licnesingservice.model.Organization;
import com.example.licnesingservice.repository.LicenseRepository;
import com.example.licnesingservice.service.client.OrganizationDiscoveryClient;
import com.example.licnesingservice.service.client.OrganizationRestTemplateClient;
import com.example.licnesingservice.utils.LicenseUtils;
import io.github.resilience4j.bulkhead.annotation.Bulkhead;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.ratelimiter.annotation.RateLimiter;
import io.github.resilience4j.retry.annotation.Retry;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.stereotype.Service;


import java.util.*;

@Service
public class LicenseService {
    @Autowired
    MessageSource messages;

    @Autowired
    private LicenseRepository licenseRepository;
    @Autowired
    ServiceConfig config;

    @Autowired
    OrganizationFeignClient organizationFeignClient;

    @Autowired
    OrganizationDiscoveryClient organizationDiscoveryClient;

    @Autowired
    OrganizationRestTemplateClient organizationRestTemplateClient;


    public License getLicense(String licenseId, String organizationId, String clientType) {
        License license = licenseRepository.findByOrganizationIdAndLicenseId(organizationId, licenseId);
        if (null == license) {
            throw new IllegalArgumentException(String.format(messages.getMessage("license.search.error.message", null, null), licenseId, organizationId));
        }
        Organization organization = retrieveOrganizationInfo(organizationId, clientType);
        if (null != organization) {
            license.setOrganizationName(organization.getName());
            license.setContactName(organization.getContactName());
            license.setContactEmail(organization.getContactEmail());
            license.setContactPhone(organization.getContactPhone());
        }
        return license.withComent(config.getProperty());
    }

    private Organization retrieveOrganizationInfo(String organizationId, String clientType) {
        Organization organization = null;

        switch (clientType) {
            case "feign":
                System.out.println("I am using the feign client");
                organization = organizationFeignClient.getOrganization(organizationId);
                break;
            case "rest":
                System.out.println("I am using the rest client");
                organization = organizationRestTemplateClient.getOrganization(organizationId);
                break;
            case "discovery":
                System.out.println("I am using the discovery client");
                organization = organizationDiscoveryClient.getOrganization(organizationId);
                break;
            default:
                organization = organizationRestTemplateClient.getOrganization(organizationId);
        }

        return organization;
    }
 /*   public License getLicense(String licenseId, String organizationId){

        License license = licenseRepository.findByOrganizationIdAndLicenseId(organizationId,licenseId);

        if(null == license){
            throw new IllegalArgumentException(String.format(messages.getMessage("license.search.error.message",null,null),licenseId,organizationId));

        }
        return license.withComent(config.getProperty());
    }*/

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

    // default bulkhead is seamphore type
    @CircuitBreaker(name = "licenseService",fallbackMethod = "buildFallbackLicenseList")
    @RateLimiter(name = "licenseService",fallbackMethod = "buildFallbackLicenseList")
    @Retry(name = "retryLicenseService",fallbackMethod = "buildFallbackLicenseList")
    @Bulkhead(name = "bulkheadLicenseService",type = Bulkhead.Type.THREADPOOL, fallbackMethod = "buildFallbackLicenseList")
    public List<License> getLicensesByOrganization(String organizationId) {

        LicenseUtils.randomlyRunLong();
        return licenseRepository.findByOrganizationId(organizationId);
    }
   // This method must
   // must reside in the same class as the original method that was protected by @Circuit Breaker
    @SuppressWarnings("unused")
    private List<License> buildFallbackLicenseList(String organizationId, Throwable t){
        List<License> fallbackList = new ArrayList<>();
        License license = new License();
        license.setLicenseId("0000000-00-00000");
        license.setOrganizationId(organizationId);
        license.setProductName("Sorry no licensing information currently available");
        fallbackList.add(license);
        return fallbackList;
    }


}
