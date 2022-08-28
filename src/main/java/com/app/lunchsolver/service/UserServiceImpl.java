package com.app.lunchsolver.service;

import com.app.lunchsolver.dto.AddressDTO;
import com.app.lunchsolver.dto.KaKaoMapResponse;
import com.app.lunchsolver.util.BaseUtility;
import com.google.gson.Gson;
import lombok.extern.slf4j.Slf4j;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponents;
import org.springframework.web.util.UriComponentsBuilder;

@Service
@Slf4j
public class UserServiceImpl implements UserService {
    @Value("${kakaoAk.key}")
    private String authorization_key;
    @Autowired
    private RestTemplate restTemplate;
    @Autowired
    private BaseUtility utility;

    Gson gson = new Gson();

    @Override
    public AddressDTO getXY(String query) {
        String url = "https://dapi.kakao.com/v2/local/search/address.json";
        UriComponents uri = UriComponentsBuilder.newInstance()
                .fromHttpUrl(url)
                .queryParam("query", query)
                .build();

        HttpHeaders httpHeaders = utility.getDefaultHeader();
        httpHeaders.add("Authorization", String.format("KakaoAK %s", authorization_key));
        HttpEntity requestMessage = new HttpEntity(httpHeaders);
        ResponseEntity response = restTemplate.exchange(
                uri.toUriString(),
                HttpMethod.GET,
                requestMessage,
                String.class);

        JSONObject datas = new JSONObject(response.getBody().toString());
        JSONObject addressData = datas.getJSONArray("documents").getJSONObject(0).getJSONObject("address");
        double x = Math.round(Double.parseDouble(addressData.getString("x")) * 10000000) / 10000000.0;
        double y = Math.round(Double.parseDouble(addressData.getString("y")) * 10000000) / 10000000.0;
        return AddressDTO.builder()
                .x(x)
                .y(y)
                .build();
    }

    @Override
    public String getAddress(double x, double y) {
        String url = "https://dapi.kakao.com/v2/local/geo/coord2regioncode.json";

        UriComponents uri = UriComponentsBuilder.newInstance()
                .fromHttpUrl(url)
                .queryParam("x", x)
                .queryParam("y", y)
                .build();

        HttpHeaders httpHeaders = utility.getDefaultHeader();
        httpHeaders.add("Authorization", String.format("KakaoAK %s", authorization_key));

        // when
        HttpEntity requestMessage = new HttpEntity(httpHeaders);
        ResponseEntity response = restTemplate.exchange(
                uri.toUriString(),
                HttpMethod.GET,
                requestMessage,
                String.class);
        // then
        KaKaoMapResponse mapped_data = gson.fromJson(response.getBody().toString(), KaKaoMapResponse.class);
        String target = mapped_data.documents.get(0).address_name;
        return target;
    }

}
