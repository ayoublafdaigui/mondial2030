package com.mondial2030.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

/**
 * Stadium Entity - Represents a stadium hosting Mondial 2030 matches.
 * Includes information about capacity, location, and facilities.
 */
@Entity
@Table(name = "stadiums")
public class Stadium {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false)
    private String city;

    @Column(nullable = false)
    private String country;

    @Column(nullable = false)
    private Integer capacity;

    @Column(name = "image_url")
    private String imageUrl;

    @Column(length = 1000)
    private String description;

    @Column(name = "year_built")
    private Integer yearBuilt;

    @Column(name = "is_main_venue")
    private Boolean isMainVenue = false;

    public Stadium() {}

    public Stadium(String name, String city, String country, Integer capacity) {
        this.name = name;
        this.city = city;
        this.country = country;
        this.capacity = capacity;
    }

    public Stadium(String name, String city, String country, Integer capacity, String description, Integer yearBuilt, boolean isMainVenue) {
        this(name, city, country, capacity);
        this.description = description;
        this.yearBuilt = yearBuilt;
        this.isMainVenue = isMainVenue;
    }

    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getCity() { return city; }
    public void setCity(String city) { this.city = city; }

    public String getCountry() { return country; }
    public void setCountry(String country) { this.country = country; }

    public Integer getCapacity() { return capacity; }
    public void setCapacity(Integer capacity) { this.capacity = capacity; }

    public String getImageUrl() { return imageUrl; }
    public void setImageUrl(String imageUrl) { this.imageUrl = imageUrl; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public Integer getYearBuilt() { return yearBuilt; }
    public void setYearBuilt(Integer yearBuilt) { this.yearBuilt = yearBuilt; }

    public Boolean getIsMainVenue() { return isMainVenue; }
    public void setIsMainVenue(Boolean isMainVenue) { this.isMainVenue = isMainVenue; }

    public String getLocation() {
        return city + ", " + country;
    }

    public String getCapacityFormatted() {
        return String.format("%,d", capacity);
    }

    @Override
    public String toString() {
        return "Stadium{" + name + " - " + city + ", " + country + " (" + capacity + " seats)}";
    }
}
