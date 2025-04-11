package com.kdu.rizzlers.service.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.kdu.rizzlers.dto.ReviewValidationResponseDTO;
import com.kdu.rizzlers.dto.graphql.BookingWithCheckoutDTO;
import com.kdu.rizzlers.dto.graphql.BookingsResponseDTO;
import com.kdu.rizzlers.dto.graphql.GraphQLResponseDTO;
import com.kdu.rizzlers.entity.ReviewInvitation;
import com.kdu.rizzlers.entity.User;
import com.kdu.rizzlers.repository.ReviewInvitationRepository;
import com.kdu.rizzlers.repository.UserRepository;
import com.kdu.rizzlers.service.EmailService;
import com.kdu.rizzlers.service.ReviewInvitationService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;

import java.time.LocalDate;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Implementation of the ReviewInvitationService
 */
@Service
@Slf4j
public class ReviewInvitationServiceImpl implements ReviewInvitationService {

    private final ReviewInvitationRepository reviewInvitationRepository;
    private final UserRepository userRepository;
    private final EmailService emailService;
    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;
    
    private static final int EXPIRY_DAYS = 7;
    
    @Value("${graphql.endpoint}")
    private String graphqlEndpoint;
    
    @Value("${graphql.api-key}")
    private String apiKey;
    
    @Value("${graphql.api-key-header}")
    private String apiKeyHeader;

    @Value("${frontend.review-url}")
    private String frontendReviewUrl;

    @Autowired
    public ReviewInvitationServiceImpl(
            ReviewInvitationRepository reviewInvitationRepository,
            UserRepository userRepository,
            EmailService emailService,
            RestTemplate restTemplate,
            ObjectMapper objectMapper) {
        this.reviewInvitationRepository = reviewInvitationRepository;
        this.userRepository = userRepository;
        this.emailService = emailService;
        this.restTemplate = restTemplate;
        this.objectMapper = objectMapper;
    }

    /**
     * Scheduled job to send review invitations at 12:55 PM daily
     */
    @Scheduled(cron = "0 05 15 * * *")
    @Transactional
    public void scheduledSendReviewInvitations() {
        log.info("Starting scheduled review invitation process");
        LocalDate today = LocalDate.now();
        int sent = sendReviewInvitationsForCheckoutDate(today);
        log.info("Completed scheduled review invitation process. Sent {} invitations", sent);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @Transactional
    public int sendReviewInvitationsForCheckoutDate(LocalDate checkoutDate) {
        log.info("Finding checkouts for date: {}", checkoutDate);
        
        try {
            // Fetch bookings with checkout date matching the target date
            List<BookingWithCheckoutDTO> checkouts = getCheckoutsForDate(checkoutDate);
            log.info("Found {} checkouts for date {}", checkouts.size(), checkoutDate);
            
            if (checkouts.isEmpty()) {
                return 0;
            }
            
            // Extract unique guest IDs
            Set<Integer> guestIds = checkouts.stream()
                    .map(BookingWithCheckoutDTO::getGuestId)
                    .collect(Collectors.toSet());
            
            // Get emails for these guest IDs from the users table
            Map<Integer, User> usersByGuestId = getUsersByGuestIds(guestIds);
            
            // Track successfully sent invitations
            int sentCount = 0;
            
            // For each checkout, create and send a review invitation
            for (BookingWithCheckoutDTO checkout : checkouts) {
                try {
                    Integer bookingId = checkout.getBookingId();
                    Integer guestId = checkout.getGuestId();
                    
                    // Skip if we've already sent an invitation for this booking
                    if (reviewInvitationRepository.existsByBookingId(bookingId)) {
                        log.info("Invitation already exists for booking {}", bookingId);
                        continue;
                    }
                    
                    // Get user email from the map
                    User user = usersByGuestId.get(guestId);
                    if (user == null || user.getEmail() == null || user.getEmail().isEmpty()) {
                        log.warn("Cannot find email for guest ID {}", guestId);
                        continue;
                    }
                    
                    // Get the first room type ID (should be the same for all rooms in the booking)
                    Integer roomTypeId = null;
                    if (checkout.getRoomBooked() != null && !checkout.getRoomBooked().isEmpty() && 
                        checkout.getRoomBooked().get(0).getRoom() != null) {
                        roomTypeId = checkout.getRoomBooked().get(0).getRoom().getRoomTypeId();
                    }
                    
                    // Create and save the invitation
                    String token = generateUniqueToken();
                    ReviewInvitation invitation = ReviewInvitation.builder()
                            .bookingId(bookingId)
                            .guestId(guestId)
                            .guestEmail(user.getEmail())
                            .token(token)
                            .sentAt(ZonedDateTime.now())
                            .expiresAt(ZonedDateTime.now().plusDays(EXPIRY_DAYS))
                            .isCompleted(false)
                            .build();
                    
                    reviewInvitationRepository.save(invitation);
                    
                    // Send the invitation email
                    if (sendReviewInvitationEmail(user, roomTypeId, token)) {
                        sentCount++;
                        log.info("Sent review invitation for booking {} to {}", bookingId, user.getEmail());
                    }
                    
                } catch (Exception e) {
                    log.error("Error processing checkout {}: {}", checkout.getBookingId(), e.getMessage(), e);
                }
            }
            
            return sentCount;
            
        } catch (Exception e) {
            log.error("Error sending review invitations: {}", e.getMessage(), e);
            return 0;
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @Transactional(readOnly = true)
    public ReviewValidationResponseDTO validateToken(String token) {
        if (token == null || token.isEmpty()) {
            return ReviewValidationResponseDTO.builder()
                    .valid(false)
                    .message("Token is missing")
                    .build();
        }
        
        try {
            Optional<ReviewInvitation> invitationOpt = reviewInvitationRepository.findByToken(token);
            
            if (invitationOpt.isEmpty()) {
                return ReviewValidationResponseDTO.builder()
                        .valid(false)
                        .message("Invalid review token")
                        .build();
            }
            
            ReviewInvitation invitation = invitationOpt.get();
            
            // Check if already completed
            if (invitation.getIsCompleted()) {
                return ReviewValidationResponseDTO.builder()
                        .valid(false)
                        .message("This review has already been submitted")
                        .build();
            }
            
            // Check if expired
            if (ZonedDateTime.now().isAfter(invitation.getExpiresAt())) {
                return ReviewValidationResponseDTO.builder()
                        .valid(false)
                        .message("This review link has expired")
                        .build();
            }
            
            // Get the guest name
            Optional<User> userOpt = userRepository.findByGuestId(invitation.getGuestId());
            String guestName = userOpt.map(user -> user.getFirstName() + " " + user.getLastName())
                    .orElse("Guest");
            
            // Need to get property name and room type ID from GraphQL or cache
            String propertyName = "Our Hotel"; // Default fallback
            Integer propertyId = null;
            Integer roomTypeId = null;
            
            // Try to get booking details from GraphQL
            try {
                BookingWithCheckoutDTO booking = getBookingById(invitation.getBookingId());
                if (booking != null) {
                    propertyId = booking.getPropertyId();
                    
                    // Get the room type ID from the first room in the booking
                    if (booking.getRoomBooked() != null && !booking.getRoomBooked().isEmpty() && 
                        booking.getRoomBooked().get(0).getRoom() != null) {
                        roomTypeId = booking.getRoomBooked().get(0).getRoom().getRoomTypeId();
                    }
                    
                    // TODO: get property name from a property repository or service
                }
            } catch (Exception e) {
                log.warn("Error getting booking details for validation: {}", e.getMessage());
            }
            
            return ReviewValidationResponseDTO.builder()
                    .valid(true)
                    .bookingId(invitation.getBookingId())
                    .guestId(invitation.getGuestId())
                    .propertyId(propertyId)
                    .roomTypeId(roomTypeId)
                    .propertyName(propertyName)
                    .guestName(guestName)
                    .build();
            
        } catch (Exception e) {
            log.error("Error validating token: {}", e.getMessage(), e);
            return ReviewValidationResponseDTO.builder()
                    .valid(false)
                    .message("An error occurred while validating your review token")
                    .build();
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @Transactional
    public boolean markInvitationCompleted(String token) {
        try {
            Optional<ReviewInvitation> invitationOpt = reviewInvitationRepository.findByToken(token);
            
            if (invitationOpt.isEmpty()) {
                log.warn("Cannot mark invitation as completed: token not found");
                return false;
            }
            
            ReviewInvitation invitation = invitationOpt.get();
            
            if (invitation.getIsCompleted()) {
                log.info("Invitation already marked as completed");
                return true;
            }
            
            invitation.setIsCompleted(true);
            reviewInvitationRepository.save(invitation);
            
            log.info("Marked invitation with ID {} as completed", invitation.getId());
            return true;
            
        } catch (Exception e) {
            log.error("Error marking invitation as completed: {}", e.getMessage(), e);
            return false;
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @Transactional(readOnly = true)
    public Optional<ReviewInvitation> findByToken(String token) {
        return reviewInvitationRepository.findByToken(token);
    }

    /**
     * Get bookings with checkout date matching the target date
     * @param date the checkout date to match
     * @return list of bookings
     */
    private List<BookingWithCheckoutDTO> getCheckoutsForDate(LocalDate date) {
        try {
            // Format date for GraphQL query - use the exact format needed by the API
            String formattedDate = date.format(DateTimeFormatter.ISO_LOCAL_DATE) + "T00:00:00.000Z";
            
            // Build GraphQL query with equals and exact date format
            String graphQLQuery = String.format(
                "query CheckoutsForDate {\n" +
                "  listBookings(where: {booking_status: {status: {equals: \"BOOKED\"}}, check_out_date: {equals: \"%s\"}}, take: 2000000) {\n" +
                "    check_out_date\n" +
                "    guest_id\n" +
                "    booking_status {\n" +
                "      status\n" +
                "    }\n" +
                "    booking_id\n" +
                "    property_id\n" +
                "    room_booked {\n" +
                "      room {\n" +
                "        room_type_id\n" +
                "        room_id\n" +
                "      }\n" +
                "    }\n" +
                "  }\n" +
                "}", formattedDate);
            
            log.debug("Executing GraphQL query for checkouts: {}", graphQLQuery);
            
            // Set up headers
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.set(apiKeyHeader, apiKey);
            
            // Create the request body
            Map<String, Object> requestBody = new HashMap<>();
            requestBody.put("query", graphQLQuery);
            
            // Create the request entity
            HttpEntity<Map<String, Object>> requestEntity = new HttpEntity<>(requestBody, headers);
            
            // Execute the request
            String response = restTemplate.postForObject(graphqlEndpoint, requestEntity, String.class);
            
            // Add debug log for raw response
            log.debug("GraphQL raw response: {}", response);
            
            // Parse the response
            if (response != null) {
                GraphQLResponseDTO<BookingsResponseDTO> graphQLResponse = 
                        objectMapper.readValue(response, objectMapper.getTypeFactory()
                                .constructParametricType(GraphQLResponseDTO.class, BookingsResponseDTO.class));
                
                if (graphQLResponse.getData() != null && graphQLResponse.getData().getListBookings() != null) {
                    log.debug("Parsed {} bookings from GraphQL response", graphQLResponse.getData().getListBookings().size());
                    return graphQLResponse.getData().getListBookings();
                } else {
                    log.warn("GraphQL response was successful but contained no bookings data");
                }
            }
            
            return Collections.emptyList();
            
        } catch (Exception e) {
            log.error("Error fetching checkouts from GraphQL: {}", e.getMessage(), e);
            return Collections.emptyList();
        }
    }
    
    /**
     * Get a booking by ID from GraphQL
     * @param bookingId the booking ID
     * @return the booking or null if not found
     */
    private BookingWithCheckoutDTO getBookingById(Integer bookingId) {
        try {
            // Build GraphQL query
            String graphQLQuery = String.format(
                "query GetBookingById {\n" +
                "  listBookings(where: {booking_id: {equals: %d}}, take: 1) {\n" +
                "    check_out_date\n" +
                "    guest_id\n" +
                "    booking_status {\n" +
                "      status\n" +
                "    }\n" +
                "    booking_id\n" +
                "    property_id\n" +
                "    room_booked {\n" +
                "      room {\n" +
                "        room_type_id\n" +
                "        room_id\n" +
                "      }\n" +
                "    }\n" +
                "  }\n" +
                "}", bookingId);
            
            log.debug("Executing GraphQL query for booking ID {}: {}", bookingId, graphQLQuery);
            
            // Set up headers
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.set(apiKeyHeader, apiKey);
            
            // Create the request body
            Map<String, Object> requestBody = new HashMap<>();
            requestBody.put("query", graphQLQuery);
            
            // Create the request entity
            HttpEntity<Map<String, Object>> requestEntity = new HttpEntity<>(requestBody, headers);
            
            // Execute the request
            String response = restTemplate.postForObject(graphqlEndpoint, requestEntity, String.class);
            
            // Parse the response
            if (response != null) {
                GraphQLResponseDTO<BookingsResponseDTO> graphQLResponse = 
                        objectMapper.readValue(response, objectMapper.getTypeFactory()
                                .constructParametricType(GraphQLResponseDTO.class, BookingsResponseDTO.class));
                
                if (graphQLResponse.getData() != null && 
                    graphQLResponse.getData().getListBookings() != null && 
                    !graphQLResponse.getData().getListBookings().isEmpty()) {
                    return graphQLResponse.getData().getListBookings().get(0);
                }
            }
            
            return null;
            
        } catch (Exception e) {
            log.error("Error fetching booking from GraphQL: {}", e.getMessage(), e);
            return null;
        }
    }

    /**
     * Get users for a set of guest IDs
     * @param guestIds the set of guest IDs
     * @return map of guest ID to User
     */
    private Map<Integer, User> getUsersByGuestIds(Set<Integer> guestIds) {
        try {
            List<User> users = userRepository.findByGuestIdIn(new ArrayList<>(guestIds));
            
            // Create a map for quick lookup
            Map<Integer, User> userMap = new HashMap<>();
            for (User user : users) {
                if (user.getGuestId() != null) {
                    userMap.put(user.getGuestId(), user);
                }
            }
            
            return userMap;
            
        } catch (Exception e) {
            log.error("Error fetching users by guest IDs: {}", e.getMessage(), e);
            return Collections.emptyMap();
        }
    }

    /**
     * Generate a secure random token
     * @return the generated token
     */
    private String generateUniqueToken() {
        return UUID.randomUUID().toString().replace("-", "");
    }

    /**
     * Send a review invitation email
     * @param user the user to send to
     * @param roomTypeId the room type ID (optional)
     * @param token the unique token
     * @return true if email was sent successfully
     */
    private boolean sendReviewInvitationEmail(User user, Integer roomTypeId, String token) {
        try {
            String reviewLink = frontendReviewUrl + "?token=" + token;
            
            // Get property name from booking data if available
            String propertyName = "Our Hotel";  // Default property name
            if (roomTypeId != null) {
                try {
                    // Try to get the real property name (can be enhanced later)
                    log.debug("Getting property name for room type ID: {}", roomTypeId);
                    // Note: In a real implementation, you would retrieve the property name 
                    // based on the room type ID or from booking details
                } catch (Exception e) {
                    log.warn("Could not get property name for room type ID {}: {}", roomTypeId, e.getMessage());
                }
            }
            
            // Use the new sendReviewInvitationEmail method
            return emailService.sendReviewInvitationEmail(
                user.getEmail(),
                reviewLink,
                user.getFirstName() + " " + user.getLastName(),
                propertyName,
                EXPIRY_DAYS
            );
            
        } catch (Exception e) {
            log.error("Error sending review invitation email: {}", e.getMessage(), e);
            return false;
        }
    }
} 