package com.atomic.param.entity;

import java.io.Serializable;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

/**
 * Method entity.
 */

public class Methods implements Serializable {

    private static final long serialVersionUID = 1L;
    private Long id;
    private String methodName;
    private String methodDesc;
    private String methodURL;
    private Services services;

    private Set<RequestParameters> requestParameters = new HashSet<>();

    private Set<ResultParameters> resultParameters = new HashSet<>();

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getMethodName() {
        return methodName;
    }

    public void setMethodName(String methodName) {
        this.methodName = methodName;
    }

    public Methods methodName(String methodName) {
        this.methodName = methodName;
        return this;
    }

    public String getMethodDesc() {
        return methodDesc;
    }

    public void setMethodDesc(String methodDesc) {
        this.methodDesc = methodDesc;
    }

    public Methods methodDesc(String methodDesc) {
        this.methodDesc = methodDesc;
        return this;
    }

    public String getMethodURL() {
        return methodURL;
    }

    public void setMethodURL(String methodURL) {
        this.methodURL = methodURL;
    }

    public Methods methodURL(String methodURL) {
        this.methodURL = methodURL;
        return this;
    }

    public Services getServices() {
        return services;
    }

    public void setServices(Services services) {
        this.services = services;
    }

    public Methods services(Services services) {
        this.services = services;
        return this;
    }

    public Set<RequestParameters> getRequestParameters() {
        return requestParameters;
    }

    public void setRequestParameters(Set<RequestParameters> requestParameters) {
        this.requestParameters = requestParameters;
    }

    public Methods requestParameters(Set<RequestParameters> requestParameters) {
        this.requestParameters = requestParameters;
        return this;
    }

    public Methods addRequestParameters(RequestParameters requestParameters) {
        this.requestParameters.add(requestParameters);
        requestParameters.setMethods(this);
        return this;
    }

    public Methods removeRequestParameters(RequestParameters requestParameters) {
        this.requestParameters.remove(requestParameters);
        requestParameters.setMethods(null);
        return this;
    }

    public Set<ResultParameters> getResultParameters() {
        return resultParameters;
    }

    public void setResultParameters(Set<ResultParameters> resultParameters) {
        this.resultParameters = resultParameters;
    }

    public Methods resultParameters(Set<ResultParameters> resultParameters) {
        this.resultParameters = resultParameters;
        return this;
    }

    public Methods addResultParameters(ResultParameters resultParameters) {
        this.resultParameters.add(resultParameters);
        resultParameters.setMethods(this);
        return this;
    }

    public Methods removeResultParameters(ResultParameters resultParameters) {
        this.resultParameters.remove(resultParameters);
        resultParameters.setMethods(null);
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
        Methods methods = (Methods) o;
        if (methods.getId() == null || getId() == null) {
            return false;
        }
        return Objects.equals(getId(), methods.getId());
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(getId());
    }

    @Override
    public String toString() {
        return "Methods{" +
                "id=" + getId() +
                ", methodName='" + getMethodName() + "'" +
                ", methodDesc='" + getMethodDesc() + "'" +
                ", methodURL='" + getMethodURL() + "'" +
                "}";
    }
}
