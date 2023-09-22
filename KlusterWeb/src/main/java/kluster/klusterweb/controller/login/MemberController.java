package kluster.klusterweb.controller.login;

import com.univcert.api.UnivCert;
import kluster.klusterweb.dto.MemberDto;
import kluster.klusterweb.service.MemberService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

@Controller
@RequiredArgsConstructor
@RequestMapping("/members")
public class MemberController {

    private final MemberService memberService;

    @Value("${univcert.api.key}")
    String apiKey;

    @GetMapping("/add")
    public String signupForm(@ModelAttribute("member") MemberDto memberDto, Model model) {
        return "members/add";
    }

    @GetMapping("/schoolAuthenticated")
    public String schoolAuthenticatedForm(){
        return "members/schoolAuthenticated";
    }

    @PostMapping("/add")
    public String save(@Validated @ModelAttribute MemberDto memberDto, BindingResult bindingResult) {
        if (bindingResult.hasErrors()) {
            return "members/add"; // 에러가 있으면 다시 홈화면으로 보내버린다.
        }
        memberService.signUp(memberDto.getEmail(), memberDto.getPassword(), memberDto.getGithubAccessToken());
        return "members/schoolAuthenticated";
    }

    @PostMapping("/certify")
    public String certify(@RequestParam("schoolEmail") String email, Model model) throws IOException {
        System.out.println("schoolEmail = " + email);
        UnivCert.clear(apiKey);
        Map<String, Object> objectMap = UnivCert.certify(apiKey, email, "건국대학교", Boolean.FALSE);
        String success = objectMap.get("success").toString();
        if (success.equals("false")) {
            String message = objectMap.get("message").toString();
            throw new RuntimeException(message);
        } else {
            model.addAttribute("message", "코드 전송 완료");
            return "redirect:/members/schoolAuthenticated"; // 화면을 업데이트하는 페이지로 이동
        }
    }

    @PostMapping("/certifyCode")
    public String certifyCode(@RequestParam("email") String email, @ModelAttribute("code") int code, Model model) throws IOException {
        Map<String, Object> objectMap = UnivCert.certifyCode(apiKey, email, "건국대학교", code);
        if(objectMap.get("success").toString().equals("false")){
            String message = objectMap.get("message").toString();
            System.out.println("message = " + message);
            memberService.updateScoolAuthenticated(email, Boolean.TRUE);
            throw new RuntimeException(message);
        }
        model.addAttribute("success", true);
        return "redirect:/login";
    }

}
