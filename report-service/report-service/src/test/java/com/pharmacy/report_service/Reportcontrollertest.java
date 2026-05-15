package com.pharmacy.report_service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Arrays;
import java.util.List;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(
    controllers = ReportController.class,
    excludeAutoConfiguration = {
        SecurityAutoConfiguration.class,
        org.springframework.boot.autoconfigure.orm.jpa.HibernateJpaAutoConfiguration.class,
        org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration.class
    }
)
class ReportControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private ReportService reportService;

    private SalesOrder order1;
    private SalesOrder order2;
    private SalesReportDTO sampleReport;

    @BeforeEach
    void setUp() {
        order1 = new SalesOrder();
        order1.setId(1);
        order1.setDoctorEmail("doctor@pharmacy.com");
        order1.setDrugName("Paracetamol");
        order1.setQuantity(5);
        order1.setTotalPrice(250.0);
        order1.setStatus(OrderStatus.PICKED_UP);

        order2 = new SalesOrder();
        order2.setId(2);
        order2.setDoctorEmail("doctor2@pharmacy.com");
        order2.setDrugName("Ibuprofen");
        order2.setQuantity(3);
        order2.setTotalPrice(150.0);
        order2.setStatus(OrderStatus.PICKED_UP);

        sampleReport = new SalesReportDTO(Arrays.asList(order1, order2), 400.0);
    }

    // ===== GET /reports/sales =====

    @Test
    void getSalesReport_ShouldReturn200WithReport() throws Exception {
        when(reportService.getSalesReport()).thenReturn(sampleReport);

        mockMvc.perform(get("/reports/sales"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalRevenue").value(400.0))
                .andExpect(jsonPath("$.sales.length()").value(2))
                .andExpect(jsonPath("$.sales[0].drugName").value("Paracetamol"))
                .andExpect(jsonPath("$.sales[1].drugName").value("Ibuprofen"));
    }

    @Test
    void getSalesReport_WhenNoOrders_ShouldReturnEmptyReport() throws Exception {
        SalesReportDTO emptyReport = new SalesReportDTO(List.of(), 0.0);
        when(reportService.getSalesReport()).thenReturn(emptyReport);

        mockMvc.perform(get("/reports/sales"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalRevenue").value(0.0))
                .andExpect(jsonPath("$.sales.length()").value(0));
    }

    // ===== GET /reports/sales/download (CSV) =====

    @Test
    void downloadSalesReport_ShouldReturn200WithCSVContent() throws Exception {
        String csvContent = "Order ID,Doctor Email,Drug Name,Quantity,Total Price,Order Date\n"
                + "1,doctor@pharmacy.com,Paracetamol,5,250.0,2026-01-01\n"
                + "\nTotal Revenue,,,,,400.0\n"
                + "Total Orders Completed,,,,,1\n";

        when(reportService.generateCSV()).thenReturn(csvContent);

        mockMvc.perform(get("/reports/sales/download"))
                .andExpect(status().isOk())
                .andExpect(header().string("Content-Disposition", "attachment; filename=sales-report.csv"))
                .andExpect(content().contentTypeCompatibleWith("text/csv"))
                .andExpect(content().string(csvContent));
    }

    @Test
    void downloadSalesReport_WhenNoOrders_ShouldReturnEmptyCSV() throws Exception {
        String emptyCSV = "Order ID,Doctor Email,Drug Name,Quantity,Total Price,Order Date\n"
                + "\nTotal Revenue,,,,,0.0\n"
                + "Total Orders Completed,,,,,0\n";

        when(reportService.generateCSV()).thenReturn(emptyCSV);

        mockMvc.perform(get("/reports/sales/download"))
                .andExpect(status().isOk())
                .andExpect(header().string("Content-Disposition", "attachment; filename=sales-report.csv"))
                .andExpect(content().string(emptyCSV));
    }

    // ===== GET /reports/sales/download/pdf =====

    @Test
    void downloadSalesReportPDF_ShouldReturn200WithPDFBytes() throws Exception {
        byte[] fakePdf = new byte[]{1, 2, 3, 4, 5};
        when(reportService.generatePDF()).thenReturn(fakePdf);

        mockMvc.perform(get("/reports/sales/download/pdf"))
                .andExpect(status().isOk())
                .andExpect(header().string("Content-Disposition", "attachment; filename=sales-report.pdf"))
                .andExpect(content().contentType("application/pdf"));
    }

    @Test
    void downloadSalesReportPDF_WhenEmptyOrders_ShouldStillReturn200() throws Exception {
        // Even with no orders, PDF generation should succeed and return 200
        byte[] emptyPdf = new byte[]{37, 80, 68, 70}; // %PDF magic bytes
        when(reportService.generatePDF()).thenReturn(emptyPdf);

        mockMvc.perform(get("/reports/sales/download/pdf"))
                .andExpect(status().isOk())
                .andExpect(header().string("Content-Disposition", "attachment; filename=sales-report.pdf"))
                .andExpect(content().contentType("application/pdf"));
    }
}