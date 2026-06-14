package com.authsystemjava.backend.model;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "user_settings")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserSettings {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;

    @OneToOne
    @ToString.Exclude
    @JoinColumn(name = "user_id", nullable = false, unique = true)
    private User user;

    private String theme;

    @OneToOne
    @ToString.Exclude
    @JoinColumn(name = "company_id", nullable = true)
    private Company company;

    private String language;

}
