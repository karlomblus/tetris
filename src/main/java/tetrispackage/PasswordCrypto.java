package tetrispackage;

import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.spec.InvalidKeySpecException;
import java.util.Arrays;
import java.util.Base64;
import java.util.Random;


// baseerub sellel koodil: http://www.appsdeveloperblog.com/encrypt-user-password-example-java/
public class PasswordCrypto {

    private static byte[] hash(char[] password, byte[] salt) {
        PBEKeySpec spec = new PBEKeySpec(password, salt, 10000, 256);
        Arrays.fill(password, Character.MIN_VALUE);
        try {
            SecretKeyFactory skf = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA1");
            return skf.generateSecret(spec).getEncoded();
        } catch (NoSuchAlgorithmException | InvalidKeySpecException e) {
            throw new AssertionError("Error while hashing a password: " + e.getMessage(), e);
        } finally {
            spec.clearPassword();
        }
    }

    public static String generateSecurePassword(String password) {
        final String ALPHABET = "0123456789ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz";
        Random RANDOM = new SecureRandom();
        StringBuilder returnValue = new StringBuilder(22);
        for (int i = 0; i < 22; i++) {
            returnValue.append(ALPHABET.charAt(RANDOM.nextInt(ALPHABET.length())));
        }
        return generateSecurePassword(password,new String(returnValue));
    }
    private static String generateSecurePassword(String password, String salt) {
        String returnValue = null;
        byte[] securePassword = hash(password.toCharArray(), salt.getBytes());
        returnValue = Base64.getEncoder().encodeToString(securePassword);
        return salt + "." + returnValue;
    }

    public static boolean verifyUserPassword(String providedPassword, String securedPassword) {
        boolean returnValue = false;
        String[] pieces = securedPassword.split("\\.",2);
        String newSecurePassword = generateSecurePassword(providedPassword, pieces[0]);
        returnValue = newSecurePassword.equalsIgnoreCase(securedPassword);
        return returnValue;
    }

}
