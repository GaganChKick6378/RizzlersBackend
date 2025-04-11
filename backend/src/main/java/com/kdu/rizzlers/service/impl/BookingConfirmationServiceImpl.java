package com.kdu.rizzlers.service.impl;

import com.kdu.rizzlers.dto.in.BookingConfirmationRequest;
import com.kdu.rizzlers.dto.out.BookingConfirmationDetailsResponse;
import com.kdu.rizzlers.entity.BillingInfo;
import com.kdu.rizzlers.entity.PaymentInfo;
import com.kdu.rizzlers.entity.User;
import com.kdu.rizzlers.entity.UserBooking;
import com.kdu.rizzlers.entity.PropertyPromotionSchedule;
import com.kdu.rizzlers.repository.BillingInfoRepository;
import com.kdu.rizzlers.repository.PaymentInfoRepository;
import com.kdu.rizzlers.repository.ReviewRepository;
import com.kdu.rizzlers.repository.UserBookingRepository;
import com.kdu.rizzlers.repository.UserRepository;
import com.kdu.rizzlers.repository.PropertyPromotionScheduleRepository;
import com.kdu.rizzlers.service.BookingConfirmationService;
import com.kdu.rizzlers.util.EncryptionUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.graphql.client.HttpGraphQlClient;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.List;

@Service
@Slf4j
public class BookingConfirmationServiceImpl implements BookingConfirmationService {

    private final WebClient webClient;
    private final HttpGraphQlClient graphQlClient;
    private final UserBookingRepository userBookingRepository;
    private final UserRepository userRepository;
    private final BillingInfoRepository billingInfoRepository;
    private final PaymentInfoRepository paymentInfoRepository;
    private final ReviewRepository reviewRepository;
    private final PropertyPromotionScheduleRepository propertyPromotionScheduleRepository;
    private final EncryptionUtil encryptionUtil;
    
    public BookingConfirmationServiceImpl(
            @Value("${graphql.endpoint}") String graphqlEndpoint,
            @Value("${graphql.api-key}") String apiKey,
            @Value("${graphql.api-key-header}") String apiKeyHeader,
            UserBookingRepository userBookingRepository,
            UserRepository userRepository,
            BillingInfoRepository billingInfoRepository,
            PaymentInfoRepository paymentInfoRepository,
            ReviewRepository reviewRepository,
            PropertyPromotionScheduleRepository propertyPromotionScheduleRepository,
            EncryptionUtil encryptionUtil) {
        
        this.userBookingRepository = userBookingRepository;
        this.userRepository = userRepository;
        this.billingInfoRepository = billingInfoRepository;
        this.paymentInfoRepository = paymentInfoRepository;
        this.reviewRepository = reviewRepository;
        this.propertyPromotionScheduleRepository = propertyPromotionScheduleRepository;
        this.encryptionUtil = encryptionUtil;
        
        this.webClient = WebClient.builder()
                .baseUrl(graphqlEndpoint)
                .defaultHeader(apiKeyHeader, apiKey)
                .build();
                
        this.graphQlClient = HttpGraphQlClient.builder(webClient)
                .build();
    }
    
    @Override
    public BookingConfirmationDetailsResponse getBookingConfirmationDetails(BookingConfirmationRequest request) {
        Integer bookingId = request.getBookingId();
        Integer guestId = request.getGuestId();
        
        log.info("Fetching booking confirmation details for bookingId={}, guestId={}", bookingId, guestId);
        
        try {
            // 1. First fetch booking financial details from RDS to get room_type_id
            Optional<UserBooking> userBookingOpt = userBookingRepository.findByBookingId(bookingId);
            if (userBookingOpt.isEmpty()) {
                log.warn("No booking financial details found in RDS for bookingId={}", bookingId);
                return createErrorResponse("No booking financial details found for ID: " + bookingId);
            }
            
            UserBooking userBooking = userBookingOpt.get();
            
            // Verify guest ID matches
            if (!guestId.equals(userBooking.getGuestId())) {
                log.warn("Guest ID mismatch. Provided: {}, Expected: {}", guestId, userBooking.getGuestId());
                return createErrorResponse("Guest ID does not match booking records");
            }
            
            // 2. Get room_type_id from UserBooking
            Integer roomTypeId = userBooking.getRoomTypeId();
            if (roomTypeId == null) {
                log.warn("Room type ID is missing for booking ID: {}", bookingId);
                return createErrorResponse("Room type information is missing for this booking");
            }
            
            // 3. Fetch room type details from GraphQL using the room_type_id
            Map<String, Object> roomTypeData = fetchRoomTypeFromGraphQL(roomTypeId);
            
            // 4. Fetch booking details from GraphQL
            Map<String, Object> graphQLBookingData = fetchBookingDetailsFromGraphQL(bookingId);
            if (graphQLBookingData == null || graphQLBookingData.isEmpty()) {
                log.warn("No booking found in GraphQL with ID: {}", bookingId);
                return createErrorResponse("No booking found with ID: " + bookingId);
            }
            
            // 5. Get room image from reviews
            String roomImage = getRoomImageForRoomType(roomTypeId);
            
            // 6. Fetch user information from RDS (by guest ID)
            Optional<User> userOpt = userRepository.findByGuestId(guestId);
            User user = userOpt.orElse(null);
            
            // 7. Fetch billing information
            Optional<BillingInfo> billingInfoOpt = billingInfoRepository.findByBookingId(bookingId);
            BillingInfo billingInfo = billingInfoOpt.orElse(null);
            
            // 8. Fetch payment information
            Optional<PaymentInfo> paymentInfoOpt = paymentInfoRepository.findByBookingId(bookingId);
            PaymentInfo paymentInfo = paymentInfoOpt.orElse(null);
            
            // Build the response
            return buildBookingConfirmationResponse(
                    graphQLBookingData,
                    roomTypeData,
                    roomImage,
                    userBooking,
                    user,
                    billingInfo,
                    paymentInfo
            );
            
        } catch (Exception e) {
            log.error("Error fetching booking confirmation details: {}", e.getMessage(), e);
            return createErrorResponse("Failed to retrieve booking details: " + e.getMessage());
        }
    }
    
    /**
     * Fetch room type details from GraphQL
     * 
     * @param roomTypeId the room type ID
     * @return Map of room type details from GraphQL
     */
    private Map<String, Object> fetchRoomTypeFromGraphQL(Integer roomTypeId) {
        final String roomTypeQuery = """
            query GetRoomType($roomTypeId: Int!) {
              getRoomType(where: {room_type_id: $roomTypeId}) {
                room_type_name
              }
            }
        """;
        
        try {
            Map<String, Object> variables = new HashMap<>();
            variables.put("roomTypeId", roomTypeId);
            
            Map<String, Object> result = graphQlClient.document(roomTypeQuery)
                .variables(variables)
                .retrieve("getRoomType")
                .toEntity(new ParameterizedTypeReference<Map<String, Object>>() {})
                .block();
                
            if (result == null) {
                log.warn("No room type found for ID: {}", roomTypeId);
                return new HashMap<>();
            }
            
            log.info("Successfully fetched room type details from GraphQL for ID: {}", roomTypeId);
            return result;
        } catch (WebClientResponseException e) {
            log.error("GraphQL API error when fetching room type: {}", e.getResponseBodyAsString(), e);
            return new HashMap<>();
        } catch (Exception e) {
            log.error("Error fetching room type from GraphQL: {}", e.getMessage(), e);
            return new HashMap<>();
        }
    }
    
    /**
     * Get room image for a room type from reviews
     * 
     * @param roomTypeId the room type ID
     * @return URL of the first image for the room type, or empty string if not found
     */
    private String getRoomImageForRoomType(Integer roomTypeId) {
        try {
            String imageUrl = reviewRepository.findFirstImageForRoomType(roomTypeId);
            return imageUrl != null ? imageUrl : "";
        } catch (Exception e) {
            log.error("Error fetching room image: {}", e.getMessage(), e);
            return "";
        }
    }
    
    /**
     * Fetch booking details from GraphQL
     * 
     * @param bookingId the booking ID
     * @return Map of booking details from GraphQL
     */
    private Map<String, Object> fetchBookingDetailsFromGraphQL(Integer bookingId) {
        final String bookingQuery = """
            query GetBooking($bookingId: Int!) {
              getBooking(where: {booking_id: $bookingId}) {
                booking_id
                adult_count
                child_count
                check_in_date
                check_out_date
                total_cost
                booking_status {
                  status
                  status_id
                }
                promotion_applied {
                  promotion_id
                  promotion_title
                  promotion_description
                }
              }
            }
        """;
        
        try {
            Map<String, Object> variables = new HashMap<>();
            variables.put("bookingId", bookingId);
            
            Map<String, Object> result = graphQlClient.document(bookingQuery)
                .variables(variables)
                .retrieve("getBooking")
                .toEntity(new ParameterizedTypeReference<Map<String, Object>>() {})
                .block();
                
            if (result == null) {
                return new HashMap<>();
            }
            
            log.info("Successfully fetched booking details from GraphQL for ID: {}", bookingId);
            return result;
        } catch (WebClientResponseException e) {
            log.error("GraphQL API error: {}", e.getResponseBodyAsString(), e);
            return new HashMap<>();
        } catch (Exception e) {
            log.error("Error fetching booking from GraphQL: {}", e.getMessage(), e);
            return new HashMap<>();
        }
    }
    
    /**
     * Build the complete booking confirmation response
     */
    private BookingConfirmationDetailsResponse buildBookingConfirmationResponse(
            Map<String, Object> graphQLBookingData,
            Map<String, Object> roomTypeData,
            String roomImage,
            UserBooking userBooking,
            User user,
            BillingInfo billingInfo,
            PaymentInfo paymentInfo) {
        
        // 1. Build booking details section
        BookingConfirmationDetailsResponse.BookingDetails bookingDetails = buildBookingDetails(
                graphQLBookingData, 
                roomTypeData,
                roomImage,
                userBooking);
        
        // 2. Build room total summary section
        BookingConfirmationDetailsResponse.RoomTotalSummary roomTotalSummary = buildRoomTotalSummary(userBooking);
        
        // 3. Build guest information section
        BookingConfirmationDetailsResponse.GuestInformation guestInformation = buildGuestInformation(user);
        
        // 4. Build billing address section
        BookingConfirmationDetailsResponse.BillingAddress billingAddress = buildBillingAddress(billingInfo);
        
        // 5. Build payment information section
        BookingConfirmationDetailsResponse.PaymentInformation paymentInformation = buildPaymentInformation(paymentInfo);
        
        // Combine all sections into the response
        return BookingConfirmationDetailsResponse.builder()
                .success(true)
                .message("Booking confirmation details retrieved successfully")
                .bookingDetails(bookingDetails)
                .roomTotalSummary(roomTotalSummary)
                .guestInformation(guestInformation)
                .billingAddress(billingAddress)
                .paymentInformation(paymentInformation)
                .build();
    }
    
    /**
     * Build the booking details section
     */
    @SuppressWarnings("unchecked")
    private BookingConfirmationDetailsResponse.BookingDetails buildBookingDetails(
            Map<String, Object> graphQLBookingData,
            Map<String, Object> roomTypeData,
            String roomImage,
            UserBooking userBooking) {
        
        // Extract check-in and check-out dates
        String checkInDateStr = (String) graphQLBookingData.get("check_in_date");
        String checkOutDateStr = (String) graphQLBookingData.get("check_out_date");
        
        // Parse dates from ISO format to LocalDate
        DateTimeFormatter formatter = DateTimeFormatter.ISO_DATE_TIME;
        LocalDate checkInDate = LocalDate.parse(checkInDateStr.substring(0, 19), formatter);
        LocalDate checkOutDate = LocalDate.parse(checkOutDateStr.substring(0, 19), formatter);
        
        // Get room type name from the room type data
        String roomTypeName = roomTypeData != null ? (String) roomTypeData.get("room_type_name") : "Unknown";
        
        // Extract promotion information
        Map<String, Object> promotionApplied = (Map<String, Object>) graphQLBookingData.get("promotion_applied");
        String promotionTitle = "";
        String promotionDescription = "";
        Integer promotionId = null;
        
        // If promotion is not available from GraphQL, try to fetch from RDS
        if (promotionApplied == null || promotionApplied.isEmpty()) {
            log.info("No promotion details found from GraphQL, checking RDS database");
            
            // Get promotion ID from booking if available
            if (userBooking.getPromotionId() != null) {
                promotionId = userBooking.getPromotionId();
                
                // Look up promotion details in the property_promotion_schedule table
                List<PropertyPromotionSchedule> promotions = 
                    propertyPromotionScheduleRepository.findByPromotionId(promotionId);
                
                if (!promotions.isEmpty()) {
                    PropertyPromotionSchedule promotion = promotions.get(0);
                    promotionTitle = promotion.getTitle();
                    promotionDescription = promotion.getDescription();
                    log.info("Found promotion details from RDS for promotionId={}: title={}", 
                            promotionId, promotionTitle);
                } else {
                    log.warn("Promotion ID {} found in booking but no details in RDS", promotionId);
                }
            } else {
                log.info("No promotion ID found in the booking");
            }
        } else {
            promotionId = (Integer) promotionApplied.get("promotion_id");
            promotionTitle = (String) promotionApplied.get("promotion_title");
            promotionDescription = (String) promotionApplied.get("promotion_description");
        }
        
        // Extract booking status
        Map<String, Object> bookingStatus = (Map<String, Object>) graphQLBookingData.get("booking_status");
        String status = "UNKNOWN";
        Integer statusId = null;
        
        if (bookingStatus != null && !bookingStatus.isEmpty()) {
            status = (String) bookingStatus.get("status");
            statusId = (Integer) bookingStatus.get("status_id");
            log.info("Booking status retrieved: {} (ID: {})", status, statusId);
        } else {
            log.warn("No booking status information found for booking ID: {}", 
                     graphQLBookingData.get("booking_id"));
        }
        
        // Build booking details section
        return BookingConfirmationDetailsResponse.BookingDetails.builder()
                .bookingId((Integer) graphQLBookingData.get("booking_id"))
                .roomTypeId(userBooking.getRoomTypeId())
                .roomTypeName(roomTypeName)
                .roomImage(roomImage)
                .adultCount((Integer) graphQLBookingData.get("adult_count"))
                .childCount((Integer) graphQLBookingData.get("child_count"))
                .checkInDate(checkInDate)
                .checkOutDate(checkOutDate)
                .promotionId(promotionId)
                .promotionTitle(promotionTitle)
                .promotionDescription(promotionDescription)
                .averageNightlyPrice(userBooking.getAverageNightlyPrice())
                .status(status)
                .statusId(statusId)
                .build();
    }
    
    /**
     * Build the room total summary section
     */
    private BookingConfirmationDetailsResponse.RoomTotalSummary buildRoomTotalSummary(UserBooking userBooking) {
        return BookingConfirmationDetailsResponse.RoomTotalSummary.builder()
                .nightlyRate(userBooking.getNightlyRate())
                .subtotal(userBooking.getSubtotal())
                .taxesAndFees(userBooking.getTaxesAndFees())
                .totalForStay(userBooking.getTotalForStay())
                .build();
    }
    
    /**
     * Build the guest information section
     */
    private BookingConfirmationDetailsResponse.GuestInformation buildGuestInformation(User user) {
        // If user is null, return empty guest information
        if (user == null) {
            return BookingConfirmationDetailsResponse.GuestInformation.builder()
                    .firstName("")
                    .lastName("")
                    .phone("")
                    .email("")
                    .build();
        }
        
        return BookingConfirmationDetailsResponse.GuestInformation.builder()
                .firstName(user.getFirstName())
                .lastName(user.getLastName())
                .phone(user.getPhone())
                .email(user.getEmail())
                .build();
    }
    
    /**
     * Build the billing address section
     */
    private BookingConfirmationDetailsResponse.BillingAddress buildBillingAddress(BillingInfo billingInfo) {
        // If billing info is null, return empty billing address
        if (billingInfo == null) {
            return BookingConfirmationDetailsResponse.BillingAddress.builder()
                    .firstName("")
                    .lastName("")
                    .mailingAddress1("")
                    .mailingAddress2("")
                    .country("")
                    .city("")
                    .state("")
                    .zip("")
                    .phone("")
                    .email("")
                    .build();
        }
        
        return BookingConfirmationDetailsResponse.BillingAddress.builder()
                .firstName(billingInfo.getFirstName())
                .lastName(billingInfo.getLastName())
                .mailingAddress1(billingInfo.getMailingAddress1())
                .mailingAddress2(billingInfo.getMailingAddress2())
                .country(billingInfo.getCountry())
                .city(billingInfo.getCity())
                .state(billingInfo.getState())
                .zip(billingInfo.getZip())
                .phone(billingInfo.getPhone())
                .email(billingInfo.getEmail())
                .build();
    }
    
    /**
     * Build the payment information section
     */
    private BookingConfirmationDetailsResponse.PaymentInformation buildPaymentInformation(PaymentInfo paymentInfo) {
        // If payment info is null, return empty payment information
        if (paymentInfo == null) {
            return BookingConfirmationDetailsResponse.PaymentInformation.builder()
                    .maskedCardNumber("")
                    .expMonth("")
                    .expYear("")
                    .specialOffers(false)
                    .agreedToTerms(false)
                    .build();
        }
        
        // Decrypt and mask card number (show only last 4 digits)
        String maskedCardNumber = "";
        try {
            if (paymentInfo.getCardNumber() != null && !paymentInfo.getCardNumber().isEmpty()) {
                String decryptedCardNumber = encryptionUtil.decrypt(paymentInfo.getCardNumber());
                if (decryptedCardNumber != null && decryptedCardNumber.length() >= 4) {
                    int length = decryptedCardNumber.length();
                    maskedCardNumber = "X".repeat(length - 4) + decryptedCardNumber.substring(length - 4);
                }
            }
        } catch (Exception e) {
            log.error("Error decrypting card number: {}", e.getMessage());
            maskedCardNumber = "XXXX-XXXX-XXXX-XXXX"; // Fallback
        }
        
        return BookingConfirmationDetailsResponse.PaymentInformation.builder()
                .maskedCardNumber(maskedCardNumber)
                .expMonth(paymentInfo.getExpMonth())
                .expYear(paymentInfo.getExpYear())
                .specialOffers(paymentInfo.getSpecialOffers())
                .agreedToTerms(paymentInfo.getAgreedToTerms())
                .build();
    }
    
    /**
     * Create an error response
     */
    private BookingConfirmationDetailsResponse createErrorResponse(String message) {
        return BookingConfirmationDetailsResponse.builder()
                .success(false)
                .message(message)
                .build();
    }
} 