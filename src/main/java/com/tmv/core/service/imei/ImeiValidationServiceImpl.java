   package com.tmv.core.service.imei;

   import com.tmv.core.persistence.imei.ImeiRepository;
   import org.springframework.beans.factory.annotation.Autowired;
   import org.springframework.stereotype.Service;

   @Service
   class ImeiValidationServiceImpl implements ImeiValidationService {
    
       private final ImeiRepository imeiRepository;

       @Autowired
       public ImeiValidationServiceImpl(ImeiRepository imeiRepository) {
           this.imeiRepository = imeiRepository;
       }

       @Override
       public boolean isActive(String imei) {
           return imeiRepository.findByImei(imei).isActive();
       }
   }