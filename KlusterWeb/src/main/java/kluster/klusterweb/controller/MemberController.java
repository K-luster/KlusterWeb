package kluster.klusterweb.controller;

import kluster.klusterweb.dto.MemberDto;
import kluster.klusterweb.dto.MemberResponseDto;
import kluster.klusterweb.service.MemberService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/members")
public class MemberController {

    private final MemberService memberService;

    @PostMapping("/login")
    @ResponseStatus(HttpStatus.ACCEPTED)
    public MemberDto login(@RequestBody MemberDto memberDto) {
        String email = memberDto.getEmail();
        String password = memberDto.getPassword();
        return memberService.login(email, password);
    }

    @PostMapping("/signup")
    @ResponseStatus(HttpStatus.CREATED)
    public MemberResponseDto signup(@RequestBody MemberDto memberDto) {
        return memberService.signup(memberDto);
    }
}
