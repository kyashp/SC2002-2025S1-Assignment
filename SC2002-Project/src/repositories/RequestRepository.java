package repositories;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import entity.domain.RegistrationRequest;
import entity.domain.WithdrawalRequest;
import entity.domain.enums.RequestStatus;



public class RequestRepository {
	// ===== Attributes =====
	
	private final List<RegistrationRequest> registrationRequests = new ArrayList<>();
	private final List<WithdrawalRequest> withdrawalRequests = new ArrayList<>();
	
	/**
     * Saves a request (either RegistrationRequest or WithdrawalRequest).
     * Automatically routes to the correct list.
     */
    public void save(Object req) {
        Objects.requireNonNull(req, "Request cannot be null");

        if (req instanceof RegistrationRequest regReq) {
            RegistrationRequest existing = findRegistrationById(regReq.getId());
            if (existing != null) registrationRequests.remove(existing);
            registrationRequests.add(regReq);
        } else if (req instanceof WithdrawalRequest withReq) {
            WithdrawalRequest existing = findWithdrawalById(withReq.getId());
            if (existing != null) withdrawalRequests.remove(existing);
            withdrawalRequests.add(withReq);
        } else {
            throw new IllegalArgumentException("Unsupported request type: " + req.getClass().getSimpleName());
        }
    }

    /**
     * Finds all pending WithdrawalRequests.
     */
    public List<WithdrawalRequest> findPendingWithdrawals() {
        List<WithdrawalRequest> pending = new ArrayList<>();
        for (WithdrawalRequest req : withdrawalRequests) {
            if (req.getStatus() == RequestStatus.PENDING) {
                pending.add(req);
            }
        }
        return pending;
    }

    /**
     * Finds all pending RegistrationRequests for Company Representatives.
     */
    public List<RegistrationRequest> findPendingRepRegistrations() {
        List<RegistrationRequest> pending = new ArrayList<>();
        for (RegistrationRequest req : registrationRequests) {
            if (req.getStatus() == RequestStatus.PENDING) {
                pending.add(req);
            }
        }
        return pending;
    }

    // ===== Helper Finders for internal use =====

    private RegistrationRequest findRegistrationById(String id) {
        if (id == null) return null;
        for (RegistrationRequest req : registrationRequests) {
            if (req.getId().equalsIgnoreCase(id)) return req;
        }
        return null;
    }

    private WithdrawalRequest findWithdrawalById(String id) {
        if (id == null) return null;
        for (WithdrawalRequest req : withdrawalRequests) {
            if (req.getId().equalsIgnoreCase(id)) return req;
        }
        return null;
    }

    /**
     * Clears all stored requests (useful for testing or resets).
     */
    public void clear() {
        registrationRequests.clear();
        withdrawalRequests.clear();
    }

}
