package com.app.lunchsolver.service;

import com.app.lunchsolver.util.BaseUtility;

import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLEncoder;

@Service
public class RestaurantServiceImpl implements RestaurantService{

    @Value("${kakaoAk.key}")
    private String authorization_key;
    @Autowired
    private RestTemplate restTemplate;
    @Autowired
    private BaseUtility utility;

    private final String HOST = "pcmap.place.naver.com";

    public void getRestaurantData(String url){
        String _url = HOST +url;
        HttpHeaders httpHeaders = utility.getDefaultHeader();

        HttpEntity request = new HttpEntity(httpHeaders);
        ResponseEntity response = restTemplate.exchange(
                _url,
                HttpMethod.GET,
                request,
                String.class);
    }

    @Override
    public void getRestaurantDetail(String url) {
        String _url = HOST +url;
        HttpHeaders httpHeaders = utility.getDefaultHeader();

        HttpEntity request = new HttpEntity(httpHeaders);
        ResponseEntity response = restTemplate.exchange(
                _url,
                HttpMethod.GET,
                request,
                String.class);

    }

    @Override
    public void getXY(String query) {
            Float[] coordinate = new Float[2];
            String apiUrl = "https://dapi.kakao.com/v2/local/search/address.json";
            String jsonString = null;

            try {
                query = URLEncoder.encode(query, "UTF-8");

                String address = apiUrl + "?query=" + query;

                URL url = new URL(address);
                URLConnection conn = url.openConnection();
                conn.setRequestProperty("Authorization", authorization_key);

                BufferedReader rd = null;
                rd = new BufferedReader(new InputStreamReader(conn.getInputStream(), "UTF-8"));
                StringBuffer docJson = new StringBuffer();

                String line;

                while ((line = rd.readLine()) != null) {
                    docJson.append(line);
                }

                jsonString = docJson.toString();
                rd.close();

                JSONObject jsonObject = new JSONObject(jsonString);
                JSONArray documentsArray = jsonObject.getJSONArray("documents");
                JSONObject documentsObject = documentsArray.getJSONObject(0);

                String longtitude = documentsObject.getString("x");
                String latitude = documentsObject.getString("y");

                coordinate[0] = Float.parseFloat(longtitude);
                coordinate[1] = Float.parseFloat(latitude);

            } catch (MalformedURLException e) {
                e.printStackTrace();
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
    }


}
