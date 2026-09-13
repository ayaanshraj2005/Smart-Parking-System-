package com.parknow.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.parknow.dto.request.CreateReservationRequest;
import com.parknow.dto.request.LoginRequest;
import com.parknow.dto.request.ParkingLotRequest;
import com.parknow.dto.request.ParkingSlotRequest;
import com.parknow.dto.request.SessionEntryRequest;
import com.parknow.dto.request.SessionExitRequest;
import com.parknow.dto.request.VehicleRequest;
import com.parknow.entity.Role;
import com.parknow.entity.User;
import com.parknow.entity.enums.RoleName;
import com.parknow.entity.enums.SlotStatus;
import com.parknow.entity.enums.VehicleType;
import com.parknow.repository.ParkingSlotRepository;
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

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class ParkingSessionServiceTest {

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
    private ParkingSessionService parkingSessionService;

    @Autowired
    private ParkingSlotRepository parkingSlotRepository;

    @Autowired
    private ObjectMapper objectMapper;

    private String userToken;
    private Long userId;

    private Long lotId;
    private Long vehicleId;
    private Long slotId;

    @BeforeEach
    void setUp() throws Exception {
        userRepository.deleteAll();

        Role userRole = roleRepository.findByName(RoleName.ROLE_USER)
                .orElseGet(() -> roleRepository.save(Role.builder().name(RoleName.ROLE_USER).build()));

        User user = userRepository.save(User.builder()
                .fullName("Session User")
                .email("session.user@parknow.com")
                .passwordHash(passwordEncoder.encode("sessionpass"))
                .roles(Set.of(userRole))
                .build());
        userId = user.getId();

        LoginRequest login = LoginRequest.builder().email("session.user@parknow.com").password("sessionpass").build();
        MvcResult res = mockMvc.perform(post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(login)))
                .andExpect(status().isOk()).andReturn();
        userToken = objectMapper.readTree(res.getResponse().getContentAsString()).get("data").get("accessToken").asText();

        var lotResp = lotService.createLot(ParkingLotRequest.builder()
                .name("Session Metro Hub")
                .address("200 City Center")
                .city("Metropolis")
                .totalCapacity(10)
                .build());
        lotId = lotResp.getId();

        var vResp = vehicleService.addVehicle(userId, VehicleRequest.builder().licensePlate("DL-01-AX-9999").vehicleType(VehicleType.CAR).build());
        vehicleId = vResp.getId();

        var sResp = slotService.createSlot(ParkingSlotRequest.builder().lotId(lotId).slotNumber("S1").floorNumber(1).vehicleType(VehicleType.CAR).build());
        slotId = sResp.getId();
    }

    @Test
    @Transactional
    @DisplayName("1. Gate Entry & Exit Lifecycle: Check-in, fee calculation, payment creation & slot release")
    void testGateEntryAndExitLifecycle() throws Exception {
        LocalDateTime start = LocalDateTime.now().plusMinutes(5);
        LocalDateTime end = start.plusHours(2);

        var res = reservationService.createReservation(userId, CreateReservationRequest.builder()
                .lotId(lotId)
                .vehicleId(vehicleId)
                .vehicleType(VehicleType.CAR)
                .startTime(start)
                .endTime(end)
                .build());

        // 1. Gate Entry Check-in
        SessionEntryRequest entryReq = SessionEntryRequest.builder()
                .ticketCode(res.getTicketCode())
                .entryTime(start)
                .build();

        MvcResult entryResult = mockMvc.perform(post("/api/sessions/entry")
                        .header("Authorization", "Bearer " + userToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(entryReq)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.data.status").value("IN_PROGRESS"))
                .andExpect(jsonPath("$.data.slotNumber").value("S1"))
                .andReturn();

        Long sessionId = objectMapper.readTree(entryResult.getResponse().getContentAsString()).get("data").get("id").asLong();

        // Verify slot is now OCCUPIED
        var occupiedSlot = parkingSlotRepository.findById(slotId).orElseThrow();
        assertEquals(SlotStatus.OCCUPIED, occupiedSlot.getStatus());

        // 2. Gate Exit Check-out
        SessionExitRequest exitReq = SessionExitRequest.builder()
                .exitTime(start.plusHours(2))
                .paymentMethod("CREDIT_CARD")
                .build();

        mockMvc.perform(post("/api/sessions/" + sessionId + "/exit")
                        .header("Authorization", "Bearer " + userToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(exitReq)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.status").value("COMPLETED"))
                .andExpect(jsonPath("$.data.totalFee").exists())
                .andExpect(jsonPath("$.data.paymentDetails.transactionId").exists())
                .andExpect(jsonPath("$.data.paymentDetails.paymentStatus").value("COMPLETED"));

        // Verify slot is now AVAILABLE again
        var releasedSlot = parkingSlotRepository.findById(slotId).orElseThrow();
        assertEquals(SlotStatus.AVAILABLE, releasedSlot.getStatus());
    }

    @Test
    @Transactional
    @DisplayName("2. Re-exit on already completed session fails with HTTP 400")
    void testReExitOnCompletedSessionFails() throws Exception {
        LocalDateTime start = LocalDateTime.now().plusMinutes(5);
        LocalDateTime end = start.plusHours(2);

        var res = reservationService.createReservation(userId, CreateReservationRequest.builder()
                .lotId(lotId)
                .vehicleId(vehicleId)
                .vehicleType(VehicleType.CAR)
                .startTime(start)
                .endTime(end)
                .build());

        var sessionResp = parkingSessionService.startSession(userId, SessionEntryRequest.builder().ticketCode(res.getTicketCode()).entryTime(start).build());
        parkingSessionService.endSession(userId, sessionResp.getId(), SessionExitRequest.builder().exitTime(end).build(), false);

        // Attempt second check-out
        mockMvc.perform(post("/api/sessions/" + sessionResp.getId() + "/exit")
                        .header("Authorization", "Bearer " + userToken))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errorCode").value("INVALID_OPERATION"));
    }

    @Test
    @Transactional
    @DisplayName("3. Entry check-in on cancelled reservation fails with HTTP 400")
    void testEntryOnCancelledReservationFails() throws Exception {
        LocalDateTime start = LocalDateTime.now().plusMinutes(5);
        LocalDateTime end = start.plusHours(2);

        var res = reservationService.createReservation(userId, CreateReservationRequest.builder()
                .lotId(lotId)
                .vehicleId(vehicleId)
                .vehicleType(VehicleType.CAR)
                .startTime(start)
                .endTime(end)
                .build());

        // Cancel reservation
        reservationService.cancelReservation(userId, res.getId(), false);

        SessionEntryRequest entryReq = SessionEntryRequest.builder().ticketCode(res.getTicketCode()).build();

        mockMvc.perform(post("/api/sessions/entry")
                        .header("Authorization", "Bearer " + userToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(entryReq)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errorCode").value("INVALID_OPERATION"));
    }

    @Test
    @Transactional
    @DisplayName("4. Exit on non-existent session fails with HTTP 404")
    void testExitNonExistentSessionFails() throws Exception {
        mockMvc.perform(post("/api/sessions/999999/exit")
                        .header("Authorization", "Bearer " + userToken))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.errorCode").value("RESOURCE_NOT_FOUND"));
    }
}
