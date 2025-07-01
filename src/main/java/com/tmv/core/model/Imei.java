   package com.tmv.core.model;

   import com.fasterxml.jackson.annotation.JsonBackReference;
   import com.fasterxml.jackson.annotation.JsonFormat;
   import jakarta.persistence.*;
   import lombok.AllArgsConstructor;
   import lombok.Getter;
   import lombok.NoArgsConstructor;
   import lombok.Setter;

   import java.util.Date;
   import java.util.Set;

   @Getter
   @Setter
   @Entity
   @NoArgsConstructor
   @AllArgsConstructor
   public class Imei {

       @Id
       @GeneratedValue(strategy = GenerationType.AUTO)
       private Long id;
       private String imei;
       private boolean active;
       @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd")
       private Date validFrom;
       @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd")
       private Date validTo;
       private String phoneNumber;

       @ManyToMany(mappedBy = "trackedByImeis", fetch = FetchType.LAZY)
       private Set<Journey> journeys;

       // Add the relationship to User
       @ManyToOne(fetch = FetchType.LAZY)
       @JoinColumn(name = "userid", nullable = false)
       @JsonBackReference // Break circular reference during serialization
       private User owner; // The owner of this IMEI


       public Imei(String imei, boolean active, Date validFrom, Date validTo, String phoneNumber, User owner) {
           this.imei = imei;
           this.active = active;
           this.validFrom = validFrom;
           this.validTo = validTo;
           this.phoneNumber = phoneNumber;
           this.owner = owner;
       }
   }