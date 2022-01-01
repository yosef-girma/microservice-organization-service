package com.example.licnesingservice.model;

import lombok.Data;
import org.springframework.hateoas.RepresentationModel;

@Data
public class Organization  extends RepresentationModel<Organization> {
    String id;
    String name;
    String contactName;
    String contactEmail;
    String contactPhone;
}
