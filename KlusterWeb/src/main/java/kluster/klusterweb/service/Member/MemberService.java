package kluster.klusterweb.service.Member;

import kluster.klusterweb.config.jwt.JwtTokenProvider;
import kluster.klusterweb.config.response.ResponseDto;
import kluster.klusterweb.config.response.ResponseUtil;
import kluster.klusterweb.domain.Member;
import kluster.klusterweb.domain.SchoolEmail;
import kluster.klusterweb.dto.*;
import kluster.klusterweb.dto.Member.LoginDto;
import kluster.klusterweb.dto.Member.MemberDto;
import kluster.klusterweb.dto.Member.SchoolDto;
import kluster.klusterweb.repository.MemberRepository;
import kluster.klusterweb.repository.SchoolEmailRepository;
import kluster.klusterweb.service.EncryptService;
import kluster.klusterweb.service.GithubService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.mail.javamail.JavaMailSender;

import java.io.IOException;
import java.util.Map;
import java.util.Optional;
import java.util.Random;

@Service
@RequiredArgsConstructor
public class MemberService {

    private static final String SCHOOL_NAME = "건국대학교";
    private final MemberRepository memberRepository;
    private final JwtTokenProvider jwtTokenProvider;
    private final PasswordEncoder passwordEncoder;
    private final GithubService githubService;
    private final EncryptService encryptService;
    private final JavaMailSender javaMailSender;
    private final SchoolEmailRepository schoolEmailRepository;

    public ResponseDto<?> login(String email, String password) {
        Optional<Member> member = memberRepository.findByEmail(email);
        if (member.isPresent()) {
            if (passwordEncoder.matches(password, member.get().getPassword())) {
                JwtTokenInfo tokenInfo = jwtTokenProvider.generateToken(member.get());
                LoginDto.Response loginResponseDTO = new LoginDto.Response(email, member.get().getGithubName(), tokenInfo.getGrantType(), tokenInfo.getAccessToken());
                return ResponseUtil.SUCCESS("로그인 성공하였습니다.", loginResponseDTO);
            }
            throw new RuntimeException("비밀번호가 일치하지 않습니다");
        }
        throw new RuntimeException("해당하는 이메일이 존재하지 않습니다.");
    }

    @Transactional
    public ResponseDto<?> signUp(String email, String password, String githubAccessToken, String dockerhubUsername, String dockerhubPassword) {
        Optional<Member> check = memberRepository.findByEmail(email);
        if (check.isPresent()) {
            throw new RuntimeException("이미 존재하는 이메일입니다.");
        }

        String githubName = githubService.getUserIdFromAccessToken(githubAccessToken);
        Member member = Member.builder()
                .email(email)
                .password(passwordEncoder.encode(password))
                .githubName(githubName.toLowerCase())
                .githubAccessToken(encryptService.encrypt(githubAccessToken))
                .dockerHubUsername(encryptService.encrypt(dockerhubUsername))
                .dockerHubPassword(encryptService.encrypt(dockerhubPassword))
                .build();

        memberRepository.save(member);
        MemberDto memberDto = MemberDto.builder()
                .email(member.getEmail())
                .password(member.getPassword())
                .githubName(githubName.toLowerCase())
                .githubAccessToken(member.getGithubAccessToken())
                .dockerHubUsername(member.getDockerHubUsername())
                .dockerHubPassword(member.getDockerHubPassword())
                .build();
        return ResponseUtil.SUCCESS("회원가입에 성공하였습니다", memberDto);
    }

    public ResponseDto<?> schoolEmail(String email) {
        SimpleMailMessage simpleMailMessage = new SimpleMailMessage();
        try {
            simpleMailMessage.setTo(email);
            simpleMailMessage.setSubject("학교 인증 코드 번호입니다.");
            String verificationCode = generateVerificationCode();
            simpleMailMessage.setText(String.format("code : %s", verificationCode));
            if (schoolEmailRepository.findByEmail(email).isPresent()) {
                schoolEmailRepository.delete(schoolEmailRepository.findByEmail(email).get());
            }
            SchoolEmail schoolEmail = SchoolEmail.builder().email(email).code(verificationCode).build();
            schoolEmailRepository.save(schoolEmail);
            javaMailSender.send(simpleMailMessage);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        return ResponseUtil.SUCCESS("학교 메일이 전송되었습니다.", email);
    }

    public ResponseDto<?> schoolEmailCheck(String email, String code) {
        SchoolEmail bySchoolEmail = schoolEmailRepository.findByEmail(email).orElseThrow(() -> new RuntimeException("해당하는 이메일이 존재하지 않습니다."));
        System.out.println("bySchoolEmail = " + bySchoolEmail.getCode());
        if (bySchoolEmail.getCode().equals(code)) {
            return ResponseUtil.SUCCESS("학교 인증이 완료되었습니다.", email);
        }
        throw new RuntimeException("코드가 일치하지 않습니다. 코드를 다시 확인하세요");
    }

    private String generateVerificationCode() {
        Random random = new Random();
        return String.format("%04d", random.nextInt(10000));
    }
}
