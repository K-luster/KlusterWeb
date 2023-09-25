package kluster.klusterweb.service;

import kluster.klusterweb.config.response.ResponseDto;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.nio.charset.Charset;
import java.util.Arrays;

@Service
public class ArgoService {

    @Value("${argocd.url}")
    private String argoApiUrl;

    @Value("${argocd.cookie}")
    private String cookieValue;

    public ResponseEntity<String> getAllApplications() {
        RestTemplate restTemplate = new RestTemplate();
        String apiUrl = argoApiUrl + "/applications";
        HttpHeaders headers = new HttpHeaders();
        headers.add("Cookie", "argocd.token=" + cookieValue);
        HttpEntity<String> requestEntity = new HttpEntity<>(null, headers);
        ResponseEntity<String> responseEntity = restTemplate.exchange(apiUrl
                ,HttpMethod.GET, requestEntity, String.class);
        return responseEntity;
    }
}
