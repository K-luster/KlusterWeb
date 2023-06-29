package kluster.klusterweb.service;

import kluster.klusterweb.domain.Member;
import kluster.klusterweb.dto.MemberDto;
import kluster.klusterweb.dto.MemberResponseDto;
import kluster.klusterweb.repository.MemberRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class MemberService {

    private final MemberRepository memberRepository;

    @Transactional
    public MemberDto login(String email, String password) {
        return null;
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
                .jwtAccessToken("123")
                .jwtRefreshToken("1234")
                .build();
        return memberResponseDto;
    }
}
