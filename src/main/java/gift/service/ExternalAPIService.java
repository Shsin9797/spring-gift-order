package gift.service;

import gift.entity.Properties;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.RequestEntity;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.web.client.RestTemplate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


import java.net.URI;

@Service
public class ExternalAPIService {

    String kakaoOauthAuthorizeUrl= "https://kauth.kakao.com/oauth/authorize";
    String kakaoOauthTokenUrl = "https://kauth.kakao.com/oauth/token";
    Properties properties;
    private static final Logger logger = LoggerFactory.getLogger(ExternalAPIService.class);


    private final RestTemplate client = new RestTemplateBuilder().build();

    public void handleKakaoRedirect(String location) {
        
        URI uri = URI.create(location);
        var query = uri.getQuery();
        String[] params = query.split("&");

        String code = null;
        String state = null;

        for (String param : params) {
            String[] keyValue = param.split("=");
            if (keyValue[0].equals("code")) {
                code = keyValue[1];
            } else if (keyValue[0].equals("state")) {
                state = keyValue[1];
            }
        }

        if (code != null && state != null) {
            getKakaoToken(code);
        }
    }


    public void getKakaoAuthorize() {
        var headers = new HttpHeaders();
        headers.add(HttpHeaders.CONTENT_TYPE,MediaType.APPLICATION_FORM_URLENCODED_VALUE);
        var body = new LinkedMultiValueMap<String,String>();
        body.add("response_type","code");
        body.add("client_id",properties.getClientId());
        body.add("redirect_uri",properties.getRedirectUri());
        body.add("state","state");
        var request =new RequestEntity<>(body,headers, HttpMethod.GET, URI.create(kakaoOauthAuthorizeUrl));
        var response = client.exchange(request, String.class);

        if (response.getStatusCode() == HttpStatus.FOUND) {
            String location = response.getHeaders().getLocation().toString();
            handleKakaoRedirect(location);
        }
    }

    public void getKakaoToken(String code) {
        var headers = new HttpHeaders();
        headers.add(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_FORM_URLENCODED_VALUE);
        var body = new LinkedMultiValueMap<String,String>();
        body.add("grant_type","authorization_code");
        body.add("client_id",properties.getClientId());
        body.add("redirect_uri",properties.getRedirectUri());
        body.add("code",code);
        var request =new RequestEntity<>(body,headers, HttpMethod.POST, URI.create(kakaoOauthTokenUrl));
        var response = client.exchange(request, String.class);

        if (response.getStatusCode() == HttpStatus.OK) {
            logger.info("Token response: " + response.getBody());
        } else {
            logger.error("토큰 가져오기 실패, 상태코드: " + response.getStatusCode());
        }
    }
}
