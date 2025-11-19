package com.tmv.core.service;

import com.tmv.core.dto.UserFeaturesDTO;
import org.springframework.stereotype.Service;

@Service
public class UserFeatureService {

    private static final long DOMAIN_MASK = 0xFFFFL;
    private static final int PACKAGES_SHIFT = 48;
    private static final long PACKAGES_MASK = 0xFFFFL;

    // for documentation purposes
    public static final int BASE_PACKAGE = 0;
    public static final int PLUS_PACKAGE = 1;
    public static final int PREMIUM_PACKAGE = 2;

    public static final int DOMAIN_CAMPING = 0;

    /**
     * Decodes a 64-bit feature flag into a DTO.
     * - The domain is stored in the 2 least significant bytes (bits 0-15).
     * - The packages are stored in the 2 most significant bytes (bits 48-63).
     * @param features The 64-bit integer from the User entity.
     * @return A DTO with the decoded domain and packages.
     */
    public UserFeaturesDTO decodeFeatures(long features) {
        int domain = (int) (features & DOMAIN_MASK);
        int packages = (int) ((features >>> PACKAGES_SHIFT) & PACKAGES_MASK);
        return new UserFeaturesDTO(domain, packages);
    }

    /**
     * Encodes a domain and packages into a 64-bit feature flag.
     * @param domain The domain identifier (0-65535).
     * @param packages The package identifier (0-65535).
     * @return A 64-bit long representing the combined features.
     */
    public long encodeFeatures(int domain, int packages) {
        long features = 0L;
        features |= (domain & DOMAIN_MASK);
        features |= ((long) (packages & PACKAGES_MASK)) << PACKAGES_SHIFT;
        return features;
    }
}
