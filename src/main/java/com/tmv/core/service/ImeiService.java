   package com.tmv.core.service;

   import com.tmv.core.model.Imei;

   import java.util.List;
   import java.util.Optional;

   public interface ImeiService {
       boolean isActive(String imei);
       Imei createNewImei(Imei newImei);
       Optional<Imei> getImeiById(Long id);
       List<Imei> allForUser();
       Imei updateImei(Long id, Imei newImei);
       void deleteImei(Long id);
   }