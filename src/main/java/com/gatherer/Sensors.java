/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.gatherer;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;
import java.util.List;

/**
 *
 * @author marius
 */
public class Sensors
{

    @SerializedName("DEBUG")
    @Expose
    private String debug;
    @SerializedName("UtcTime")
    @Expose
    private String utcTime;
    @SerializedName("Hostname")
    @Expose
    private String hostname;
    @SerializedName("IpAddress")
    @Expose
    private String ipAddress;
    @SerializedName("MacAddress")
    @Expose
    private String macAddress;
    @SerializedName("Gpio")
    @Expose
    private Integer gpio;
    @SerializedName("DeviceType")
    @Expose
    private String deviceType;
    @SerializedName("DeviceCount")
    @Expose
    private Integer deviceCount;
    @SerializedName("Sensors")
    @Expose
    private List<Sensor> sensors = null;

    public String getDEBUG()
    {
        return debug;
    }

    public void setDEBUG(String dEBUG)
    {
        this.debug = dEBUG;
    }

    public String getUtcTime()
    {
        return utcTime;
    }

    public void setUtcTime(String utcTime)
    {
        this.utcTime = utcTime;
    }

    public String getHostname()
    {
        return hostname;
    }

    public void setHostname(String hostname)
    {
        this.hostname = hostname;
    }

    public String getIpAddress()
    {
        return ipAddress;
    }

    public void setIpAddress(String ipAddress)
    {
        this.ipAddress = ipAddress;
    }

    public String getMacAddress()
    {
        return macAddress;
    }

    public void setMacAddress(String macAddress)
    {
        this.macAddress = macAddress;
    }

    public Integer getGpio()
    {
        return gpio;
    }

    public void setGpio(Integer gpio)
    {
        this.gpio = gpio;
    }

    public String getDeviceType()
    {
        return deviceType;
    }

    public void setDeviceType(String deviceType)
    {
        this.deviceType = deviceType;
    }

    public Integer getDeviceCount()
    {
        return deviceCount;
    }

    public void setDeviceCount(Integer deviceCount)
    {
        this.deviceCount = deviceCount;
    }

    public List<Sensor> getSensors()
    {
        return sensors;
    }

    public void setSensors(List<Sensor> sensors)
    {
        this.sensors = sensors;
    }

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
