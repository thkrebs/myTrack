   package com.tmv.core.persistence;

   import com.tmv.core.model.Imei;
   import com.tmv.core.model.User;
   import org.springframework.data.domain.Page;
   import org.springframework.data.domain.Pageable;
   import org.springframework.data.jpa.repository.JpaRepository;
   import org.springframework.stereotype.Repository;

   import java.util.List;

   @Repository
   public interface ImeiRepository extends JpaRepository<Imei, Long> {
       Imei findByImei(String imei);
       Page<Imei> findAll(Pageable pageable);
       List<Imei> findByOwner(User user);
   }