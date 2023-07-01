package kluster.klusterweb.service;

import kluster.klusterweb.config.jwt.JwtTokenProvider;
import kluster.klusterweb.domain.Member;
import kluster.klusterweb.dto.JwtTokenInfo;
import kluster.klusterweb.dto.MemberDto;
import kluster.klusterweb.dto.MemberResponseDto;
import kluster.klusterweb.repository.MemberRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class MemberService {

    private final MemberRepository memberRepository;
    private final AuthenticationManagerBuilder authenticationManagerBuilder;
    private final JwtTokenProvider jwtTokenProvider;

    @Transactional
    public MemberResponseDto login(String email, String password) {
        UsernamePasswordAuthenticationToken authenticationToken = new UsernamePasswordAuthenticationToken(email, password);

        Authentication authentication = authenticationManagerBuilder.getObject().authenticate(authenticationToken);

        System.out.println("authentication = " + authentication);
        Optional<Member> member = memberRepository.findByEmail(email);
        JwtTokenInfo tokenInfo = jwtTokenProvider.generateToken(member.get());

        return new MemberResponseDto(email, member.get().getGithubAccessToken(), member.get().getSchoolAuthenticated(), tokenInfo.getAccessToken(), tokenInfo.getRefreshToken());
    }

    @Transactional
    public MemberResponseDto signup(MemberDto memberDto) {
        System.out.println("memberDto = " + memberDto);
        Member member = Member.builder()
                .email(memberDto.getEmail())
                .password(memberDto.getPassword())
                .githubAccessToken(memberDto.getGithubAccessToken())
                .schoolAuthenticated(memberDto.getSchoolAuthenticated())
                .build();
        Member savedMember = memberRepository.save(member);
        MemberResponseDto memberResponseDto = MemberResponseDto.builder()
                .email(savedMember.getEmail())
                .githubAccessToken(savedMember.getGithubAccessToken())
                .schoolAuthenticated(savedMember.getSchoolAuthenticated())
                .build();
        return memberResponseDto;
    }
}
