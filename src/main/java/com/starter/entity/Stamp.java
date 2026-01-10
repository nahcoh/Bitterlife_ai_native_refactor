package com.starter.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "stamp")
public class Stamp extends BaseTimeEntity{
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long stampId;

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

    @Column(nullable = false, length = 50)
    private String status;

    @Column(nullable = false)
    private LocalDateTime salesAt;

    private LocalDateTime salesEnd;

    // getter/setter
    public Long getStampId() { return stampId; }
    public void setStampId(Long stampId) { this.stampId = stampId; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getImage() { return image; }
    public void setImage(String image) { this.image = image; }
    public String getQualification() { return qualification; }
    public void setQualification(String qualification) { this.qualification = qualification; }
    public int getPrice() { return price; }
    public void setPrice(int price) { this.price = price; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public java.time.LocalDateTime getSalesAt() { return salesAt; }
    public void setSalesAt(java.time.LocalDateTime salesAt) { this.salesAt = salesAt; }
    public java.time.LocalDateTime getSalesEnd() { return salesEnd; }
    public void setSalesEnd(java.time.LocalDateTime salesEnd) { this.salesEnd = salesEnd; }
} 