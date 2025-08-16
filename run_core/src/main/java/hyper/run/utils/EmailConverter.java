package hyper.run.utils;

public class EmailConverter {

    private static final String KAKAO_EMAIL_SUFFIX = "@kakao.com";

    public static String toKaKaoEmail(final String userId){
        return userId+KAKAO_EMAIL_SUFFIX;
    }
}
