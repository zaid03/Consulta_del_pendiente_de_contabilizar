package com.example.backend.service;

import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.test.util.ReflectionTestUtils;

import com.example.backend.dto.ProjectionContabilizar;
import com.example.backend.sqlserver2.repository.FdeRepository;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

class ContabilizarSearchTest {

    @Mock
    private FdeRepository fdeRepository;

    private ContabilizarSearch contabilizarSearch;
    private List<ProjectionContabilizar> mockFacturas;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        contabilizarSearch = new ContabilizarSearch();
        ReflectionTestUtils.setField(contabilizarSearch, "fdeRepository", fdeRepository);
        mockFacturas = createMockFacturas();
    }

    private List<ProjectionContabilizar> createMockFacturas() {
        List<ProjectionContabilizar> facturas = new ArrayList<>();
        
        facturas.add(new MockProjection(
            1, "REF001", "OPE1", "ORG1", "FUN1", "ECO001", "SUB1", 100.0, 0.0,
            "CGEC01", "DOC001", null, 12345678, 100.0, 2024, 1, LocalDateTime.now(),
            "12345678A", "12345678A"
        ));

        facturas.add(new MockProjection(
            2, "REF002", "OPE2", "ORG2", "FUN2", "ECO002", "SUB2", 50.0, 10.0,
            "CGEC02", "DOC002", null, 87654321, 150.0, 2024, 2, LocalDateTime.now(),
            "Proveedor B Inc.", "87654321B"
        ));

        facturas.add(new MockProjection(
            3, "REF003", "OPE3", "ORG3", "FUN3", "ECO003", "SUB3", 75.0, 5.0,
            "CGEC03", "DOC003", null, 11111111, 200.0, 2023, 3, LocalDateTime.now(),
            "Proveedor C", "11111111C"
        ));

        facturas.add(new MockProjection(
            4, "REF004", "OPE4", "ORG4", "FUN4", "ECO001", "SUB4", 125.0, 0.0,
            "CGEC04", "DOC004", null, 22222222, 250.0, 2024, 4, LocalDateTime.now(),
            "Proveedor D", "22222222D"
        ));

        facturas.add(new MockProjection(
            5, "REF005", "OPE5", "ORG5", "FUN5", "ECO005", "SUB5", 200.0, 25.0,
            "CGEC01", "DOC005", null, 33333333, 300.0, 2024, 5, LocalDateTime.now(),
            "Proveedor E", "33333333E"
        ));

        return facturas;
    }
    
    @Test
    void testSearchContabilizadoWithAllFilters() {
        when(fdeRepository.findPendienteContabilizar(1, "2024")).thenReturn(mockFacturas);

        List<ProjectionContabilizar> result = contabilizarSearch.searchContabilizado(
            1, "2024", "12345678A", "CGEC01", "ECO001", 2024
        );

        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals("REF001", result.get(0).getFDEREF());
    }

    @Test
    void testSearchContabilizadoWithoutFilters() {
        when(fdeRepository.findPendienteContabilizar(1, "2024")).thenReturn(mockFacturas);

        List<ProjectionContabilizar> result = contabilizarSearch.searchContabilizado(
            1, "2024", null, null, null, null
        );

        assertEquals(mockFacturas.size(), result.size());
    }

    @Test
    void testSearchContabilizadoWithEmptyResult() {
        when(fdeRepository.findPendienteContabilizar(1, "2024")).thenReturn(new ArrayList<>());

        List<ProjectionContabilizar> result = contabilizarSearch.searchContabilizado(
            1, "2024", "NonExistent", null, null, null
        );

        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

    @Test
    void testSearchContabilizadoWithNullResult() {
        when(fdeRepository.findPendienteContabilizar(1, "2024")).thenReturn(null);

        List<ProjectionContabilizar> result = contabilizarSearch.searchContabilizado(
            1, "2024", null, null, null, null
        );

        assertNull(result);
    }

    @Test
    void testFilterByProveedorAllByNIF() {
        when(fdeRepository.findPendienteContabilizar(1, "2024")).thenReturn(mockFacturas);

        List<ProjectionContabilizar> result = contabilizarSearch.searchContabilizado(
            1, "2024", "12345678A", null, null, null
        );

        assertEquals(1, result.size());
        assertEquals("12345678A", result.get(0).getTERNIF());
    }

    @Test
    void testFilterByProveedorAllByNIFCaseInsensitive() {
        when(fdeRepository.findPendienteContabilizar(1, "2024")).thenReturn(mockFacturas);

        List<ProjectionContabilizar> result = contabilizarSearch.searchContabilizado(
            1, "2024", "12345678a", null, null, null
        );

        assertEquals(1, result.size());
        assertEquals("12345678A", result.get(0).getTERNIF());
    }

    @Test
    void testFilterByProveedorNombrePartial() {
        when(fdeRepository.findPendienteContabilizar(1, "2024")).thenReturn(mockFacturas);

        List<ProjectionContabilizar> result = contabilizarSearch.searchContabilizado(
            1, "2024", "proveedor", null, null, null
        );

        assertEquals(4, result.size());
    }

    @Test
    void testFilterByProveedorNombreExact() {
        when(fdeRepository.findPendienteContabilizar(1, "2024")).thenReturn(mockFacturas);

        List<ProjectionContabilizar> result = contabilizarSearch.searchContabilizado(
            1, "2024", "proveedor b", null, null, null
        );

        assertEquals(1, result.size());
        assertEquals("Proveedor B Inc.", result.get(0).getTERNOM());
    }

    @Test
    void testFilterByProveedorNombreNotFound() {
        when(fdeRepository.findPendienteContabilizar(1, "2024")).thenReturn(mockFacturas);

        List<ProjectionContabilizar> result = contabilizarSearch.searchContabilizado(
            1, "2024", "NonExistent", null, null, null
        );

        assertTrue(result.isEmpty());
    }

    @Test
    void testFilterByProveedorNullValues() {
        List<ProjectionContabilizar> testFacturas = new ArrayList<>();
        testFacturas.add(new MockProjection(
            1, "REF", "OPE", "ORG", "FUN", "ECO", "SUB", 100.0, 0.0,
            "CGEC", "DOC", null, (Integer) null, 100.0, 2024, 1, LocalDateTime.now(),
            null, null
        ));

        when(fdeRepository.findPendienteContabilizar(1, "2024")).thenReturn(testFacturas);

        List<ProjectionContabilizar> result = contabilizarSearch.searchContabilizado(
            1, "2024", "12345678A", null, null, null
        );

        assertTrue(result.isEmpty());
    }

    @Test
    void testFilterByCentroGestor() {
        when(fdeRepository.findPendienteContabilizar(1, "2024")).thenReturn(mockFacturas);

        List<ProjectionContabilizar> result = contabilizarSearch.searchContabilizado(
            1, "2024", null, "CGEC01", null, null
        );

        assertEquals(2, result.size());
        assertTrue(result.stream().allMatch(f -> "CGEC01".equals(f.getCGECOD())));
    }

    @Test
    void testFilterByCentroGestorCaseInsensitive() {
        when(fdeRepository.findPendienteContabilizar(1, "2024")).thenReturn(mockFacturas);

        List<ProjectionContabilizar> result = contabilizarSearch.searchContabilizado(
            1, "2024", null, "cgec01", null, null
        );

        assertEquals(2, result.size());
    }

    @Test
    void testFilterByCentroGestorNotFound() {
        when(fdeRepository.findPendienteContabilizar(1, "2024")).thenReturn(mockFacturas);

        List<ProjectionContabilizar> result = contabilizarSearch.searchContabilizado(
            1, "2024", null, "NONEXIST", null, null
        );

        assertTrue(result.isEmpty());
    }

    @Test
    void testFilterByCentroGestorNullValue() {
        List<ProjectionContabilizar> testFacturas = new ArrayList<>();
        testFacturas.add(new MockProjection(
            1, "REF", "OPE", "ORG", "FUN", "ECO", "SUB", 100.0, 0.0,
            null, "DOC", null, 12345678, 100.0, 2024, 1, LocalDateTime.now(),
            "Provider", "12345678"
        ));

        when(fdeRepository.findPendienteContabilizar(1, "2024")).thenReturn(testFacturas);

        List<ProjectionContabilizar> result = contabilizarSearch.searchContabilizado(
            1, "2024", null, "CGEC01", null, null
        );

        assertTrue(result.isEmpty());
    }

    @Test
    void testFilterByEconomica() {
        when(fdeRepository.findPendienteContabilizar(1, "2024")).thenReturn(mockFacturas);

        List<ProjectionContabilizar> result = contabilizarSearch.searchContabilizado(
            1, "2024", null, null, "ECO001", null
        );

        assertEquals(2, result.size());
        assertTrue(result.stream().allMatch(f -> "ECO001".equals(f.getFDEECO())));
    }

    @Test
    void testFilterByEconomicaCaseInsensitive() {
        when(fdeRepository.findPendienteContabilizar(1, "2024")).thenReturn(mockFacturas);

        List<ProjectionContabilizar> result = contabilizarSearch.searchContabilizado(
            1, "2024", null, null, "eco001", null
        );

        assertEquals(2, result.size());
    }

    @Test
    void testFilterByEconomicaNotFound() {
        when(fdeRepository.findPendienteContabilizar(1, "2024")).thenReturn(mockFacturas);

        List<ProjectionContabilizar> result = contabilizarSearch.searchContabilizado(
            1, "2024", null, null, "NONEXIST", null
        );

        assertTrue(result.isEmpty());
    }

    @Test
    void testFilterByAno() {
        when(fdeRepository.findPendienteContabilizar(1, "2024")).thenReturn(mockFacturas);

        List<ProjectionContabilizar> result = contabilizarSearch.searchContabilizado(
            1, "2024", null, null, null, 2024
        );

        assertEquals(4, result.size());
        assertTrue(result.stream().allMatch(f -> 2024 == f.getFACANN()));
    }

    @Test
    void testFilterByAnoDifferentYear() {
        when(fdeRepository.findPendienteContabilizar(1, "2024")).thenReturn(mockFacturas);

        List<ProjectionContabilizar> result = contabilizarSearch.searchContabilizado(
            1, "2024", null, null, null, 2023
        );

        assertEquals(1, result.size());
        assertEquals(2023, result.get(0).getFACANN());
    }

    @Test
    void testFilterByAnoNotFound() {
        when(fdeRepository.findPendienteContabilizar(1, "2024")).thenReturn(mockFacturas);

        List<ProjectionContabilizar> result = contabilizarSearch.searchContabilizado(
            1, "2024", null, null, null, 2025
        );

        assertTrue(result.isEmpty());
    }

    @Test
    void testFilterByAnoNullValue() {
        List<ProjectionContabilizar> testFacturas = new ArrayList<>();
        testFacturas.add(new MockProjection(
            1, "REF", "OPE", "ORG", "FUN", "ECO", "SUB", 100.0, 0.0,
            "CGEC", "DOC", null, 12345678, 100.0, null, 1, LocalDateTime.now(),
            "Provider", "12345678"
        ));

        when(fdeRepository.findPendienteContabilizar(1, "2024")).thenReturn(testFacturas);

        List<ProjectionContabilizar> result = contabilizarSearch.searchContabilizado(
            1, "2024", null, null, null, 2024
        );

        assertTrue(result.isEmpty());
    }

    @Test
    void testMultipleFiltersAND() {
        when(fdeRepository.findPendienteContabilizar(1, "2024")).thenReturn(mockFacturas);

        List<ProjectionContabilizar> result = contabilizarSearch.searchContabilizado(
            1, "2024", "12345678A", "CGEC01", "ECO001", 2024
        );

        assertEquals(1, result.size());
        assertEquals("REF001", result.get(0).getFDEREF());
    }

    @Test
    void testMultipleFiltersNoMatch() {
        when(fdeRepository.findPendienteContabilizar(1, "2024")).thenReturn(mockFacturas);

        List<ProjectionContabilizar> result = contabilizarSearch.searchContabilizado(
            1, "2024", "Proveedor A", "CGEC02", null, null
        );

        assertTrue(result.isEmpty());
    }

    @Test
    void testIsNumbersOnlyTrue() {
        assertTrue(isNumbersOnly("12345678"));
    }

    @Test
    void testIsNumbersOnlyFalse() {
        assertFalse(isNumbersOnly("12345678A"));
    }

    @Test
    void testIsNumbersOnlyMixed() {
        assertFalse(isNumbersOnly("abc123"));
    }

    @Test
    void testIsMixedTrue() {
        assertTrue(isMixed("12345678A"));
    }

    @Test
    void testIsMixedFalse() {
        assertFalse(isMixed("12345678"));
    }

    private boolean isNumbersOnly(String text) {
        return text.matches("^[0-9]+$");
    }

    private boolean isMixed(String text) {
        return !isNumbersOnly(text);
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