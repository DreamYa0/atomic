package com.atomic.param.entity;

import java.io.Serializable;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;


public class Packages implements Serializable {

    private static final long serialVersionUID = 1L;
    private Long id;
    private String packageName;
    private String packageDesc;
    private Set<Services> services = new HashSet<>();

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getPackageName() {
        return packageName;
    }

    public void setPackageName(String packageName) {
        this.packageName = packageName;
    }

    public Packages packageName(String packageName) {
        this.packageName = packageName;
        return this;
    }

    public String getPackageDesc() {
        return packageDesc;
    }

    public void setPackageDesc(String packageDesc) {
        this.packageDesc = packageDesc;
    }

    public Packages packageDesc(String packageDesc) {
        this.packageDesc = packageDesc;
        return this;
    }

    public Set<Services> getServices() {
        return services;
    }

    public void setServices(Set<Services> services) {
        this.services = services;
    }

    public Packages services(Set<Services> services) {
        this.services = services;
        return this;
    }

    public Packages addServices(Services services) {
        this.services.add(services);
        services.setPackages(this);
        return this;
    }

    public Packages removeServices(Services services) {
        this.services.remove(services);
        services.setPackages(null);
        return this;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        Packages packages = (Packages) o;
        if (packages.getId() == null || getId() == null) {
            return false;
        }
        return Objects.equals(getId(), packages.getId());
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(getId());
    }

    @Override
    public String toString() {
        return "Packages{" +
                "id=" + getId() +
                ", packageName='" + getPackageName() + "'" +
                ", packageDesc='" + getPackageDesc() + "'" +
                "}";
    }
}
