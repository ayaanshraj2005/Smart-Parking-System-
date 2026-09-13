package com.parknow.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.parknow.dto.request.LoginRequest;
import com.parknow.dto.request.ParkingLotRequest;
import com.parknow.dto.request.ParkingSlotRequest;
import com.parknow.entity.Role;
import com.parknow.entity.User;
import com.parknow.entity.enums.RoleName;
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

import java.util.Set;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class AdminDashboardServiceTest {

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
        userRepository.deleteAll();

        Role userRole = roleRepository.findByName(RoleName.ROLE_USER)
                .orElseGet(() -> roleRepository.save(Role.builder().name(RoleName.ROLE_USER).build()));
        Role adminRole = roleRepository.findByName(RoleName.ROLE_ADMIN)
                .orElseGet(() -> roleRepository.save(Role.builder().name(RoleName.ROLE_ADMIN).build()));

        userRepository.save(User.builder()
                .fullName("System Admin")
                .email("admin.dash@parknow.com")
                .passwordHash(passwordEncoder.encode("adminpass"))
                .roles(Set.of(userRole, adminRole))
                .build());

        userRepository.save(User.builder()
                .fullName("Regular User")
                .email("user.dash@parknow.com")
                .passwordHash(passwordEncoder.encode("userpass"))
                .roles(Set.of(userRole))
                .build());

        LoginRequest adminLogin = LoginRequest.builder().email("admin.dash@parknow.com").password("adminpass").build();
        MvcResult adminRes = mockMvc.perform(post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(adminLogin)))
                .andExpect(status().isOk()).andReturn();
        adminToken = objectMapper.readTree(adminRes.getResponse().getContentAsString()).get("data").get("accessToken").asText();

        LoginRequest userLogin = LoginRequest.builder().email("user.dash@parknow.com").password("userpass").build();
        MvcResult userRes = mockMvc.perform(post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(userLogin)))
                .andExpect(status().isOk()).andReturn();
        userToken = objectMapper.readTree(userRes.getResponse().getContentAsString()).get("data").get("accessToken").asText();

        // Seed sample parking lot and slots
        var lotResp = lotService.createLot(ParkingLotRequest.builder()
                .name("Admin Central Hub")
                .address("500 Executive Blvd")
                .city("Metropolis")
                .totalCapacity(10)
                .build());

        slotService.createSlot(ParkingSlotRequest.builder().lotId(lotResp.getId()).slotNumber("A1").floorNumber(1).vehicleType(VehicleType.CAR).build());
        slotService.createSlot(ParkingSlotRequest.builder().lotId(lotResp.getId()).slotNumber("A2").floorNumber(1).vehicleType(VehicleType.CAR).build());
        slotService.createSlot(ParkingSlotRequest.builder().lotId(lotResp.getId()).slotNumber("E1").floorNumber(1).vehicleType(VehicleType.ELECTRIC_VEHICLE).build());
    }

    @Test
    @Transactional
    @DisplayName("1. Admin should successfully retrieve dashboard summary metrics")
    void testAdminGetDashboardSummary() throws Exception {
        mockMvc.perform(get("/api/admin/dashboard")
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.totalUsers").value(2))
                .andExpect(jsonPath("$.data.totalParkingLots").value(1))
                .andExpect(jsonPath("$.data.totalParkingSlots").value(3))
                .andExpect(jsonPath("$.data.availableSlots").value(3))
                .andExpect(jsonPath("$.data.todaysRevenue").exists())
                .andExpect(jsonPath("$.data.vehicleTypeDistribution").exists());
    }

    @Test
    @Transactional
    @DisplayName("2. Admin should successfully retrieve revenue and occupancy reports")
    void testAdminGetRevenueAndOccupancyReports() throws Exception {
        mockMvc.perform(get("/api/admin/revenue")
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.todaysRevenue").exists())
                .andExpect(jsonPath("$.data.totalRevenue").exists());

        mockMvc.perform(get("/api/admin/occupancy")
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.totalSlots").value(3))
                .andExpect(jsonPath("$.data.lotOccupancyList").isArray());
    }

    @Test
    @Transactional
    @DisplayName("3. Admin should successfully retrieve paginated users and reservations lists")
    void testAdminGetPaginatedUsersAndReservations() throws Exception {
        mockMvc.perform(get("/api/admin/users?page=0&size=10")
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.content").isArray())
                .andExpect(jsonPath("$.data.totalElements").value(2))
                .andExpect(jsonPath("$.data.pageNumber").value(0));

        mockMvc.perform(get("/api/admin/reservations?page=0&size=10")
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.content").isArray())
                .andExpect(jsonPath("$.data.pageNumber").value(0));
    }

    @Test
    @Transactional
    @DisplayName("4. Regular user should be forbidden (HTTP 403) from admin dashboard APIs")
    void testUserForbiddenFromAdminDashboard() throws Exception {
        mockMvc.perform(get("/api/admin/dashboard")
                        .header("Authorization", "Bearer " + userToken))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.errorCode").value("ACCESS_DENIED"));
    }
}
