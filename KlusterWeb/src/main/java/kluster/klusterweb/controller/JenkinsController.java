package kluster.klusterweb.controller;

import kluster.klusterweb.service.JenkinsService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;

@Controller
@RequiredArgsConstructor
public class JenkinsController {

    private final JenkinsService jenkinsService;

    @GetMapping("/jenkins/jobBuild")
    @ResponseStatus(HttpStatus.ACCEPTED)
    public ResponseEntity<String> postJobBuild() {
        return jenkinsService.buildJenkins();
    }

    @GetMapping("/jenkins/jobList")
    @ResponseStatus(HttpStatus.ACCEPTED)
    public ResponseEntity<String> getJobList() {
        return jenkinsService.getAllJenkins();
    }
}

