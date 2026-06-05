package com.medislot.app.service;

import com.lowagie.text.Document;
import com.lowagie.text.Font;
import com.lowagie.text.FontFactory;
import com.lowagie.text.Paragraph;
import com.lowagie.text.pdf.PdfWriter;
import com.medislot.app.entity.Prescription;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.awt.Color;

@Service
public class PdfService {

    public byte[] generatePrescriptionPdf(Prescription prescription) {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        Document document = new Document();
        PdfWriter.getInstance(document, out);
        document.open();

        Font titleFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 22, Color.BLACK);
        Font subtitleFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 14, Color.DARK_GRAY);
        Font normalFont = FontFactory.getFont(FontFactory.HELVETICA, 12, Color.BLACK);

        Paragraph title = new Paragraph("MediSlot - Medical Prescription", titleFont);
        title.setAlignment(Paragraph.ALIGN_CENTER);
        document.add(title);
        document.add(new Paragraph("\n"));

        document.add(new Paragraph("Doctor: Dr. " + prescription.getAppointment().getDoctor().getName(), subtitleFont));
        document.add(new Paragraph("Patient: " + prescription.getAppointment().getPatient().getName(), subtitleFont));
        document.add(new Paragraph("Date: " + prescription.getCreatedAt().toLocalDate(), normalFont));
        document.add(new Paragraph("\n"));
        document.add(new Paragraph("-------------------------------------------------------------------", normalFont));
        document.add(new Paragraph("\n"));

        document.add(new Paragraph("DIAGNOSIS:", subtitleFont));
        document.add(new Paragraph(prescription.getDiagnosis(), normalFont));
        document.add(new Paragraph("\n"));

        document.add(new Paragraph("MEDICINES (AI Formatted):", subtitleFont));
        document.add(new Paragraph(prescription.getMedicines(), normalFont));
        document.add(new Paragraph("\n"));

        if (prescription.getNotes() != null && !prescription.getNotes().trim().isEmpty()) {
            document.add(new Paragraph("ADDITIONAL NOTES:", subtitleFont));
            document.add(new Paragraph(prescription.getNotes(), normalFont));
            document.add(new Paragraph("\n"));
        }

        document.add(new Paragraph("-------------------------------------------------------------------", normalFont));
        Paragraph footer = new Paragraph("This is an electronically generated prescription via MediSlot AI.", FontFactory.getFont(FontFactory.HELVETICA_OBLIQUE, 10, Color.GRAY));
        footer.setAlignment(Paragraph.ALIGN_CENTER);
        document.add(footer);

        document.close();
        return out.toByteArray();
    }
}
