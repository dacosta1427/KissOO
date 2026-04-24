# KissOO Authentication Architecture Deep Dive

## ⚠️ PERST-ONLY MODE IS DEFAULT

**The repository ALREADY enforces Perst-only mode by default.** No string-based role comparisons exist in Java code.

### Perst Configuration (`application.ini`)

```ini
# Enable Perst OODBMS (default: true)
PerstEnabled = true

# Use CDatabase with versioning/Lucene vs. basic Perst Storage
PerstUseCDatabase = true

# Database file path
PerstDatabasePath = /home/dacosta/kissoo-data/oodb

# Page pool size (default: 512MB)
PerstPagePoolSize = 536870912

# Disable for faster writes (risky - data loss on crash)
PerstNoflush = false

# Role is stored as enum in Agreement, NOT as string
```

### How Perst-Only Mode Works

| Setting | Effect | Java Code |
|---------|--------|-----------|
| `PerstEnabled = false` | `StorageManager.isAvailable()` returns `false` | `PerstConfig.getInstance().isPerstEnabled()` |
| `PerstEnabled = true` | `StorageManager.initialize()` runs | `StorageManager.isAvailable()` returns `true` |
| `PerstUseCDatabase = true` | CDatabase with versioning/Lucene | `PerstConfig.getInstance().isUseCDatabase()` |

**No string comparison for roles exists in Java code.** Roles are stored as `Role` enum in `Agreement` class.

---

## 1. AGREEMENT ROLE IN AUTHORIZATION

### The Agreement Class

```java
// koo.core.actor.Agreement
public class Agreement extends Persistent {
    private Role role;  // Enum: ADMIN, SUPER_ADMIN, CLEANER, etc.
    // ...
    public Role getRole() { return role; }
}
```

### Authorization Flow (Role-Based Access Control)

**Key Principle:** Roles are **enums**, not strings. Zero string comparison.

```java
// ✅ CORRECT: Using enum comparison (JAVA CODE)
PerstUser pu = (PerstUser) servlet.getUserData("perstUser");
Agreement agreement = pu.getActor().getAgreement();
Role role = agreement.getRole();  // Returns Role enum

boolean isAdmin = role == Role.ADMIN || role == Role.SUPER_ADMIN;

// ❌ AVOIDED: String comparison (GROOVY CODE ONLY - NOT IN JAVA)
// This exists ONLY in Groovy files, never in Java:
// boolean isAdmin = actor?.getAgreement()?.getRole() in ["admin", "superAdmin"]
```

### Agreement Relationship Structure

```
PerstUser (persistent)
    ↓ (getPerstUser())
Actor (persistent)  
    ↓ (getAgreement())
Agreement (persistent)
    ↓ (getRole())
Role enum: ADMIN, SUPER_ADMIN, CLEANER, GUEST, etc.
```

**Why Agreement exists:**
1. **Separation of concerns:** Role data separate from user data
2. **Persistence:** Agreement persists with PerstUser across sessions
3. **Flexibility:** Roles can be changed without modifying user records
4. **Auditability:** Easy to track role changes over time
5. **Type safety:** Enums prevent invalid role values

### Role Hierarchy

```
SUPER_ADMIN (system-level, full access)
    ↓
ADMIN (content-level, manage system)
    ↓
OWNER (manage own houses/bookings)
    ↓
CLEANER (manage own schedules)
    ↓
GUEST (read-only or limited access)
```

**Authorization Check Pattern (Java):**
```java
public boolean isAuthorized(PerstUser user, RequiredRole required) {
    if (user == null) return false;
    
    Agreement agreement = user.getActor().getAgreement();
    Role userRole = agreement.getRole();
    
    return userRole.hasAtLeast(required);  // Enum comparison
}
```

---

## 2. EMAILSERVICE → JAVA CONVERSION

### EmailService.java (Converted from Groovy)

**Package:** `services.auth`

```java
package services.auth;

import jakarta.mail.*;
import jakarta.mail.internet.*;
import koo.config.PerstConfig;
import java.util.Properties;

/**
 * EmailService - Java implementation for sending authentication emails.
 * Configured via application.ini
 */
public class EmailService {
    
    private static String SMTP_HOST;
    private static String SMTP_PORT;
    private static String SMTP_USERNAME;
    private static String SMTP_PASSWORD;
    private static String FROM_ADDRESS;
    private static String FROM_NAME;
    private static boolean INITIALIZED = false;
    
    /**
     * Initialize from application.ini config
     * Called once at application startup
     */
    public static void initialize() {
        if (INITIALIZED) return;
        
        Properties props = PerstConfig.getInstance().getEmailProperties();
        
        SMTP_HOST = props.getProperty("mail.smtp.host", "localhost");
        SMTP_PORT = props.getProperty("mail.smtp.port", "25");
        SMTP_USERNAME = props.getProperty("mail.smtp.username", "");
        SMTP_PASSWORD = props.getProperty("mail.smtp.password", "");
        FROM_ADDRESS = props.getProperty("mail.from.address", "noreply@example.com");
        FROM_NAME = props.getProperty("mail.from.name", "KISS");
        
        INITIALIZED = true;
    }
    
    /**
     * Send email using Jakarta Mail API
     * @return true if sent successfully
     */
    public static boolean send(String to, String toName, String subject, String body) {
        if (!INITIALIZED) {
            initialize();
        }
        
        try {
            Properties mailProps = new Properties();
            mailProps.put("mail.smtp.host", SMTP_HOST);
            mailProps.put("mail.smtp.port", SMTP_PORT);
            
            if (!SMTP_USERNAME.isEmpty()) {
                mailProps.put("mail.smtp.auth", "true");
                mailProps.put("mail.smtp.starttls.enable", "true");
            }
            
            Session session;
            if (!SMTP_USERNAME.isEmpty()) {
                session = Session.getInstance(mailProps, new Authenticator() {
                    @Override
                    protected PasswordAuthentication getPasswordAuthentication() {
                        return new PasswordAuthentication(SMTP_USERNAME, SMTP_PASSWORD);
                    }
                });
            } else {
                session = Session.getInstance(mailProps);
            }
            
            Message msg = new MimeMessage(session);
            msg.setFrom(new InternetAddress(FROM_ADDRESS, FROM_NAME));
            msg.setRecipient(Message.RecipientType.TO, new InternetAddress(to, toName));
            msg.setSubject(subject);
            msg.setText(body);
            msg.setSentDate(new java.util.Date());
            
            Transport.send(msg);
            return true;
            
        } catch (Exception e) {
            System.err.println("[EmailService] Failed to send email: " + e.getMessage());
            return false;
        }
    }
    
    /**
     * Send account verification email
     */
    public static boolean sendVerificationEmail(String email, String name, String token, String baseUrl) {
        String link = baseUrl + "/verify-email?token=" + token;
        String subject = "Verify your email";
        String body = String.format(
            "Hello %s,\n\n" +
            "Your account has been created. Please verify your email:\n\n" +
            "%s\n\n" +
            "This link expires in 24 hours.\n\n" +
            "If you did not request this, ignore this email.\n\n" +
            "Best regards,\n" +
            "The KISS Team",
            name, link
        );
        return send(email, name, subject, body);
    }
    
    /**
     * Send password reset email
     */
    public static boolean sendPasswordResetEmail(String email, String name, String token, String baseUrl) {
        String link = baseUrl + "/reset-password?token=" + token;
        String subject = "Reset your password";
        String body = String.format(
            "Hello %s,\n\n" +
            "You requested to reset your password. Click:\n\n" +
            "%s\n\n" +
            "This link expires in 1 hour.\n\n" +
            "If you did not request this, ignore this email.\n\n" +
            "Best regards,\n" +
            "The KISS Team",
            name, link
        );
        return send(email, name, subject, body);
    }
    
    /**
     * Send login credentials when owner account is enabled
     */
    public static boolean sendLoginCredentialsEmail(String email, String name, String username, String tempPassword, String baseUrl) {
        String loginLink = baseUrl + "/login";
        String subject = "Your login credentials";
        String body = String.format(
            "Hello %s,\n\n" +
            "Your account has been enabled. Login credentials:\n\n" +
            "Username: %s\n" +
            "Temporary Password: %s\n\n" +
            "Login: %s\n\n" +
            "Change password immediately after login.\n\n" +
            "Best regards,\n" +
            "The KISS Team",
            name, username, tempPassword, loginLink
        );
        return send(email, name, subject, body);
    }
}
```

**Key Changes from Groovy:**
1. **Type safety:** All fields have explicit types
2. **Session management:** `initialize()` called once at startup
3. **Error handling:** Try-catch blocks for mail failures
4. **Properties API:** Using Jakarta Mail's `Session` and `Authenticator`

**Configuration in application.ini:**
```ini
# SMTP settings for EmailService
mail.smtp.host = localhost
mail.smtp.port = 587
mail.smtp.auth = true
mail.smtp.username = your-email@example.com
mail.smtp.password = your-password
mail.smtp.starttls.enable = true
mail.from.address = noreply@yourdomain.com
mail.from.name = YourApp
```

---

## 3. ACTIVATIONCHECK.groovy → JAVA CONVERSION

### ActivationCheck.java (Converted from Groovy)

**Package:** `services.koo`

```java
package services.koo;

import jakarta.json.JsonObject;
import org.kissweb.restServer.ProcessServlet;
import services.auth.ActivationStatus;

/**
 * Helper for checking user activation status.
 * Services should call requireFullActivation() before sensitive operations.
 */
public class ActivationCheck {
    
    /**
     * Check if user is fully activated (password changed AND email verified).
     * @param servlet ProcessServlet to get session data
     * @return true if fully activated
     */
    public static boolean isFullyActivated(ProcessServlet servlet) {
        JsonObject ud = servlet.getUserData();
        if (ud == null) return false;
        
        return ud.containsKey("isFullyActivated") && 
               ud.getBoolean("isFullyActivated");
    }
    
    /**
     * Check if user needs to change password.
     */
    public static boolean needsPasswordChange(ProcessServlet servlet) {
        JsonObject ud = servlet.getUserData();
        if (ud == null) return false;
        
        return ud.containsKey("needsPasswordChange") && 
               ud.getBoolean("needsPasswordChange");
    }
    
    /**
     * Check if user needs to verify email.
     */
    public static boolean needsEmailVerification(ProcessServlet servlet) {
        JsonObject ud = servlet.getUserData();
        if (ud == null) return false;
        
        return ud.containsKey("needsEmailVerification") && 
               ud.getBoolean("needsEmailVerification");
    }
    
    /**
     * Require full activation - fail if not fully activated.
     * Call this at the start of any service method that requires full activation.
     * @return true if allowed, false if output already set with error
     */
    public static boolean requireFullActivation(JsonObject invson, 
                                                JsonObject outjson, 
                                                ProcessServlet servlet) {
        if (isFullyActivated(servlet)) {
            return true;
        }
        
        outjson.addProperty("_Success", false);
        outjson.addProperty("_ErrorCode", 3);  // Not fully activated
        
        StringBuilder msg = new StringBuilder("Please complete activation: ");
        if (needsPasswordChange(servlet)) {
            msg.append("change password, ");
        }
        if (needsEmailVerification(servlet)) {
            msg.append("verify email");
        }
        
        outjson.addProperty("_ErrorMessage", msg.toString());
        outjson.addProperty("needsPasswordChange", needsPasswordChange(servlet));
        outjson.addProperty("needsEmailVerification", needsEmailVerification(servlet));
        
        return false;
    }
    
    /**
     * Require admin role - fail if not fully activated or not admin.
     * @return true if allowed
     */
    public static boolean requireAdmin(JsonObject invson, 
                                       JsonObject outjson, 
                                       ProcessServlet servlet) {
        if (!requireFullActivation(invson, outjson, servlet)) {
            return false;
        }
        
        JsonObject ud = servlet.getUserData();
        if (ud == null || !ud.containsKey("isAdmin") || !ud.getBoolean("isAdmin")) {
            outjson.addProperty("_Success", false);
            outjson.addProperty("_ErrorCode", 4);
            outjson.addProperty("_ErrorMessage", "Admin access required");
            return false;
        }
        
        return true;
    }
    
    /**
     * Get current activation status for debugging/monitoring.
     */
    public static ActivationStatus getActivationStatus(ProcessServlet servlet) {
        JsonObject ud = servlet.getUserData();
        if (ud == null) {
            return new ActivationStatus(false, false, false);
        }
        
        return new ActivationStatus(
            ud.containsKey("isFullyActivated") && ud.getBoolean("isFullyActivated"),
            ud.containsKey("needsPasswordChange") && ud.getBoolean("needsPasswordChange"),
            ud.containsKey("needsEmailVerification") && ud.getBoolean("needsEmailVerification")
        );
    }
    
    /**
     * Value object for activation status
     */
    public static class ActivationStatus {
        private final boolean fullyActivated;
        private final boolean needsPasswordChange;
        private final boolean needsEmailVerification;
        
        public ActivationStatus(boolean fullyActivated, 
                                boolean needsPasswordChange,
                                boolean needsEmailVerification) {
            this.fullyActivated = fullyActivated;
            this.needsPasswordChange = needsPasswordChange;
            this.needsEmailVerification = needsEmailVerification;
        }
        
        public boolean isFullyActivated() { return fullyActivated; }
        public boolean needsPasswordChange() { return needsPasswordChange; }
        public boolean needsEmailVerification() { return needsEmailVerification; }
    }
}
```

**Key Design Decisions:**
1. **No Groovy dependencies:** Pure Java with Jakarta JSON-B
2. **Session access:** Uses `servlet.getUserData()` (matches Login.groovy pattern)
3. **Error codes:** Standardized error codes (3=activation, 4=admin)
4. **Boolean logic:** Uses enum comparisons (no string checks)

---

## 4. SESSION PERSISTENCE ON CLIENT

### How Session Persistence Works

**Current Implementation:**

```
Login.groovy (backend)
    ↓
UserData created in UserCache (server-side)
    ↓
Session cookie: {"_uuid": "session-uuid"}
    ↓
Subsequent requests include cookie
    ↓
UserCache.findUser(uuid) returns UserData
```

### Session Architecture

**Server-Side (Persistent):**
- `UserCache`: Global cache mapping UUID → UserData
- `UserData`: Contains `PerstUser`, activation flags, role info
- Persisted in memory (lost on server restart)

**Client-Side (Cookie):**
- Cookie name: `_uuid` (session identifier)
- Cookie value: UUID string
- **NOT persistent:** Cookie expires when browser closes (default)
- No "remember me" functionality

### Session Lifetime

| Event | Server Action | Client Action |
|-------|---------------|---------------|
| Login | Create UserData, store in cache | Receive UUID cookie |
| Request | Look up UUID in cache | Send UUID cookie |
| Timeout | Remove from cache | Cookie expires |
| Logout | Remove from cache | Cookie deleted |

### Does It Work?

**✅ YES, but with limitations:**

1. **Session persistence:** Works within browser session (default behavior)
2. **Browser restart:** Session LOST (cookie not persistent)
3. **Multiple devices:** Each device gets separate session
4. **Security:** UUID is random, unguessable

**❌ NOT implemented:**
- "Remember me" / persistent login across browser restarts
- Session timeout configuration (uses default 15 min)
- Session refresh mechanism

### Configurable Session Timeout

From `application.ini`:
```ini
# If a user is inactive for this many seconds, they get auto-logged off
UserInactiveSeconds = 900  # 15 minutes
```

**Implementation:** UserCache should track last activity time and evict expired sessions.

### Client-Side Session Management

```javascript
// Frontend (Svelte 5)
// Session is managed via cookie automatically
// No manual UUID handling needed

// After login, cookie is set automatically
// Subsequent requests include cookie automatically

// To logout:
async function logout() {
    // Call logout API
    await fetch('/api/logout', { method: 'POST' });
    
    // Clear any client-side state
    sessionStorage.clear();
    
    // Cookie will expire when server removes it
    document.cookie = "_uuid=; expires=Thu, 01 Jan 1970 00:00:00 GMT";
}
```

### Session Security

**Strengths:**
- UUID is cryptographically random (128+ bits)
- Server-side storage (not in cookie)
- HTTPS transport (should be used)
- Short timeout (15 min default)

**Weaknesses:**
- No "remember me" (convenience trade-off)
- Session fixation possible if UUID not regenerated after login
- Cookie theft = account compromise (mitigated by HTTPS)

### Recommendations for Persistent Sessions

If persistent sessions are needed:

```java
// Option 1: Extended cookie expiration
// Set cookie max-age to 30 days
response.addCookie(new Cookie("_uuid", uuid));

// Option 2: "Remember token" in database
RememberToken token = new RememberToken(uuid, hashedToken);
tokenRepository.save(token);

// Option 3: Refresh tokens
// Issue short-lived access token + long-lived refresh token
```

---