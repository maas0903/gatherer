package com.gatherer;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class Humid
{

    @SerializedName("DEBUG")
    @Expose
    private String debug;
    
    @SerializedName("UtcTime")
    @Expose
    private String utcTime;
    
    @SerializedName("DeviceCount")
    @Expose
    private int deviceCount;
    
    @SerializedName("Hostname")
    @Expose
    private String hostname;
    
    @SerializedName("IpAddress")
    @Expose
    private String ipAddress;
    
    @SerializedName("Mac Address")
    @Expose
    private String macAddress;
    
    @SerializedName("Gpio")
    @Expose
    private int gpio;
    
    @SerializedName("Humidity")
    @Expose
    private float humidity;
    
    @SerializedName("Temperature")
    @Expose
    private float temperature;
    
    @SerializedName("Heat_Index")
    @Expose
    private float heatIndex;

    public String getDEBUG()
    {
        return debug;
    }

    public void setDebug(String debug)
    {
        this.debug = debug;
    }

    public String getUtcTime()
    {
        return utcTime;
    }

    public void setUtcTime(String utcTime)
    {
        this.utcTime = utcTime;
    }

    public int getDeviceCount()
    {
        return deviceCount;
    }

    public void setDeviceCount(int deviceCount)
    {
        this.deviceCount = deviceCount;
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

    public int getGpio()
    {
        return gpio;
    }

    public void setGpio(int gpio)
    {
        this.gpio = gpio;
    }

    public float getHumidity()
    {
        return humidity;
    }

    public void setHumidity(float humidity)
    {
        this.humidity = humidity;
    }

    public float getTemperature()
    {
        return temperature;
    }

    public void setTemperature(float temperature)
    {
        this.temperature = temperature;
    }

    public float getHeatIndex()
    {
        return heatIndex;
    }

    public void setHeatIndex(float heatIndex)
    {
        this.heatIndex = heatIndex;
    }

}
