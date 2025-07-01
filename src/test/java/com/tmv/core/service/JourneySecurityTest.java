package com.tmv.core.service;
import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.*;

import com.tmv.core.exception.ResourceNotFoundException;
import com.tmv.core.model.Journey;
import com.tmv.core.model.User;
import com.tmv.core.persistence.JourneyRepository;
import org.mockito.MockedStatic;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.Authentication;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;

import java.util.Optional;

class JourneySecurityTest {

    private final JourneyRepository journeyRepository = mock(JourneyRepository.class);
    private final JourneySecurity journeySecurity = new JourneySecurity(journeyRepository);

    @Test
    public void testIsOwner_JourneyDoesNotExist_ShouldReturnFalse() {
        // Mock the repository to return an empty result
        when(journeyRepository.findById(5L)).thenReturn(Optional.empty());

        try (MockedStatic<SecurityContextHolder> mockedSecurityContext = mockStatic(SecurityContextHolder.class)) {
            // Mock the SecurityContext
            SecurityContext securityContext = mock(SecurityContext.class);
            mockedSecurityContext.when(SecurityContextHolder::getContext).thenReturn(securityContext);

            // Mock the Authentication
            Authentication authentication = mock(Authentication.class);
            when(securityContext.getAuthentication()).thenReturn(authentication);

            // Mock getPrincipal (returning the authenticated user)
            User currentUser = new User();
            currentUser.setId(1L); // Set the user ID for the authenticated user
            when(authentication.getPrincipal()).thenReturn(currentUser);

            // Act & Assert: Verify the exception is thrown
            ResourceNotFoundException exception = assertThrows(ResourceNotFoundException.class, () -> journeySecurity.isOwner(5L));
            assertEquals("Entity with ID 5 not found", exception.getMessage());

        }

    }

    @Test
    public void testIsOwner_JourneyExists_ButNotOwnedByCurrentUser_ShouldReturnFalse() {
        // Mock the repository to return a journey with a different owner
        Journey journey = new Journey();
        User owner = new User();
        owner.setId(2L); // Different user ID
        journey.setOwner(owner);
        when(journeyRepository.findById(5L)).thenReturn(Optional.of(journey));

        // Mock SecurityContextHolder authentication
        try (MockedStatic<SecurityContextHolder> mockedSecurityContext = mockStatic(SecurityContextHolder.class)) {
            // Mock the SecurityContext
            SecurityContext securityContext = mock(SecurityContext.class);
            mockedSecurityContext.when(SecurityContextHolder::getContext).thenReturn(securityContext);

            // Mock the Authentication
            Authentication authentication = mock(Authentication.class);
            when(securityContext.getAuthentication()).thenReturn(authentication);

            // Mock the authenticated user (principal)
            User currentUser = new User();
            currentUser.setId(1L); // Set the user's ID to 1L
            when(authentication.getPrincipal()).thenReturn(currentUser);

            // Test `isOwner` logic
            assertFalse(journeySecurity.isOwner(5L));
        }

    }

    @Test
    public void testIsOwner_JourneyExists_AndOwnedByCurrentUser_ShouldReturnTrue() {
        // Mock the repository to return a journey owned by the current user
        Journey journey = new Journey();
        User owner = new User();
        owner.setId(1L); // Same as the current user's ID
        journey.setOwner(owner);
        when(journeyRepository.findById(5L)).thenReturn(Optional.of(journey));

        // Mock SecurityContextHolder authentication
        try (MockedStatic<SecurityContextHolder> mockedSecurityContext = mockStatic(SecurityContextHolder.class)) {
            // Mock the SecurityContext
            SecurityContext securityContext = mock(SecurityContext.class);
            mockedSecurityContext.when(SecurityContextHolder::getContext).thenReturn(securityContext);

            // Mock the Authentication
            Authentication authentication = mock(Authentication.class);
            when(securityContext.getAuthentication()).thenReturn(authentication);

            // Mock getting the authenticated user (principal)
            User currentUser = new User();
            currentUser.setId(1L); // Current user's ID matches the journey owner
            when(authentication.getPrincipal()).thenReturn(currentUser);

            // Test `isOwner` logic
            assertTrue(journeySecurity.isOwner(5L)); // Example assertion based on matching ID logic
        }

    }

    @Test
    public void testIsOwner_NullJourneyId_ShouldReturnFalse() {
        // Test with null journey ID
        assertFalse(journeySecurity.isOwner(null));
    }

    @Test
    public void testIsOwner_NoAuthenticatedUser_ShouldReturnFalse() {
        // Mock SecurityContextHolder where no user is logged in
        try (MockedStatic<SecurityContextHolder> mockedSecurityContext = mockStatic(SecurityContextHolder.class)) {
            // Mock the SecurityContext
            SecurityContext securityContext = mock(SecurityContext.class);
            mockedSecurityContext.when(SecurityContextHolder::getContext).thenReturn(securityContext);

            // Simulate an unauthenticated context
            when(securityContext.getAuthentication()).thenReturn(null);

            // Test `isOwner` with a valid journey ID
            assertFalse(journeySecurity.isOwner(5L)); // Replace with the appropriate assertion
        }
    }
}