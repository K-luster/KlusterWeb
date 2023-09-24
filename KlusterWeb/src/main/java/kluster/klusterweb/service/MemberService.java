package kluster.klusterweb.service;

import com.univcert.api.UnivCert;
import kluster.klusterweb.config.jwt.JwtTokenProvider;
import kluster.klusterweb.config.response.ResponseDto;
import kluster.klusterweb.config.response.ResponseUtil;
import kluster.klusterweb.domain.Member;
import kluster.klusterweb.dto.*;
import kluster.klusterweb.repository.MemberRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.util.Map;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class MemberService {

    @Value("${univcert.api.key}")
    String univCertApiKey;

    private static String SCHOOL_NAME = "건국대학교";

    private final MemberRepository memberRepository;
    private final AuthenticationManagerBuilder authenticationManagerBuilder;
    private final JwtTokenProvider jwtTokenProvider;
    private final PasswordEncoder passwordEncoder;

    public LoginDto.Response login(String email, String password) {
        Optional<Member> member = memberRepository.findByEmail(email);
        if (member.isPresent()) {
            if (passwordEncoder.matches(password, member.get().getPassword())) {
                JwtTokenInfo tokenInfo = jwtTokenProvider.generateToken(member.get());
                LoginDto.Response loginResponseDTO = new LoginDto.Response(email, tokenInfo.getGrantType(), tokenInfo.getAccessToken());
                return loginResponseDTO;
            }
            throw new RuntimeException("비밀번호가 일치하지 않습니다.");
        }
        throw new RuntimeException("해당하는 이메일이 존재하지 않습니다.");
    }

    @Transactional
    public MemberDto signUp(String email, String password, String githubName, String githubAccessToken, String dockerhubUsername, String dockerhubPassword, Boolean schoolAuthenticated) {
        Optional<Member> check = memberRepository.findByEmail(email);
        if (check.isPresent()) {
            throw new RuntimeException("이미 존재하는 이메일입니다");
        }
        Member member = Member.builder()
                .email(email)
                .password(passwordEncoder.encode(password))
                .githubName(githubName)
                .githubAccessToken(githubAccessToken)
                .dockerHubUsername(dockerhubUsername)
                .dockerHubPassword(dockerhubPassword)
                .schoolAuthenticated(schoolAuthenticated)
                .build();

        memberRepository.save(member);
        MemberDto memberDto = MemberDto.builder()
                .email(member.getEmail())
                .password(member.getPassword())
                .githubName(githubName)
                .githubAccessToken(member.getGithubAccessToken())
                .dockerHubUsername(member.getDockerHubUsername())
                .dockerHubPassword(member.getDockerHubPassword())
                .schoolAuthenticated(member.getSchoolAuthenticated())
                .build();
        return memberDto;
    }

    public String schoolEmail(String email) throws IOException {
        Map<String, Object> objectMap = UnivCert.certify(univCertApiKey, email, SCHOOL_NAME, true);
        String success = objectMap.get("success").toString();
        if (success.equals("false")) {
            String message = objectMap.get("message").toString();
            throw new RuntimeException(message);
        }
        return email;
    }

    public ResponseDto schoolEmailCheck(String email, int code) throws IOException {
        Map<String, Object> objectMap = UnivCert.certifyCode(univCertApiKey, email, SCHOOL_NAME, code);
        if (objectMap.get("success").toString().equals("false")) {
            String message = objectMap.get("message").toString();
            // throw new RuntimeException(message);
            return ResponseUtil.FAILURE("학교 인증에 실패했습니다.", message);
        }
        return ResponseUtil.SUCCESS("학교 인증이 완료되었습니다.", SchoolDto.ResponseSuccess.builder()
                .univName(objectMap.get("univName").toString())
                .certified_email(objectMap.get("certified_email").toString())
                .certified_date(objectMap.get("certified_date").toString())
                .build());
    }

    public String schoolEmailReset(String email) throws IOException {
        UnivCert.clear(univCertApiKey);
        return schoolEmail(email);
    }
}
