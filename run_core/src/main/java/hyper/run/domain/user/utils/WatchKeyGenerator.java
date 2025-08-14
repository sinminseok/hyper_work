package hyper.run.domain.user.utils;

import java.security.SecureRandom;

public class WatchKeyGenerator {
    private static final String LETTERS = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz";
    private static final String DIGITS = "0123456789";
    private static final String ALL_CHARACTERS = LETTERS + DIGITS;
    private static final int CODE_LENGTH = 6;
    private static final SecureRandom RANDOM = new SecureRandom();

    public static String generateRandomCode() {
        char[] code = new char[CODE_LENGTH];
        code[0] = LETTERS.charAt(RANDOM.nextInt(LETTERS.length()));
        code[1] = DIGITS.charAt(RANDOM.nextInt(DIGITS.length()));
        for (int i = 2; i < CODE_LENGTH; i++) {
            code[i] = ALL_CHARACTERS.charAt(RANDOM.nextInt(ALL_CHARACTERS.length()));
        }
        for (int i = code.length - 1; i > 0; i--) {
            int j = RANDOM.nextInt(i + 1);
            char temp = code[i];
            code[i] = code[j];
            code[j] = temp;
        }
        return new String(code);
    }
}
