package com.alejandro.microservices.hr_api.infrastructure.controller;

import com.alejandro.microservices.hr_api.application.dto.RoleDTO;
import com.alejandro.microservices.hr_api.application.service.RoleService;
import com.alejandro.microservices.hr_api.infrastructure.controller.RoleController.CreateRoleRequest;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Arrays;
import java.util.List;
import java.util.UUID;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(RoleController.class)
class RoleControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private RoleService roleService;

    @Autowired
    private ObjectMapper objectMapper;

    private RoleDTO roleDTO;
    private UUID roleId;

    @BeforeEach
    void setUp() {
        roleId = UUID.randomUUID();
        roleDTO = new RoleDTO(roleId, "Developer");
    }

    @Test
    void createRole_ShouldReturnCreatedRole() throws Exception {
        CreateRoleRequest request = new CreateRoleRequest("Developer");
        when(roleService.createRole("Developer")).thenReturn(roleDTO);

        mockMvc.perform(post("/api/roles")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(roleId.toString()))
                .andExpect(jsonPath("$.name").value("Developer"));
    }

    @Test
    void getAllRoles_ShouldReturnRoleList() throws Exception {
        List<RoleDTO> roles = Arrays.asList(
                roleDTO,
                new RoleDTO(UUID.randomUUID(), "Manager")
        );
        when(roleService.getAllRoles()).thenReturn(roles);

        mockMvc.perform(get("/api/roles"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$[0].name").value("Developer"))
                .andExpect(jsonPath("$[1].name").value("Manager"));
    }

    @Test
    void getRoleById_ShouldReturnRole() throws Exception {
        when(roleService.getRoleById(roleId)).thenReturn(roleDTO);

        mockMvc.perform(get("/api/roles/{id}", roleId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(roleId.toString()))
                .andExpect(jsonPath("$.name").value("Developer"));
    }

    @Test
    void deleteRole_ShouldReturnNoContent() throws Exception {
        mockMvc.perform(delete("/api/roles/{id}", roleId))
                .andExpect(status().isNoContent());
    }

    @Test
    void createRole_WithBlankName_ShouldReturnBadRequest() throws Exception {
        CreateRoleRequest invalidRequest = new CreateRoleRequest("");

        mockMvc.perform(post("/api/roles")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidRequest)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void getRoleById_WithInvalidUUID_ShouldReturnBadRequest() throws Exception {
        mockMvc.perform(get("/api/roles/invalid-uuid"))
                .andExpect(status().isBadRequest());
    }
}
