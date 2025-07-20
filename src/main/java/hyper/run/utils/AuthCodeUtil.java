package hyper.run.utils;

import java.util.Random;

public class AuthCodeUtil {
    public static String generate6DigitCode() {
        Random random = new Random();
        int code = 1000 + random.nextInt(9000); // 4자리
        return String.valueOf(code);
    }
}