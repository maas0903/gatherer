/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.gatherer;

/**
 *
 * @author marius
 */
import com.pi4j.io.gpio.GpioController;
import com.pi4j.io.gpio.GpioFactory;
import com.pi4j.io.gpio.GpioPinDigitalOutput;
import com.pi4j.io.gpio.RaspiPin;
import com.google.gson.Gson;
import java.io.BufferedReader;
import java.io.File;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Scanner;
import com.melektro.tools.spreadsheetdb.*;
import com.pi4j.io.gpio.PinState;
import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.security.GeneralSecurityException;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import org.apache.commons.net.ntp.NTPUDPClient;
import org.apache.commons.net.ntp.TimeInfo;

public class gatherer
{

    private static float verandaTemp;
    private static float livingTemp;

    private static final String APPLICATION_NAME = "Gatherer";
    private static final String CREDENTIALS_FILE_PATH = "google-sheets-client-secret.json";
    private static final String SPREADSHEET_ID = "115UPv1D8GZOrPZVAGi7olAIGxBUVSk45RespRGYPncs";
    private static final SpreadsheetDatabase.CredentialsProvider CREDENTIALS_PROVIDER = () -> gatherer.class.getResourceAsStream(CREDENTIALS_FILE_PATH);

    private static final int MaxRows = 499992;
    //5,000,000	  max selle
    //500,000     max rye (10 selle per ry)
    //8           margin
    //499,992     rye
    //5           aantal devices
    //99,998      aantal lesings
    //10          minute
    //999,984     totaal minute
    //16,666      totaal ure
    //694.4333333 totaal dae
    //1.901254848 totaal jare

    //private static int DeviceReadingDelay = 600000;
    //do with crontab
    private static final int TRYREADSENSORCOUNT = 3;
    private static final boolean DEBUG = false;

    static String GetForConnection(String Url) throws MalformedURLException, IOException
    {
        try
        {
            URL url = new URL(Url);
            HttpURLConnection con = (HttpURLConnection) url.openConnection();
            con.setRequestMethod("GET");

            int status = con.getResponseCode();

            if (status == 200)
            {
                String result;
                try (BufferedReader in = new BufferedReader(
                        new InputStreamReader(con.getInputStream())))
                {
                    result = "";
                    String inputLine;
                    while ((inputLine = in.readLine()) != null)
                    {
                        result = result + inputLine;
                    }
                }
                if (!result.contains("No devices found"))
                {
                    return result;
                }
            }
            return "";
        } catch (IOException ex)
        {
            //ex.printStackTrace();
            System.out.println("Host unreachable");
            return "";
        }
    }

    private static String getNTPDate() throws UnknownHostException, IOException
    {
        String TIME_SERVER = "pool.ntp.org";
        //time-a.nist.gov
        //from 8266: pool.ntp.org
        NTPUDPClient timeClient = new NTPUDPClient();
        InetAddress inetAddress = InetAddress.getByName(TIME_SERVER);
        TimeInfo timeInfo = timeClient.getTime(inetAddress);
        long returnTime = timeInfo.getReturnTime();
        SimpleDateFormat format = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss");
        return (format.format(new Date(returnTime)));
    }
    
    private static String getSystemDate()
    {
        DateTimeFormatter dtf = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss");
        LocalDateTime now = LocalDateTime.now();
        return dtf.format(now);
    }

    private static boolean toDb(String sNTPDate, String jsonString, GpioPinDigitalOutput pin00, GpioPinDigitalOutput pin02) throws GeneralSecurityException, IOException
    {
        try
        {

            Gson gson = new Gson();
            SensorsNormalised sensorsNormalised = gson.fromJson(jsonString, SensorsNormalised.class);
            System.out.println("Got JSON Object");

            doSomeBusinessLogic(sensorsNormalised, pin00, pin02);

            if (!DEBUG)
            {

                SpreadsheetDatabase db = SpreadsheetDatabase.getPersonalDatabase(SPREADSHEET_ID, APPLICATION_NAME, CREDENTIALS_PROVIDER);
                System.out.println("Got Spreadsheet DB");

                try
                {
                    String tmpHostName = "";
                    int sensorNumber = 1;
                    for (Sensor sensor : sensorsNormalised.sensors)
                    {
                        if (!tmpHostName.equals(sensorsNormalised.getHostname()))
                        {
                            sensorNumber = 1;
                        }
                        String tableName = "";
                        if (sensorsNormalised.sensors.size() > 1)
                        {
                            tableName = sensorsNormalised.getHostname() + sensorNumber;
                            sensorNumber++;
                        } else
                        {
                            tableName = sensorsNormalised.getHostname();
                        }

                        System.out.println("tableName=" + tableName);
                        db.createTableRequest(tableName, Arrays.asList("DEBUG", "UtcTime", "DeviceCount", "Hostname", "IpAddress", "MacAddress", "Gpio", "DeviceType", "Id", "Value")).execute();
                        System.out.println("createTableRequest done");

                        List<SpreadsheetRecord> records = db.queryRequest(tableName).all().execute();
                        System.out.println("queryRequest done");

                        while (records.size() >= MaxRows)
                        {
                            records = db.queryRequest(tableName).all().execute();
                            //delete first row
                            db.deleteRequest(tableName)
                                    .setRecords(Arrays.asList(records.get(0)))
                                    .execute();
                        }

                        String sDEBUG = sensorsNormalised.getDEBUG();
                        int iDeviceCount = sensorsNormalised.getDeviceCount();
                        String sHostname = tableName;
                        String sIpAddress = sensorsNormalised.getIpAddress();
                        String sMacAddress = sensorsNormalised.getMacAddress();
                        int iGpio = sensorsNormalised.getGpio();
                        String sValueType = sensor.getValueType();
                        String sId = sensor.getId();
                        String sValue = sensor.getValue();

                        System.out.println("got values from JSON object");

                        db.updateRequest(tableName)
                                .insert(new SpreadsheetRecord(Arrays.asList(
                                        sDEBUG,
                                        sNTPDate,
                                        iDeviceCount,
                                        sHostname,
                                        sIpAddress,
                                        sMacAddress,
                                        iGpio,
                                        sValueType,
                                        sId,
                                        sValue)))
                                .execute();
                        System.out.println("updateRequest done");
                        records = db.queryRequest(tableName).all().execute();
                        System.out.println("execute done");
                        if (records.size() > 0)
                        {
                            System.out.print("DEBUG = " + sDEBUG + ", ");
                            System.out.print("UtcTime = " + sNTPDate + ", ");
                            System.out.print("DeviceCount = " + iDeviceCount + ", ");
                            System.out.print("Hostname = " + sHostname + ", ");
                            System.out.print("IpAddress = " + sIpAddress + ", ");
                            System.out.print("MacAddress = " + sMacAddress + ", ");
                            System.out.print("Gpio = " + iGpio + ", ");
                            System.out.print("ValueType = " + sValueType + ", ");
                            System.out.print("Id = " + sId + ", ");
                            System.out.println("Value = " + sValue);
                        } else
                        {
                            System.out.println("Nothing written");
                        }
                        tmpHostName = sensorsNormalised.getHostname();
                    }
                } catch (Exception ex)
                {
                    db.createTableRequest("Exceptions", Arrays.asList("ExceptionDate", "Exception")).execute();
                    List<SpreadsheetRecord> exceptions = db.queryRequest("Exceptions").all().execute();
                    db.updateRequest("Exceptions")
                            .insert(new SpreadsheetRecord(Arrays.asList(
                                    sNTPDate,
                                    ex.getMessage())))
                            .execute();
                }
            }
        } catch (Exception e)
        {
        }
        return false;

    }

    public static void main(String[] args) throws InterruptedException, IOException, GeneralSecurityException
    {
        boolean hellFreezesOver = false;
        System.out.println("GpioFactory.getInstance ok");
        GpioController gpio = GpioFactory.getInstance();
        GpioPinDigitalOutput pin00 = gpio.provisionDigitalOutputPin(RaspiPin.GPIO_00);
        GpioPinDigitalOutput pin02 = gpio.provisionDigitalOutputPin(RaspiPin.GPIO_02);
        pin00.setShutdownOptions(true, PinState.LOW);
        pin02.setShutdownOptions(true, PinState.LOW);

        while (!hellFreezesOver)
        {
            System.out.println("In loop");
            ArrayList<String> list;
            verandaTemp = -10000;
            livingTemp = verandaTemp;
            try (Scanner urlNames = new Scanner(new File("links.txt")))
            {
                list = new ArrayList<>();
                while (urlNames.hasNext())
                {
                    list.add(urlNames.next());
                }

                for (var urlName : list)
                {
                    if (urlName.startsWith("#"))
                    {
                        System.out.println("***********Skipped " + urlName.substring(1));
                    } else
                    {
                        System.out.println("***********Reading " + urlName);
                        int tryCount = 0;

                        String jsonString = "";
                        do
                        {
                            jsonString = GetForConnection(urlName);
                            tryCount++;
                            System.out.print("***********Attempt ");
                            System.out.println(tryCount);
                            if (jsonString.equals(""))
                            {
                                Thread.sleep(1000);
                            }
                        } while (jsonString.equals("") && tryCount <= TRYREADSENSORCOUNT);

                        if (!jsonString.equals(""))
                        {
                            System.out.println("***********Read OK");
                            //String sNTPDate = getNTPDate();
                            String sNTPDate = getSystemDate();
                            System.out.println("Got Time  - doing toDb now");
                            boolean toDb = toDb(sNTPDate, jsonString, pin00, pin02);
                            System.out.println("toDb done");
                        }
                    }
                }

            } catch (Exception e)
            {
            }
            System.out.println("Going to sleep - a LED should be on");

            long msTime = (15 * 60 * 1000);
            long doUntil = msTime + System.currentTimeMillis();
            long currentTimeMillis = System.currentTimeMillis();
            while (currentTimeMillis != doUntil)
            {
                /*
                //Some live checking
                if (currentTimeMillis % (60 * 1000) == 0)
                {
                    WriteToExceptions("live at");
                }
                */
                currentTimeMillis = System.currentTimeMillis();
            }
              

            pin00.low();
            pin02.low();
            //gpio.shutdown();
        }
    }

    private static void doSomeBusinessLogic(SensorsNormalised sensorsNormalised, GpioPinDigitalOutput pin00, GpioPinDigitalOutput pin02)
    {
        System.out.println("host=" + sensorsNormalised.getHostname());
        if (sensorsNormalised.getHostname().equals("veranda"))
        {
            var sensors = sensorsNormalised.getSensors();
            verandaTemp = Float.parseFloat(sensors.get(1).getValue());
            System.out.print("verandaTemp=");
            System.out.println(verandaTemp);
        }

        if (sensorsNormalised.getHostname().equals("living"))
        {
            var sensors = sensorsNormalised.getSensors();
            livingTemp = Float.parseFloat(sensors.get(0).getValue());
            System.out.print("livingTemp=");
            System.out.println(livingTemp);
        }

        if (livingTemp != -10000 && verandaTemp != -10000)
        {
            System.out.print("********* In evaluate - verandaTemp=");
            System.out.println(verandaTemp);
            System.out.print("********* In evaluate - livingTemp=");
            System.out.println(livingTemp);
            if (livingTemp > verandaTemp && livingTemp > 22)
            {
                try
                {
                    //deure en vensters oop - groen pin 11
                    pin00.low();
                    pin02.high();
                    System.out.println("deure en vensters oop - groen pin 11");
                } catch (Exception e)
                {
                    System.out.println("Exception setting pin");
                }
            }

            if (livingTemp < verandaTemp && verandaTemp > 22)
            {
                try
                {
                    //deure en vensters toe - rooi - pin 13
                    pin02.low();
                    pin00.high();
                    System.out.println("deure en vensters toe - rooi - pin 13");
                } catch (Exception e)
                {
                    System.out.println("Exception setting pin");
                }
            }
            if (livingTemp == verandaTemp && livingTemp > 22)
            {
                pin02.high();
                pin00.high();
                System.out.println("wat ookal!!");
            }

        }
    }

    private static void WriteToExceptions(String message) throws GeneralSecurityException, IOException
    {
        String sNTPDate = getSystemDate();
        SpreadsheetDatabase db = SpreadsheetDatabase.getPersonalDatabase(SPREADSHEET_ID, APPLICATION_NAME, CREDENTIALS_PROVIDER);
        db.createTableRequest("Exceptions", Arrays.asList("ExceptionDate", "Exception")).execute();
        List<SpreadsheetRecord> exceptions = db.queryRequest("Exceptions").all().execute();
        db.updateRequest("Exceptions")
                .insert(new SpreadsheetRecord(Arrays.asList(sNTPDate + ":" + message)))
                .execute();
    }

}
