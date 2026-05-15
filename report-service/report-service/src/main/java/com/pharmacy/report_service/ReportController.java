package com.pharmacy.report_service;

import com.itextpdf.text.DocumentException;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "Sales Reports", description = "APIs for viewing and downloading sales reports")
@RestController
@RequestMapping("/reports")
public class ReportController {

    @Autowired
    private ReportService reportService;

    @Operation(summary = "View Sales Report",
        description = "Returns all completed orders (PICKED_UP) with total revenue summary")
    @GetMapping("/sales")
    public SalesReportDTO getSalesReport() {
        return reportService.getSalesReport();
    }

    @Operation(summary = "Download Sales Report as CSV",
        description = "Downloads the sales report as a CSV file")
    @GetMapping("/sales/download")
    public ResponseEntity<String> downloadSalesReport() {
        String csvContent = reportService.generateCSV();
        HttpHeaders headers = new HttpHeaders();
        headers.add(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=sales-report.csv");
        headers.add(HttpHeaders.CONTENT_TYPE, "text/csv");
        return ResponseEntity.ok()
            .headers(headers)
            .contentType(MediaType.parseMediaType("text/csv"))
            .body(csvContent);
    }

    @Operation(summary = "Download Sales Report as PDF",
        description = "Downloads the sales report as a formatted PDF file")
    @GetMapping("/sales/download/pdf")
    public ResponseEntity<byte[]> downloadSalesReportPDF() throws DocumentException {
        byte[] pdfContent = reportService.generatePDF();
        HttpHeaders headers = new HttpHeaders();
        headers.add(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=sales-report.pdf");
        return ResponseEntity.ok()
            .headers(headers)
            .contentType(MediaType.APPLICATION_PDF)
            .body(pdfContent);
    }
}