package com.nutrigo.nutrigo_backend.domain.menu;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "menu")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Menu {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "menu_id")
    private Long id;

    @Column(nullable = false, length = 100)
    private String name;

    @Column(name = "base_price")
    private Integer basePrice;

    @Column(name = "is_active", nullable = false)
    private Boolean active;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    // 일단 store / menu_category 는 그냥 ID 컬럼으로만 매핑
    @Column(name = "store_id", nullable = false)
    private Long storeId;

    @Column(name = "menu_category_id", nullable = false)
    private Long menuCategoryId;
}
