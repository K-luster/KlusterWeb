package kluster.klusterweb.service.Member;

import com.univcert.api.UnivCert;
import kluster.klusterweb.config.jwt.JwtTokenProvider;
import kluster.klusterweb.config.response.ResponseDto;
import kluster.klusterweb.config.response.ResponseUtil;
import kluster.klusterweb.domain.Member;
import kluster.klusterweb.dto.*;
import kluster.klusterweb.dto.Member.LoginDto;
import kluster.klusterweb.dto.Member.MemberDto;
import kluster.klusterweb.dto.Member.SchoolDto;
import kluster.klusterweb.repository.MemberRepository;
import kluster.klusterweb.service.GithubService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
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
    private final JwtTokenProvider jwtTokenProvider;
    private final PasswordEncoder passwordEncoder;
    private final GithubService githubService;

    public ResponseDto<?> login(String email, String password) {
        Optional<Member> member = memberRepository.findByEmail(email);
        if (member.isPresent()) {
            if (passwordEncoder.matches(password, member.get().getPassword())) {
                JwtTokenInfo tokenInfo = jwtTokenProvider.generateToken(member.get());
                LoginDto.Response loginResponseDTO = new LoginDto.Response(email, member.get().getGithubName(), tokenInfo.getGrantType(), tokenInfo.getAccessToken());
                return ResponseUtil.SUCCESS("로그인 성공하였습니다.", loginResponseDTO);
            }
            return ResponseUtil.FAILURE("비밀번호가 일치하지 않습니다.", password);
        }
        return ResponseUtil.FAILURE("해당하는 이메일이 존재하지 않습니다.", email);
    }

    @Transactional
    public ResponseDto<?> signUp(String email, String password, String githubAccessToken, String dockerhubUsername, String dockerhubPassword) {
        Optional<Member> check = memberRepository.findByEmail(email);
        check.ifPresent(member -> ResponseUtil.FAILURE("이미 존재하는 이메일입니다", member.getEmail()));

        String githubName = githubService.getUserIdFromAccessToken(githubAccessToken);
        Member member = Member.builder()
                .email(email)
                .password(passwordEncoder.encode(password))
                .githubName(githubName)
                .githubAccessToken(githubAccessToken)
                .dockerHubUsername(dockerhubUsername)
                .dockerHubPassword(dockerhubPassword)
                .build();

        memberRepository.save(member);
        MemberDto memberDto = MemberDto.builder()
                .email(member.getEmail())
                .password(member.getPassword())
                .githubName(githubName)
                .githubAccessToken(member.getGithubAccessToken())
                .dockerHubUsername(member.getDockerHubUsername())
                .dockerHubPassword(member.getDockerHubPassword())
                .build();
        return ResponseUtil.SUCCESS("회원가입에 성공하였습니다", memberDto);
    }

    public ResponseDto<?> schoolEmail(String email) throws IOException {
        Map<String, Object> objectMap = UnivCert.certify(univCertApiKey, email, SCHOOL_NAME, true);
        String success = objectMap.get("success").toString();
        if (success.equals("false")) {
            String message = objectMap.get("message").toString();
            ResponseUtil.FAILURE(email + " 이메일의 학교 인증이 실패했습니다.", message);
        }
        return ResponseUtil.SUCCESS("학교 메일이 전송되었습니다.", email);
    }

    public ResponseDto<?> schoolEmailCheck(String email, int code) throws IOException {
        Map<String, Object> objectMap = UnivCert.certifyCode(univCertApiKey, email, SCHOOL_NAME, code);
        if (objectMap.get("success").toString().equals("false")) {
            String message = objectMap.get("message").toString();
            return ResponseUtil.FAILURE("학교 인증에 실패했습니다.", message);
        }
        return ResponseUtil.SUCCESS("학교 인증이 완료되었습니다.", SchoolDto.ResponseSuccess.builder()
                .univName(objectMap.get("univName").toString())
                .certified_email(objectMap.get("certified_email").toString())
                .certified_date(objectMap.get("certified_date").toString())
                .build());
    }

    public ResponseDto schoolEmailReset(String email) throws IOException {
        UnivCert.clear(univCertApiKey);
        return schoolEmail(email);
    }
}
