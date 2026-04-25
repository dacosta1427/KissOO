/**
 * Login.groovy
 */
class Login {
    static Object login(Object db, String username, String password, Object outjson, Object servlet) {
        try {
            def mgrClass = Class.forName("koo.core.user.PerstUserManager")
            def authMethod = mgrClass.getMethod("authenticate", String.class, String.class)
            def user = authMethod.invoke(null, username, password)
            
            if (user == null) {
                return null
            }
            
            def actor = user.getActor()
            long ownerOid = actor ? actor.getOid() : 0
            
            outjson.put("userOid", user.getOid())
            outjson.put("username", user.getUsername())
            outjson.put("email", user.getEmail() ?: "")
            outjson.put("ownerOid", ownerOid)
            outjson.put("isAdmin", true)
            outjson.put("needsPasswordChange", false)
            outjson.put("needsEmailVerification", false)
            outjson.put("fullyActivated", true)
            
            def udClass = Class.forName("org.kissweb.restServer.UserCache")
            def newUserMethod = udClass.getMethod("newUser", String.class, String.class, Object.class)
            def ud = newUserMethod.invoke(null, username, password, null)
            
            return ud
            
        } catch (Exception e) {
            return null
        }
    }
}