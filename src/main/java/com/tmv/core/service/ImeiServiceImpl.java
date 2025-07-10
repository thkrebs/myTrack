   package com.tmv.core.service;

   import com.tmv.core.exception.ConstraintViolationException;
   import com.tmv.core.exception.ResourceNotFoundException;
   import com.tmv.core.model.Imei;
   import com.tmv.core.model.User;
   import com.tmv.core.persistence.ImeiRepository;
   import lombok.extern.slf4j.Slf4j;
   import org.springframework.beans.factory.annotation.Autowired;
   import org.springframework.data.domain.Page;
   import org.springframework.data.domain.PageRequest;
   import org.springframework.data.domain.Pageable;
   import org.springframework.security.core.Authentication;
   import org.springframework.security.core.context.SecurityContextHolder;
   import org.springframework.stereotype.Service;

   import java.util.List;
   import java.util.Optional;

   @Slf4j
   @Service
   public class ImeiServiceImpl implements ImeiService {
    
       private final ImeiRepository imeiRepository;

       @Autowired
       public ImeiServiceImpl(ImeiRepository imeiRepository) {
           this.imeiRepository = imeiRepository;
       }

       @Override
       public boolean isActive(String imei) {
           var imeiRecord = imeiRepository.findByImei(imei);
           return imeiRecord != null && imeiRecord.isActive();
       }

       public Imei createNewImei(Imei newImei) {
           // Fetch the authenticated user
           Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
           if (authentication == null || !(authentication.getPrincipal() instanceof User)) {
               throw new IllegalStateException("No authenticated user found");
           }
           User authenticatedUser = (User) authentication.getPrincipal();

           // Assign the authenticated user as the owner of the IMEI
           newImei.setOwner(authenticatedUser);

           log.info("Creating a new Imei: {}", newImei);
           return imeiRepository.save(newImei);
       }

       public Optional<Imei> getImeiById(Long id) {
           return imeiRepository.findById(id);
       }

       public Page<Imei>allPaginated(int page, int size) {
           Pageable pageable = PageRequest.of(page, size);
           return imeiRepository.findAll(pageable);
       }

       public List<Imei> allForUser() {
           Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
           if (authentication == null || !(authentication.getPrincipal() instanceof User)) {
               throw new IllegalStateException("No authenticated user found");
           }
           // Set the owner of the journey to the authenticated user
           User authenticatedUser = (User) authentication.getPrincipal();
           return imeiRepository.findByOwner(authenticatedUser);
       }

       public Imei updateImei(Long id, Imei newImei) {
           return imeiRepository.findById(id)
                   .map(existingImei -> {
                       existingImei.setImei(newImei.getImei());
                       existingImei.setPhoneNumber(newImei.getPhoneNumber());
                       existingImei.setActive(newImei.isActive());
                       existingImei.setValidFrom(newImei.getValidFrom());
                       existingImei.setValidTo(newImei.getValidTo());
                       return imeiRepository.save(existingImei);
                   })
                   .orElseGet(() -> {
                       //newImei.setId(id);
                       return imeiRepository.save(newImei);
                   });
       }

       public void deleteImei(Long id) {
           if (!imeiRepository.existsById(id)) {
               throw new ResourceNotFoundException("Imei not found with id: " + id);
           }
           // Do not allow deletions if this imei is still used in journeys - set to inactive instead
           Optional<Imei> imeiOptional = imeiRepository.findById(id);
           imeiOptional.ifPresent(imei -> {
               if (!imei.getJourneys().isEmpty()) {
                   log.error("Cannot delete. Imei {} is still used in journeys",id);
                   throw new ConstraintViolationException("Cannot delete. Imei is still used in journeys");
               }
           });
           imeiRepository.deleteById(id);
       }
   }