package ru.nordmine.entities.raw;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;

@Entity
@Table(name = "raw_regions")
public class RawRegion extends Raw {

    @Column(nullable = true, length = 500, name = "capital_url")
    private String capitalUrl;

    @Column(nullable = true, length = 500, name = "administrative_center_url")
    private String administrativeCenterUrl;

    public String getCapitalUrl() {
        return capitalUrl;
    }

    public void setCapitalUrl(String capitalUrl) {
        this.capitalUrl = capitalUrl;
    }

    public String getAdministrativeCenterUrl() {
        return administrativeCenterUrl;
    }

    public void setAdministrativeCenterUrl(String administrativeCenterUrl) {
        this.administrativeCenterUrl = administrativeCenterUrl;
    }
}
