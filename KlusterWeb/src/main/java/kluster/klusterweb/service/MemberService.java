package kluster.klusterweb.service;

import kluster.klusterweb.domain.Member;
import kluster.klusterweb.dto.MemberDto;
import kluster.klusterweb.dto.MemberResponseDto;
import kluster.klusterweb.repository.MemberRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class MemberService {

    private final MemberRepository memberRepository;

    @Transactional
    public MemberDto login(String email, String password) {
        Optional<Member> member = memberRepository.findByEmail(email);
        MemberDto memberDto = MemberDto.builder()
                .email(member.get().getEmail())
                .password(member.get().getPassword())
                .build();
        if (memberDto.getPassword().equals(password)) {
            if(memberDto.getSchoolAuthenticated()){
                return memberDto;
            }else{
                throw new RuntimeException("학교 인증을 완료해주세요");
            }
        } else {
            return null;
        }

    }

    @Transactional
    public MemberDto signUp(String email, String password, String githubAccessToken) {
        Optional<Member> check = memberRepository.findByEmail(email);
        if (check.isPresent()) {
            throw new RuntimeException("이미 존재하는 이메일입니다");
        }
        Member member = Member.builder()
                .email(email)
                .password(password)
                .githubAccessToken(githubAccessToken)
                .schoolAuthenticated(Boolean.FALSE)
                .build();
        memberRepository.save(member);
        MemberDto memberDto = MemberDto.builder()
                .email(member.getEmail())
                .password(member.getPassword())
                .githubAccessToken(member.getGithubAccessToken())
                .schoolAuthenticated(member.getSchoolAuthenticated())
                .build();
        return memberDto;
    }

    @Transactional
    public MemberDto updateScoolAuthenticated(String email, Boolean schoolAuthenticated) {
        Optional<Member> byEmail = memberRepository.findByEmail(email);
        if (byEmail.isPresent()) {
            Member findMember = byEmail.get();
            Member member = Member.builder()
                    .email(findMember.getEmail())
                    .password(findMember.getPassword())
                    .githubAccessToken(findMember.getGithubAccessToken())
                    .schoolAuthenticated(schoolAuthenticated)
                    .build();
            memberRepository.save(member);
        }
        throw new RuntimeException("학교 인증에 실패하였습니다.");
    }
}
