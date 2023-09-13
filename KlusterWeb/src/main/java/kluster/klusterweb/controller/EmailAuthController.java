package kluster.klusterweb.controller;

import kluster.klusterweb.dto.EmailAuthDto;
import kluster.klusterweb.dto.EmailAuthResponseDto;
import kluster.klusterweb.dto.MemberDto;
import kluster.klusterweb.dto.MemberResponseDto;
import kluster.klusterweb.service.EmailAuthService;
import kluster.klusterweb.service.MemberService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;

@RestController
@RequiredArgsConstructor
@RequestMapping("/auth")
public class EmailAuthController {
    private final EmailAuthService emailAuthService;

    @PostMapping("/certify")
    @ResponseStatus(HttpStatus.ACCEPTED)
    public String Certify(@RequestBody EmailAuthDto emailAuthDto) throws IOException {
        String email = emailAuthDto.getEmail();
        String univName = emailAuthDto.getUnivName();
        return EmailAuthService.certify(email, univName);
    }

    @PostMapping("/certifyCode")
    @ResponseStatus(HttpStatus.ACCEPTED)
    public String certifyCode(@RequestBody EmailAuthDto emailAuthDto) throws IOException {
        String email = emailAuthDto.getEmail();
        String univName = emailAuthDto.getUnivName();
        Integer code = emailAuthDto.getCode();
        return EmailAuthService.certifyCode(email, univName, code);
    }

}
