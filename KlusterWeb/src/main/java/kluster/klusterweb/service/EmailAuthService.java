package kluster.klusterweb.service;

import com.fasterxml.jackson.databind.util.JSONPObject;
import com.univcert.api.UnivCert;
import kluster.klusterweb.domain.Member;
import kluster.klusterweb.dto.EmailAuthResponseDto;
import kluster.klusterweb.repository.MemberRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Configurable;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;

@Service
@RequiredArgsConstructor
public class EmailAuthService {
//    @Value("${univcert.api-key}")
//    private static String apiKey;
    private static String apiKey = "0a034965-a167-4a65-8175-0a7bb1aaf5b0";
    private static MemberRepository memberRepository;
    @Transactional
    public static String certify(String email, String univName) throws IOException {
//        System.out.println(apiKey);
        return UnivCert.certify(apiKey, email, univName, true).get("success").toString();
    }

    public static String certifyCode(String email, String univName, Integer code) throws IOException {
        memberRepository.findByEmail(email).get().setSchoolAuthenticated("false");
        return UnivCert.certifyCode(apiKey, email, univName, code).get("success").toString();
    }
}
