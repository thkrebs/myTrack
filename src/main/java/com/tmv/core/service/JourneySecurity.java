package com.tmv.core.service;

import com.tmv.core.model.Journey;
import com.tmv.core.model.User;
import com.tmv.core.persistence.JourneyRepository;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component("journeySecurity")
public class JourneySecurity extends AbstractOwnershipSecurity<Journey> {

    private final JourneyRepository journeyRepository;

    public JourneySecurity(JourneyRepository journeyRepository) {
        this.journeyRepository = journeyRepository;
    }

    @Override
    protected Optional<Journey> findEntityById(Long journeyId) {
        return journeyRepository.findById(journeyId); // Fetch the Journey by its ID
    }

    @Override
    protected User getOwnerFromEntity(Journey journey) {
        return journey.getOwner(); // Extract the owner of the Journey
    }
}