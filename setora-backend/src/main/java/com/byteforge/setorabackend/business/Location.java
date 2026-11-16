package com.byteforge.setorabackend.business;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Embeddable//no table of its own, will be embedded inside business
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class Location {
    @Column
    String locationName;//briefer than address. e.g. Piezisa

    @Column
    String address;//e.g Piezisa nr. 4
    @Column
    Double latitude;
    @Column
    Double longitude;
}
