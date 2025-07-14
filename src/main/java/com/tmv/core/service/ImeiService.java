   package com.tmv.core.service;

   import com.tmv.core.model.Imei;

   import java.util.List;
   import java.util.Optional;

   public interface ImeiService {
       boolean isActive(String imei);
       Imei createNewImei(Imei newImei);
       Optional<Imei> findById(Long id);
       List<Imei> allForUser();
       Imei updateImei(Long id, Imei newImei);
       void deleteImei(Long id);


       /**
        * Saves the given Imei entity to the database.
        *
        * @param imei the entity to be saved
        * @return the saved Imei entity
        */
       Imei save(Imei imei);

   }