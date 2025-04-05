package com.kdu.rizzlers.service.impl;

import com.itextpdf.text.*;
import com.itextpdf.text.pdf.PdfPCell;
import com.itextpdf.text.pdf.PdfPTable;
import com.itextpdf.text.pdf.PdfWriter;
import com.kdu.rizzlers.dto.TravelItineraryDto;
import com.kdu.rizzlers.service.EmailService;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.UrlResource;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import java.io.ByteArrayOutputStream;
import java.net.URL;
import java.text.NumberFormat;
import java.time.format.DateTimeFormatter;
import java.util.Locale;

/**
 * Implementation of the EmailService for sending travel itineraries
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class EmailServiceImpl implements EmailService {

    private final JavaMailSender mailSender;
    private final TemplateEngine templateEngine;
    
    @Value("${spring.mail.username}")
    private String senderEmail;
    
    // Using an Unsplash image URL for the logo
    private static final String LOGO_URL = "https://images.unsplash.com/photo-1522199710521-72d69614c702?ixlib=rb-4.0.3&q=85&fm=jpg&crop=entropy&cs=srgb&w=200";
    private static final String TEMPLATE_NAME = "travel-itinerary-email";
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("MMM dd, yyyy");
    private static final NumberFormat CURRENCY_FORMATTER = NumberFormat.getCurrencyInstance(Locale.US);

    @Override
    public boolean sendTravelItineraryEmail(TravelItineraryDto itinerary, String recipientEmail) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");
            
            // Set basic email properties
            helper.setFrom(senderEmail);
            helper.setTo(recipientEmail);
            helper.setSubject("Your Travel Itinerary - Booking #" + itinerary.getBookingId());
            
            // Prepare the email content using Thymeleaf template
            String emailContent = prepareEmailContent(itinerary);
            helper.setText(emailContent, true);
            
            // Add company logo as inline attachment using Unsplash URL
            try {
                helper.addInline("logo", new UrlResource(new URL(LOGO_URL)));
            } catch (Exception e) {
                log.warn("Could not add logo to email: {}", e.getMessage());
                // Continue without the logo if there's an issue
            }
            
            // Generate PDF and attach it
            byte[] pdfBytes = generateItineraryPdf(itinerary);
            helper.addAttachment("travel-itinerary.pdf", new ByteArrayResource(pdfBytes));
            
            // Send the email
            mailSender.send(message);
            log.info("Travel itinerary email sent successfully to {}", recipientEmail);
            return true;
        } catch (MessagingException e) {
            log.error("Failed to send travel itinerary email: {}", e.getMessage());
            return false;
        }
    }
    
    /**
     * Prepares the email content using Thymeleaf template
     */
    private String prepareEmailContent(TravelItineraryDto itinerary) {
        Context context = new Context();
        
        // Add all itinerary data to the context
        context.setVariable("bookingId", itinerary.getBookingId());
        context.setVariable("bookingDate", itinerary.getBookingDate().format(DATE_FORMATTER));
        context.setVariable("travelerName", itinerary.getTravelerName());
        context.setVariable("travelerEmail", itinerary.getTravelerEmail());
        context.setVariable("travelerPhone", itinerary.getTravelerPhone());
        context.setVariable("checkInDate", itinerary.getCheckInDate().format(DATE_FORMATTER));
        context.setVariable("checkOutDate", itinerary.getCheckOutDate().format(DATE_FORMATTER));
        context.setVariable("destination", itinerary.getDestination());
        context.setVariable("totalAmount", CURRENCY_FORMATTER.format(itinerary.getTotalAmount()));
        context.setVariable("promotionApplied", CURRENCY_FORMATTER.format(itinerary.getPromotionApplied()));
        context.setVariable("amountPaid", CURRENCY_FORMATTER.format(itinerary.getAmountPaid()));
        context.setVariable("amountDue", CURRENCY_FORMATTER.format(itinerary.getAmountDue()));
        context.setVariable("paymentMethod", itinerary.getPaymentMethod());
        context.setVariable("billingAddress", itinerary.getBillingAddress());
        
        // Process the template
        return templateEngine.process(TEMPLATE_NAME, context);
    }
    
    /**
     * Generates a PDF version of the travel itinerary
     */
    private byte[] generateItineraryPdf(TravelItineraryDto itinerary) {
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        
        try {
            Document document = new Document();
            PdfWriter.getInstance(document, outputStream);
            document.open();
            
            // Add title
            Font titleFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 18, BaseColor.BLACK);
            Paragraph title = new Paragraph("Travel Itinerary - Booking #" + itinerary.getBookingId(), titleFont);
            title.setAlignment(Element.ALIGN_CENTER);
            document.add(title);
            document.add(Chunk.NEWLINE);
            
            // Add booking details
            addSection(document, "Booking Information");
            addKeyValueRow(document, "Booking ID:", itinerary.getBookingId());
            addKeyValueRow(document, "Booking Date:", itinerary.getBookingDate().format(DATE_FORMATTER));
            document.add(Chunk.NEWLINE);
            
            // Add traveler details
            addSection(document, "Traveler Information");
            addKeyValueRow(document, "Name:", itinerary.getTravelerName());
            addKeyValueRow(document, "Email:", itinerary.getTravelerEmail());
            addKeyValueRow(document, "Phone:", itinerary.getTravelerPhone());
            document.add(Chunk.NEWLINE);
            
            // Add trip details
            addSection(document, "Trip Details");
            addKeyValueRow(document, "Check-in Date:", itinerary.getCheckInDate().format(DATE_FORMATTER));
            addKeyValueRow(document, "Check-out Date:", itinerary.getCheckOutDate().format(DATE_FORMATTER));
            addKeyValueRow(document, "Destination:", itinerary.getDestination());
            document.add(Chunk.NEWLINE);
            
            // Add payment summary table
            addSection(document, "Payment Summary");
            PdfPTable table = new PdfPTable(2);
            table.setWidthPercentage(100);
            
            // Add table headers
            PdfPCell headerCell1 = new PdfPCell(new Phrase("Description"));
            PdfPCell headerCell2 = new PdfPCell(new Phrase("Amount"));
            headerCell1.setBackgroundColor(BaseColor.LIGHT_GRAY);
            headerCell2.setBackgroundColor(BaseColor.LIGHT_GRAY);
            table.addCell(headerCell1);
            table.addCell(headerCell2);
            
            // Add table rows
            table.addCell("Total Amount");
            table.addCell(CURRENCY_FORMATTER.format(itinerary.getTotalAmount()));
            
            table.addCell("Promotion Applied");
            table.addCell(CURRENCY_FORMATTER.format(itinerary.getPromotionApplied()));
            
            table.addCell("Amount Paid");
            table.addCell(CURRENCY_FORMATTER.format(itinerary.getAmountPaid()));
            
            table.addCell("Amount Due at Resort");
            table.addCell(CURRENCY_FORMATTER.format(itinerary.getAmountDue()));
            
            document.add(table);
            document.add(Chunk.NEWLINE);
            
            // Add billing information
            addSection(document, "Billing Information");
            addKeyValueRow(document, "Payment Method:", itinerary.getPaymentMethod());
            addKeyValueRow(document, "Billing Address:", itinerary.getBillingAddress());
            
            // Close the document
            document.close();
            
            return outputStream.toByteArray();
        } catch (DocumentException e) {
            log.error("Error generating PDF: {}", e.getMessage());
            return new byte[0];
        }
    }
    
    /**
     * Adds a section title to the PDF
     */
    private void addSection(Document document, String title) throws DocumentException {
        Font sectionFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 14, BaseColor.BLACK);
        Paragraph section = new Paragraph(title, sectionFont);
        section.setSpacingBefore(10);
        section.setSpacingAfter(5);
        document.add(section);
    }
    
    /**
     * Adds a key-value row to the PDF
     */
    private void addKeyValueRow(Document document, String key, String value) throws DocumentException {
        Font keyFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 12);
        Font valueFont = FontFactory.getFont(FontFactory.HELVETICA, 12);
        
        Paragraph paragraph = new Paragraph();
        paragraph.add(new Chunk(key + " ", keyFont));
        paragraph.add(new Chunk(value, valueFont));
        
        document.add(paragraph);
    }
} 