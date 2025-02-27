package com.spotifyapi.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Getter
@Setter
@Table(name = "logger")
public class Logger {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String username;

    private String userId;

    private String methodName;

    private String parameters;

    private LocalDateTime DateTime;

    private String message;

    private String status;
}
