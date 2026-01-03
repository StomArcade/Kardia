package net.bitbylogic.kardiavelocity.auth;

import net.bitbylogic.kardia.util.RedisKeys;
import net.bitbylogic.kardiavelocity.KardiaVelocity;
import org.redisson.api.RMap;
import org.redisson.api.RedissonClient;

import java.util.UUID;

public class KardiaAuth {

    private static RedissonClient redisson() {
        return KardiaVelocity.getInstance().getRedisClient().getRedisClient();
    }

    private static RMap<String, String> authMap() {
        return redisson().getMap(RedisKeys.AUTH);
    }

    public static AuthResult getAuth(UUID uuid) {
        try {
            boolean exists = authMap().containsKey(uuid.toString());
            return exists ? AuthResult.AUTHENTICATED : AuthResult.NOT_AUTHENTICATED;
        } catch (Exception e) {
            return AuthResult.NO_CONNECTION;
        }
    }

    public static int getAuthedPlayers() {
        try {
            return authMap().size();
        } catch (Exception e) {
            return 0;
        }
    }

    public static AuthResult auth(UUID uuid) {
        try {
            RMap<String, String> map = authMap();
            String key = uuid.toString();

            String existing = map.putIfAbsent(key, "AUTH");

            if (existing == null) {
                return AuthResult.AUTHENTICATED;
            } else {
                return AuthResult.AUTHENTICATION_EXISTS;
            }
        } catch (Exception e) {
            return AuthResult.NOT_AUTHENTICATED;
        }
    }

    public static void invalidate(UUID uuid) {
        try {
            authMap().remove(uuid.toString());
        } catch (Exception ignored) {}
    }

    public static void invalidateAll() {
        try {
            authMap().clear();
        } catch (Exception ignored) {
        }
    }

    public enum AuthResult {
        AUTHENTICATED,
        AUTHENTICATION_EXISTS,
        NOT_AUTHENTICATED,
        NO_CONNECTION
    }

}