   package com.tmv.core.service;

   import com.tmv.core.model.Imei;
   import com.tmv.core.model.Journey;
   import org.springframework.data.domain.Page;

   import java.util.List;
   import java.util.Optional;

   public interface ImeiService {
       boolean isActive(String imei);
       Imei createNewImei(Imei newImei);
       Optional<Imei> getImeiById(Long id);
       Page<Imei> allPaginated(int page, int size);
       Imei updateImei(Long id, Imei newImei);
       void deleteImei(Long id);
   }