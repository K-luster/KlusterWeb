package kluster.klusterweb.util;

import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.http.*;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;
import org.springframework.beans.factory.annotation.Value;

import javax.net.ssl.SSLContext;
import java.security.KeyManagementException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;
import java.util.Map;

@Component
@RequiredArgsConstructor
@Log4j2
public class RestApiUtil {

    @Value("${config.kubernetes.url}")
    private String API_SERVER;

    @Value("${config.kubernetes.token}")
    private String API_TOKEN;

    @Value("${config.kubernetes.namespace}")
    private String API_NAMESPACE;

    private final RestTemplate restTemplateEKS;

    private final String API_URL = "/api/v1/namespaces/";

    public static final String RESOURCE_TYPE_POD = "pods";

    public ResponseEntity execute(HttpMethod httpMethod, String resourceType) throws KeyStoreException, NoSuchAlgorithmException, KeyManagementException {
        String url = API_SERVER + API_URL + API_NAMESPACE +"/"+resourceType;

        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(API_TOKEN);
        headers.setContentType(MediaType.APPLICATION_JSON);

        HttpEntity httpEntity = new HttpEntity(headers);

        return restTemplateEKS.exchange(url, httpMethod, httpEntity, Map.class);
    }

}
