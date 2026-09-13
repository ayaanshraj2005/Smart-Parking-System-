package com.parknow.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.parknow.dto.request.CreateReservationRequest;
import com.parknow.dto.request.LoginRequest;
import com.parknow.dto.request.ParkingLotRequest;
import com.parknow.dto.request.ParkingSlotRequest;
import com.parknow.dto.request.VehicleRequest;
import com.parknow.entity.Role;
import com.parknow.entity.User;
import com.parknow.entity.enums.RoleName;
import com.parknow.entity.enums.SlotStatus;
import com.parknow.entity.enums.VehicleType;
import com.parknow.repository.RoleRepository;
import com.parknow.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Set;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class ReservationServiceTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private RoleRepository roleRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private ParkingLotService lotService;

    @Autowired
    private ParkingSlotService slotService;

    @Autowired
    private VehicleService vehicleService;

    @Autowired
    private ReservationService reservationService;

    @Autowired
    private ObjectMapper objectMapper;

    private String user1Token;
    private String user2Token;
    private Long user1Id;
    private Long user2Id;

    private Long lotId;
    private Long vehicle1Id;
    private Long vehicle2Id;

    @BeforeEach
    void setUp() throws Exception {
        userRepository.deleteAll();

        Role userRole = roleRepository.findByName(RoleName.ROLE_USER)
                .orElseGet(() -> roleRepository.save(Role.builder().name(RoleName.ROLE_USER).build()));
        Role adminRole = roleRepository.findByName(RoleName.ROLE_ADMIN)
                .orElseGet(() -> roleRepository.save(Role.builder().name(RoleName.ROLE_ADMIN).build()));

        User user1 = userRepository.save(User.builder()
                .fullName("Test User 1")
                .email("user1.res@parknow.com")
                .passwordHash(passwordEncoder.encode("user1pass"))
                .roles(Set.of(userRole))
                .build());
        user1Id = user1.getId();

        User user2 = userRepository.save(User.builder()
                .fullName("Test User 2")
                .email("user2.res@parknow.com")
                .passwordHash(passwordEncoder.encode("user2pass"))
                .roles(Set.of(userRole))
                .build());
        user2Id = user2.getId();

        // Get JWT Tokens
        LoginRequest login1 = LoginRequest.builder().email("user1.res@parknow.com").password("user1pass").build();
        MvcResult res1 = mockMvc.perform(post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(login1)))
                .andExpect(status().isOk()).andReturn();
        user1Token = objectMapper.readTree(res1.getResponse().getContentAsString()).get("data").get("accessToken").asText();

        LoginRequest login2 = LoginRequest.builder().email("user2.res@parknow.com").password("user2pass").build();
        MvcResult res2 = mockMvc.perform(post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(login2)))
                .andExpect(status().isOk()).andReturn();
        user2Token = objectMapper.readTree(res2.getResponse().getContentAsString()).get("data").get("accessToken").asText();

        // Create Parking Lot
        var lotResp = lotService.createLot(ParkingLotRequest.builder()
                .name("Grand Plaza Garage")
                .address("100 Main St")
                .city("Metropolis")
                .totalCapacity(5)
                .build());
        lotId = lotResp.getId();

        // Add Vehicles
        var v1Resp = vehicleService.addVehicle(user1Id, VehicleRequest.builder().licensePlate("KA-01-AB-1234").vehicleType(VehicleType.CAR).build());
        vehicle1Id = v1Resp.getId();

        var v2Resp = vehicleService.addVehicle(user2Id, VehicleRequest.builder().licensePlate("KA-02-XY-9876").vehicleType(VehicleType.CAR).build());
        vehicle2Id = v2Resp.getId();
    }

    @Test
    @Transactional
    @DisplayName("1. Successful Reservation: Priority allocator selects Floor 1 slot A1 over Floor 2 slot A2")
    void testSuccessfulBookingWithPriorityAllocation() throws Exception {
        // Create 2 slots: A2 on floor 2, A1 on floor 1
        slotService.createSlot(ParkingSlotRequest.builder().lotId(lotId).slotNumber("A2").floorNumber(2).vehicleType(VehicleType.CAR).build());
        slotService.createSlot(ParkingSlotRequest.builder().lotId(lotId).slotNumber("A1").floorNumber(1).vehicleType(VehicleType.CAR).build());

        LocalDateTime start = LocalDateTime.now().plusHours(1);
        LocalDateTime end = start.plusHours(2);

        CreateReservationRequest req = CreateReservationRequest.builder()
                .lotId(lotId)
                .vehicleId(vehicle1Id)
                .vehicleType(VehicleType.CAR)
                .startTime(start)
                .endTime(end)
                .build();

        mockMvc.perform(post("/api/reservations")
                        .header("Authorization", "Bearer " + user1Token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.data.ticketCode").exists())
                .andExpect(jsonPath("$.data.slotNumber").value("A1")) // Priority: floor 1 selected first
                .andExpect(jsonPath("$.data.floorNumber").value(1))
                .andExpect(jsonPath("$.data.status").value("CONFIRMED"));
    }

    @Test
    @Transactional
    @DisplayName("2. Same-Slot Conflict / Double Booking: Second booking rejected when no other slots exist")
    void testSameSlotConflictDoubleBooking() throws Exception {
        // Create single slot A1
        slotService.createSlot(ParkingSlotRequest.builder().lotId(lotId).slotNumber("A1").floorNumber(1).vehicleType(VehicleType.CAR).build());

        LocalDateTime start = LocalDateTime.now().plusHours(1);
        LocalDateTime end = start.plusHours(3);

        // User 1 books slot A1
        CreateReservationRequest req1 = CreateReservationRequest.builder()
                .lotId(lotId)
                .vehicleId(vehicle1Id)
                .vehicleType(VehicleType.CAR)
                .startTime(start)
                .endTime(end)
                .build();

        reservationService.createReservation(user1Id, req1);

        // User 2 attempts booking same timeframe
        CreateReservationRequest req2 = CreateReservationRequest.builder()
                .lotId(lotId)
                .vehicleId(vehicle2Id)
                .vehicleType(VehicleType.CAR)
                .startTime(start.plusMinutes(30))
                .endTime(end.plusMinutes(30))
                .build();

        mockMvc.perform(post("/api/reservations")
                        .header("Authorization", "Bearer " + user2Token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req2)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.errorCode").value("DOUBLE_BOOKING"));
    }

    @Test
    @Transactional
    @DisplayName("3. Invalid Vehicle: Booking fails if vehicle does not belong to user or type mismatch")
    void testInvalidVehicleReservation() throws Exception {
        slotService.createSlot(ParkingSlotRequest.builder().lotId(lotId).slotNumber("A1").floorNumber(1).vehicleType(VehicleType.CAR).build());

        LocalDateTime start = LocalDateTime.now().plusHours(1);
        LocalDateTime end = start.plusHours(2);

        // User 1 attempts booking with User 2's vehicle
        CreateReservationRequest req = CreateReservationRequest.builder()
                .lotId(lotId)
                .vehicleId(vehicle2Id) // User 2's vehicle
                .vehicleType(VehicleType.CAR)
                .startTime(start)
                .endTime(end)
                .build();

        mockMvc.perform(post("/api/reservations")
                        .header("Authorization", "Bearer " + user1Token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errorCode").value("INVALID_OPERATION"));
    }

    @Test
    @Transactional
    @DisplayName("4. Maintenance Slot: Booking fails if slot is in MAINTENANCE or DISABLED")
    void testMaintenanceSlotRejection() throws Exception {
        var slotResp = slotService.createSlot(ParkingSlotRequest.builder().lotId(lotId).slotNumber("M1").floorNumber(1).vehicleType(VehicleType.CAR).build());
        slotService.updateSlotStatus(slotResp.getId(), SlotStatus.MAINTENANCE);

        LocalDateTime start = LocalDateTime.now().plusHours(1);
        LocalDateTime end = start.plusHours(2);

        CreateReservationRequest req = CreateReservationRequest.builder()
                .lotId(lotId)
                .vehicleId(vehicle1Id)
                .vehicleType(VehicleType.CAR)
                .startTime(start)
                .endTime(end)
                .build();

        mockMvc.perform(post("/api/reservations")
                        .header("Authorization", "Bearer " + user1Token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errorCode").value("SLOT_NOT_AVAILABLE"));
    }

    @Test
    @Transactional
    @DisplayName("5. Cancellation: Reservation cancelled successfully and slot released to AVAILABLE")
    void testReservationCancellation() throws Exception {
        slotService.createSlot(ParkingSlotRequest.builder().lotId(lotId).slotNumber("A1").floorNumber(1).vehicleType(VehicleType.CAR).build());

        LocalDateTime start = LocalDateTime.now().plusHours(1);
        LocalDateTime end = start.plusHours(2);

        CreateReservationRequest req = CreateReservationRequest.builder()
                .lotId(lotId)
                .vehicleId(vehicle1Id)
                .vehicleType(VehicleType.CAR)
                .startTime(start)
                .endTime(end)
                .build();

        var res = reservationService.createReservation(user1Id, req);

        // User 1 cancels reservation
        mockMvc.perform(delete("/api/reservations/" + res.getId())
                        .header("Authorization", "Bearer " + user1Token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.status").value("CANCELLED"));

        // Subsequent cancellation fails
        mockMvc.perform(delete("/api/reservations/" + res.getId())
                        .header("Authorization", "Bearer " + user1Token))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errorCode").value("INVALID_OPERATION"));
    }

    @Test
    @Transactional
    @DisplayName("6. Unauthorized Reservation Access: User 2 cannot access or cancel User 1's reservation")
    void testUnauthorizedReservationAccess() throws Exception {
        slotService.createSlot(ParkingSlotRequest.builder().lotId(lotId).slotNumber("A1").floorNumber(1).vehicleType(VehicleType.CAR).build());

        LocalDateTime start = LocalDateTime.now().plusHours(1);
        LocalDateTime end = start.plusHours(2);

        CreateReservationRequest req = CreateReservationRequest.builder()
                .lotId(lotId)
                .vehicleId(vehicle1Id)
                .vehicleType(VehicleType.CAR)
                .startTime(start)
                .endTime(end)
                .build();

        var res = reservationService.createReservation(user1Id, req);

        // User 2 attempts viewing User 1's reservation
        mockMvc.perform(get("/api/reservations/" + res.getId())
                        .header("Authorization", "Bearer " + user2Token))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.errorCode").value("UNAUTHORIZED"));

        // User 2 attempts cancelling User 1's reservation
        mockMvc.perform(delete("/api/reservations/" + res.getId())
                        .header("Authorization", "Bearer " + user2Token))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.errorCode").value("UNAUTHORIZED"));
    }

    @Test
    @DisplayName("7. Concurrent Booking Scenario: Two threads attempt simultaneous reservation for single slot")
    void testConcurrentBookingScenario() throws Exception {
        slotService.createSlot(ParkingSlotRequest.builder().lotId(lotId).slotNumber("C1").floorNumber(1).vehicleType(VehicleType.CAR).build());

        LocalDateTime start = LocalDateTime.now().plusHours(2);
        LocalDateTime end = start.plusHours(2);

        ExecutorService executor = Executors.newFixedThreadPool(2);
        CountDownLatch startLatch = new CountDownLatch(1);
        CountDownLatch finishLatch = new CountDownLatch(2);

        AtomicInteger successCount = new AtomicInteger(0);
        AtomicInteger conflictCount = new AtomicInteger(0);

        Runnable bookTask1 = () -> {
            try {
                startLatch.await();
                CreateReservationRequest req = CreateReservationRequest.builder()
                        .lotId(lotId)
                        .vehicleId(vehicle1Id)
                        .vehicleType(VehicleType.CAR)
                        .startTime(start)
                        .endTime(end)
                        .build();
                reservationService.createReservation(user1Id, req);
                successCount.incrementAndGet();
            } catch (Exception e) {
                conflictCount.incrementAndGet();
            } finally {
                finishLatch.countDown();
            }
        };

        Runnable bookTask2 = () -> {
            try {
                startLatch.await();
                CreateReservationRequest req = CreateReservationRequest.builder()
                        .lotId(lotId)
                        .vehicleId(vehicle2Id)
                        .vehicleType(VehicleType.CAR)
                        .startTime(start)
                        .endTime(end)
                        .build();
                reservationService.createReservation(user2Id, req);
                successCount.incrementAndGet();
            } catch (Exception e) {
                conflictCount.incrementAndGet();
            } finally {
                finishLatch.countDown();
            }
        };

        executor.submit(bookTask1);
        executor.submit(bookTask2);

        startLatch.countDown(); // Start both threads simultaneously
        finishLatch.await();    // Wait for both to finish
        executor.shutdown();

        assertEquals(1, successCount.get(), "Exactly ONE thread should successfully reserve the slot");
        assertEquals(1, conflictCount.get(), "Exactly ONE thread should fail with double booking conflict");
    }
}
