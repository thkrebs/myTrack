package com.tmv.core.service;

import com.tmv.core.exception.ResourceNotFoundException;
import com.tmv.core.model.User;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.Optional;

public abstract class AbstractOwnershipSecurity<T> {

    /**
     * Checks if the currently authenticated user is the owner of the specified entity.
     * 
     * @param entityId The ID of the entity to check.
     * @return true if the authenticated user owns the entity, false otherwise.
     */
    public boolean isOwner(Long entityId) {
        if (entityId == null) {
            return false; // If no entity ID is provided, user cannot own it
        }

        Long currentUserId = getCurrentUserId();
        if (currentUserId == null) {
            return false; // If no user is authenticated, deny access
        }

        // Fetch the entity from the repository
        return findEntityById(entityId)
                .map(entity -> {
                    User owner = getOwnerFromEntity(entity);
                    if (owner == null || owner.getId() == null) {
                        throw new IllegalStateException("Entity owner or owner ID is null");
                    }
                    return owner.getId().equals(currentUserId); // Check ownership
                })
                .orElseThrow(() -> new ResourceNotFoundException("Entity with ID " + entityId + " not found")); // Throw exception if no entity is found
    }


    /**
     * Fetches the ID of the currently authenticated user.
     * 
     * @return The user ID or null if no user is authenticated.
     */
    protected Long getCurrentUserId() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        return (auth != null && auth.getPrincipal() instanceof User)
                ? ((User) auth.getPrincipal()).getId()
                : null;
    }

    /**
     * Finds the entity by its ID through the repository.
     * To be implemented by subclasses.
     * 
     * @param entityId The entity ID.
     * @return Optional containing the entity if found.
     */
    protected abstract Optional<T> findEntityById(Long entityId);

    /**
     * Extracts the owner of the given entity.
     * To be implemented by subclasses.
     * 
     * @param entity The entity.
     * @return The owner of the entity.
     */
    protected abstract User getOwnerFromEntity(T entity);
}