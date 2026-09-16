package com.tomatosystem.core.util;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PushbackInputStream;
import java.io.Reader;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import javax.servlet.http.HttpServletResponse;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;
import org.json.simple.JSONObject;

import com.cleopatra.protocol.builder.TSVResponseBuilder;
import com.cleopatra.protocol.data.DataResponse;

public class CSVSheetHandler {
    
    private List<String> header = new ArrayList<>();
    private static DataResponse dataResponse = null;
    static JSONObject colInfo = new JSONObject();
    
    public static CSVSheetHandler readCSV(File file, JSONObject poColInfo, HttpServletResponse response) throws Exception {
        CSVSheetHandler sheetHandler = new CSVSheetHandler();
        
        dataResponse = DataResponse.getInstance(TSVResponseBuilder.CONTENT_TYPE, response);
        colInfo = poColInfo;
        
        try (BufferedReader br =new BufferedReader(removeBOM(new FileInputStream(file)));
             CSVParser csvParser = new CSVParser(br, CSVFormat.DEFAULT.withFirstRecordAsHeader().withIgnoreEmptyLines().withAllowMissingColumnNames())) {
            
            sheetHandler.header = new ArrayList<>(csvParser.getHeaderNames());
            
            for (CSVRecord record : csvParser) {
                Map<String, String> rowMap = IntStream.range(0, sheetHandler.header.size())
                        .filter(i -> colInfo.containsKey(sheetHandler.header.get(i)))
                        .boxed()
                        .collect(Collectors.toMap(i -> sheetHandler.header.get(i).toString(), i -> record.get(i).trim()));
                
                try {
                    dataResponse.send(rowMap);
                    dataResponse.flush();
                } catch (IOException e) {
                    if (!(e.getCause() instanceof org.apache.catalina.connector.ClientAbortException)) {
                        e.printStackTrace();
                    }
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            dataResponse.finish();
        }
        
        return sheetHandler;
    }
    
    public static Reader removeBOM(InputStream in) throws IOException {

        PushbackInputStream pushbackInputStream = new PushbackInputStream(in, 3);
        byte[] bom = new byte[3];

        int read = pushbackInputStream.read(bom, 0, bom.length);

        if (read == 3) {
            // UTF-8 BOM
            if (bom[0] == (byte)0xEF && bom[1] == (byte)0xBB && bom[2] == (byte)0xBF) {
                return new InputStreamReader(pushbackInputStream, StandardCharsets.UTF_8);
            } else {
                pushbackInputStream.unread(bom, 0, read);
            }
        } else if (read != -1) {
            pushbackInputStream.unread(bom, 0, read);
        }

        // Excel CSV 기본 인코딩
        return new InputStreamReader(pushbackInputStream, Charset.forName("MS949"));
    }
}


