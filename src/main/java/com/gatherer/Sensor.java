package com.gatherer;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class Sensor
{
    @SerializedName("Id")
    @Expose
    private String id;
    @SerializedName("ValueType")
    @Expose
    private String valueType;
    @SerializedName("Value")
    @Expose
    private String value;

    public String getId()
    {
        return id;
    }

    public void setId(String id)
    {
        this.id = id;
    }

    public String getValueType()
    {
        return valueType;
    }

    public void setValueType(String valueType)
    {
        this.valueType = valueType;
    }

    public String getValue()
    {
        return value;
    }

    public void setValue(String value)
    {
        this.value = value;
    }
}
