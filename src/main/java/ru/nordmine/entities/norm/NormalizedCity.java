package ru.nordmine.entities.norm;

import javax.persistence.*;

@Entity
@Table(name = "norm_cities")
public class NormalizedCity extends Norm {

    @Column(nullable = false)
    private String country;

    @Column(nullable = true, name = "sec_location")
    private String secondLocation;

    @Column(nullable = true, name = "third_location")
    private String thirdLocation;

    @Column(nullable = true, length = 50)
    private String longitude;

    @Column(nullable = true, length = 50)
    private String latitude;

    @Column(nullable = false)
    private String type;

    @Column(nullable = true, length = 250, name = "capital_of")
    private String capitalOf;

    @Column(nullable = true, length = 250, name = "adm_center_of")
    private String administrativeCenterOf;

    public String getCountry() {
        return country;
    }

    public void setCountry(String country) {
        this.country = country;
    }

    public String getSecondLocation() {
        return secondLocation;
    }

    public void setSecondLocation(String secondLocation) {
        this.secondLocation = secondLocation;
    }

    public String getThirdLocation() {
        return thirdLocation;
    }

    public void setThirdLocation(String thirdLocation) {
        this.thirdLocation = thirdLocation;
    }

    public String getLongitude() {
        return longitude;
    }

    public void setLongitude(String longitude) {
        this.longitude = longitude;
    }

    public String getLatitude() {
        return latitude;
    }

    public void setLatitude(String latitude) {
        this.latitude = latitude;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getCapitalOf() {
        return capitalOf;
    }

    public void setCapitalOf(String capitalOf) {
        this.capitalOf = capitalOf;
    }

    public String getAdministrativeCenterOf() {
        return administrativeCenterOf;
    }

    public void setAdministrativeCenterOf(String administrativeCenterOf) {
        this.administrativeCenterOf = administrativeCenterOf;
    }
}
