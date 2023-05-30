package kluster.klusterweb.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.client.RestTemplate;

@Controller
@RequiredArgsConstructor
public class JenkinsController {
    @Value("${jenkins.secret-key}")
    private String jenkinsKey;

    @GetMapping("/jenkins/jobBuild")
    @ResponseBody
    public String postJobBuild() {
        // Jenkins API URL
        String apiUrl = "http://kluster.iptime.org:9000/job/k8s_deploy/build";

        // Jenkins API credentials
        String username = "jenkins";
        String password = jenkinsKey;

        // Create RestTemplate with basic authentication
        RestTemplate restTemplate = new RestTemplate(new HttpComponentsClientHttpRequestFactory());
        restTemplate.getInterceptors().add((request, body, execution) -> {
            HttpHeaders headers = request.getHeaders();
            headers.setBasicAuth(username, password);
            return execution.execute(request, body);
        });

        // jenkins서버에 요청 보내기
        ResponseEntity<String> response = restTemplate.exchange(apiUrl, HttpMethod.POST, null, String.class);
        System.out.println("response = " + response);

        return response.toString();
    }

    @GetMapping("/jenkins/jobList")
    @ResponseBody
    public String getJobList() {
        // Jenkins API URL
        String apiUrl = "http://kluster.iptime.org:9000/job/k8s_deploy/api/json";

        // Jenkins API credentials
        String username = "jenkins";
        String password = jenkinsKey;

        // Create RestTemplate with basic authentication
        RestTemplate restTemplate = new RestTemplate(new HttpComponentsClientHttpRequestFactory());
        restTemplate.getInterceptors().add((request, body, execution) -> {
            HttpHeaders headers = request.getHeaders();
            headers.setBasicAuth(username, password);
            return execution.execute(request, body);
        });

        // jenkins서버에 요청 보내기
        ResponseEntity<String> response = restTemplate.exchange(apiUrl, HttpMethod.GET, null, String.class);
        System.out.println("response = " + response);

        return response.toString();
    }
}

