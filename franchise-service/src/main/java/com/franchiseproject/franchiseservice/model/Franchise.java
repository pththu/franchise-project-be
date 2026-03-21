package com.franchiseproject.franchiseservice.model;

import com.franchiseproject.franchiseservice.enums.FranchiseStatus;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "franchises")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Franchise {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String address;

    private String googleMapsUrl;

    private String phone;

    private String email;

    private LocalDate opened;

    private LocalDate closed;

    private String at;

    @Convert(converter = FranchiseStatusConverter.class)
    @Column(length = 10)
    private FranchiseStatus status = FranchiseStatus.NEW;

    @CreationTimestamp
    private LocalDateTime createdAt;  // THÊM

    @UpdateTimestamp
    private LocalDateTime updatedAt;  // THÊM
}