
import com.google.api.services.sheets.v4.Sheets;
import com.google.api.services.sheets.v4.model.UpdateValuesResponse;
import com.google.api.services.sheets.v4.model.ValueRange;
import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.ObjectInputFilter.Config;
import java.security.GeneralSecurityException;
import java.util.Arrays;
import org.junit.BeforeClass;
import org.junit.Test;

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
/**
 *
 * @author marius
 */
public class GoogleSheetsLiveTest
{

    private static Sheets sheetsService;
    private static String SPREADSHEET_ID = "1ed_BKnMzWTik0AmWdpxYwFfKjwQBwNW-9uCzCylO-tY";// ...

    @BeforeClass
    public static void setup() throws GeneralSecurityException, IOException
    {
        //sheetsService = SheetsServiceUtil.getSheetsService();
    }

    @Test
    public void whenWriteSheet_thenReadSheetOk() throws IOException
    {
//        ValueRange body = new ValueRange()
//                .setValues(Arrays.asList(
//                        Arrays.asList("Expenses January"),
//                        Arrays.asList("books", "30"),
//                        Arrays.asList("pens", "10"),
//                        Arrays.asList("Expenses February"),
//                        Arrays.asList("clothes", "20"),
//                        Arrays.asList("shoes", "5")));
//        UpdateValuesResponse result = sheetsService.spreadsheets().values()
//                .update(SPREADSHEET_ID, "A1", body)
//                .setValueInputOption("RAW")
//                .execute();
    }

}
