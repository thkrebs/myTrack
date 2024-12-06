   package com.tmv.core.persistence.imei;

   import com.tmv.core.model.imei.Imei;
   import org.springframework.data.jpa.repository.JpaRepository;
   import org.springframework.data.repository.CrudRepository;

   public interface ImeiRepository extends CrudRepository<Imei, Long> {
       Imei findByImei(String imei);
   }