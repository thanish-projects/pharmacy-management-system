package com.pharmacy.report_service;

import com.itextpdf.text.*;
import com.itextpdf.text.pdf.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.io.ByteArrayOutputStream;
import java.util.List;

@Service
public class ReportService {

    @Autowired
    private SalesReportRepository reportRepository;

    public SalesReportDTO getSalesReport() {
        List<SalesOrder> completedOrders =
            reportRepository.findByStatus(OrderStatus.PICKED_UP);
        Double revenue = reportRepository.getTotalRevenue();
        double totalRevenue = (revenue != null) ? revenue : 0.0;
        return new SalesReportDTO(completedOrders, totalRevenue);
    }

    public String generateCSV() {
        List<SalesOrder> sales =
            reportRepository.findByStatus(OrderStatus.PICKED_UP);
        StringBuilder csv = new StringBuilder();
        csv.append("Order ID,Doctor Email,Drug Name,Quantity,Total Price,Order Date\n");
        for (SalesOrder order : sales) {
            csv.append(order.getId()).append(",");
            csv.append(order.getDoctorEmail()).append(",");
            csv.append(order.getDrugName()).append(",");
            csv.append(order.getQuantity()).append(",");
            csv.append(order.getTotalPrice()).append(",");
            csv.append(order.getOrderDate()).append("\n");
        }
        Double revenue = reportRepository.getTotalRevenue();
        double totalRevenue = (revenue != null) ? revenue : 0.0;
        csv.append("\nTotal Revenue,,,,,").append(totalRevenue).append("\n");
        csv.append("Total Orders Completed,,,,,").append(sales.size()).append("\n");
        return csv.toString();
    }

    public byte[] generatePDF() throws DocumentException {
        List<SalesOrder> sales =
            reportRepository.findByStatus(OrderStatus.PICKED_UP);
        Double revenue = reportRepository.getTotalRevenue();
        double totalRevenue = (revenue != null) ? revenue : 0.0;

        Document document = new Document(PageSize.A4.rotate());
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        PdfWriter.getInstance(document, out);
        document.open();

        // Title
        Font titleFont = new Font(Font.FontFamily.HELVETICA, 18, Font.BOLD);
        Paragraph title = new Paragraph("Pharmacy Sales Report", titleFont);
        title.setAlignment(Element.ALIGN_CENTER);
        title.setSpacingAfter(20);
        document.add(title);

        // Table with 6 columns
        PdfPTable table = new PdfPTable(6);
        table.setWidthPercentage(100);
        table.setWidths(new float[]{1f, 3f, 3f, 1.5f, 2f, 3f});

        // Header row
        Font headerFont = new Font(Font.FontFamily.HELVETICA, 11, Font.BOLD);
        BaseColor headerColor = new BaseColor(63, 81, 181);
        for (String col : new String[]{"Order ID", "Doctor Email", "Drug Name", "Quantity", "Total Price", "Order Date"}) {
            PdfPCell cell = new PdfPCell(new Phrase(col, headerFont));
            cell.setBackgroundColor(headerColor);
            cell.setHorizontalAlignment(Element.ALIGN_CENTER);
            cell.setPadding(8);
            // Set header text color to white
            Font whiteFont = new Font(Font.FontFamily.HELVETICA, 11, Font.BOLD, BaseColor.WHITE);
            cell.setPhrase(new Phrase(col, whiteFont));
            table.addCell(cell);
        }

        // Data rows
        Font cellFont = new Font(Font.FontFamily.HELVETICA, 10);
        boolean alternate = false;
        for (SalesOrder order : sales) {
            BaseColor rowColor = alternate ? new BaseColor(232, 234, 246) : BaseColor.WHITE;
            String[] values = {
                String.valueOf(order.getId()),
                order.getDoctorEmail(),
                order.getDrugName(),
                String.valueOf(order.getQuantity()),
                String.format("%.2f", order.getTotalPrice()),
                order.getOrderDate() != null ? order.getOrderDate().toString() : ""
            };
            for (String val : values) {
                PdfPCell cell = new PdfPCell(new Phrase(val, cellFont));
                cell.setBackgroundColor(rowColor);
                cell.setPadding(6);
                cell.setHorizontalAlignment(Element.ALIGN_CENTER);
                table.addCell(cell);
            }
            alternate = !alternate;
        }

        document.add(table);

        // Summary
        Font summaryFont = new Font(Font.FontFamily.HELVETICA, 12, Font.BOLD);
        Paragraph summary = new Paragraph();
        summary.setSpacingBefore(20);
        summary.add(new Chunk("Total Orders Completed: " + sales.size() + "\n", summaryFont));
        summary.add(new Chunk("Total Revenue: Rs. " + String.format("%.2f", totalRevenue), summaryFont));
        document.add(summary);

        document.close();
        return out.toByteArray();
    }
}