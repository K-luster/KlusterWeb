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

    private final String GET_POD_URL = "/api/v1/namespaces/";
    private final String GET_POD_DETAIL_URL = "/apis/metrics.k8s.io/v1beta1/namespaces/";

    public static final String RESOURCE_TYPE_POD = "pods";

    public ResponseEntity execute(HttpMethod httpMethod, String resourceType, String apiName) throws KeyStoreException, NoSuchAlgorithmException, KeyManagementException {
        String url;
        if (apiName.equals("pod_list")){
            url = API_SERVER + GET_POD_URL + API_NAMESPACE + "/" + resourceType;
        }
        else{
            url = API_SERVER + GET_POD_DETAIL_URL + API_NAMESPACE +"/"+resourceType;
        }

        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(API_TOKEN);
        headers.setContentType(MediaType.APPLICATION_JSON);

        HttpEntity httpEntity = new HttpEntity(headers);

        return restTemplateEKS.exchange(url, httpMethod, httpEntity, Map.class);
    }
}
