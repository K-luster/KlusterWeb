package kluster.klusterweb.controller;

import kluster.klusterweb.config.response.ResponseDto;
import kluster.klusterweb.config.response.ResponseUtil;
import kluster.klusterweb.dto.LoginDto;
import kluster.klusterweb.dto.MemberDto;
import kluster.klusterweb.dto.SchoolDto;
import kluster.klusterweb.service.MemberService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import java.io.IOException;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/members")
public class LoginController {

    private final MemberService memberService;

    @PostMapping("/login")
    public ResponseDto login(@RequestBody LoginDto.Request loginRequestDto) {
        return ResponseUtil.SUCCESS("로그인 성공하였습니다.", memberService.login(loginRequestDto.getEmail(), loginRequestDto.getPassword()));
    }

    @PostMapping("/signup")
    public ResponseDto signUp(@RequestBody MemberDto memberDto){
        return ResponseUtil.SUCCESS("회원가입 성공하였습니다.", memberService.signUp(memberDto.getEmail(), memberDto.getPassword(), memberDto.getGithubName(),memberDto.getGithubAccessToken(), memberDto.getDockerHubUsername(), memberDto.getDockerHubPassword(), memberDto.getSchoolAuthenticated()));
    }

    @PostMapping("/school-email")
    public ResponseDto schoolEmail(@RequestBody SchoolDto.codeCheck codeCheck) throws IOException {
        return ResponseUtil.SUCCESS("학교 인증 메일이 전송되었습니다", memberService.schoolEmail(codeCheck.getEmail()));
    }

    @PostMapping("/school-email-check")
    public ResponseDto schoolEmailCheck(@RequestBody SchoolDto.codeCheck codeCheck) throws IOException {
        return ResponseUtil.SUCCESS("학교 인증이 완료되었습니다.", memberService.schoolEmailCheck(codeCheck.getEmail(), codeCheck.getCode()));
    }

    @PostMapping("/school-email-resend")
    public ResponseDto schoolEmailResend(@RequestBody SchoolDto.codeCheck codeCheck) throws IOException {
        return ResponseUtil.SUCCESS("학교 인증 메일이 재전송되었습니다", memberService.schoolEmailReset(codeCheck.getEmail()));
    }
}
