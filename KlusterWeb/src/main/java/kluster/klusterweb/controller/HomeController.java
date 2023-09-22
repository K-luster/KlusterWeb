package kluster.klusterweb.controller;

import kluster.klusterweb.dto.MemberDto;
import kluster.klusterweb.session.SessionConst;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.SessionAttribute;

@Slf4j
@Controller
@RequiredArgsConstructor
public class HomeController {
    // 메인 화면
    @GetMapping("/")
    public String homeLoginSpring(@SessionAttribute(name = SessionConst.LOGIN_MEMBER, required = false) MemberDto loginMemberDto, Model model) {
        //세션에 회원 데이터가 있는 경우
        if (loginMemberDto == null) {
            return "home";
        }
        //세션이 유지되면 로그인으로 이동
        model.addAttribute("member", loginMemberDto);
        return "index";
    }
}