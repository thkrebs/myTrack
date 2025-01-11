   package com.tmv.core.model;

   import jakarta.persistence.*;
   import lombok.Getter;
   import lombok.Setter;

   import java.util.Date;

   @Getter
   @Setter
   @Entity
   public class Imei {

       @Id
       @GeneratedValue(strategy = GenerationType.IDENTITY)
       private Long id;
       private String imei;
       private boolean active;
       private Date validFrom;
       private Date validTo;
       private String phoneNumber;

       protected Imei() {}

       public Imei(String imei, boolean active, Date validFrom, Date validTo, String phoneNumber    ) {
           this.imei = imei;
           this.active = active;
           this.validFrom = validFrom;
           this.validTo = validTo;
           this.phoneNumber = phoneNumber;
       }
   }