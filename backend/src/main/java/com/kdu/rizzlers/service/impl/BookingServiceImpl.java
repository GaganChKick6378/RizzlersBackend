package com.kdu.rizzlers.service.impl;

import com.kdu.rizzlers.dto.BookingRequest;
import com.kdu.rizzlers.dto.BookingResponse;
import com.kdu.rizzlers.dto.PropertyConfigurationDTO;
import com.kdu.rizzlers.entity.BookingLock;
import com.kdu.rizzlers.entity.PropertyPromotion;
import com.kdu.rizzlers.entity.User;
import com.kdu.rizzlers.entity.BillingInfo;
import com.kdu.rizzlers.entity.PaymentInfo;
import com.kdu.rizzlers.repository.BookingLockRepository;
import com.kdu.rizzlers.repository.PropertyPromotionRepository;
import com.kdu.rizzlers.repository.UserRepository;
import com.kdu.rizzlers.repository.BillingInfoRepository;
import com.kdu.rizzlers.repository.PaymentInfoRepository;
import com.kdu.rizzlers.service.BookingService;
import com.kdu.rizzlers.service.PropertyConfigurationService;
import com.kdu.rizzlers.service.PromotionGraphQLService;
import com.kdu.rizzlers.service.RoomAvailabilityCheckService;
import com.kdu.rizzlers.service.RoomDailyRatesService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.graphql.client.HttpGraphQlClient;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;
import java.util.HashMap;

import com.kdu.rizzlers.util.EncryptionUtil;

/**
 * Implementation of BookingService interface handling room booking operations
 */
@Service
@Slf4j
public class BookingServiceImpl implements BookingService {

    private final BookingLockRepository bookingLockRepository;
    private final RoomAvailabilityCheckService roomAvailabilityCheckService;
    private final HttpGraphQlClient graphQlClient;
    private final WebClient webClient;
    private final String serverId;
    private final UserRepository userRepository;
    private final RoomDailyRatesService dailyRatesService;
    private final PropertyConfigurationService propertyConfigurationService;
    private final PropertyPromotionRepository propertyPromotionRepository;
    private final PromotionGraphQLService promotionGraphQLService;
    private final BillingInfoRepository billingInfoRepository;
    private final PaymentInfoRepository paymentInfoRepository;
    private final EncryptionUtil encryptionUtil;
    
    private static final int LOCK_EXPIRY_MINUTES = 15;
    private static final String GRAPHQL_SOURCE_POSTFIX = "1";
    private static final String RDS_SOURCE_POSTFIX = "2";

    public BookingServiceImpl(
            BookingLockRepository bookingLockRepository,
            RoomAvailabilityCheckService roomAvailabilityCheckService,
            WebClient.Builder webClientBuilder,
            @Value("${graphql.endpoint}") String graphqlEndpoint,
            @Value("${graphql.api-key}") String apiKey,
            @Value("${graphql.api-key-header}") String apiKeyHeader,
            com.kdu.rizzlers.config.ServerConfig serverConfig,
            UserRepository userRepository,
            RoomDailyRatesService dailyRatesService,
            PropertyConfigurationService propertyConfigurationService,
            PropertyPromotionRepository propertyPromotionRepository,
            PromotionGraphQLService promotionGraphQLService,
            BillingInfoRepository billingInfoRepository,
            PaymentInfoRepository paymentInfoRepository,
            EncryptionUtil encryptionUtil) {
        
        this.bookingLockRepository = bookingLockRepository;
        this.roomAvailabilityCheckService = roomAvailabilityCheckService;
        this.serverId = serverConfig.getServerId();
        this.userRepository = userRepository;
        this.dailyRatesService = dailyRatesService;
        this.propertyConfigurationService = propertyConfigurationService;
        this.propertyPromotionRepository = propertyPromotionRepository;
        this.promotionGraphQLService = promotionGraphQLService;
        this.billingInfoRepository = billingInfoRepository;
        this.paymentInfoRepository = paymentInfoRepository;
        this.encryptionUtil = encryptionUtil;
        
        this.webClient = webClientBuilder
                .baseUrl(graphqlEndpoint)
                .defaultHeader(apiKeyHeader, apiKey)
                .build();
                
        this.graphQlClient = HttpGraphQlClient.builder(webClient)
                .build();
    }

    @Override
    @Transactional
    public BookingResponse bookRoom(BookingRequest bookingRequest) {
        List<BookingLock> acquiredLocks = new ArrayList<>();
        
        try {
            log.info("Starting booking process for propertyId={}, roomTypeId={}, startDate={}, endDate={}, roomCount={}",
                    bookingRequest.getPropertyId(), bookingRequest.getRoomTypeId(), 
                    bookingRequest.getStartDate(), bookingRequest.getEndDate(),
                    bookingRequest.getRoomCount());
            
            // Step 1: Clean up expired locks
            try {
                cleanupExpiredLocks();
            } catch (Exception e) {
                log.warn("Error cleaning up expired locks: {}", e.getMessage());
                // Continue with booking process even if cleanup fails
            }
            
            // Step 2: Check if rooms of the requested type are available
            Map<String, Object> availabilityResult;
            try {
                availabilityResult = roomAvailabilityCheckService.checkRoomTypeAvailability(
                        bookingRequest.getPropertyId(),
                        bookingRequest.getRoomTypeId(),
                        bookingRequest.getStartDate(),
                        bookingRequest.getEndDate(),
                        bookingRequest.getRoomCount());
            } catch (Exception e) {
                log.error("Error checking room availability: {}", e.getMessage(), e);
                return BookingResponse.failure("Error checking room availability: " + e.getMessage());
            }
            
            if (availabilityResult.isEmpty()) {
                log.warn("No rooms available for propertyId={}, roomTypeId={}, dates={} to {}",
                        bookingRequest.getPropertyId(), bookingRequest.getRoomTypeId(),
                        bookingRequest.getStartDate(), bookingRequest.getEndDate());
                return BookingResponse.failure("No rooms available for the selected dates and property");
            }
            
            // Step 3: Get available room IDs
            @SuppressWarnings("unchecked")
            List<Integer> availableRoomIds = (List<Integer>) availabilityResult.get("availableRoomIds");
            if (availableRoomIds == null || availableRoomIds.isEmpty()) {
                log.warn("Available room IDs list is empty or null");
                return BookingResponse.failure("No available rooms found");
            }
            
            // Step 4: Filter rooms that are currently locked
            List<Integer> unlocked;
            try {
                unlocked = filterLockedRooms(
                        availableRoomIds,
                        bookingRequest.getPropertyId(),
                        bookingRequest.getStartDate(),
                        bookingRequest.getEndDate().minusDays(1)); // Exclude checkout date from booking period
            } catch (Exception e) {
                log.error("Error filtering locked rooms: {}", e.getMessage(), e);
                return BookingResponse.failure("Error filtering locked rooms: " + e.getMessage());
            }
            
            if (unlocked.isEmpty()) {
                log.warn("All available rooms are currently locked. Please try again later.");
                return BookingResponse.failure("All available rooms are currently locked. Please try again later.");
            }
            
            // Check if enough rooms are available for booking
            int roomCount = bookingRequest.getRoomCount();
            if (unlocked.size() < roomCount) {
                log.warn("Not enough rooms available. Requested: {}, Available: {}", roomCount, unlocked.size());
                return BookingResponse.failure("Not enough rooms available for the selected dates. Please try with fewer rooms or a different date range.");
            }
            
            // Only take the number of rooms requested
            List<Integer> roomsToBook = unlocked.subList(0, roomCount);
            log.info("Selected {} rooms for booking: {}", roomCount, roomsToBook);
            
            // Step 5: First, acquire locks for all rooms
            List<Integer> lockedRoomIds = new ArrayList<>();
            
            String sessionId = UUID.randomUUID().toString();
            for (Integer roomId : roomsToBook) {
                try {
                    Optional<BookingLock> acquiredLock = acquireRoomLock(
                            roomId,
                            bookingRequest.getPropertyId(),
                            bookingRequest.getStartDate(),
                            bookingRequest.getEndDate().minusDays(1), // Exclude checkout date from booking period
                            sessionId);
                    
                    if (acquiredLock.isEmpty()) {
                        log.warn("Failed to acquire lock for room {}", roomId);
                        continue;
                    }
                    
                    acquiredLocks.add(acquiredLock.get());
                    lockedRoomIds.add(roomId);
                    log.info("Successfully acquired lock for roomId={}", roomId);
                } catch (Exception e) {
                    log.error("Error acquiring lock for room {}: {}", roomId, e.getMessage());
                    // Continue with other rooms
                }
            }
            
            if (lockedRoomIds.isEmpty()) {
                log.error("Failed to acquire locks for any rooms");
                return BookingResponse.failure("Failed to acquire locks for any rooms. Please try again later.");
            }
            
            // Step 6: Create a single booking for all rooms
            Integer guestId = null;
            try {
                // Create the guest in GraphQL
                try {
                    guestId = createGuestInGraphQL(bookingRequest.getTravelInfo());
                } catch (RuntimeException e) {
                    log.error("Failed to create guest: {}", e.getMessage());
                    throw new RuntimeException("Guest creation failed: " + e.getMessage(), e);
                }
                
                if (guestId == null) {
                    throw new RuntimeException("Failed to create guest in GraphQL");
                }
                
                // Create a single booking in GraphQL
                Optional<Integer> bookingIdOpt = createBookingInGraphQL(bookingRequest, lockedRoomIds.get(0), guestId);
                if (bookingIdOpt.isEmpty()) {
                    throw new RuntimeException("Failed to create booking in GraphQL");
                }
                Integer bookingId = bookingIdOpt.get();
                log.info("Created booking with ID {} for {} rooms", bookingId, lockedRoomIds.size());
                
                // Update all room availabilities with the same booking ID
                List<Integer> successfullyBookedRoomIds = new ArrayList<>();
                for (int i = 0; i < lockedRoomIds.size(); i++) {
                    Integer roomId = lockedRoomIds.get(i);
                    BookingLock lock = acquiredLocks.get(i);
                    
                    try {
                        boolean availabilitiesUpdated = updateRoomAvailabilities(
                                bookingId,
                                roomId,
                                bookingRequest.getPropertyId(),
                                bookingRequest.getStartDate(),
                                bookingRequest.getEndDate().minusDays(1)); // Exclude checkout date from booking period
                        
                        if (availabilitiesUpdated) {
                            successfullyBookedRoomIds.add(roomId);
                        } else {
                            log.error("Failed to update availabilities for room {}", roomId);
                        }
                    } catch (Exception e) {
                        log.error("Error updating availabilities for room {}: {}", roomId, e.getMessage());
                    }
                }
                
                // Clean up all locks in a new transaction to avoid rollback issues
                cleanupLocks(acquiredLocks);
                
                if (successfullyBookedRoomIds.isEmpty()) {
                    log.error("Failed to associate any rooms with booking {}", bookingId);
                    return BookingResponse.failure("Failed to associate any rooms with the booking. Please try again later.");
                }
                
                log.info("Successfully booked {} rooms out of {} requested with booking ID {}", 
                        successfullyBookedRoomIds.size(), roomCount, bookingId);
                
                // Build and return the success response
                BookingResponse response = buildSuccessResponse(bookingRequest, bookingId, 
                        successfullyBookedRoomIds.get(0), availabilityResult, guestId);
                
                // Save billing and payment information
                try {
                    saveBillingInfo(bookingRequest.getBillingInfo(), bookingId);
                    savePaymentInfo(bookingRequest.getPaymentInfo(), bookingId);
                    log.info("Successfully saved billing and payment information for booking ID {}", bookingId);
                } catch (Exception e) {
                    log.error("Error saving billing or payment information: {}", e.getMessage(), e);
                    // Continue with the booking process even if saving billing/payment info fails
                }
                
                // Add information about all booked rooms
                response.setBookedRoomIds(successfullyBookedRoomIds);
                response.setAllBookingIds(Collections.singletonList(bookingId)); // Only one booking ID now
                response.setTotalRoomsBooked(successfullyBookedRoomIds.size());
                
                return response;
                
            } catch (Exception e) {
                log.error("Error during booking process: {}", e.getMessage(), e);
                
                // Clean up all locks in a new transaction to avoid rollback issues
                cleanupLocks(acquiredLocks);
                
                return BookingResponse.failure("Booking failed: " + e.getMessage());
            }
            
        } catch (Exception e) {
            log.error("Unexpected error during booking process: {}", e.getMessage(), e);
            
            // Clean up all locks in a new transaction to avoid rollback issues
            cleanupLocks(acquiredLocks);
            
            return BookingResponse.failure("An unexpected error occurred. Please try again later.");
        }
    }

    @Override
    @Transactional
    public Optional<BookingLock> acquireRoomLock(
            Integer roomId,
            Integer propertyId,
            LocalDate startDate,
            LocalDate endDate,
            String sessionId) {
        
        try {
            // Check if there's already a lock for this room and dates - use a separate query to avoid flushing issues
            Long existingLockCount = bookingLockRepository.count(
                    (root, query, builder) -> 
                        builder.and(
                            builder.equal(root.get("roomId"), roomId),
                            builder.equal(root.get("startDate"), startDate),
                            builder.equal(root.get("endDate"), endDate),
                            builder.equal(root.get("status"), BookingLock.BookingLockStatus.PENDING)
                        )
            );
            
            if (existingLockCount > 0) {
                log.info("Room {} already has a pending lock for the requested dates", roomId);
                return Optional.empty();
            }
            
            // Create a new lock
            BookingLock lock = BookingLock.builder()
                    .roomId(roomId)
                    .propertyId(propertyId)
                    .startDate(startDate)
                    .endDate(endDate)
                    .lockTimestamp(ZonedDateTime.now())
                    .lockExpiry(ZonedDateTime.now().plusMinutes(LOCK_EXPIRY_MINUTES))
                    .lockOwner(sessionId)
                    .status(BookingLock.BookingLockStatus.PENDING)
                    .serverId(serverId)
                    .statusUpdatedAt(ZonedDateTime.now())
                    .build();
            
            // Save the lock with a unique constraint that will fail if another lock was created in the meantime
            try {
                lock = bookingLockRepository.save(lock);
                bookingLockRepository.flush(); // Explicitly flush to ensure the ID is generated
                log.info("Lock acquired by server {} for room {}, dates {} to {}", 
                        serverId, roomId, startDate, endDate);
                return Optional.of(lock);
            } catch (org.springframework.dao.DataIntegrityViolationException e) {
                // This is expected if another transaction already created a lock
                log.info("Concurrent lock attempt for room {}: {}", roomId, e.getMessage());
                return Optional.empty();
            } catch (Exception e) {
                log.warn("Failed to acquire lock for room {}: {}", roomId, e.getMessage());
                return Optional.empty();
            }
        } catch (Exception e) {
            log.error("Error acquiring room lock: {}", e.getMessage(), e);
            return Optional.empty();
        }
    }

    @Override
    @Transactional
    public boolean releaseRoomLock(Long lockId) {
        try {
            // Delete the lock instead of updating the status
            bookingLockRepository.deleteById(lockId);
            log.info("Deleted booking lock with ID: {}", lockId);
            return true;
        } catch (Exception e) {
            log.error("Error releasing room lock: {}", e.getMessage(), e);
            return false;
        }
    }

    @Override
    @Transactional(readOnly = true)
    public List<Integer> filterLockedRooms(
            List<Integer> availableRoomIds,
            Integer propertyId,
            LocalDate startDate,
            LocalDate endDate) {
        
        try {
            // Find all locks for the given room IDs that overlap with the requested dates
            List<BookingLock> locks = bookingLockRepository.findByRoomIdInAndStartDateLessThanEqualAndEndDateGreaterThanEqualAndStatus(
                    availableRoomIds, endDate, startDate, BookingLock.BookingLockStatus.PENDING);
            
            if (locks.isEmpty()) {
                return availableRoomIds;
            }
            
            // Extract the IDs of locked rooms
            Set<Integer> lockedRoomIds = locks.stream()
                    .map(BookingLock::getRoomId)
                    .collect(Collectors.toSet());
            
            // Return only the room IDs that are not locked
            return availableRoomIds.stream()
                    .filter(id -> !lockedRoomIds.contains(id))
                    .collect(Collectors.toList());
                    
        } catch (Exception e) {
            log.error("Error filtering locked rooms: {}", e.getMessage(), e);
            return new ArrayList<>();
        }
    }

    @Override
    @Transactional
    public int cleanupExpiredLocks() {
        try {
            // Delete expired locks instead of updating their status
            ZonedDateTime now = ZonedDateTime.now();
            return bookingLockRepository.deleteExpiredLocks(now);
        } catch (Exception e) {
            log.error("Error cleaning up expired locks: {}", e.getMessage(), e);
            return 0;
        }
    }
    
    /**
     * Create a guest in GraphQL
     * 
     * @param travelInfo the guest travel information
     * @return the guest ID, or null if failed
     */
    private Integer createGuestInGraphQL(BookingRequest.TravelInfo travelInfo) {
        String guestName = travelInfo.getFirst_name() + " " + travelInfo.getLast_name();
        String email = travelInfo.getEmail();
        
        // Check if guest name is "DONALD TRUMP" - return ID 1 directly
        if (guestName.equalsIgnoreCase("DONALD TRUMP")) {
            log.info("Using existing guest ID 1 for Donald Trump");
            return 1;
        }
        
        try {
            // First check if we already have a user with this email in RDS
            Optional<User> existingUser = userRepository.findByEmailWithGuestId(email);
            
            if (existingUser.isPresent() && existingUser.get().getGuestId() != null) {
                // We have an existing user with a guest ID - use that guest ID
                Integer existingGuestId = existingUser.get().getGuestId();
                log.info("Using existing guest ID {} for email {}", existingGuestId, email);
                return existingGuestId;
            }
            
            // Check if a guest with the same name already exists in GraphQL
            Integer existingGuestId = findExistingGuestIdByName(guestName);
            if (existingGuestId != null) {
                log.info("Found existing guest with name '{}' and ID {}", guestName, existingGuestId);
                
                // Save the user information to RDS with the existing guest ID
                saveUserToRds(travelInfo, existingGuestId);
                
                return existingGuestId;
            }
            
            // Try to create a new guest with multiple retries
            Integer guestId = null;
            int maxRetries = 3;
            int retryCount = 0;
            Exception lastException = null;
            
            while (guestId == null && retryCount < maxRetries) {
                try {
                    // Create a new guest and let GraphQL generate the ID
                    final String createGuestMutation = """
                        mutation CreateGuest($guestName: String!) {
                          createGuest(data: {
                            guest_name: $guestName
                          }) {
                            guest_id
                            guest_name
                          }
                        }
                    """;
                    
                    Map<String, Object> result = graphQlClient.document(createGuestMutation)
                        .variable("guestName", guestName)
                        .retrieve("createGuest")
                        .toEntity(new ParameterizedTypeReference<Map<String, Object>>() {})
                        .onErrorResume(e -> {
                            log.error("Error creating guest in GraphQL: {}", e.getMessage(), e);
                            return Mono.empty();
                        })
                        .block();
                    
                    if (result != null && result.containsKey("guest_id")) {
                        guestId = (Integer) result.get("guest_id");
                        log.info("Created new guest with ID: {}", guestId);
                    } else {
                        log.warn("Failed to get guest_id from GraphQL response, retry {}/{}", retryCount + 1, maxRetries);
                        retryCount++;
                        Thread.sleep(500); // Brief delay before retry
                    }
                } catch (Exception e) {
                    lastException = e;
                    log.error("Error creating new guest, retry {}/{}: {}", retryCount + 1, maxRetries, e.getMessage());
                    retryCount++;
                    try {
                        Thread.sleep(500); // Brief delay before retry
                    } catch (InterruptedException ie) {
                        Thread.currentThread().interrupt();
                    }
                }
            }
            
            // If we successfully created a guest, save it to RDS and return the ID
            if (guestId != null) {
                saveUserToRds(travelInfo, guestId);
                return guestId;
            }
            
            // If we couldn't create a new guest, this is a critical error
            // We should not use a hardcoded fallback as it creates confusion
            String errorMessage = "Failed to create new guest after " + maxRetries + " attempts";
            if (lastException != null) {
                errorMessage += ": " + lastException.getMessage();
            }
            log.error(errorMessage);
            throw new RuntimeException(errorMessage);
            
        } catch (Exception e) {
            log.error("Critical error creating guest in GraphQL: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to create or find guest: " + e.getMessage(), e);
        }
    }
    
    /**
     * Find an existing guest by name in GraphQL
     * 
     * @param guestName the guest name to look for
     * @return the guest ID if found, null otherwise
     */
    private Integer findExistingGuestIdByName(String guestName) {
        final String findGuestQuery = """
            query FindGuest($guestName: String!) {
              listGuests(where: {guest_name: {equals: $guestName}}, take: 1) {
                guest_id
                guest_name
              }
            }
        """;
        
        try {
            List<Map<String, Object>> guests = graphQlClient.document(findGuestQuery)
                .variable("guestName", guestName)
                .retrieve("listGuests")
                .toEntity(new ParameterizedTypeReference<List<Map<String, Object>>>() {})
                .onErrorResume(e -> {
                    log.warn("Error finding existing guest: {}", e.getMessage());
                    return Mono.just(Collections.emptyList());
                })
                .block();
                
            if (guests != null && !guests.isEmpty()) {
                Map<String, Object> guest = guests.get(0);
                if (guest.containsKey("guest_id")) {
                    return (Integer) guest.get("guest_id");
                }
            }
            
            return null;
        } catch (Exception e) {
            log.warn("Error finding existing guest: {}", e.getMessage());
            return null;
        }
    }
    
    /**
     * Save user information to RDS
     * 
     * @param travelInfo the guest travel information
     * @param guestId the guest ID from GraphQL
     */
    private void saveUserToRds(BookingRequest.TravelInfo travelInfo, Integer guestId) {
        if (travelInfo == null || guestId == null) {
            log.warn("Cannot save user to RDS: travelInfo or guestId is null");
            return;
        }
        
        String email = travelInfo.getEmail();
        if (email == null || email.trim().isEmpty()) {
            log.warn("Cannot save user to RDS: email is null or empty");
            return;
        }
        
        try {
            // Check if user already exists
            Optional<User> existingUser = userRepository.findByEmail(email);
            
            if (existingUser.isPresent()) {
                // Update existing user with guest ID
                User user = existingUser.get();
                if (!guestId.equals(user.getGuestId())) {
                    user.setGuestId(guestId);
                    userRepository.save(user);
                    log.info("Updated existing user {} with guest ID: {}", email, guestId);
                } else {
                    log.info("User {} already has guest ID: {}", email, guestId);
                }
            } else {
                // Create default preferences map
                Map<String, String> preferencesMap = new HashMap<>();
                preferencesMap.put("currency", "USD");
                preferencesMap.put("language", "english");
                
                // Create new user
                User newUser = User.builder()
                        .tenantId(1) // Default tenant ID
                        .email(email)
                        .phone(travelInfo.getPhone())
                        .firstName(travelInfo.getFirst_name())
                        .lastName(travelInfo.getLast_name())
                        .emailVerified(false)
                        .preferencesConfig(preferencesMap)
                        .guestId(guestId)
                        .build();
                
                userRepository.save(newUser);
                log.info("Created new user {} with guest ID: {}", email, guestId);
            }
        } catch (Exception e) {
            log.error("Error saving user to RDS: {}", e.getMessage(), e);
            // Continue with the booking process even if saving to RDS fails
        }
    }

    @Override
    public Optional<Integer> createBookingInGraphQL(BookingRequest bookingRequest, Integer roomId) {
        try {
            // Get guest ID from the helper method
            Integer guestId = createGuestInGraphQL(bookingRequest.getTravelInfo());
            if (guestId == null) {
                log.error("Failed to get valid guest ID");
                return Optional.empty();
            }
            
            // Continue with booking creation using the guest ID
            return createBookingInGraphQL(bookingRequest, roomId, guestId);
        } catch (Exception e) {
            log.error("Error creating booking in GraphQL: {}", e.getMessage(), e);
            return Optional.empty();
        }
    }
    
    /**
     * Create a booking in GraphQL with an existing guest ID
     * 
     * @param bookingRequest the booking request
     * @param roomId the selected room ID
     * @param guestId the guest ID
     * @return Optional of booking ID
     */
    private Optional<Integer> createBookingInGraphQL(BookingRequest bookingRequest, Integer roomId, Integer guestId) {
        try {
            String checkInDateStr = bookingRequest.getStartDate().atStartOfDay(ZoneOffset.UTC).format(DateTimeFormatter.ISO_INSTANT);
            String checkOutDateStr = bookingRequest.getEndDate().atStartOfDay(ZoneOffset.UTC).format(DateTimeFormatter.ISO_INSTANT);
            
            // Calculate guest counts, properly handling seniors and teens
            Map<String, Integer> adjustedGuestCount = calculateAdjustedGuestCount(bookingRequest.getGuestCount());
            Integer adultCount = adjustedGuestCount.getOrDefault("Adults", 0);
            Integer childCount = adjustedGuestCount.getOrDefault("Children", 0);
            
            // Calculate prices
            PriceCalculationResult priceResult = calculatePrices(
                    bookingRequest.getPropertyId(),
                    bookingRequest.getRoomTypeId(),
                    bookingRequest.getStartDate(),
                    bookingRequest.getEndDate(),
                    bookingRequest.getRoomCount(),
                    bookingRequest.getPromotionId());
            
            // NOTE: The GraphQL schema uses INT2 (smallint) which cannot store large values
            // Instead of converting to cents (which causes overflow), use the integer part of the dollar amount
            // This matches how RoomAvailabilityStatusServiceImpl handles prices
            Integer totalCost = priceResult.getFinalTotal().intValue(); // Use integer part of dollar amount
            Integer amountDueAtResort = priceResult.getDueAtResort().intValue(); // Use integer part of dollar amount
            
            log.info("Price calculation result: totalCost={}, amountDueAtResort={}, sent to GraphQL as integers: totalCost={}, amountDueAtResort={}", 
                    priceResult.getFinalTotal(), priceResult.getDueAtResort(), totalCost, amountDueAtResort);
            
            // Process promotion IDs
            boolean isGraphQLPromotion = false;
            Integer promotionId = bookingRequest.getPromotionId();
            Integer originalPromotionId = null;
            
            // First, check if we even have a promotion ID
            if (promotionId != null) {
                // Extract the original promotion ID and source indicator from the encoded ID
                // If ID is 6001, the original ID is 6 (6001/1000 = 6) and source is 1 (6001 % 1000 = 1)
                originalPromotionId = promotionId / 1000;
                int sourceIndicator = promotionId % 1000;
                
                // Check if this is a GraphQL promotion (ends with 1) or RDS promotion (ends with 2)
                isGraphQLPromotion = (sourceIndicator == 1);
                
                log.info("Processing promotion ID: {} (original ID: {}, source: {})", 
                        promotionId, originalPromotionId, isGraphQLPromotion ? "GraphQL" : "RDS");
                
                // If it's an RDS promotion, we don't include it in the GraphQL mutation
                if (sourceIndicator == 2) {
                    log.info("Promotion {} is from RDS, not including in GraphQL booking", promotionId);
                    promotionId = null;
                    isGraphQLPromotion = false;
                }
                
                // If it's supposed to be a GraphQL promotion, verify it exists
                if (isGraphQLPromotion && originalPromotionId != null) {
                    boolean promotionExists = checkIfPromotionExists(originalPromotionId);
                    if (!promotionExists) {
                        log.warn("Promotion with ID {} (original ID: {}) does not exist in GraphQL. Creating booking without promotion.", 
                                promotionId, originalPromotionId);
                        promotionId = null;
                        isGraphQLPromotion = false;
                    } else {
                        // Use the original promotion ID for the GraphQL mutation
                        promotionId = originalPromotionId;
                    }
                }
            }
            
            // Create the booking mutation - allowing GraphQL to auto-generate the ID
            final String createBookingMutation;
            
            // Use different mutation template based on whether promotion should be included
            if (promotionId != null && isGraphQLPromotion) {
                createBookingMutation = """
                    mutation CreateBooking(
                        $checkInDate: AWSDateTime!,
                        $checkOutDate: AWSDateTime!,
                        $adultCount: Int!,
                        $childCount: Int!,
                        $totalCost: Int!,
                        $amountDueAtResort: Int!,
                        $propertyId: Int!,
                        $statusId: Int!,
                        $guestId: Int!,
                        $promotionId: Int!
                    ) {
                      createBooking(
                        data: {
                          check_in_date: $checkInDate,
                          check_out_date: $checkOutDate,
                          adult_count: $adultCount,
                          child_count: $childCount,
                          total_cost: $totalCost,
                          amount_due_at_resort: $amountDueAtResort,
                          property_booked: { connect: { property_id: $propertyId } },
                          booking_status: { connect: { status_id: $statusId } },
                          guest: { connect: { guest_id: $guestId } },
                          promotion_applied: { connect: { promotion_id: $promotionId } }
                        }
                      ) {
                        booking_id
                      }
                    }
                """;
            } else {
                createBookingMutation = """
                    mutation CreateBooking(
                        $checkInDate: AWSDateTime!,
                        $checkOutDate: AWSDateTime!,
                        $adultCount: Int!,
                        $childCount: Int!,
                        $totalCost: Int!,
                        $amountDueAtResort: Int!,
                        $propertyId: Int!,
                        $statusId: Int!,
                        $guestId: Int!
                    ) {
                      createBooking(
                        data: {
                          check_in_date: $checkInDate,
                          check_out_date: $checkOutDate,
                          adult_count: $adultCount,
                          child_count: $childCount,
                          total_cost: $totalCost,
                          amount_due_at_resort: $amountDueAtResort,
                          property_booked: { connect: { property_id: $propertyId } },
                          booking_status: { connect: { status_id: $statusId } },
                          guest: { connect: { guest_id: $guestId } }
                        }
                      ) {
                        booking_id
                      }
                    }
                """;
            }
            
            HttpGraphQlClient.RequestSpec requestSpec = graphQlClient.document(createBookingMutation)
                .variable("checkInDate", checkInDateStr)
                .variable("checkOutDate", checkOutDateStr)
                .variable("adultCount", adultCount)
                .variable("childCount", childCount)
                .variable("totalCost", totalCost)
                .variable("amountDueAtResort", amountDueAtResort)
                .variable("propertyId", bookingRequest.getPropertyId())
                .variable("statusId", 1) // Assuming 1 = BOOKED status
                .variable("guestId", guestId);
                
            // Add promotion ID if it exists and is from GraphQL
            if (promotionId != null && isGraphQLPromotion) {
                requestSpec = requestSpec.variable("promotionId", promotionId);
            }
            
            Map<String, Object> result = requestSpec
                .retrieve("createBooking")
                .toEntity(new ParameterizedTypeReference<Map<String, Object>>() {})
                .block();
                
            if (result != null && result.containsKey("booking_id")) {
                return Optional.of((Integer) result.get("booking_id"));
            }
            
            log.error("Failed to create booking: No booking_id in response");
            return Optional.empty();
            
        } catch (Exception e) {
            log.error("Error creating booking in GraphQL: {}", e.getMessage(), e);
            return Optional.empty();
        }
    }
    
    /**
     * Check if a promotion exists in GraphQL
     * 
     * @param promotionId the promotion ID to check
     * @return true if the promotion exists, false otherwise
     */
    private boolean checkIfPromotionExists(Integer promotionId) {
        final String promotionQuery = """
            query CheckPromotion($promotionId: Int!) {
              countPromotions(where: {promotion_id: {equals: $promotionId}})
            }
        """;
        
        try {
            Integer count = graphQlClient.document(promotionQuery)
                .variable("promotionId", promotionId)
                .retrieve("countPromotions")
                .toEntity(Integer.class)
                .onErrorResume(e -> {
                    log.warn("Error checking if promotion exists: {}", e.getMessage());
                    return Mono.just(0);
                })
                .block();
                
            return count != null && count > 0;
        } catch (Exception e) {
            log.warn("Exception checking if promotion exists: {}", e.getMessage());
            return false;
        }
    }

    @Override
    public boolean updateRoomAvailabilities(
            Integer bookingId,
            Integer roomId,
            Integer propertyId,
            LocalDate startDate,
            LocalDate endDate) {
        
        try {
            // Step 1: Fetch all availability IDs for the room and date range
            List<Integer> roomIds = Collections.singletonList(roomId);
            Set<Integer> availableRoomIds = roomAvailabilityCheckService.fetchAvailableRoomIds(
                    propertyId, 
                    roomIds, 
                    startDate, 
                    endDate);
            
            if (availableRoomIds.isEmpty() || !availableRoomIds.contains(roomId)) {
                log.warn("Room {} is not available for the specified period", roomId);
                return false;
            }
            
            // Step 2: Fetch availability IDs
            List<Integer> availabilityIds = fetchAvailabilityIds(roomId, propertyId, startDate, endDate);
            
            if (availabilityIds.isEmpty()) {
                log.error("No availability records found for roomId={}, dates={} to {}", 
                        roomId, startDate, endDate);
                return false;
            }
            
            // Step 3: Update each availability with the booking ID
            boolean allUpdated = true;
            for (Integer availabilityId : availabilityIds) {
                boolean updated = updateSingleAvailability(availabilityId, bookingId);
                if (!updated) {
                    allUpdated = false;
                    log.error("Failed to update availability ID: {}", availabilityId);
                }
            }
            
            return allUpdated;
            
        } catch (Exception e) {
            log.error("Error updating room availabilities: {}", e.getMessage(), e);
            return false;
        }
    }
    
    /**
     * Fetch availability IDs for a specific room and date range
     * 
     * @param roomId the room ID
     * @param propertyId the property ID
     * @param startDate the start date
     * @param endDate the end date
     * @return list of availability IDs
     */
    private List<Integer> fetchAvailabilityIds(
            Integer roomId,
            Integer propertyId,
            LocalDate startDate, 
            LocalDate endDate) {
            
        String startDateStr = startDate.atStartOfDay(ZoneOffset.UTC).format(DateTimeFormatter.ISO_INSTANT);
        String endDateStr = endDate.atTime(23, 59, 59).atZone(ZoneOffset.UTC).format(DateTimeFormatter.ISO_INSTANT);
        
        final String availabilityQuery = """
            query GetAvailabilityIds($propertyId: Int!, $roomId: Int!, $startDate: AWSDateTime!, $endDate: AWSDateTime!) {
              listRoomAvailabilities(
                where: {
                  property_id: {equals: $propertyId}, 
                  room_id: {equals: $roomId},
                  date: {gte: $startDate, lte: $endDate},
                  booking: {
                    booking_status: {
                      status: {not: {equals: "BOOKED"}},
                      is_deactivated: {equals: false}
                    }
                  }
                }
              ) {
                availability_id
                property_id
                room_id
                date
              }
            }
        """;
        
        try {
            List<Map<String, Object>> availabilities = graphQlClient.document(availabilityQuery)
                .variable("propertyId", propertyId)
                .variable("roomId", roomId)
                .variable("startDate", startDateStr)
                .variable("endDate", endDateStr)
                .retrieve("listRoomAvailabilities")
                .toEntity(new ParameterizedTypeReference<List<Map<String, Object>>>() {})
                .onErrorResume(e -> {
                    log.error("Error fetching availability IDs: {}", e.getMessage(), e);
                    return Mono.just(new ArrayList<>());
                })
                .block();
                
            if (availabilities == null || availabilities.isEmpty()) {
                return new ArrayList<>();
            }
            
            return availabilities.stream()
                    .map(avail -> (Integer) avail.get("availability_id"))
                    .collect(Collectors.toList());
                    
        } catch (Exception e) {
            log.error("Error fetching availability IDs: {}", e.getMessage(), e);
            return new ArrayList<>();
        }
    }
    
    /**
     * Update a single availability with booking ID
     * 
     * @param availabilityId the availability ID to update
     * @param bookingId the booking ID to associate
     * @return true if successful, false otherwise
     */
    private boolean updateSingleAvailability(Integer availabilityId, Integer bookingId) {
        final String updateMutation = """
            mutation UpdateAvailability($availabilityId: Int!, $bookingId: Int!) {
              updateRoomAvailability(
                where: {availability_id: $availabilityId}
                data: {booking: {connect: {booking_id: $bookingId}}}
              ) {
                availability_id
                booking_id
              }
            }
        """;
        
        try {
            Map<String, Object> result = graphQlClient.document(updateMutation)
                .variable("availabilityId", availabilityId)
                .variable("bookingId", bookingId)
                .retrieve("updateRoomAvailability")
                .toEntity(new ParameterizedTypeReference<Map<String, Object>>() {})
                .block();
                
            return result != null && result.containsKey("availability_id");
            
        } catch (Exception e) {
            log.error("Error updating availability {}: {}", availabilityId, e.getMessage(), e);
            return false;
        }
    }
    
    /**
     * Calculate adjusted guest counts, mapping seniors to adults and teens to children
     * 
     * @param originalGuestCount the original guest count map
     * @return an adjusted guest count map with only Adults and Children keys
     */
    private Map<String, Integer> calculateAdjustedGuestCount(Map<String, Integer> originalGuestCount) {
        if (originalGuestCount == null || originalGuestCount.isEmpty()) {
            return Map.of("Adults", 0, "Children", 0);
        }
        
        int adultCount = originalGuestCount.getOrDefault("Adults", 0);
        int childCount = originalGuestCount.getOrDefault("Children", 0);
        
        // Add seniors to adult count
        adultCount += originalGuestCount.getOrDefault("Seniors", 0);
        
        // Add teens to children count
        childCount += originalGuestCount.getOrDefault("Teens", 0);
        
        return Map.of("Adults", adultCount, "Children", childCount);
    }
    
    /**
     * Calculate all pricing details for a booking
     * 
     * @param propertyId the property ID
     * @param roomTypeId the room type ID
     * @param startDate the check-in date
     * @param endDate the check-out date
     * @param roomCount the number of rooms
     * @param promotionId the promotion ID (optional)
     * @return a PriceCalculationResult with all price details
     */
    private PriceCalculationResult calculatePrices(
            Integer propertyId,
            Integer roomTypeId,
            LocalDate startDate,
            LocalDate endDate,
            Integer roomCount,
            Integer promotionId) {
        
        try {
            // Step 1: Get daily prices
            Map<LocalDate, Double> dailyPrices = dailyRatesService.fetchDailyRoomTypePrices(
                    roomTypeId, startDate, endDate);
            
            if (dailyPrices.isEmpty()) {
                log.warn("No pricing information available for roomTypeId={} and date range", roomTypeId);
                return new PriceCalculationResult(BigDecimal.valueOf(100), BigDecimal.valueOf(100)); // Default values
            }
            
            // Step 2: Calculate total price
            double totalPrice = dailyRatesService.calculateTotalPrice(dailyPrices, roomCount);
            BigDecimal baseAmount = BigDecimal.valueOf(totalPrice).setScale(2, java.math.RoundingMode.HALF_UP);
            
            // Step 3: Apply promotion if available
            BigDecimal priceFactor = BigDecimal.ONE;
            String promoTitle = "";
            
            if (promotionId != null) {
                // Check promotion ID format
                int remainder = promotionId % 1000;
                Integer originalPromotionId = promotionId / 1000;
                
                if (remainder == 1) {
                    // This is a GraphQL promotion (ends with 001)
                    log.info("Processing GraphQL promotion with ID: {}, original ID: {}", promotionId, originalPromotionId);
                    
                    try {
                        // Call GraphQL API to get promotion details
                        Map<String, Object> graphQLPromotion = promotionGraphQLService.fetchPromotion(originalPromotionId);
                        
                        if (graphQLPromotion != null && !graphQLPromotion.isEmpty()) {
                            priceFactor = new BigDecimal(graphQLPromotion.get("price_factor").toString());
                            promoTitle = graphQLPromotion.get("promotion_title").toString();
                            log.info("Found GraphQL promotion: {}, price factor: {}", promoTitle, priceFactor);
                        } else {
                            log.warn("GraphQL promotion not found with ID: {}", originalPromotionId);
                        }
                    } catch (Exception e) {
                        log.error("Error fetching GraphQL promotion: {}", e.getMessage(), e);
                    }
                } else if (remainder == 2) {
                    // This is an RDS promotion (ends with 002)
                    log.info("Processing RDS promotion with ID: {}, original ID: {}", promotionId, originalPromotionId);
                    
                    try {
                        // Get promotion from database
                        Optional<PropertyPromotion> optionalPromotion = propertyPromotionRepository
                                .findById(Long.valueOf(originalPromotionId));
                        
                        if (optionalPromotion.isPresent()) {
                            PropertyPromotion rdsPromotion = optionalPromotion.get();
                            priceFactor = BigDecimal.valueOf(rdsPromotion.getPriceFactor());
                            promoTitle = rdsPromotion.getTitle();
                            log.info("Found RDS promotion: {}, price factor: {}", promoTitle, priceFactor);
                        } else {
                            log.warn("RDS promotion not found with ID: {}", originalPromotionId);
                        }
                    } catch (Exception e) {
                        log.error("Error fetching RDS promotion: {}", e.getMessage(), e);
                    }
                } else {
                    log.warn("Invalid promotion ID format: {}", promotionId);
                }
            }
            
            // Apply promotion discount to base amount
            BigDecimal discountedBaseAmount = baseAmount.multiply(priceFactor).setScale(2, java.math.RoundingMode.HALF_UP);
            BigDecimal promoDiscount = baseAmount.subtract(discountedBaseAmount).setScale(2, java.math.RoundingMode.HALF_UP);
            
            log.info("Applied promotion discount: original price: {}, price factor: {}, " +
                    "discounted price: {}, discount amount: {}", 
                    baseAmount, priceFactor, discountedBaseAmount, promoDiscount);
            
            // Step 4: Get property configuration for surcharge, fees, and tax
            BigDecimal surcharge = BigDecimal.ZERO;
            BigDecimal surchargeAmount = BigDecimal.ZERO;
            BigDecimal fees = BigDecimal.ZERO;
            BigDecimal tax = BigDecimal.ZERO;
            BigDecimal taxAmount = BigDecimal.ZERO;
            BigDecimal totalWithSurchargeAndFees = discountedBaseAmount;
            BigDecimal finalTotal = discountedBaseAmount;
            
            try {
                PropertyConfigurationDTO.Response propertyConfig = 
                        propertyConfigurationService.getPropertyConfigurationByPropertyId(propertyId);
                
                if (propertyConfig != null) {
                    // 1. Get configuration values
                    surcharge = propertyConfig.getSurcharge() != null ? propertyConfig.getSurcharge() : BigDecimal.ZERO;
                    fees = propertyConfig.getFees() != null ? propertyConfig.getFees() : BigDecimal.ZERO;
                    tax = propertyConfig.getTax() != null ? propertyConfig.getTax() : BigDecimal.ZERO;
                    
                    // 2. Order of calculation:
                    // Step 1: Calculate surcharge (percentage of discounted base amount)
                    surchargeAmount = discountedBaseAmount
                            .multiply(surcharge)
                            .divide(BigDecimal.valueOf(100), 2, java.math.RoundingMode.HALF_UP);
                    
                    // Step 2: Add fixed fees
                    totalWithSurchargeAndFees = discountedBaseAmount
                            .add(surchargeAmount)
                            .add(fees);
                    
                    // Step 3: Calculate tax on the total with surcharge and fees
                    taxAmount = totalWithSurchargeAndFees
                            .multiply(tax)
                            .divide(BigDecimal.valueOf(100), 2, java.math.RoundingMode.HALF_UP);
                    
                    // Calculate final total
                    finalTotal = totalWithSurchargeAndFees.add(taxAmount);
                    
                    log.info("Price calculation with promotion: Base amount: {}, Discounted amount: {}, " +
                            "Promotion discount: {}, Surcharge({}%): {}, Fees: {}, " +
                            "Subtotal: {}, Tax({}%): {}, Final Total: {}", 
                            baseAmount, discountedBaseAmount, promoDiscount, surcharge, surchargeAmount, fees, 
                            totalWithSurchargeAndFees, tax, taxAmount, finalTotal);
                }
            } catch (Exception e) {
                log.warn("Property configuration not found for propertyId={}. Using default values.", propertyId, e);
            }
            
            // Calculate payment amounts (due now and due at resort)
            BigDecimal dueNow;
            BigDecimal dueAtResort;
            
            // Check if this is an upfront payment promotion (ID 5001)
            boolean isUpfrontPaymentPromotion = promotionId != null && promotionId == 5001;
            
            if (isUpfrontPaymentPromotion) {
                // For upfront payment promotion, 100% is due now
                dueNow = finalTotal;
                dueAtResort = BigDecimal.ZERO;
                log.info("Upfront payment required: dueNow={}, dueAtResort={}", dueNow, dueAtResort);
            } else {
                // For regular promotions, 5% is due now, rest at resort
                dueNow = finalTotal.multiply(new BigDecimal("0.05")).setScale(2, java.math.RoundingMode.HALF_UP);
                dueAtResort = finalTotal.subtract(dueNow).setScale(2, java.math.RoundingMode.HALF_UP);
                log.info("Regular payment schedule: dueNow={} (5%), dueAtResort={} (95%)", dueNow, dueAtResort);
            }
            
            return new PriceCalculationResult(finalTotal, dueAtResort);
            
        } catch (Exception e) {
            log.error("Error calculating prices: {}", e.getMessage(), e);
            return new PriceCalculationResult(BigDecimal.valueOf(100), BigDecimal.valueOf(95)); // Default values
        }
    }
    
    /**
     * Helper class to hold price calculation results
     */
    private static class PriceCalculationResult {
        private final BigDecimal finalTotal;
        private final BigDecimal dueAtResort;
        
        public PriceCalculationResult(BigDecimal finalTotal, BigDecimal dueAtResort) {
            this.finalTotal = finalTotal;
            this.dueAtResort = dueAtResort;
        }
        
        public BigDecimal getFinalTotal() {
            return finalTotal;
        }
        
        public BigDecimal getDueAtResort() {
            return dueAtResort;
        }
    }
    
    /**
     * Build a success response for the booking
     * 
     * @param request the booking request
     * @param bookingId the booking ID
     * @param roomId the room ID
     * @param availabilityResult the availability check result
     * @param guestId the guest ID
     * @return a BookingResponse object
     */
    private BookingResponse buildSuccessResponse(
            BookingRequest request,
            Integer bookingId,
            Integer roomId,
            Map<String, Object> availabilityResult,
            Integer guestId) {
            
        // Calculate prices using the new method
        PriceCalculationResult priceResult = calculatePrices(
                request.getPropertyId(),
                request.getRoomTypeId(),
                request.getStartDate(),
                request.getEndDate(),
                request.getRoomCount(),
                request.getPromotionId());
        
        // Calculate adjusted guest counts
        Map<String, Integer> adjustedGuestCount = calculateAdjustedGuestCount(request.getGuestCount());
        
        // Note: These are the full price values, different from what may be sent to GraphQL 
        // due to INT2 limitations in GraphQL schema
        log.info("Building success response with full price values: totalCost={}, amountDueAtResort={} " +
                "(Note: GraphQL may store different values due to INT2 limitations)", 
                priceResult.getFinalTotal(), priceResult.getDueAtResort());
        
        return BookingResponse.builder()
                .bookingId(bookingId)
                .propertyId(request.getPropertyId())
                .roomTypeId(request.getRoomTypeId())
                .roomTypeName((String) availabilityResult.get("roomTypeName"))
                .checkInDate(request.getStartDate())
                .checkOutDate(request.getEndDate())
                .adultCount(adjustedGuestCount.getOrDefault("Adults", 0))
                .childCount(adjustedGuestCount.getOrDefault("Children", 0))
                .totalCost(priceResult.getFinalTotal()) // Use calculated final total
                .amountDueAtResort(priceResult.getDueAtResort()) // Use calculated amount due at resort
                .guestName(request.getTravelInfo().getFirst_name() + " " + request.getTravelInfo().getLast_name())
                .guestId(guestId) // Include guest ID in response
                .status("BOOKED")
                .promotionId(request.getPromotionId())
                .confirmationCode(generateConfirmationCode(bookingId))
                .bookingTime(ZonedDateTime.now())
                .success(true)
                .message("Booking successful")
                .build();
    }
    
    /**
     * Generate a confirmation code for the booking
     * 
     * @param bookingId the booking ID
     * @return a confirmation code string
     */
    private String generateConfirmationCode(Integer bookingId) {
        return "RZ" + String.format("%06d", bookingId);
    }
    
    /**
     * Delete all locks in a new transaction to avoid rollback issues
     * 
     * @param locks the list of locks to delete
     */
    @Transactional(propagation = org.springframework.transaction.annotation.Propagation.REQUIRES_NEW)
    public void cleanupLocks(List<BookingLock> locks) {
        if (locks == null || locks.isEmpty()) {
            return;
        }
        
        log.info("Cleaning up {} locks", locks.size());
        for (BookingLock lock : locks) {
            try {
                bookingLockRepository.delete(lock);
            } catch (Exception e) {
                log.error("Error deleting lock ID {}: {}", lock.getId(), e.getMessage());
            }
        }
    }

    /**
     * Save billing information for a booking
     * 
     * @param billingInfoRequest the billing info from the request
     * @param bookingId the booking ID
     */
    private void saveBillingInfo(BookingRequest.BillingInfo billingInfoRequest, Integer bookingId) {
        if (billingInfoRequest == null || bookingId == null) {
            log.warn("Cannot save billing info: billingInfoRequest or bookingId is null");
            return;
        }
        
        try {
            BillingInfo billingInfo = BillingInfo.builder()
                    .bookingId(bookingId)
                    .firstName(billingInfoRequest.getFirst_name())
                    .lastName(billingInfoRequest.getLast_name())
                    .mailingAddress1(billingInfoRequest.getMailing_address1())
                    .mailingAddress2(billingInfoRequest.getMailing_address2())
                    .country(billingInfoRequest.getCountry())
                    .city(billingInfoRequest.getCity())
                    .state(billingInfoRequest.getState())
                    .zip(billingInfoRequest.getZip())
                    .phone(billingInfoRequest.getPhone())
                    .email(billingInfoRequest.getEmail())
                    .build();
            
            billingInfoRepository.save(billingInfo);
            log.info("Saved billing information for booking ID: {}", bookingId);
        } catch (Exception e) {
            log.error("Error saving billing information: {}", e.getMessage(), e);
            // Continue with the booking process even if saving billing info fails
        }
    }
    
    /**
     * Save payment information for a booking
     * 
     * @param paymentInfoRequest the payment info from the request
     * @param bookingId the booking ID
     */
    private void savePaymentInfo(BookingRequest.PaymentInfo paymentInfoRequest, Integer bookingId) {
        if (paymentInfoRequest == null || bookingId == null) {
            log.warn("Cannot save payment info: paymentInfoRequest or bookingId is null");
            return;
        }
        
        try {
            // Encrypt the card number before saving
            String encryptedCardNumber = null;
            if (paymentInfoRequest.getCard_number() != null && !paymentInfoRequest.getCard_number().isEmpty()) {
                encryptedCardNumber = encryptionUtil.encrypt(paymentInfoRequest.getCard_number());
                log.debug("Card number encrypted successfully");
            }
            
            PaymentInfo paymentInfo = PaymentInfo.builder()
                    .bookingId(bookingId)
                    .cardNumber(encryptedCardNumber)
                    .expMonth(paymentInfoRequest.getExp_mm())
                    .expYear(paymentInfoRequest.getExp_yy())
                    .specialOffers(paymentInfoRequest.getSpecial_offers())
                    .agreedToTerms(paymentInfoRequest.getAgreed_to_terms())
                    .build();
            
            paymentInfoRepository.save(paymentInfo);
            log.info("Saved encrypted payment information for booking ID: {}", bookingId);
        } catch (Exception e) {
            log.error("Error saving payment information: {}", e.getMessage(), e);
            // Continue with the booking process even if saving payment info fails
        }
    }

    /**
     * Get payment information for a booking, including decrypted card number
     * 
     * @param bookingId the booking ID
     * @return Optional of PaymentInfo DTO with decrypted card number
     */
    public Optional<Map<String, Object>> getDecryptedPaymentInfo(Integer bookingId) {
        if (bookingId == null) {
            log.warn("Cannot retrieve payment info: bookingId is null");
            return Optional.empty();
        }
        
        try {
            Optional<PaymentInfo> paymentInfoOpt = paymentInfoRepository.findByBookingId(bookingId);
            
            if (paymentInfoOpt.isEmpty()) {
                log.warn("No payment information found for booking ID: {}", bookingId);
                return Optional.empty();
            }
            
            PaymentInfo paymentInfo = paymentInfoOpt.get();
            String decryptedCardNumber = null;
            
            // Decrypt the card number if it exists
            if (paymentInfo.getCardNumber() != null && !paymentInfo.getCardNumber().isEmpty()) {
                decryptedCardNumber = encryptionUtil.decrypt(paymentInfo.getCardNumber());
            }
            
            // Create a map with the payment information
            Map<String, Object> result = new HashMap<>();
            result.put("bookingId", paymentInfo.getBookingId());
            result.put("cardNumber", decryptedCardNumber);
            result.put("expMonth", paymentInfo.getExpMonth());
            result.put("expYear", paymentInfo.getExpYear());
            result.put("specialOffers", paymentInfo.getSpecialOffers());
            result.put("agreedToTerms", paymentInfo.getAgreedToTerms());
            
            return Optional.of(result);
            
        } catch (Exception e) {
            log.error("Error retrieving payment information: {}", e.getMessage(), e);
            return Optional.empty();
        }
    }
} 