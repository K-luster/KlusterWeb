package kluster.klusterweb.controller;

import kluster.klusterweb.config.response.ResponseDto;
import kluster.klusterweb.dto.Member.LoginDto;
import kluster.klusterweb.dto.Member.MemberDto;
import kluster.klusterweb.dto.Member.SchoolDto;
import kluster.klusterweb.service.Member.MemberService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import java.io.IOException;

@Slf4j
@RestController
@CrossOrigin(allowedHeaders = "*")
@RequiredArgsConstructor
@RequestMapping("/members")
public class LoginController {

    private final MemberService memberService;

    @PostMapping("/login")
    public ResponseDto<?> login(@RequestBody LoginDto.Request loginRequestDto) {
        return memberService.login(loginRequestDto.getEmail(), loginRequestDto.getPassword());
    }

    @PostMapping("/signup")
    public ResponseDto<?> signUp(@RequestBody MemberDto memberDto){
        return memberService.signUp(memberDto.getEmail(), memberDto.getPassword(), memberDto.getGithubAccessToken(), memberDto.getDockerHubUsername(), memberDto.getDockerHubPassword());
    }

    @PostMapping("/school-email")
    public ResponseDto<?> schoolEmail(@RequestBody SchoolDto.codeCheck codeCheck) throws IOException {
        return memberService.schoolEmail(codeCheck.getEmail());
    }

    @PostMapping("/school-email-check")
    public ResponseDto<?> schoolEmailCheck(@RequestBody SchoolDto.codeCheck codeCheck) throws IOException {
        return memberService.schoolEmailCheck(codeCheck.getEmail(), codeCheck.getCode());
    }

    @PostMapping("/school-email-resend")
    public ResponseDto<?> schoolEmailResend(@RequestBody SchoolDto.codeCheck codeCheck) throws IOException {
        return memberService.schoolEmailReset(codeCheck.getEmail());
    }
}
