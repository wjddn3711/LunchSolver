package com.app.lunchsolver;

import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


public class TempTest {

    @Test
    public void tt(){
        String x ="0:전체 등기부상태,12300:살아있는 등기,12301:회사정리절차개시결정,12331:본점전출,12341:해산,12342:합병해산,12343:청산종결,12344:기타폐쇄,12345:상호폐지,12347:해산간주,12348:청산종결간주,12349:파산,12350:분할합병해산,12351:분할해산,12352:조직변경해산,12353:보전관리,12354:회생절차,12355:종료";

        String[] res = x.split(",");
        List<String> names = new ArrayList<>();
        List<String> vals = new ArrayList<>();

        for (String re:res) {
            String[] r = re.split(":");
            names.add(r[0]);
            vals.add(r[1]);
        }
        System.out.println(res.length);

        for (int i = 0; i < names.size(); i++) {
            System.out.printf("[Code(\"%s\"),Description(\"%s\")]\n",names.get(i),vals.get(i));
            String[] sub = vals.get(i).split(",");
            if (sub.length>1) {
                System.out.printf(sub[0]);
            }
            else{
            System.out.printf(vals.get(i).replace(" ",""));
            }
            System.out.printf(",\n");
        }


    }
}
