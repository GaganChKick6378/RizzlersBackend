package com.kdu.rizzlers.service.impl;

import com.itextpdf.text.*;
import com.itextpdf.text.pdf.PdfPCell;
import com.itextpdf.text.pdf.PdfPTable;
import com.itextpdf.text.pdf.PdfWriter;
import com.kdu.rizzlers.dto.BookingDetailsDTO;
import com.kdu.rizzlers.dto.TravelItineraryDto;
import com.kdu.rizzlers.dto.out.BookingConfirmationDetailsResponse;
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
import java.time.LocalDate;
import java.time.ZonedDateTime;
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
    private static final String OTP_TEMPLATE_NAME = "booking-cancellation-otp";
    private static final String MY_BOOKINGS_OTP_TEMPLATE_NAME = "my-bookings-otp";
    private static final String REVIEW_INVITATION_TEMPLATE_NAME = "review-invitation";
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
    
    @Override
    public boolean sendTravelItineraryEmail(BookingConfirmationDetailsResponse bookingDetails, String recipientEmail) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");
            
            // Set basic email properties
            helper.setFrom(senderEmail);
            helper.setTo(recipientEmail);
            helper.setSubject("Your Travel Itinerary - Booking #" + bookingDetails.getBookingDetails().getBookingId());
            
            // Prepare the email content using Thymeleaf template
            String emailContent = prepareEmailContent(bookingDetails);
            helper.setText(emailContent, true);
            
            // Add company logo as inline attachment using Unsplash URL
            try {
                helper.addInline("logo", new UrlResource(new URL(LOGO_URL)));
            } catch (Exception e) {
                log.warn("Could not add logo to email: {}", e.getMessage());
                // Continue without the logo if there's an issue
            }
            
            // Generate PDF and attach it
            byte[] pdfBytes = generateItineraryPdf(bookingDetails);
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
     * Prepares the email content using Thymeleaf template
     */
    private String prepareEmailContent(BookingConfirmationDetailsResponse bookingDetails) {
        Context context = new Context();
        
        // Extract necessary details from the response
        Integer bookingId = bookingDetails.getBookingDetails().getBookingId();
        String travelerName = bookingDetails.getGuestInformation().getFirstName() + " " + 
                              bookingDetails.getGuestInformation().getLastName();
        String travelerEmail = bookingDetails.getGuestInformation().getEmail();
        String travelerPhone = bookingDetails.getGuestInformation().getPhone();
        LocalDate checkInDate = bookingDetails.getBookingDetails().getCheckInDate();
        LocalDate checkOutDate = bookingDetails.getBookingDetails().getCheckOutDate();
        
        // Create a better destination string
        String destination;
        try {
            // Try to extract property name from image URL if possible
            String imageUrl = bookingDetails.getBookingDetails().getRoomImage();
            if (imageUrl != null && imageUrl.contains("cloudfront.net")) {
                String imageName = imageUrl.substring(imageUrl.lastIndexOf("/") + 1);
                String propertyName = imageName.split("\\.")[0].replace("Room", "Resort ");
                destination = bookingDetails.getBookingDetails().getRoomTypeName() + " at " + propertyName;
            } else {
                destination = bookingDetails.getBookingDetails().getRoomTypeName() + " Room";
            }
        } catch (Exception e) {
            destination = bookingDetails.getBookingDetails().getRoomTypeName() + " Room";
            log.warn("Error parsing destination from room image: {}", e.getMessage());
        }
        
        // Financial details
        double totalAmount = bookingDetails.getRoomTotalSummary().getTotalForStay().doubleValue();
        double promotionApplied = 0.0; // Default if no promotion
        if (bookingDetails.getBookingDetails().getPromotionTitle() != null && 
            !bookingDetails.getBookingDetails().getPromotionTitle().isEmpty()) {
            // Calculate promotion discount (estimated as 10% of total if not available)
            promotionApplied = totalAmount * 0.1; 
        }
        
        double amountPaid = totalAmount * 0.5; // Assuming 50% paid upfront
        double amountDue = totalAmount - amountPaid;
        
        // Payment method and billing address
        String paymentMethod = "Credit Card (" + bookingDetails.getPaymentInformation().getMaskedCardNumber() + ")";
        String billingAddress = formatBillingAddress(bookingDetails.getBillingAddress());
        
        // Add all itinerary data to the context
        context.setVariable("bookingId", bookingId);
        context.setVariable("bookingDate", ZonedDateTime.now().format(DATE_FORMATTER)); // Current date as booking date
        context.setVariable("travelerName", travelerName);
        context.setVariable("travelerEmail", travelerEmail);
        context.setVariable("travelerPhone", travelerPhone);
        context.setVariable("checkInDate", checkInDate.format(DATE_FORMATTER));
        context.setVariable("checkOutDate", checkOutDate.format(DATE_FORMATTER));
        context.setVariable("destination", destination);
        context.setVariable("totalAmount", CURRENCY_FORMATTER.format(totalAmount));
        context.setVariable("promotionApplied", CURRENCY_FORMATTER.format(promotionApplied));
        context.setVariable("amountPaid", CURRENCY_FORMATTER.format(amountPaid));
        context.setVariable("amountDue", CURRENCY_FORMATTER.format(amountDue));
        context.setVariable("paymentMethod", paymentMethod);
        context.setVariable("billingAddress", billingAddress);
        
        // Process the template
        return templateEngine.process(TEMPLATE_NAME, context);
    }
    
    /**
     * Format billing address as a single string
     */
    private String formatBillingAddress(BookingConfirmationDetailsResponse.BillingAddress address) {
        StringBuilder sb = new StringBuilder();
        sb.append(address.getFirstName()).append(" ").append(address.getLastName()).append(", ");
        sb.append(address.getMailingAddress1());
        
        if (address.getMailingAddress2() != null && !address.getMailingAddress2().isEmpty()) {
            sb.append(", ").append(address.getMailingAddress2());
        }
        
        sb.append(", ").append(address.getCity())
          .append(", ").append(address.getState())
          .append(", ").append(address.getZip())
          .append(", ").append(address.getCountry());
        
        return sb.toString();
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
     * Generates a PDF version of the travel itinerary
     */
    private byte[] generateItineraryPdf(BookingConfirmationDetailsResponse bookingDetails) {
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        
        try {
            Document document = new Document();
            PdfWriter.getInstance(document, outputStream);
            document.open();
            
            // Add title
            Font titleFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 18, BaseColor.BLACK);
            Paragraph title = new Paragraph("Travel Itinerary - Booking #" + 
                                           bookingDetails.getBookingDetails().getBookingId(), titleFont);
            title.setAlignment(Element.ALIGN_CENTER);
            document.add(title);
            document.add(Chunk.NEWLINE);
            
            // Extract necessary data for the PDF
            String travelerName = bookingDetails.getGuestInformation().getFirstName() + " " + 
                                 bookingDetails.getGuestInformation().getLastName();
            
            // Create destination string for PDF with better error handling
            String destination;
            try {
                // Try to extract property name from image URL if possible
                String imageUrl = bookingDetails.getBookingDetails().getRoomImage();
                if (imageUrl != null && imageUrl.contains("cloudfront.net")) {
                    String imageName = imageUrl.substring(imageUrl.lastIndexOf("/") + 1);
                    String propertyName = imageName.split("\\.")[0].replace("Room", "Resort ");
                    destination = bookingDetails.getBookingDetails().getRoomTypeName() + " at " + propertyName;
                } else {
                    destination = bookingDetails.getBookingDetails().getRoomTypeName() + " Room";
                }
            } catch (Exception e) {
                destination = bookingDetails.getBookingDetails().getRoomTypeName() + " Room";
                log.warn("Error parsing destination for PDF: {}", e.getMessage());
            }
            
            // Financial details
            double totalAmount = bookingDetails.getRoomTotalSummary().getTotalForStay().doubleValue();
            double promotionApplied = 0.0; // Default if no promotion
            if (bookingDetails.getBookingDetails().getPromotionTitle() != null && 
                !bookingDetails.getBookingDetails().getPromotionTitle().isEmpty()) {
                promotionApplied = totalAmount * 0.1; // Estimated as 10% of total
            }
            double amountPaid = totalAmount * 0.5; // Assuming 50% paid upfront
            double amountDue = totalAmount - amountPaid;
            
            // Add booking details
            addSection(document, "Booking Information");
            addKeyValueRow(document, "Booking ID:", bookingDetails.getBookingDetails().getBookingId().toString());
            addKeyValueRow(document, "Booking Date:", ZonedDateTime.now().format(DATE_FORMATTER));
            document.add(Chunk.NEWLINE);
            
            // Add traveler details
            addSection(document, "Traveler Information");
            addKeyValueRow(document, "Name:", travelerName);
            addKeyValueRow(document, "Email:", bookingDetails.getGuestInformation().getEmail());
            addKeyValueRow(document, "Phone:", bookingDetails.getGuestInformation().getPhone());
            document.add(Chunk.NEWLINE);
            
            // Add trip details
            addSection(document, "Trip Details");
            addKeyValueRow(document, "Check-in Date:", bookingDetails.getBookingDetails().getCheckInDate().format(DATE_FORMATTER));
            addKeyValueRow(document, "Check-out Date:", bookingDetails.getBookingDetails().getCheckOutDate().format(DATE_FORMATTER));
            addKeyValueRow(document, "Destination:", destination);
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
            table.addCell(CURRENCY_FORMATTER.format(totalAmount));
            
            table.addCell("Promotion Applied");
            table.addCell(CURRENCY_FORMATTER.format(promotionApplied));
            
            table.addCell("Amount Paid");
            table.addCell(CURRENCY_FORMATTER.format(amountPaid));
            
            table.addCell("Amount Due at Resort");
            table.addCell(CURRENCY_FORMATTER.format(amountDue));
            
            document.add(table);
            document.add(Chunk.NEWLINE);
            
            // Add billing information
            addSection(document, "Billing Information");
            addKeyValueRow(document, "Payment Method:", "Credit Card (" + 
                          bookingDetails.getPaymentInformation().getMaskedCardNumber() + ")");
            addKeyValueRow(document, "Billing Address:", formatBillingAddress(bookingDetails.getBillingAddress()));
            
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

    @Override
    public void sendOtpEmail(String to, String otp, BookingDetailsDTO bookingDetails) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");
            
            // Set email properties
            helper.setFrom(senderEmail);
            helper.setTo(to);
            helper.setSubject("Booking Cancellation OTP - Booking #" + bookingDetails.getBookingId());
            
            // Prepare email content using Thymeleaf template
            String emailContent = prepareOtpEmailContent(otp, bookingDetails);
            helper.setText(emailContent, true);
            
            // Add logo
            try {
                helper.addInline("logo", new UrlResource(new URL(LOGO_URL)));
            } catch (Exception e) {
                log.warn("Could not add logo to email: {}", e.getMessage());
            }
            
            // Send the email
            mailSender.send(message);
            log.info("OTP email sent successfully to {}", to);
        } catch (MessagingException e) {
            log.error("Failed to send OTP email: {}", e.getMessage());
            throw new RuntimeException("Failed to send OTP email", e);
        }
    }
    
    /**
     * Prepares the OTP email content using Thymeleaf template
     */
    private String prepareOtpEmailContent(String otp, BookingDetailsDTO booking) {
        Context context = new Context();
        
        // Add booking and OTP data to context
        context.setVariable("otp", otp);
        context.setVariable("bookingId", booking.getBookingId());
        context.setVariable("propertyName", booking.getPropertyName());
        context.setVariable("checkInDate", booking.getCheckInDate().format(DATE_FORMATTER));
        context.setVariable("checkOutDate", booking.getCheckOutDate().format(DATE_FORMATTER));
        context.setVariable("guestName", booking.getGuestName());
        
        // Process template
        return templateEngine.process(OTP_TEMPLATE_NAME, context);
    }
    
    @Override
    public void sendMyBookingsOtpEmail(String to, String otp, String guestName, String propertyName) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");
            
            // Set email properties
            helper.setFrom(senderEmail);
            helper.setTo(to);
            helper.setSubject("Your Bookings - Identity Verification");
            
            // Prepare email content using Thymeleaf template
            String emailContent = prepareMyBookingsOtpEmailContent(otp, guestName, propertyName);
            helper.setText(emailContent, true);
            
            // Add logo
            try {
                helper.addInline("logo", new UrlResource(new URL(LOGO_URL)));
            } catch (Exception e) {
                log.warn("Could not add logo to email: {}", e.getMessage());
            }
            
            // Send the email
            mailSender.send(message);
            log.info("My Bookings OTP email sent successfully to {}", to);
        } catch (MessagingException e) {
            log.error("Failed to send My Bookings OTP email: {}", e.getMessage());
            throw new RuntimeException("Failed to send My Bookings OTP email", e);
        }
    }
    
    /**
     * Prepares the My Bookings OTP email content using Thymeleaf template
     */
    private String prepareMyBookingsOtpEmailContent(String otp, String guestName, String propertyName) {
        Context context = new Context();
        
        // Add guest and OTP data to context
        context.setVariable("otp", otp);
        context.setVariable("guestName", guestName);
        context.setVariable("propertyName", propertyName);
        
        // Process template
        return templateEngine.process(MY_BOOKINGS_OTP_TEMPLATE_NAME, context);
    }

    /**
     * Implements the sending of review invitation emails
     */
    @Override
    public boolean sendReviewInvitationEmail(String to, String reviewLink, String guestName, String propertyName, int expiryDays) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");
            
            // Set email properties
            helper.setFrom(senderEmail);
            helper.setTo(to);
            helper.setSubject("Tell us about your stay - Your feedback matters!");
            
            // Try to prepare email content using Thymeleaf template
            String emailContent;
            try {
                emailContent = prepareReviewInvitationEmailContent(reviewLink, guestName, propertyName, expiryDays);
            } catch (Exception e) {
                log.warn("Could not process Thymeleaf template, falling back to plain text: {}", e.getMessage());
                // Create a simple plain text fallback
                emailContent = "Hello " + guestName + ",\n\n" +
                        "Thank you for choosing to stay with us at " + propertyName + ". " +
                        "We hope you had a memorable experience!\n\n" +
                        "Your feedback is incredibly important to us. Would you take a moment to share your thoughts about your recent stay?\n\n" +
                        "Click here to write your review: " + reviewLink + "\n\n" +
                        "Please note: This review link will expire in " + expiryDays + " days.\n\n" +
                        "Thank you for your time and we look forward to welcoming you again!\n\n" +
                        "Best regards,\n" +
                        "The " + propertyName + " Team";
                
                // Set as plain text
                helper.setText(emailContent, false);
                // Return early since we're using plain text
                mailSender.send(message);
                log.info("Review invitation email (plain text fallback) sent successfully to {}", to);
                return true;
            }
            
            // Set HTML content if Thymeleaf template was processed successfully
            helper.setText(emailContent, true);
            
            // Add logo
            try {
                helper.addInline("logo", new UrlResource(new URL(LOGO_URL)));
            } catch (Exception e) {
                log.warn("Could not add logo to email: {}", e.getMessage());
            }
            
            // Send the email
            mailSender.send(message);
            log.info("Review invitation email sent successfully to {}", to);
            return true;
        } catch (MessagingException e) {
            log.error("Failed to send review invitation email: {}", e.getMessage());
            return false;
        }
    }
    
    /**
     * Prepares the review invitation email content using Thymeleaf template
     */
    private String prepareReviewInvitationEmailContent(String reviewLink, String guestName, String propertyName, int expiryDays) {
        Context context = new Context();
        
        // Add data to context
        context.setVariable("name", guestName);
        context.setVariable("propertyName", propertyName);
        context.setVariable("reviewLink", reviewLink);
        context.setVariable("expiryDays", expiryDays);
        
        // Process template
        return templateEngine.process(REVIEW_INVITATION_TEMPLATE_NAME, context);
    }
} 