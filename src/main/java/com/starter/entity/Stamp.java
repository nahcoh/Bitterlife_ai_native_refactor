package com.starter.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "stamp")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class Stamp extends BaseTimeEntity{
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 100)
    private String name;

    @Column(nullable = false, length = 255)
    private String image;

    @Column(length = 255)
    private String qualification;

    @Column(nullable = false)
    private int price;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String description;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private SaleStampStatus status = SaleStampStatus.SALE;

    @Column(nullable = false)
    private LocalDateTime salesAt;

    private LocalDateTime salesEnd;

    public enum SaleStampStatus {
        PREPARING, SALE, SOLD_OUT, HIDDEN
    }

} 