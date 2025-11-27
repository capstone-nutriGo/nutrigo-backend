package com.nutrigo.nutrigo_backend.domain.store;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "store")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Store {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "store_id")
    private Long id;

    @Column(nullable = false, length = 100)
    private String name;

    // ENUM('baemin','yogiyo','coupang','other')
    @Column(name = "provider",
            columnDefinition = "ENUM('baemin','yogiyo','coupang','other')",
            nullable = false)
    private String provider;

    @Column(name = "external_store_id", length = 100)
    private String externalStoreId;

    @Column(name = "region_code", length = 50)
    private String regionCode;

    @Column(length = 255)
    private String address;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;
}
