package kluster.klusterweb.service;

import kluster.klusterweb.dto.ArgoApiDto.ArgoApiRequestDto;
import kluster.klusterweb.dto.ArgoApiDto.ArgoApiResponseDto;
import org.apache.http.conn.ssl.NoopHostnameVerifier;
import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.apache.http.conn.ssl.TrustStrategy;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import javax.net.ssl.SSLContext;
import java.security.KeyManagementException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.X509Certificate;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
@Service
public class ArgoService {

    @Value("${argocd.url}")
    private String argoApiUrl;

    @Value("${argocd.cookie}")
    private String cookieValue;

    public RestTemplate makeRestTemplate() throws KeyStoreException, KeyManagementException, NoSuchAlgorithmException {
        TrustStrategy acceptingTrustStrategy = (X509Certificate[] chain, String authType) -> true;
        SSLContext sslContext = org.apache.http.ssl.SSLContexts.custom()
                .loadTrustMaterial(null, acceptingTrustStrategy)
                .build();
        SSLConnectionSocketFactory csf = new SSLConnectionSocketFactory(sslContext, new NoopHostnameVerifier());
        CloseableHttpClient httpClient = HttpClients.custom()
                .setSSLSocketFactory(csf)
                .build();
        HttpComponentsClientHttpRequestFactory requestFactory = new HttpComponentsClientHttpRequestFactory();
        requestFactory.setHttpClient(httpClient);
        requestFactory.setConnectTimeout(3 * 1000);
        requestFactory.setReadTimeout(5 * 1000);
        return new RestTemplate(requestFactory);
    }


    public ResponseEntity<String> getAllApplications() throws KeyStoreException, NoSuchAlgorithmException, KeyManagementException {
        RestTemplate restTemplate = this.makeRestTemplate();
        String apiUrl = argoApiUrl + "/applications";
        HttpHeaders headers = new HttpHeaders();
        headers.add("Cookie", "argocd.token=" + cookieValue);
        HttpEntity<String> requestEntity = new HttpEntity<>(null, headers);
        ResponseEntity<String> responseEntity = restTemplate.exchange(apiUrl
                ,HttpMethod.GET, requestEntity, String.class);
        return responseEntity;
    }

    public ResponseEntity<ArgoApiResponseDto> makeApplications(ArgoApiRequestDto requestDto) throws KeyStoreException, NoSuchAlgorithmException, KeyManagementException {
        RestTemplate restTemplate = this.makeRestTemplate();
        String apiUrl = argoApiUrl + "/applications";
        HttpHeaders headers = new HttpHeaders();
        headers.add("Cookie", "argocd.token=" + cookieValue);
        HttpEntity<ArgoApiRequestDto> requestEntity = new HttpEntity<>(requestDto, headers);
        ResponseEntity<ArgoApiResponseDto> responseEntity = restTemplate.exchange(
                apiUrl,
                HttpMethod.POST,
                requestEntity,
                ArgoApiResponseDto.class
        );
        return responseEntity;
    }

}
