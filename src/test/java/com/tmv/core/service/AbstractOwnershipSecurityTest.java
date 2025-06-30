package com.tmv.core.service;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
import java.util.Optional;

import com.tmv.core.util.TestUserFactory;
import lombok.Getter;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import com.tmv.core.model.User;

@ExtendWith(MockitoExtension.class)
public class AbstractOwnershipSecurityTest {

    private AbstractOwnershipSecurity<TestEntity> security;

    @Mock
    private Authentication authentication;

    @Mock
    private SecurityContext securityContext;

    @BeforeEach
    void setup() {
        SecurityContextHolder.setContext(securityContext);

        security = new AbstractOwnershipSecurity<TestEntity>() {
            @Override
            protected Optional<TestEntity> findEntityById(Long entityId) {
                User user = TestUserFactory.createTestUserWithAuthorities("testUser", "email", 1L, "ROLE_USER" );
                TestEntity entity = new TestEntity(entityId, user);
                return Optional.of(entity);
            }

            @Override
            protected User getOwnerFromEntity(TestEntity entity) {
                return entity.getOwner();
            }
        };
    }

    @Test
    void testIsOwner_ShouldReturnTrue_WhenUserIsOwner() {
        User currentUser = TestUserFactory.createTestUserWithAuthorities("testUser", "email", 1L, "ROLE_USER" );
        when(securityContext.getAuthentication()).thenReturn(authentication);
        when(authentication.getPrincipal()).thenReturn(currentUser);

        boolean result = security.isOwner(1L);
        assertTrue(result);
    }

    @Test
    void testIsOwner_ShouldReturnFalse_WhenUserIsNotOwner() {
        User currentUser = TestUserFactory.createTestUserWithAuthorities("otherUser", "email", 2L, "ROLE_USER" );
        when(securityContext.getAuthentication()).thenReturn(authentication);
        when(authentication.getPrincipal()).thenReturn(currentUser);

        boolean result = security.isOwner(1L);
        assertFalse(result);
    }

    /**
     * TestEntity class declared inside the test class.
     * Represents a mock entity with an ID and an owner.
     */
    @Getter
    private static class TestEntity {
        private final Long id;
        private final User owner;

        public TestEntity(Long id, User owner) {
            this.id = id;
            this.owner = owner;
        }
    }
}