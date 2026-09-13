package com.parknow.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.parknow.dto.request.CreateReservationRequest;
import com.parknow.dto.request.LoginRequest;
import com.parknow.dto.request.ParkingLotRequest;
import com.parknow.dto.request.ParkingSlotRequest;
import com.parknow.dto.request.RegisterRequest;
import com.parknow.dto.request.SessionEntryRequest;
import com.parknow.dto.request.VehicleRequest;
import com.parknow.entity.ParkingSession;
import com.parknow.entity.Role;
import com.parknow.entity.User;
import com.parknow.entity.enums.RoleName;
import com.parknow.entity.enums.SlotStatus;
import com.parknow.entity.enums.VehicleType;
import com.parknow.exception.InvalidOperationException;
import com.parknow.exception.ResourceNotFoundException;
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

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
public class QualityPassEdgeCasesTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private RoleRepository roleRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private AuthService authService;

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
    private BillingService billingService;

    @Autowired
    private com.parknow.repository.PaymentRepository paymentRepository;

    @Autowired
    private com.parknow.repository.ParkingSessionRepository parkingSessionRepository;

    @Autowired
    private com.parknow.repository.ReservationRepository reservationRepository;

    @Autowired
    private com.parknow.repository.ParkingSlotRepository parkingSlotRepository;

    @Autowired
    private com.parknow.repository.ParkingLotRepository parkingLotRepository;

    @Autowired
    private com.parknow.repository.VehicleRepository vehicleRepository;

    @Autowired
    private ObjectMapper objectMapper;

    private String userToken;
    private String adminToken;
    private Long userId;
    private Long lotId;
    private Long vehicleId;

    @BeforeEach
    void setUp() throws Exception {
        paymentRepository.deleteAll();
        parkingSessionRepository.deleteAll();
        reservationRepository.deleteAll();
        vehicleRepository.deleteAll();
        parkingSlotRepository.deleteAll();
        parkingLotRepository.deleteAll();
        userRepository.deleteAll();

        Role userRole = roleRepository.findByName(RoleName.ROLE_USER)
                .orElseGet(() -> roleRepository.save(Role.builder().name(RoleName.ROLE_USER).build()));
        Role adminRole = roleRepository.findByName(RoleName.ROLE_ADMIN)
                .orElseGet(() -> roleRepository.save(Role.builder().name(RoleName.ROLE_ADMIN).build()));

        User user = userRepository.save(User.builder()
                .fullName("Quality User")
                .email("quality.user@parknow.com")
                .passwordHash(passwordEncoder.encode("userpass"))
                .roles(Set.of(userRole))
                .build());
        userId = user.getId();

        User admin = userRepository.save(User.builder()
                .fullName("Quality Admin")
                .email("quality.admin@parknow.com")
                .passwordHash(passwordEncoder.encode("adminpass"))
                .roles(Set.of(userRole, adminRole))
                .build());

        // Get Tokens
        LoginRequest userLogin = LoginRequest.builder().email("quality.user@parknow.com").password("userpass").build();
        MvcResult res1 = mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(userLogin)))
                .andExpect(status().isOk()).andReturn();
        userToken = objectMapper.readTree(res1.getResponse().getContentAsString()).get("data").get("accessToken").asText();

        LoginRequest adminLogin = LoginRequest.builder().email("quality.admin@parknow.com").password("adminpass").build();
        MvcResult res2 = mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(adminLogin)))
                .andExpect(status().isOk()).andReturn();
        adminToken = objectMapper.readTree(res2.getResponse().getContentAsString()).get("data").get("accessToken").asText();

        // Create Lot & Vehicle
        var lotResp = lotService.createLot(ParkingLotRequest.builder()
                .name("Quality Test Lot")
                .address("500 Quality Way")
                .city("TechCity")
                .totalCapacity(10)
                .build());
        lotId = lotResp.getId();

        var vehicleResp = vehicleService.addVehicle(userId, VehicleRequest.builder()
                .licensePlate("QP-99-ZZ-0001")
                .vehicleType(VehicleType.CAR)
                .build());
        vehicleId = vehicleResp.getId();
    }

    @Test
    @DisplayName("1. Defensive Null Handling in Reservation & Session Services")
    void testDefensiveNullHandling() {
        assertThrows(InvalidOperationException.class, () -> reservationService.createReservation(userId, null));
        assertThrows(InvalidOperationException.class, () -> parkingSessionService.startSession(userId, null));
        assertThrows(InvalidOperationException.class, () -> authService.register(null));
    }

    @Test
    @DisplayName("2. Duplicate Email Registration Rejection")
    void testDuplicateEmailRegistration() {
        RegisterRequest request = RegisterRequest.builder()
                .fullName("Duplicate User")
                .email("quality.user@parknow.com")
                .password("password123")
                .build();

        assertThrows(InvalidOperationException.class, () -> authService.register(request));
    }

    @Test
    @DisplayName("3. Invalid ID ResourceNotFoundException")
    void testInvalidIdResourceNotFound() {
        assertThrows(ResourceNotFoundException.class, () -> reservationService.getReservationById(userId, 999999L, false));
        assertThrows(ResourceNotFoundException.class, () -> parkingSessionService.getSessionById(userId, 999999L, false));
        assertThrows(ResourceNotFoundException.class, () -> lotService.getLotById(999999L));
    }

    @Test
    @DisplayName("4. RBAC Authorization: Standard user denied access to Admin endpoints")
    void testRbacAdminAccessDenied() throws Exception {
        mockMvc.perform(get("/api/admin/dashboard")
                        .header("Authorization", "Bearer " + userToken))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.errorCode").value("ACCESS_DENIED"));

        mockMvc.perform(post("/api/admin/parking-lots")
                        .header("Authorization", "Bearer " + userToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(ParkingLotRequest.builder().name("Unauthorized Lot").address("Addr").city("City").totalCapacity(5).build())))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("5. Time Constraint Validation: Start time in past or after end time rejected")
    void testTimeConstraintValidation() {
        LocalDateTime now = LocalDateTime.now();

        // Start time after end time
        CreateReservationRequest req1 = CreateReservationRequest.builder()
                .lotId(lotId)
                .vehicleId(vehicleId)
                .vehicleType(VehicleType.CAR)
                .startTime(now.plusHours(3))
                .endTime(now.plusHours(1))
                .build();
        assertThrows(InvalidOperationException.class, () -> reservationService.createReservation(userId, req1));

        // Start time in past
        CreateReservationRequest req2 = CreateReservationRequest.builder()
                .lotId(lotId)
                .vehicleId(vehicleId)
                .vehicleType(VehicleType.CAR)
                .startTime(now.minusHours(2))
                .endTime(now.plusHours(1))
                .build();
        assertThrows(InvalidOperationException.class, () -> reservationService.createReservation(userId, req2));
    }

    @Test
    @Transactional
    @DisplayName("6. Parking Session Billing & Overstay Penalty Engine")
    void testBillingOverstayFeeCalculation() {
        LocalDateTime entryTime = LocalDateTime.now().minusHours(4);
        LocalDateTime reservedEndTime = entryTime.plusHours(2);
        LocalDateTime actualExitTime = entryTime.plusHours(4); // 2 hours overstay

        ParkingSession mockSession = ParkingSession.builder()
                .entryTime(entryTime)
                .slot(slotService.createSlot(ParkingSlotRequest.builder().lotId(lotId).slotNumber("Q1").floorNumber(1).vehicleType(VehicleType.CAR).build()) != null
                        ? com.parknow.entity.ParkingSlot.builder().id(1L).lot(com.parknow.entity.ParkingLot.builder().id(lotId).build()).build() : null)
                .vehicle(com.parknow.entity.Vehicle.builder().vehicleType(VehicleType.CAR).build())
                .reservation(com.parknow.entity.Reservation.builder().endTime(reservedEndTime).build())
                .build();

        BillingService.BillingBreakdown breakdown = billingService.calculateBillingFee(mockSession, actualExitTime);

        assertNotNull(breakdown);
        assertEquals(240L, breakdown.getDurationMinutes());
        assertTrue(breakdown.getBaseFee().compareTo(BigDecimal.ZERO) > 0);
        assertTrue(breakdown.getOverstayFee().compareTo(BigDecimal.ZERO) > 0); // 2 hours overstay penalty applied
        assertEquals(breakdown.getBaseFee().add(breakdown.getOverstayFee()), breakdown.getTotalFee());
    }

    @Test
    @Transactional
    @DisplayName("7. Admin Paginated Analytics & Management Endpoints")
    void testAdminPaginatedEndpoints() throws Exception {
        mockMvc.perform(get("/api/admin/users?page=0&size=10")
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.content").isArray())
                .andExpect(jsonPath("$.data.totalElements").exists());

        mockMvc.perform(get("/api/admin/reservations?page=0&size=10")
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.content").isArray());
    }
}
