package com.contarbn.util;

import com.contarbn.model.beans.SortOrder;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.CharSet;
import org.apache.tomcat.util.http.fileupload.FileUtils;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.InputStream;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.nio.charset.Charset;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

@Slf4j
public class Utils {

    public static boolean stringContainsOnlyCertainCharacters(String input){
        for (int i = 0; i < input.length(); i++) {
            char ch = input.charAt(i);
            if(!Constants.BARCODE_ALLOWED_CHARS.contains(ch)){
                return false;
            }
        }
        return true;
    }

    public static void removeFileOrDirectory(Path path){
        try{
            File file = path.toFile();
            if(file.isDirectory()){
                FileUtils.deleteDirectory(file);
            } else{
                file.delete();
            }

        } catch(Exception e){
            e.printStackTrace();
            log.error("Error deleting file '{}'", path.toAbsolutePath());
        }
    }

    public static BigDecimal roundPrice(BigDecimal price){
        return price.setScale(2, RoundingMode.HALF_UP);
    }

    public static BigDecimal roundQuantity(BigDecimal quantity){
        return quantity.setScale(2, RoundingMode.HALF_UP);
    }

    public static List<SortOrder> getSortOrders(Map<String, String> requestParams){
        List<SortOrder> sortOrders = new ArrayList<>();
        if(!requestParams.isEmpty()){
            Map<Integer, SortOrder> tempMap = new TreeMap<>();

            for(String key : requestParams.keySet()){
                if(key.startsWith("order[")){
                    Integer index = Integer.parseInt(key.replace("[column]", "").replace("[dir]","")
                            .replace("order","").replace("[","").replace("]",""));
                    SortOrder sortOrder;
                    if(tempMap.containsKey(index)){
                        sortOrder = tempMap.get(index);
                    } else {
                        sortOrder = new SortOrder();
                    }
                    if(key.contains("[column]")){
                        sortOrder.setColumnName(requestParams.get("columns["+requestParams.get(key)+"][name]"));
                    } else {
                        sortOrder.setDirection(requestParams.get(key));
                    }
                    tempMap.put(index, sortOrder);
                }
            }
            if(!tempMap.isEmpty()){
                for(Integer index : tempMap.keySet()){
                    sortOrders.add(tempMap.get(index));
                }
            }
        }
        return sortOrders;
    }

    public static String convertInputStream(InputStream inputStream) throws Exception{
        ByteArrayOutputStream result = new ByteArrayOutputStream();
        byte[] buffer = new byte[1024];
        for (int length; (length = inputStream.read(buffer)) != -1; ) {
            result.write(buffer, 0, length);
        }
        return result.toString("UTF-8");
    }

    public static float computePercentuale(Float quantita, Float quantitaTotale){
        float percentualeNotRounded = (quantita*100)/quantitaTotale;
        return (float)Math.round(percentualeNotRounded*100)/100;
    }

}
