package com.tmv.core.service;

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

    @Override
    protected Optional<Imei> findEntityById(Long imeiId) {
        return imeiRepository.findById(imeiId); // Fetch the IMEI by its ID
    }

    @Override
    protected User getOwnerFromEntity(Imei imei) {
        return imei.getOwner(); // Extract the owner of the IMEI
    }
}