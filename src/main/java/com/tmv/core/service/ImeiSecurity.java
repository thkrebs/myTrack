package com.tmv.core.service;

import com.tmv.core.exception.ResourceNotFoundException;
import com.tmv.core.model.Imei;
import com.tmv.core.model.User;
import com.tmv.core.persistence.ImeiRepository;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component("imeiSecurity")
public class ImeiSecurity extends AbstractOwnershipSecurity<Imei> {

    private final ImeiRepository imeiRepository;

    public ImeiSecurity(ImeiRepository imeiRepository) {
        this.imeiRepository = imeiRepository;
    }

    public boolean isOwner(String imei) {
        if (imei == null) {
            return false; // If no entity ID is provided, user cannot own it
        }

        Long currentUserId = getCurrentUserId();
        if (currentUserId == null) {
            return false; // If no user is authenticated, deny access
        }

        // Fetch the entity from the repository
        return findEntityByImei(imei)
                .map(entity -> {
                    User owner = getOwnerFromEntity(entity);
                    if (owner == null || owner.getId() == null) {
                        throw new IllegalStateException("Entity owner or owner ID is null");
                    }
                    return owner.getId().equals(currentUserId); // Check ownership
                })
                .orElseThrow(() -> new ResourceNotFoundException("Entity with imei " + imei + " not found")); // Throw exception if no entity is found
    }
    @Override
    protected Optional<Imei> findEntityById(Long imeiId) {
        return imeiRepository.findById(imeiId); // Fetch the IMEI by its ID
    }

    protected Optional<Imei> findEntityByImei(String imei) {
        return Optional.ofNullable(imeiRepository.findByImei(imei));
    }

    @Override
    protected User getOwnerFromEntity(Imei imei) {
        return imei.getOwner(); // Extract the owner of the IMEI
    }
}