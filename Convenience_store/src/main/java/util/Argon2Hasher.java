package util;

import de.mkammerer.argon2.Argon2;
import de.mkammerer.argon2.Argon2Factory;

public class Argon2Hasher {

    private static final Argon2 argon2 = Argon2Factory.create(Argon2Factory.Argon2Types.ARGON2id);

    public static String hash(String rawPassword) {
        if (rawPassword == null || rawPassword.isEmpty()) {
            throw new IllegalArgumentException("Mật khẩu không được để trống");
        }
        return argon2.hash(2, 16384, 1, rawPassword.toCharArray());
    }

    public static boolean verify(String hash, String rawPassword) {
        if (hash == null || rawPassword == null)
            return false;
        return argon2.verify(hash, rawPassword.toCharArray());
    }
    public static void main(String[] args) {
        String pass = hash("hfshghsgfgshgf");
        String pass2 = hash("hfshghsgfgshgf");
        System.out.println(pass);
        System.out.println(pass2);
    }
}