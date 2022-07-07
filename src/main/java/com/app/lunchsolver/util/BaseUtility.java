package com.app.lunchsolver.util;

import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.util.UriComponentsBuilder;

import java.util.Iterator;
import java.util.Map;


@Component
public class BaseUtility {
    private final String HOST = "pcmap.place.naver.com";
    private final String HOST_v2 = "https://pcmap-api.place.naver.com";
    private final String USER_AGENT = "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/103.0.0.0 Safari/537.36";
    private final String REFERER = "https://map.naver.com/";

    public HttpHeaders getDefaultHeader(){
        HttpHeaders httpHeaders = new HttpHeaders();
        MultiValueMap<String, String> headerValues = new LinkedMultiValueMap<>();
        headerValues.add(HttpHeaders.ACCEPT, "*/*");
        headerValues.add(HttpHeaders.HOST, HOST_v2);
        headerValues.add(HttpHeaders.USER_AGENT, USER_AGENT);
        headerValues.add("Referer", REFERER);
        headerValues.add("Connection","keep-alive");
        httpHeaders.addAll(headerValues);
        httpHeaders.setContentType(MediaType.APPLICATION_JSON);
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
}
