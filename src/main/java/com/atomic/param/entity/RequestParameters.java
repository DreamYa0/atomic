package com.atomic.param.entity;

import java.io.Serializable;
import java.util.Objects;

/**
 * The RequestParameters entity.
 */

public class RequestParameters implements Serializable {

    private static final long serialVersionUID = 1L;

    private Long id;

    private String parameterName;

    private String parameterType;

    private String parameterValue;

    private String parameterDesc;

    private Boolean parameterRequired;

    private Methods methods;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getParameterName() {
        return parameterName;
    }

    public void setParameterName(String parameterName) {
        this.parameterName = parameterName;
    }

    public RequestParameters parameterName(String parameterName) {
        this.parameterName = parameterName;
        return this;
    }

    public String getParameterType() {
        return parameterType;
    }

    public void setParameterType(String parameterType) {
        this.parameterType = parameterType;
    }

    public RequestParameters parameterType(String parameterType) {
        this.parameterType = parameterType;
        return this;
    }

    public String getParameterValue() {
        return parameterValue;
    }

    public void setParameterValue(String parameterValue) {
        this.parameterValue = parameterValue;
    }

    public RequestParameters parameterValue(String parameterValue) {
        this.parameterValue = parameterValue;
        return this;
    }

    public String getParameterDesc() {
        return parameterDesc;
    }

    public void setParameterDesc(String parameterDesc) {
        this.parameterDesc = parameterDesc;
    }

    public RequestParameters parameterDesc(String parameterDesc) {
        this.parameterDesc = parameterDesc;
        return this;
    }

    public Boolean isParameterRequired() {
        return parameterRequired;
    }

    public RequestParameters parameterRequired(Boolean parameterRequired) {
        this.parameterRequired = parameterRequired;
        return this;
    }

    public void setParameterRequired(Boolean parameterRequired) {
        this.parameterRequired = parameterRequired;
    }

    public Methods getMethods() {
        return methods;
    }

    public void setMethods(Methods methods) {
        this.methods = methods;
    }

    public RequestParameters methods(Methods methods) {
        this.methods = methods;
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
        RequestParameters requestParameters = (RequestParameters) o;
        if (requestParameters.getId() == null || getId() == null) {
            return false;
        }
        return Objects.equals(getId(), requestParameters.getId());
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(getId());
    }

    @Override
    public String toString() {
        return "RequestParameters{" +
                "id=" + getId() +
                ", parameterName='" + getParameterName() + "'" +
                ", parameterType='" + getParameterType() + "'" +
                ", parameterValue='" + getParameterValue() + "'" +
                ", parameterDesc='" + getParameterDesc() + "'" +
                ", parameterRequired='" + isParameterRequired() + "'" +
                "}";
    }
}
