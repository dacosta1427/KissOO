package domain;

import java.util.UUID;

/**
 * EmailVerification - Handles email verification for Actors.
 * 
 * When an Actor signs up:
 * 1. Generate verification token
 * 2. Send email with verification link
 * 3. Actor clicks link to verify
 * 4. Only then is the Actor fully active
 */
public class EmailVerification {
    
    private static final long VERIFICATION_EXPIRY_MS = 24 * 60 * 60 * 1000; // 24 hours
    
    private String token;
    private long createdAt;
    private long expiresAt;
    private boolean verified;
    private long verifiedAt;
    
    public EmailVerification() {
        this.token = UUID.randomUUID().toString();
        this.createdAt = System.currentTimeMillis();
        this.expiresAt = createdAt + VERIFICATION_EXPIRY_MS;
        this.verified = false;
    }
    
    public String getToken() {
        return token;
    }
    
    public long getCreatedAt() {
        return createdAt;
    }
    
    public long getExpiresAt() {
        return expiresAt;
    }
    
    public boolean isVerified() {
        return verified;
    }
    
    public long getVerifiedAt() {
        return verifiedAt;
    }
    
    /**
     * Mark this verification as verified
     */
    public void verify() {
        this.verified = true;
        this.verifiedAt = System.currentTimeMillis();
    }
    
    /**
     * Check if verification has expired
     */
    public boolean isExpired() {
        return System.currentTimeMillis() > expiresAt;
    }
    
    /**
     * Generate a new verification token (for resending)
     */
    public void regenerateToken() {
        this.token = UUID.randomUUID().toString();
        this.createdAt = System.currentTimeMillis();
        this.expiresAt = createdAt + VERIFICATION_EXPIRY_MS;
        this.verified = false;
        this.verifiedAt = 0;
    }
    
    /**
     * Get verification link URL
     */
    public String getVerificationLink(String baseUrl) {
        return baseUrl + "?token=" + token;
    }
}
