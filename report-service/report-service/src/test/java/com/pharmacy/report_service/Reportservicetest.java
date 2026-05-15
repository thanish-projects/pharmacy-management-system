package com.pharmacy.report_service;

import com.itextpdf.text.DocumentException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ReportServiceTest {

    @Mock
    private SalesReportRepository reportRepository;

    @InjectMocks
    private ReportService reportService;

    private SalesOrder order1;
    private SalesOrder order2;

    @BeforeEach
    void setUp() {
        order1 = new SalesOrder();
        order1.setId(1);
        order1.setDoctorEmail("doctor@pharmacy.com");
        order1.setDrugName("Paracetamol");
        order1.setQuantity(5);
        order1.setTotalPrice(250.0);
        order1.setStatus(OrderStatus.PICKED_UP);
        order1.setOrderDate(LocalDateTime.of(2026, 1, 1, 10, 0));

        order2 = new SalesOrder();
        order2.setId(2);
        order2.setDoctorEmail("doctor2@pharmacy.com");
        order2.setDrugName("Ibuprofen");
        order2.setQuantity(3);
        order2.setTotalPrice(150.0);
        order2.setStatus(OrderStatus.PICKED_UP);
        order2.setOrderDate(LocalDateTime.of(2026, 1, 2, 11, 0));
    }

    // ===== getSalesReport() tests =====

    @Test
    void getSalesReport_ShouldReturnCompletedOrdersAndRevenue() {
        when(reportRepository.findByStatus(OrderStatus.PICKED_UP))
                .thenReturn(Arrays.asList(order1, order2));
        when(reportRepository.getTotalRevenue()).thenReturn(400.0);

        SalesReportDTO result = reportService.getSalesReport();

        assertNotNull(result);
        assertEquals(2, result.getSales().size());
        assertEquals(400.0, result.getTotalRevenue());
        verify(reportRepository, times(1)).findByStatus(OrderStatus.PICKED_UP);
        verify(reportRepository, times(1)).getTotalRevenue();
    }

    @Test
    void getSalesReport_WhenNoOrders_ShouldReturnEmptyListAndZeroRevenue() {
        when(reportRepository.findByStatus(OrderStatus.PICKED_UP)).thenReturn(List.of());
        when(reportRepository.getTotalRevenue()).thenReturn(null);

        SalesReportDTO result = reportService.getSalesReport();

        assertNotNull(result);
        assertTrue(result.getSales().isEmpty());
        // When revenue is null from DB, it should default to 0.0
        assertEquals(0.0, result.getTotalRevenue());
    }

    @Test
    void getSalesReport_WhenRevenueIsNull_ShouldDefaultToZero() {
        when(reportRepository.findByStatus(OrderStatus.PICKED_UP))
                .thenReturn(Arrays.asList(order1));
        when(reportRepository.getTotalRevenue()).thenReturn(null);

        SalesReportDTO result = reportService.getSalesReport();

        // Null revenue from DB must be treated as 0.0 — not crash
        assertEquals(0.0, result.getTotalRevenue());
    }

    // ===== generateCSV() tests =====

    @Test
    void generateCSV_ShouldContainHeaderRow() {
        when(reportRepository.findByStatus(OrderStatus.PICKED_UP))
                .thenReturn(Arrays.asList(order1));
        when(reportRepository.getTotalRevenue()).thenReturn(250.0);

        String csv = reportService.generateCSV();

        // CSV must always start with correct headers
        assertTrue(csv.startsWith("Order ID,Doctor Email,Drug Name,Quantity,Total Price,Order Date"));
    }

    @Test
    void generateCSV_ShouldContainOrderData() {
        when(reportRepository.findByStatus(OrderStatus.PICKED_UP))
                .thenReturn(Arrays.asList(order1));
        when(reportRepository.getTotalRevenue()).thenReturn(250.0);

        String csv = reportService.generateCSV();

        // All order fields should appear in the CSV
        assertTrue(csv.contains("doctor@pharmacy.com"));
        assertTrue(csv.contains("Paracetamol"));
        assertTrue(csv.contains("250.0"));
    }

    @Test
    void generateCSV_ShouldContainTotalRevenueAndOrderCount() {
        when(reportRepository.findByStatus(OrderStatus.PICKED_UP))
                .thenReturn(Arrays.asList(order1, order2));
        when(reportRepository.getTotalRevenue()).thenReturn(400.0);

        String csv = reportService.generateCSV();

        // Summary rows must appear at the bottom of the CSV
        assertTrue(csv.contains("Total Revenue"));
        assertTrue(csv.contains("400.0"));
        assertTrue(csv.contains("Total Orders Completed"));
        assertTrue(csv.contains("2"));
    }

    @Test
    void generateCSV_WhenNoOrders_ShouldReturnHeaderAndZeroSummary() {
        when(reportRepository.findByStatus(OrderStatus.PICKED_UP)).thenReturn(List.of());
        when(reportRepository.getTotalRevenue()).thenReturn(null);

        String csv = reportService.generateCSV();

        assertTrue(csv.contains("Order ID,Doctor Email,Drug Name,Quantity,Total Price,Order Date"));
        // Total revenue should be 0.0 when null from DB
        assertTrue(csv.contains("0.0"));
        assertTrue(csv.contains("Total Orders Completed"));
    }

    @Test
    void generateCSV_ShouldContainMultipleOrderRows() {
        when(reportRepository.findByStatus(OrderStatus.PICKED_UP))
                .thenReturn(Arrays.asList(order1, order2));
        when(reportRepository.getTotalRevenue()).thenReturn(400.0);

        String csv = reportService.generateCSV();

        // Both doctors should appear in the CSV
        assertTrue(csv.contains("doctor@pharmacy.com"));
        assertTrue(csv.contains("doctor2@pharmacy.com"));
        assertTrue(csv.contains("Paracetamol"));
        assertTrue(csv.contains("Ibuprofen"));
    }

    // ===== generatePDF() tests =====

    @Test
    void generatePDF_ShouldReturnNonEmptyByteArray() throws DocumentException {
        when(reportRepository.findByStatus(OrderStatus.PICKED_UP))
                .thenReturn(Arrays.asList(order1, order2));
        when(reportRepository.getTotalRevenue()).thenReturn(400.0);

        byte[] pdf = reportService.generatePDF();

        // PDF output must not be null or empty
        assertNotNull(pdf);
        assertTrue(pdf.length > 0);
    }

    @Test
    void generatePDF_ShouldStartWithPDFHeader() throws DocumentException {
        when(reportRepository.findByStatus(OrderStatus.PICKED_UP))
                .thenReturn(Arrays.asList(order1));
        when(reportRepository.getTotalRevenue()).thenReturn(250.0);

        byte[] pdf = reportService.generatePDF();

        // All valid PDF files start with the magic bytes "%PDF"
        String pdfStart = new String(pdf, 0, 4);
        assertEquals("%PDF", pdfStart);
    }

    @Test
    void generatePDF_WhenNoOrders_ShouldStillReturnValidPDF() throws DocumentException {
        when(reportRepository.findByStatus(OrderStatus.PICKED_UP)).thenReturn(List.of());
        when(reportRepository.getTotalRevenue()).thenReturn(null);

        byte[] pdf = reportService.generatePDF();

        // Even with no orders, PDF generation should not crash
        assertNotNull(pdf);
        assertTrue(pdf.length > 0);
        String pdfStart = new String(pdf, 0, 4);
        assertEquals("%PDF", pdfStart);
    }

    @Test
    void generatePDF_WhenRevenueIsNull_ShouldDefaultToZeroAndNotCrash() throws DocumentException {
        when(reportRepository.findByStatus(OrderStatus.PICKED_UP))
                .thenReturn(Arrays.asList(order1));
        when(reportRepository.getTotalRevenue()).thenReturn(null);

        // Must not throw NullPointerException when revenue is null
        assertDoesNotThrow(() -> reportService.generatePDF());
    }
}