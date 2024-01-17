package com.contarbn;

import com.contarbn.model.reports.SchedaTecnicaAnalisiDataSource;
import org.junit.Test;

public class UtilsTest {

    @Test
    public void firstTest(){
        String input = "< 10^5 UFC/gr AND < 10^2 UFC/gr";
        //String input = "abcdef";

        System.out.println(SchedaTecnicaAnalisiDataSource.parseRisultato(input));
    }
}