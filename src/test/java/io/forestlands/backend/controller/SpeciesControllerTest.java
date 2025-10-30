package io.forestlands.backend.controller;

import io.forestlands.backend.entity.Species;
import io.forestlands.backend.service.SpeciesService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;
import java.util.UUID;

import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = SpeciesController.class)
class SpeciesControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private SpeciesService speciesService;

    @Test
    @WithMockUser
    void listSpeciesReturnsEnabledByDefault() throws Exception {
        Species species = buildSpecies("oak", "Oak", false, 20, true, 10);

        when(speciesService.listSpecies(false)).thenReturn(List.of(species));

        mockMvc.perform(get("/api/v1/species"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.items[0].code").value("oak"))
                .andExpect(jsonPath("$.items[0].isEnabled").value(true))
                .andExpect(jsonPath("$.items[0].sortOrder").value(10));

        verify(speciesService, times(1)).listSpecies(false);
    }

    @Test
    @WithMockUser
    void listSpeciesIncludesDisabledWhenRequested() throws Exception {
        Species disabledSpecies = buildSpecies("pine", "Pine", false, 15, false, 20);

        when(speciesService.listSpecies(true)).thenReturn(List.of(disabledSpecies));

        mockMvc.perform(get("/api/v1/species").param("includeDisabled", "true"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.items[0].code").value("pine"))
                .andExpect(jsonPath("$.items[0].isEnabled").value(false))
                .andExpect(jsonPath("$.items[0].sortOrder").value(20));

        verify(speciesService, times(1)).listSpecies(true);
        verify(speciesService, times(0)).listSpecies(false);
    }

    private Species buildSpecies(String code, String name, boolean premium, int price, boolean enabled, int sortOrder) {
        Species species = new Species();
        species.setUuid(UUID.randomUUID());
        species.setCode(code);
        species.setName(name);
        species.setPremium(premium);
        species.setPrice(price);
        species.setEnabled(enabled);
        species.setSortOrder(sortOrder);
        return species;
    }
}
