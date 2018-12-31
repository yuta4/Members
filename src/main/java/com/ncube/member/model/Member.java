package com.ncube.member.model;

import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDate;

@Data
@RequiredArgsConstructor
@Document(collection="members")
public class Member {

    @Id
    private String id;
    private final String firstName;
    private final String lastName;
    private final LocalDate birthDate;
    private final String postalCode;
    private String imageUrl;

}
