package com.app.lunchsolver.util;

import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.util.UriComponentsBuilder;

import java.nio.charset.Charset;
import java.util.Iterator;
import java.util.Map;


@Component
public class BaseUtility {
    private final String HOST = "pcmap.place.naver.com";
    private final String HOST_v2 = "https://pcmap-api.place.naver.com";
    private final String USER_AGENT = "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/103.0.0.0 Safari/537.36";
    private final String REFERER = "https://map.naver.com/";

    public String getBoundary(double x, double y){
        return String.format("%s;%s;%f;%f",
                x,
                y,
                x+0.0241399,
                y+0.0193742);
    }

    public HttpHeaders getDefaultHeader(){
        MediaType mediaType = new MediaType("application", "json", Charset.forName("UTF-8"));
        HttpHeaders httpHeaders = new HttpHeaders();
        MultiValueMap<String, String> headerValues = new LinkedMultiValueMap<>();
        headerValues.add(HttpHeaders.ACCEPT, "*/*");
        headerValues.add(HttpHeaders.HOST, HOST_v2);
        headerValues.add(HttpHeaders.USER_AGENT, USER_AGENT);
        headerValues.add("Referer", REFERER);
        headerValues.add("Connection","keep-alive");
        httpHeaders.addAll(headerValues);
        httpHeaders.setContentType(mediaType);
        return httpHeaders;
    }

    public static UriComponentsBuilder getUriComponents (Map<String,String> parameters, String url){
        // query param 으로 파라미터를 넘겼을때 제대로 인식하는것을 확인
        UriComponentsBuilder builder = UriComponentsBuilder.fromHttpUrl(url);
        for (Map.Entry<String, String> entry : parameters.entrySet()) {
            builder.queryParam(entry.getKey(), entry.getValue());
        }
        return builder;
    }

    public String uriParameterBuilder (Map<String,String> parameters, String url) {
        String result = url+"?";

        Iterator<Map.Entry<String, String>> iterator = parameters.entrySet().iterator();
        while(iterator.hasNext()){
            Map.Entry<String,String>entry=iterator.next();
            result+=entry.getKey()+"="+entry.getValue();
            if(iterator.hasNext()) result+="&"; // 만약 다음 파라미터가 존재한다면 & 추가
        }
        return result;
    }

    public Long stringToLongDistance(String distance){
        distance = distance.replace(",","000");
        distance = distance.replace("km","");
        distance = distance.replace(".","000");
        distance = distance.replace("m","");
        return Long.parseLong(distance);
    }

    public Long stringToLongSaveCnt(String saveCount){
        saveCount = saveCount.replace("~","");
        saveCount = saveCount.replace(",","");
        saveCount = saveCount.replace("+","");
        return Long.parseLong(saveCount);
    }

    public double distance(double lat1, double lon1, double lat2, double lon2, String unit) {

        double theta = lon1 - lon2;
        double dist = Math.sin(deg2rad(lat1)) * Math.sin(deg2rad(lat2)) + Math.cos(deg2rad(lat1)) * Math.cos(deg2rad(lat2)) * Math.cos(deg2rad(theta));

        dist = Math.acos(dist);
        dist = rad2deg(dist);
        dist = dist * 60 * 1.1515;

        if (unit == "kilometer") {
            dist = dist * 1.609344;
        } else if(unit == "meter"){
            dist = dist * 1609.344;
        }

        return (dist);
    }


    // This function converts decimal degrees to radians
    public double deg2rad(double deg) {
        return (deg * Math.PI / 180.0);
    }

    // This function converts radians to decimal degrees
    public double rad2deg(double rad) {
        return (rad * 180 / Math.PI);
    }
}
