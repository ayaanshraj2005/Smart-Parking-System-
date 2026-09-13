package com.parknow.util;

import com.parknow.entity.ParkingSlot;
import com.parknow.entity.enums.SlotStatus;
import com.parknow.entity.enums.VehicleType;
import org.springframework.stereotype.Component;

import java.util.Collection;
import java.util.Optional;
import java.util.PriorityQueue;

/**
 * DSA Intelligent Slot Allocator using Java PriorityQueue (Min-Heap).
 * Sorts available candidate slots by floor level (lowest floor first) and slot number.
 */
@Component
public class SlotPriorityAllocator {

    public Optional<ParkingSlot> allocateBestSlot(Collection<ParkingSlot> candidateSlots, VehicleType requestedType) {
        if (candidateSlots == null || candidateSlots.isEmpty()) {
            return Optional.empty();
        }

        // PriorityQueue heapifies slots using ParkingSlot's compareTo implementation
        PriorityQueue<ParkingSlot> minHeap = new PriorityQueue<>();

        for (ParkingSlot slot : candidateSlots) {
            if (slot.getStatus() != SlotStatus.MAINTENANCE && slot.getStatus() != SlotStatus.DISABLED && slot.getVehicleType() == requestedType) {
                minHeap.offer(slot);
            }
        }

        return Optional.ofNullable(minHeap.poll());
    }
}
