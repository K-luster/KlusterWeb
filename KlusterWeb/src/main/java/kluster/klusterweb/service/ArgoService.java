package kluster.klusterweb.service;

import kluster.klusterweb.config.response.ResponseDto;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.ssl.SSLContextBuilder;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.nio.charset.Charset;
import java.security.KeyManagementException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;

@Service
public class ArgoService {

    @Value("${argocd.url}")
    private String argoApiUrl;

    @Value("${argocd.cookie}")
    private String cookieValue;

    public ResponseEntity<String> getAllApplications() throws NoSuchAlgorithmException, KeyStoreException, KeyManagementException {
        CloseableHttpClient httpClient = HttpClients.custom()
                .setSslcontext(SSLContextBuilder
                        .create()
                        .loadTrustMaterial((chain, authType) -> true)
                        .build())
                .build();
        HttpComponentsClientHttpRequestFactory factory = new HttpComponentsClientHttpRequestFactory(httpClient);
        RestTemplate restTemplate = new RestTemplate();
        String apiUrl = argoApiUrl + "/applications";
        HttpHeaders headers = new HttpHeaders();
        headers.add("Cookie", cookieValue);
        HttpEntity<String> requestEntity = new HttpEntity<>(null, headers);
        ResponseEntity<String> responseEntity = restTemplate.exchange(apiUrl
                ,HttpMethod.GET, requestEntity, String.class);
        return responseEntity;
    }
}
