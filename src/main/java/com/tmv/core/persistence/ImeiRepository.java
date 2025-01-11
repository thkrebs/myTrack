   package com.tmv.core.persistence;

   import com.tmv.core.model.Imei;
   import org.springframework.data.jpa.repository.JpaRepository;
   import org.springframework.stereotype.Repository;

   @Repository
   public interface ImeiRepository extends JpaRepository<Imei, Long> {
       Imei findByImei(String imei);
   }