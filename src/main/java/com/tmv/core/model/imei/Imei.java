   package com.tmv.core.model.imei;

   import jakarta.persistence.Entity;
   import jakarta.persistence.GeneratedValue;
   import jakarta.persistence.GenerationType;
   import jakarta.persistence.Id;
   import lombok.Getter;
   import lombok.NoArgsConstructor;
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

       protected Imei() {}

       public Imei(String imei, boolean active, Date validFrom, Date validTo) {
           this.imei = imei;
           this.active = active;
           this.validFrom = validFrom;
           this.validTo = validTo;
       }
   }