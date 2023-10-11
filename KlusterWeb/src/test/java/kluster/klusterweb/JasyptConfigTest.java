package kluster.klusterweb;

import org.assertj.core.api.Assertions;
import org.jasypt.encryption.pbe.StandardPBEStringEncryptor;
import org.junit.jupiter.api.Test;

class JasyptConfigTest {

    @Test
    void jasypt(){
        String DBurl = "jdbc:mysql://54.180.150.131:3306/kluster";
        String username = "root";
        String password = "rlaxogjs8312";
        String jwtSecret = "RlaXoVBsYt9V7zq57TejMnVUyzblYcfPQye08f7MGVA9XkHa";
        String githubAccessToken = "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";
        String univCertAPIKey = "7559e79a-1806-496c-adad-77394927b6b1";
        String kubernetesToken = "eyJhbGciOiJSUzI1NiIsImtpZCI6IkhTTHhOeTZad0RpbjB2d1dLSjJGRjBNeUctWF9KeEJyeHNVR0Z5dDdTLWMifQ.eyJpc3MiOiJrdWJlcm5ldGVzL3NlcnZpY2VhY2NvdW50Iiwia3ViZXJuZXRlcy5pby9zZXJ2aWNlYWNjb3VudC9uYW1lc3BhY2UiOiJrbHVzdGVyLW5hbWVzcGFjZSIsImt1YmVybmV0ZXMuaW8vc2VydmljZWFjY291bnQvc2VjcmV0Lm5hbWUiOiJwb2Qtdmlld2VyLXRva2VuLWxyNXg5Iiwia3ViZXJuZXRlcy5pby9zZXJ2aWNlYWNjb3VudC9zZXJ2aWNlLWFjY291bnQubmFtZSI6InBvZC12aWV3ZXIiLCJrdWJlcm5ldGVzLmlvL3NlcnZpY2VhY2NvdW50L3NlcnZpY2UtYWNjb3VudC51aWQiOiJhNzI4OGQ4Zi00YWIyLTQ5NzctYWYxYS04MjA2YmU2MTVlYjciLCJzdWIiOiJzeXN0ZW06c2VydmljZWFjY291bnQ6a2x1c3Rlci1uYW1lc3BhY2U6cG9kLXZpZXdlciJ9.dCckbK82C8y2ESqi6BVqzuWKip4OWtCXDH-UvYjx2pSzA3M51lPoVxVf85cb1kyzzuJw0Bt4F36-B6mEV8SJybJWIP5Wb-aY-ZwTMmUWRGTyGTbndJ8xcswT0yWOOUuOc8-ktYYh1K9B52DqNiNIzGls9gyKIKG78vjnrBXBBuiJKhGdnM4kW90Wl_3P9WwQznQCPJZrAjQyyYT5qbU39Y4_wOciYgUyxSzxqPNq9e-JP15WJwHNeFVfihalA_TmMZRCL1HuYXXbzVqbzx5wSW9Re7A6Lj0FXB_RbdwWOkkKTEf91C9g5Z7bsboKO7VXUapS6AyOkfMkkrKePFyHTQ";
        String argocdCookie = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJpc3MiOiJhcmdvY2QiLCJzdWIiOiJrbHVzdGVyOmFwaUtleSIsIm5iZiI6MTY5NzAwNTExMiwiaWF0IjoxNjk3MDA1MTEyLCJqdGkiOiJmN2RhMjVhMS1jOTNkLTRhMTMtYjZhMS1lMjMyYjkzYWY4Y2QifQ.AzOqp6KARKGvgiTXcMYO8w2mUBnc1g3xBE3-pzK2bE8";

        String encryptUrl = jasyptEncrypt(DBurl);
        String encryptUsername = jasyptEncrypt(username);
        String encryptPassword = jasyptEncrypt(password);
        String encryptJwtSecret = jasyptEncrypt(jwtSecret);
        String encryptGithubAccessToken = jasyptEncrypt(githubAccessToken);
        String encryptUnivCertAPIKey = jasyptEncrypt(univCertAPIKey);
        String encryptKubernetesToken = jasyptEncrypt(kubernetesToken);
        String encryptArgocdCookie = jasyptEncrypt(argocdCookie);

        System.out.println("encryptUrl : " + encryptUrl);
        System.out.println("encryptUsername : " + encryptUsername);
        System.out.println("encryptPassword : " + encryptPassword);
        System.out.println("encryptJwtSecret = " + encryptJwtSecret);
        System.out.println("encryptGithubAccessToken = " + encryptGithubAccessToken);
        System.out.println("encryptUnivCertAPIKey = " + encryptUnivCertAPIKey);
        System.out.println("encryptKubernetesToken = " + encryptKubernetesToken);
        System.out.println("encryptArgocdCookie = " + encryptArgocdCookie);

        Assertions.assertThat(DBurl).isEqualTo(jasyptDecryt(encryptUrl));
    }

    private String jasyptEncrypt(String input) {
        String key = "5678";
        StandardPBEStringEncryptor encryptor = new StandardPBEStringEncryptor();
        encryptor.setAlgorithm("PBEWithMD5AndDES");
        encryptor.setPassword(key);
        return encryptor.encrypt(input);
    }

    private String jasyptDecryt(String input){
        String key = "5678";
        StandardPBEStringEncryptor encryptor = new StandardPBEStringEncryptor();
        encryptor.setAlgorithm("PBEWithMD5AndDES");
        encryptor.setPassword(key);
        return encryptor.decrypt(input);
    }

}
