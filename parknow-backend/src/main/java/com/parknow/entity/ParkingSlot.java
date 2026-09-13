package com.parknow.entity;

import com.parknow.entity.enums.SlotStatus;
import com.parknow.entity.enums.VehicleType;
import jakarta.persistence.*;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.*;

@Entity
@Table(name = "parking_slots", 
    uniqueConstraints = {
        @UniqueConstraint(name = "uq_lot_slot", columnNames = {"lot_id", "slot_number"})
    },
    indexes = {
        @Index(name = "idx_slot_lot_status", columnList = "lot_id, status"),
        @Index(name = "idx_slot_vehicle_type", columnList = "vehicle_type")
    }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ParkingSlot implements Comparable<ParkingSlot> {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "lot_id", nullable = false)
    private ParkingLot lot;

    @NotBlank
    @Size(max = 20)
    @Column(name = "slot_number", nullable = false, length = 20)
    private String slotNumber;

    @NotNull
    @Min(0)
    @Column(name = "floor_number", nullable = false)
    private Integer floorNumber;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(name = "vehicle_type", nullable = false, length = 30)
    private VehicleType vehicleType;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    @Builder.Default
    private SlotStatus status = SlotStatus.AVAILABLE;

    @Version
    @Column(name = "version")
    private Integer version;

    /**
     * DSA Priority Comparison:
     * Lower floor numbers first, then alphanumeric slot numbers.
     */
    @Override
    public int compareTo(ParkingSlot o) {
        int floorCompare = Integer.compare(this.floorNumber, o.floorNumber);
        if (floorCompare != 0) {
            return floorCompare;
        }
        return this.slotNumber.compareTo(o.slotNumber);
    }
}
