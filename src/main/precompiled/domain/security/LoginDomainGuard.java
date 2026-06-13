package domain.security;

import koo.core.actor.AActor;
import koo.core.actor.ActorType;
import koo.core.exceptions.DomainLoginException;
import org.kissweb.json.JSONObject;

import org.kissweb.security.INTERNAL_CALL;

/**
 * This domain based class is intended to be extended with code that takes care of supporting the generic Login class with domain specific Login particulars.
 */
public class LoginDomainGuard {
    @INTERNAL_CALL
    public JSONObject login(AActor actor) {
        JSONObject domainData = new JSONObject();

        try {
            // 1. Actor type-specific processing
            if (ActorType.NATURAL == actor.getActorType()) {
                processNaturalActor(actor, domainData);
            } else if (ActorType.CORPORATE == actor.getActorType()) {
                processCorporateActor(actor, domainData);
            }

            // 2. Domain validation
            if (!validateDomainRequirements(actor)) {
                throw new DomainLoginException("Domain validation failed");
            }

            // 3. Apply policies
            applyDomainPolicies(actor);

            // 4. Log success
            logDomainLoginEvent(actor, "SUCCESS");

            return domainData;

        } catch (DomainLoginException e) {
            logDomainLoginEvent(actor, "FAILED: " + e.getMessage());
            throw e;
        }
    }

    private void logDomainLoginEvent(AActor actor, String success) {
    }

    private void processCorporateActor(AActor actor, JSONObject domainData) {
    }

    private void processNaturalActor(AActor actor, JSONObject domainData) {
    }

    public boolean validateDomainRequirements(AActor actor) {
        // Implement domain-specific business rules
        // Return false if login should be denied
        return true;
    }

    public void applyDomainPolicies(AActor actor) {
        // Apply domain-specific policies or setup
    }

}