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
import java.io.IOException;
import java.io.InputStream;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.security.GeneralSecurityException;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import org.apache.commons.net.ntp.NTPUDPClient;
import org.apache.commons.net.ntp.TimeInfo;
import com.melektro.tools.LogsFormatter;
import static com.melektro.tools.LogsFormatter.Log;
import java.util.logging.Level;
import java.util.logging.Logger;

public class gatherer
{

    private static final String APPLICATION_NAME = "Gatherer";
    private static final String CREDENTIALS_FILE_PATH = "google-sheets-client-secret.json";
    private static String SPREADSHEET_ID = "115UPv1D8GZOrPZVAGi7olAIGxBUVSk45RespRGYPncs";
    private static final SpreadsheetDatabase.CredentialsProvider CREDENTIALS_PROVIDER = new SpreadsheetDatabase.CredentialsProvider()
    {

        @Override
        public InputStream getCredentials()
        {
            return gatherer.class.getResourceAsStream(CREDENTIALS_FILE_PATH);
        }
    };

    private static int MaxRows = 31780000;
    private static int DeviceReadingDelay = 600000;

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
                BufferedReader in = new BufferedReader(
                        new InputStreamReader(con.getInputStream()));
                String result = "";
                String inputLine;
                while ((inputLine = in.readLine()) != null)
                {
                    result = result + inputLine;
                }
                in.close();
                if (!result.contains("No devices found"))
                {
                    return result;
                }
            }
            return "";
        } catch (Exception ex)
        {
            ex.printStackTrace();
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

    private static boolean toDb(String jsonString) throws GeneralSecurityException, IOException
    {
        try
        {

            String sNTPDate = getNTPDate();

            Gson gson = new Gson();
            SensorsNormalised sensorsNormalised = gson.fromJson(jsonString, SensorsNormalised.class);

            SpreadsheetDatabase db = SpreadsheetDatabase.getPersonalDatabase(SPREADSHEET_ID, APPLICATION_NAME, CREDENTIALS_PROVIDER);
            db.createTableRequest("Sensors", Arrays.asList("DEBUG", "UtcTime", "DeviceCount", "Hostname", "IpAddress", "MacAddress", "Gpio", "DeviceType", "Id", "Value")).execute();
            List<Record> records = db.queryRequest("Sensors").all().execute();

            while (records.size() >= MaxRows)
            {
                records = db.queryRequest("Sensors").all().execute();
                //delete first row
                db.deleteRequest("Sensors")
                        .setRecords(Arrays.asList(records.get(0)))
                        .execute();
            }

            try
            {
                for (Sensor sensor : sensorsNormalised.sensors)
                {

                    String sDEBUG = sensorsNormalised.getDEBUG();
                    int iDeviceCount = sensorsNormalised.getDeviceCount();
                    String sHostname = sensorsNormalised.getHostname();
                    String sIpAddress = sensorsNormalised.getIpAddress();
                    String sMacAddress = sensorsNormalised.getMacAddress();
                    int iGpio = sensorsNormalised.getGpio();
                    String sValueType = sensor.getValueType();
                    String sId = sensor.getId();
                    String sValue = sensor.getValue();

                    db.updateRequest("Sensors")
                            .insert(new Record(Arrays.asList(
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
                    records = db.queryRequest("Sensors").all().execute();
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
                }
            } catch (Exception ex)
            {
                db.createTableRequest("Exceptions", Arrays.asList("ExceptionDate", "Exception")).execute();
                List<Record> exceptions = db.queryRequest("Exceptions").all().execute();
                db.updateRequest("Exceptions")
                        .insert(new Record(Arrays.asList(
                                sNTPDate,
                                ex.getMessage())))
                        .execute();
            }
        } catch (Exception e)
        {
            Log("Exception in toDB: " + e.getMessage());
        }
        return false;

    }

    public static void main(String[] args) throws InterruptedException, IOException, GeneralSecurityException
    {
        Logger logger = new LogsFormatter().setLogging(Level.ALL);
        boolean HellFreezesOver = false;
        while (!HellFreezesOver)
        {
            String sNTPDate = getNTPDate();
            ArrayList<String> list;
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
                        String jsonString = GetForConnection(urlName);
                        if (!jsonString.equals(""))
                        {
                            boolean toDb = toDb(jsonString);
                        }
                    }
                }

            } catch (Exception e)
            {
                Log("Exception in main: " + e.getMessage());
            }
            
            Thread.sleep(DeviceReadingDelay);
        }
    }
}
