package com.example.drivingschool.service;

import com.example.drivingschool.model.Registration;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
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

        String[] headers = {"ID", "First Name", "Last Name", "Email", "Phone", "Course Type"};

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

            createCell(row, columnCount++, reg.getId(), style);
            createCell(row, columnCount++, reg.getFirstName(), style);
            createCell(row, columnCount++, reg.getLastName(), style);
            createCell(row, columnCount++, reg.getEmail(), style);
            createCell(row, columnCount++, reg.getPhone(), style);
            createCell(row, columnCount++, reg.getCourseType(), style);
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