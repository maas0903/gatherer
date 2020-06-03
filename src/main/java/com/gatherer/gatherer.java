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
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Scanner;
import com.melektro.tools.spreadsheetdb.*;
import java.io.IOException;
import java.io.InputStream;
import java.security.GeneralSecurityException;
import java.util.Arrays;
import java.util.List;

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

    private static int SHEET_ROW_LIMIT = 674;

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
                return result;

            } else
            {
                return "";
            }
        } catch (Exception e)
        {
            return "";
        }
    }

    private static boolean toDb(String jsonString) throws GeneralSecurityException, IOException
    {
        Gson gson = new Gson();
        Humid humid = gson.fromJson(jsonString, Humid.class);

        //SpreadsheetDatabase db = SpreadsheetDatabase.newPersonalDatabase(APPLICATION_NAME, CREDENTIALS_PROVIDER);
        SpreadsheetDatabase db = SpreadsheetDatabase.getPersonalDatabase(SPREADSHEET_ID, APPLICATION_NAME, CREDENTIALS_PROVIDER);
        db.createTableRequest("humid", Arrays.asList("DEBUG", "UtcTime", "DeviceCount", "Hostname", "IpAddress", "Mac Address", "Gpio", "Humidity", "Temperature", "Heat_Index")).execute();
        List<Record> records = db.queryRequest("humid").all().execute();
        int count = records.size();
        if (records.size() >= SHEET_ROW_LIMIT)
        {
            //delete first row
            db.deleteRequest("humid")
                .setRecords(Arrays.asList(records.get(0)))
                .execute();
        }

        db.updateRequest("humid")
                .insert(new Record(Arrays.asList(humid.getDEBUG(), humid.getUtcTime(), humid.getDeviceCount(), humid.getHostname(), humid.getIpAddress(),
                        humid.getMacAddress(), humid.getGpio(), humid.getHumidity(), humid.getTemperature(), humid.getHeatIndex())))
                .execute();
        records = db.queryRequest("humid").all().execute();

        Table memberTable = db.getTable("humid");

        System.out.println("Select all>");
        records.forEach((record) ->
        {
            System.out.printf("%s, %s, %d, %s, %s, %s, %d, %s, %s, %s\n",
                    record.getString(memberTable.getColumnIndex("DEBUG")),
                    record.getString(memberTable.getColumnIndex("UtcTime")),
                    record.getInt(memberTable.getColumnIndex("DeviceCount")),
                    record.getString(memberTable.getColumnIndex("Hostname")),
                    record.getString(memberTable.getColumnIndex("IpAddress")),
                    record.getString(memberTable.getColumnIndex("Mac Address")),
                    record.getInt(memberTable.getColumnIndex("Gpio")),
                    record.getFloat(memberTable.getColumnIndex("Humidity")),
                    record.getFloat(memberTable.getColumnIndex("Temperature")),
                    record.getFloat(memberTable.getColumnIndex("Heat_Index")));
        });
        return false;

    }

    public static void main(String[] args) throws InterruptedException, IOException, GeneralSecurityException
    {

        boolean HellFreezesOver = false;
        while (!HellFreezesOver)
        {
            Path filePath;

            ArrayList<String> list;
            try (Scanner s = new Scanner(new File("links.txt")))
            {
                list = new ArrayList<>();
                while (s.hasNext())
                {
                    list.add(s.next());
                }
            }

            String jsonString = "";
            for (String string : list)
            {
                jsonString = jsonString + GetForConnection(string);
            }
            
            if (!jsonString.equals(""))
            {
                boolean toDb = toDb(jsonString);
            }

            Thread.sleep(10000);
        }
    }
}
