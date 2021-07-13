package gov.nysenate.openleg.common.util;

import java.math.BigInteger;
import java.security.SecureRandom;

public abstract class RandomUtils {

    private static final SecureRandom secureRandom = new SecureRandom();

    public static String getRandomString(int numCharacters) {
        return new BigInteger(numCharacters*5, secureRandom).toString(32);
    }
}
