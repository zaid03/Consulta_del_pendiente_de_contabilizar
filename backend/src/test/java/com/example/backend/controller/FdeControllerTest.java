package com.example.backend.controller;

import static org.mockito.Mockito.*;
import static org.mockito.ArgumentMatchers.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.dao.DataAccessException;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import com.example.backend.config.JwtAuthFilter;
import com.example.backend.config.JwtUtil;
import com.example.backend.dto.ProjectionContabilizar;
import com.example.backend.sqlserver2.repository.FdeRepository;
import com.example.backend.service.ContabilizarSearch;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@WebMvcTest(FdeController.class)
@AutoConfigureMockMvc(addFilters = false)
class FdeControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private FdeRepository fdeRepository;

    @MockBean
    private ContabilizarSearch contabilizarSearch;

    @MockBean
    private JwtUtil jwtUtil;

    @MockBean
    private JwtAuthFilter jwtAuthFilter;

    private static final String AUTH_HEADER = "Authorization";
    private static final String BEARER_TOKEN = "Bearer test-token";

    private List<ProjectionContabilizar> mockFacturas;
    private ProjectionContabilizar mockFactura1;
    private ProjectionContabilizar mockFactura2;

    @BeforeEach
    void setUp() throws Exception {        
        mockFactura1 = new MockProjection(
            1, "REF001", "OPE1", "ORG1", "FUN1", "ECO001", "SUB1", 100.0, 0.0,
            "CGEC01", "DOC001", null, 12345678, 100.0, 2024, 1, LocalDateTime.now(),
            "Proveedor A", "12345678A"
        );

        mockFactura2 = new MockProjection(
            2, "REF002", "OPE2", "ORG2", "FUN2", "ECO002", "SUB2", 50.0, 10.0,
            "CGEC02", "DOC002", null, 87654321, 150.0, 2024, 2, LocalDateTime.now(),
            "Proveedor B", "87654321B"
        );

        mockFacturas = new ArrayList<>();
        mockFacturas.add(mockFactura1);
        mockFacturas.add(mockFactura2);
    }

    @Test
    void testFetchContabilizadoSuccess() throws Exception {
        when(fdeRepository.findPendienteContabilizar(1, "2024")).thenReturn(mockFacturas);

        mockMvc.perform(get("/api/fde/fetch-pendiente-del-contabilizar/1/2024"))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(jsonPath("$[0].fderef").value("REF001"))
            .andExpect(jsonPath("$[1].fderef").value("REF002"))
            .andExpect(jsonPath("$.length()").value(2));

        verify(fdeRepository, times(1)).findPendienteContabilizar(1, "2024");
    }

    @Test
    void testFetchContabilizadoWithEmptyResult() throws Exception {
        when(fdeRepository.findPendienteContabilizar(1, "2024")).thenReturn(new ArrayList<>());

        mockMvc.perform(get("/api/fde/fetch-pendiente-del-contabilizar/1/2024"))
            .andExpect(status().isNotFound())
            .andExpect(content().string("Sin resultado"));

        verify(fdeRepository, times(1)).findPendienteContabilizar(1, "2024");
    }

    @Test
    void testFetchContabilizadoWithDataAccessException() throws Exception {
        String errorMessage = "Database connection failed";
        DataAccessException ex = new DataAccessException(errorMessage) {
            @Override
            public Throwable getMostSpecificCause() {
                return new RuntimeException(errorMessage);
            }
        };

        when(fdeRepository.findPendienteContabilizar(1, "2024")).thenThrow(ex);

        mockMvc.perform(get("/api/fde/fetch-pendiente-del-contabilizar/1/2024"))
            .andExpect(status().isBadRequest())
            .andExpect(content().string("Error :" + errorMessage));

        verify(fdeRepository, times(1)).findPendienteContabilizar(1, "2024");
    }

    @Test
    void testFetchContabilizadoWithGenericException() throws Exception {
        String errorMessage = "Unexpected error";
        when(fdeRepository.findPendienteContabilizar(1, "2024"))
            .thenThrow(new RuntimeException(errorMessage));

        mockMvc.perform(get("/api/fde/fetch-pendiente-del-contabilizar/1/2024"))
            .andExpect(status().isBadRequest())
            .andExpect(content().string("Error :" + errorMessage));

        verify(fdeRepository, times(1)).findPendienteContabilizar(1, "2024");
    }

    @Test
    void testFetchContabilizadoWithSingleResult() throws Exception {
        List<ProjectionContabilizar> singleResult = new ArrayList<>();
        singleResult.add(mockFactura1);

        when(fdeRepository.findPendienteContabilizar(2, "2025")).thenReturn(singleResult);

        mockMvc.perform(get("/api/fde/fetch-pendiente-del-contabilizar/2/2025"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.length()").value(1))
            .andExpect(jsonPath("$[0].fderef").value("REF001"));

        verify(fdeRepository, times(1)).findPendienteContabilizar(2, "2025");
    }

    @Test
    void testFetchContabilizadoPathVariablesExtracted() throws Exception {
        when(fdeRepository.findPendienteContabilizar(99, "TEST")).thenReturn(mockFacturas);

        mockMvc.perform(get("/api/fde/fetch-pendiente-del-contabilizar/99/TEST"))
            .andExpect(status().isOk());

        verify(fdeRepository, times(1)).findPendienteContabilizar(99, "TEST");
    }

    @Test
    void testSearchContabilizadoWithAllFilters() throws Exception {
        List<ProjectionContabilizar> searchResult = new ArrayList<>();
        searchResult.add(mockFactura1);

        when(contabilizarSearch.searchContabilizado(1, "2024", "Prov", "CGEC", "ECO", 2024))
            .thenReturn(searchResult);

        mockMvc.perform(get("/api/fde/search-pendiente-contabilizar")
                .param("ent", "1")
                .param("eje", "2024")
                .param("proveedor", "Prov")
                .param("centroGestor", "CGEC")
                .param("economica", "ECO")
                .param("ano", "2024"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.length()").value(1))
            .andExpect(jsonPath("$[0].fderef").value("REF001"));

        verify(contabilizarSearch, times(1)).searchContabilizado(1, "2024", "Prov", "CGEC", "ECO", 2024);
    }

    @Test
    void testSearchContabilizadoWithoutOptionalFilters() throws Exception {
        when(contabilizarSearch.searchContabilizado(1, "2024", null, null, null, null))
            .thenReturn(mockFacturas);

        mockMvc.perform(get("/api/fde/search-pendiente-contabilizar")
                .param("ent", "1")
                .param("eje", "2024"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.length()").value(2));

        verify(contabilizarSearch, times(1)).searchContabilizado(1, "2024", null, null, null, null);
    }

    @Test
    void testSearchContabilizadoWithProveedorFilterOnly() throws Exception {
        when(contabilizarSearch.searchContabilizado(1, "2024", "Provider", null, null, null))
            .thenReturn(mockFacturas);

        mockMvc.perform(get("/api/fde/search-pendiente-contabilizar")
                .param("ent", "1")
                .param("eje", "2024")
                .param("proveedor", "Provider"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.length()").value(2));

        verify(contabilizarSearch, times(1)).searchContabilizado(1, "2024", "Provider", null, null, null);
    }

    @Test
    void testSearchContabilizadoWithCentroGestorFilterOnly() throws Exception {
        when(contabilizarSearch.searchContabilizado(1, "2024", null, "CGEC01", null, null))
            .thenReturn(mockFacturas);

        mockMvc.perform(get("/api/fde/search-pendiente-contabilizar")
                .param("ent", "1")
                .param("eje", "2024")
                .param("centroGestor", "CGEC01"))
            .andExpect(status().isOk());

        verify(contabilizarSearch, times(1)).searchContabilizado(1, "2024", null, "CGEC01", null, null);
    }

    @Test
    void testSearchContabilizadoWithEconomicaFilterOnly() throws Exception {
        when(contabilizarSearch.searchContabilizado(1, "2024", null, null, "ECO001", null))
            .thenReturn(mockFacturas);

        mockMvc.perform(get("/api/fde/search-pendiente-contabilizar")
                .param("ent", "1")
                .param("eje", "2024")
                .param("economica", "ECO001"))
            .andExpect(status().isOk());

        verify(contabilizarSearch, times(1)).searchContabilizado(1, "2024", null, null, "ECO001", null);
    }

    @Test
    void testSearchContabilizadoWithAnoFilterOnly() throws Exception {
        when(contabilizarSearch.searchContabilizado(1, "2024", null, null, null, 2024))
            .thenReturn(mockFacturas);

        mockMvc.perform(get("/api/fde/search-pendiente-contabilizar")
                .param("ent", "1")
                .param("eje", "2024")
                .param("ano", "2024"))
            .andExpect(status().isOk());

        verify(contabilizarSearch, times(1)).searchContabilizado(1, "2024", null, null, null, 2024);
    }

    @Test
    void testSearchContabilizadoWithEmptyResult() throws Exception {
        when(contabilizarSearch.searchContabilizado(1, "2024", "NonExistent", null, null, null))
            .thenReturn(new ArrayList<>());

        mockMvc.perform(get("/api/fde/search-pendiente-contabilizar")
                .param("ent", "1")
                .param("eje", "2024")
                .param("proveedor", "NonExistent"))
            .andExpect(status().isNotFound())
            .andExpect(content().string("Sin resultado"));

        verify(contabilizarSearch, times(1)).searchContabilizado(1, "2024", "NonExistent", null, null, null);
    }

    @Test
    void testSearchContabilizadoWithDataAccessException() throws Exception {
        String errorMessage = "Database query failed";
        DataAccessException ex = new DataAccessException(errorMessage) {
            @Override
            public Throwable getMostSpecificCause() {
                return new RuntimeException(errorMessage);
            }
        };

        when(contabilizarSearch.searchContabilizado(1, "2024", null, null, null, null))
            .thenThrow(ex);

        mockMvc.perform(get("/api/fde/search-pendiente-contabilizar")
                .param("ent", "1")
                .param("eje", "2024"))
            .andExpect(status().isBadRequest())
            .andExpect(content().string("Error :" + errorMessage));

        verify(contabilizarSearch, times(1)).searchContabilizado(1, "2024", null, null, null, null);
    }

    @Test
    void testSearchContabilizadoWithMultipleResults() throws Exception {
        when(contabilizarSearch.searchContabilizado(1, "2024", "Prov", null, null, null))
            .thenReturn(mockFacturas);

        mockMvc.perform(get("/api/fde/search-pendiente-contabilizar")
                .param("ent", "1")
                .param("eje", "2024")
                .param("proveedor", "Prov"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.length()").value(2))
            .andExpect(jsonPath("$[0].fderef").value("REF001"))
            .andExpect(jsonPath("$[1].fderef").value("REF002"));

        verify(contabilizarSearch, times(1)).searchContabilizado(1, "2024", "Prov", null, null, null);
    }

    @Test
    void testSearchContabilizadoWithSingleResult() throws Exception {
        List<ProjectionContabilizar> singleResult = new ArrayList<>();
        singleResult.add(mockFactura1);

        when(contabilizarSearch.searchContabilizado(2, "2025", "Test", null, null, null))
            .thenReturn(singleResult);

        mockMvc.perform(get("/api/fde/search-pendiente-contabilizar")
                .param("ent", "2")
                .param("eje", "2025")
                .param("proveedor", "Test"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.length()").value(1));

        verify(contabilizarSearch, times(1)).searchContabilizado(2, "2025", "Test", null, null, null);
    }

    @Test
    void testSearchContabilizadoCombinedFiltersProveedorAndAno() throws Exception {
        when(contabilizarSearch.searchContabilizado(1, "2024", "Prov", null, null, 2024))
            .thenReturn(mockFacturas);

        mockMvc.perform(get("/api/fde/search-pendiente-contabilizar")
                .param("ent", "1")
                .param("eje", "2024")
                .param("proveedor", "Prov")
                .param("ano", "2024"))
            .andExpect(status().isOk());

        verify(contabilizarSearch, times(1)).searchContabilizado(1, "2024", "Prov", null, null, 2024);
    }

    @Test
    void testSearchContabilizadoCombinedFiltersCentroAndEconomica() throws Exception {
        when(contabilizarSearch.searchContabilizado(1, "2024", null, "CGEC01", "ECO001", null))
            .thenReturn(mockFacturas);

        mockMvc.perform(get("/api/fde/search-pendiente-contabilizar")
                .param("ent", "1")
                .param("eje", "2024")
                .param("centroGestor", "CGEC01")
                .param("economica", "ECO001"))
            .andExpect(status().isOk());

        verify(contabilizarSearch, times(1)).searchContabilizado(1, "2024", null, "CGEC01", "ECO001", null);
    }

    @Test
    void testSearchContabilizadoRequestMappingVerification() throws Exception {
        when(contabilizarSearch.searchContabilizado(1, "2024", null, null, null, null))
            .thenReturn(mockFacturas);

        mockMvc.perform(get("/api/fde/search-pendiente-contabilizar")
                .param("ent", "1")
                .param("eje", "2024"))
            .andExpect(status().isOk());
    }

    @Test
    void testFetchContabilizadoRequestMappingVerification() throws Exception {
        when(fdeRepository.findPendienteContabilizar(1, "2024")).thenReturn(mockFacturas);

        mockMvc.perform(get("/api/fde/fetch-pendiente-del-contabilizar/1/2024"))
            .andExpect(status().isOk());
    }

    @Test
    void testSearchContabilizadoPropagatesEmptyStringFilters() throws Exception {
        when(contabilizarSearch.searchContabilizado(1, "2024", "", null, null, null))
            .thenReturn(mockFacturas);

        mockMvc.perform(get("/api/fde/search-pendiente-contabilizar")
                .param("ent", "1")
                .param("eje", "2024")
                .param("proveedor", ""))
            .andExpect(status().isOk());

        verify(contabilizarSearch, times(1)).searchContabilizado(1, "2024", "", null, null, null);
    }

    static class MockProjection implements ProjectionContabilizar {
        private Integer facnum;
        private String fderef;
        private String fdeope;
        private String fdeorg;
        private String fdefun;
        private String fdeeco;
        private String fdesub;
        private Double fdeimp;
        private Double fdedif;
        private String cgecod;
        private String facdoc;
        private LocalDateTime facfco;
        private Integer tercod;
        private Double facimp;
        private Integer facann;
        private Integer facfac;
        private LocalDateTime facdat;
        private String ternom;
        private String ternif;

        MockProjection(Integer facnum, String fderef, String fdeope, String fdeorg,
                      String fdefun, String fdeeco, String fdesub, Double fdeimp, Double fdedif,
                      String cgecod, String facdoc, LocalDateTime facfco, Integer tercod,
                      Double facimp, Integer facann, Integer facfac, LocalDateTime facdat,
                      String ternom, String ternif) {
            this.facnum = facnum;
            this.fderef = fderef;
            this.fdeope = fdeope;
            this.fdeorg = fdeorg;
            this.fdefun = fdefun;
            this.fdeeco = fdeeco;
            this.fdesub = fdesub;
            this.fdeimp = fdeimp;
            this.fdedif = fdedif;
            this.cgecod = cgecod;
            this.facdoc = facdoc;
            this.facfco = facfco;
            this.tercod = tercod;
            this.facimp = facimp;
            this.facann = facann;
            this.facfac = facfac;
            this.facdat = facdat;
            this.ternom = ternom;
            this.ternif = ternif;
        }

        @Override
        public Integer getFACNUM() { return facnum; }
        @Override
        public String getFDEREF() { return fderef; }
        @Override
        public String getFDEOPE() { return fdeope; }
        @Override
        public String getFDEORG() { return fdeorg; }
        @Override
        public String getFDEFUN() { return fdefun; }
        @Override
        public String getFDEECO() { return fdeeco; }
        @Override
        public String getFDESUB() { return fdesub; }
        @Override
        public Double getFDEIMP() { return fdeimp; }
        @Override
        public Double getFDEDIF() { return fdedif; }
        @Override
        public String getCGECOD() { return cgecod; }
        @Override
        public String getFACDOC() { return facdoc; }
        @Override
        public LocalDateTime getFACFCO() { return facfco; }
        @Override
        public Integer getTERCOD() { return tercod; }
        @Override
        public Double getFACIMP() { return facimp; }
        @Override
        public Integer getFACANN() { return facann; }
        @Override
        public Integer getFACFAC() { return facfac; }
        @Override
        public LocalDateTime getFACDAT() { return facdat; }
        @Override
        public String getTERNOM() { return ternom; }
        @Override
        public String getTERNIF() { return ternif; }
    }
}
