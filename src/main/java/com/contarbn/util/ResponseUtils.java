package com.contarbn.util;

import com.contarbn.model.beans.PageResponse;

import java.util.List;

public class ResponseUtils {

    public static PageResponse createPageResponse(Integer draw, Integer recordsTotal, Integer recordsFiltered, List<?> data){
        PageResponse pageResponse = new PageResponse();
        pageResponse.setDraw(draw);
        pageResponse.setRecordsTotal(recordsTotal);
        pageResponse.setRecordsFiltered(recordsFiltered);
        pageResponse.setData(data);
        return pageResponse;
    }
}
