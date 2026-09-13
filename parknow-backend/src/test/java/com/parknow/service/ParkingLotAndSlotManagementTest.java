package com.parknow.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.parknow.dto.request.LoginRequest;
import com.parknow.dto.request.ParkingLotRequest;
import com.parknow.dto.request.ParkingSlotRequest;
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

import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class ParkingLotAndSlotManagementTest {

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
    private ObjectMapper objectMapper;

    private String adminToken;
    private String userToken;

    @BeforeEach
    void setUp() throws Exception {
        Role userRole = roleRepository.findByName(RoleName.ROLE_USER)
                .orElseGet(() -> roleRepository.save(Role.builder().name(RoleName.ROLE_USER).build()));
        Role adminRole = roleRepository.findByName(RoleName.ROLE_ADMIN)
                .orElseGet(() -> roleRepository.save(Role.builder().name(RoleName.ROLE_ADMIN).build()));

        userRepository.save(User.builder()
                .fullName("Admin User")
                .email("admin.phase4@parknow.com")
                .passwordHash(passwordEncoder.encode("adminpass"))
                .roles(Set.of(userRole, adminRole))
                .build());

        userRepository.save(User.builder()
                .fullName("Regular User")
                .email("user.phase4@parknow.com")
                .passwordHash(passwordEncoder.encode("userpass"))
                .roles(Set.of(userRole))
                .build());

        LoginRequest adminLogin = LoginRequest.builder().email("admin.phase4@parknow.com").password("adminpass").build();
        MvcResult adminRes = mockMvc.perform(post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(adminLogin)))
                .andExpect(status().isOk()).andReturn();
        adminToken = objectMapper.readTree(adminRes.getResponse().getContentAsString()).get("data").get("accessToken").asText();

        LoginRequest userLogin = LoginRequest.builder().email("user.phase4@parknow.com").password("userpass").build();
        MvcResult userRes = mockMvc.perform(post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(userLogin)))
                .andExpect(status().isOk()).andReturn();
        userToken = objectMapper.readTree(userRes.getResponse().getContentAsString()).get("data").get("accessToken").asText();
    }

    @Test
    @DisplayName("Admin should create parking lot, add slots, and update status to MAINTENANCE/DISABLED")
    void testAdminLotAndSlotManagement() throws Exception {
        ParkingLotRequest lotReq = ParkingLotRequest.builder()
                .name("Tech City Hub Garage")
                .address("500 Tech Blvd")
                .city("Metropolis")
                .totalCapacity(10)
                .build();

        MvcResult lotResult = mockMvc.perform(post("/api/admin/parking-lots")
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(lotReq)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.data.name").value("Tech City Hub Garage"))
                .andReturn();

        Long lotId = objectMapper.readTree(lotResult.getResponse().getContentAsString()).get("data").get("id").asLong();

        // Add Slot A1 (CAR)
        ParkingSlotRequest slotReq1 = ParkingSlotRequest.builder()
                .lotId(lotId)
                .slotNumber("A1")
                .floorNumber(1)
                .vehicleType(VehicleType.CAR)
                .build();

        MvcResult slotResult = mockMvc.perform(post("/api/admin/slots")
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(slotReq1)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.data.slotNumber").value("A1"))
                .andReturn();

        Long slotId1 = objectMapper.readTree(slotResult.getResponse().getContentAsString()).get("data").get("id").asLong();

        // Mark Slot A1 as MAINTENANCE
        mockMvc.perform(patch("/api/admin/slots/" + slotId1 + "/status?status=MAINTENANCE")
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.status").value("MAINTENANCE"));

        // Verify available slots API filters out MAINTENANCE slots
        var availableSlots = slotService.getAvailableSlots(lotId, VehicleType.CAR);
        assertEquals(0, availableSlots.size(), "MAINTENANCE slot should NOT be returned in available slots query");
    }

    @Test
    @DisplayName("User should be forbidden (403) from accessing Admin parking management endpoints")
    void testUserForbiddenFromAdminEndpoints() throws Exception {
        ParkingLotRequest lotReq = ParkingLotRequest.builder()
                .name("Unauthorized Lot")
                .address("123 Forbidden St")
                .city("Metropolis")
                .totalCapacity(5)
                .build();

        mockMvc.perform(post("/api/admin/parking-lots")
                        .header("Authorization", "Bearer " + userToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(lotReq)))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.errorCode").value("ACCESS_DENIED"));
    }

    @Test
    @DisplayName("Public/User should view active lots and available slots filtered by vehicle type")
    void testPublicViewLotsAndAvailableSlots() throws Exception {
        var lotResp = lotService.createLot(ParkingLotRequest.builder()
                .name("Public Central Park")
                .address("1 Park Ave")
                .city("Gotham")
                .totalCapacity(5)
                .build());

        slotService.createSlot(ParkingSlotRequest.builder().lotId(lotResp.getId()).slotNumber("B1").floorNumber(1).vehicleType(VehicleType.CAR).build());
        slotService.createSlot(ParkingSlotRequest.builder().lotId(lotResp.getId()).slotNumber("M1").floorNumber(1).vehicleType(VehicleType.TWO_WHEELER).build());

        mockMvc.perform(get("/api/parking-lots?city=Gotham"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data[0].name").value("Public Central Park"));

        mockMvc.perform(get("/api/parking-slots/available?lotId=" + lotResp.getId() + "&vehicleType=CAR"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.length()").value(1))
                .andExpect(jsonPath("$.data[0].slotNumber").value("B1"));
    }
}
