package com.example.drivingschool.service;

import com.example.drivingschool.model.Payment;
import com.example.drivingschool.model.Registration;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.math.BigDecimal;
import java.util.List;

public class ExcelExporter {

    private XSSFWorkbook workbook;
    private Sheet sheet;
    private List<Registration> registrations;

    public ExcelExporter(List<Registration> registrations) {
        this.registrations = registrations;
        workbook = new XSSFWorkbook();
    }

    private void writeHeaderLine() {
        sheet = workbook.createSheet("Registrations");
        Row row = sheet.createRow(0);

        CellStyle style = workbook.createCellStyle();
        Font font = workbook.createFont();
        font.setBold(true);
        font.setFontHeight((short) (16 * 20)); // Set font size
        style.setFont(font);

        String[] headers = {"ID", "First Name", "Last Name", "Admission Date", "Phone", "Course Type","Dl Number","LastPayment Date","Total fee","Total paid","Balance"};

        for (int i = 0; i < headers.length; i++) {
            createCell(row, i, headers[i], style);
        }
    }

    private void createCell(Row row, int columnCount, Object value, CellStyle style) {
        sheet.autoSizeColumn(columnCount);
        Cell cell = row.createCell(columnCount);
        if (value instanceof Long) {
            cell.setCellValue((Long) value);
        } else if (value instanceof Integer) {
            cell.setCellValue((Integer) value);
        } else {
            cell.setCellValue((String) value);
        }
        cell.setCellStyle(style);
    }

    private void writeDataLines() {
        int rowCount = 1;

        CellStyle style = workbook.createCellStyle();
        Font font = workbook.createFont();
        font.setFontHeight((short) (14 * 20));
        style.setFont(font);

        for (Registration reg : registrations) {
            Row row = sheet.createRow(rowCount++);
            int columnCount = 0;

            BigDecimal totalPaid = BigDecimal.ZERO;
            if (reg.getPayments() != null) {
                for (Payment p : reg.getPayments()) {
                    if (p.getAmountPaid() != null) {
                        totalPaid = totalPaid.add(BigDecimal.valueOf(p.getAmountPaid()));
                    }
                }
            }

            BigDecimal totalFees = (reg.getTotalFees() != null) ? reg.getTotalFees() : BigDecimal.ZERO;
            BigDecimal balance = totalFees.subtract(totalPaid);

            String paymentDate = null;
            if(reg.getPayments() != null){

                paymentDate = reg.getPayments().getFirst().getPaymentDate().toString();
            }
            createCell(row, columnCount++, reg.getId(), style);
            createCell(row, columnCount++, reg.getFirstName(), style);
            createCell(row, columnCount++, reg.getAddress(), style);
            createCell(row, columnCount++, reg.getAdmissionDate().toString(), style);
            createCell(row, columnCount++, reg.getPhone(), style);
            createCell(row, columnCount++, reg.getCourseType(), style);
            createCell(row, columnCount++, reg.getDlnumber(), style);
            createCell(row, columnCount++, (paymentDate !=null)? paymentDate: "2999-12-12", style);
            createCell(row, columnCount++, totalFees.toString(), style);
            createCell(row, columnCount++, totalPaid.toString(), style);
            createCell(row, columnCount++, balance.toString(), style);
        }
    }

    public void export(HttpServletResponse response) throws IOException {
        writeHeaderLine();
        writeDataLines();

        // Write the workbook to the HTTP response output stream
        workbook.write(response.getOutputStream());
        workbook.close();
    }
}