package com.eventsaathi.config;

import com.eventsaathi.entity.*;
import com.eventsaathi.entity.enums.*;
import com.eventsaathi.repository.*;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Component
public class DataInitializer implements ApplicationRunner {
    private final RoleRepository roleRepository;
    private final CityRepository cityRepository;
    private final VendorCategoryRepository categoryRepository;
    private final EventTypeRepository eventTypeRepository;
    private final UserRepository userRepository;
    private final VendorRepository vendorRepository;
    private final EventRepository eventRepository;
    private final BookingRepository bookingRepository;
    private final PaymentRepository paymentRepository;
    private final ReviewRepository reviewRepository;
    private final NotificationRepository notificationRepository;
    private final PasswordEncoder passwordEncoder;
    private final String adminEmail;
    private final String adminPassword;

    public DataInitializer(RoleRepository roleRepository, CityRepository cityRepository,
                           VendorCategoryRepository categoryRepository, EventTypeRepository eventTypeRepository,
                           UserRepository userRepository, VendorRepository vendorRepository,
                           EventRepository eventRepository, BookingRepository bookingRepository,
                           PaymentRepository paymentRepository, ReviewRepository reviewRepository,
                           NotificationRepository notificationRepository, PasswordEncoder passwordEncoder,
                           @Value("${app.seed.admin-email:admin@eventsaathi.com}") String adminEmail,
                           @Value("${app.seed.admin-password:Admin@123456}") String adminPassword) {
        this.roleRepository = roleRepository;
        this.cityRepository = cityRepository;
        this.categoryRepository = categoryRepository;
        this.eventTypeRepository = eventTypeRepository;
        this.userRepository = userRepository;
        this.vendorRepository = vendorRepository;
        this.eventRepository = eventRepository;
        this.bookingRepository = bookingRepository;
        this.paymentRepository = paymentRepository;
        this.reviewRepository = reviewRepository;
        this.notificationRepository = notificationRepository;
        this.passwordEncoder = passwordEncoder;
        this.adminEmail = adminEmail;
        this.adminPassword = adminPassword;
    }

    @Override
    @Transactional
    public void run(ApplicationArguments args) {
        // 1. Roles
        for (RoleName name : RoleName.values()) {
            if (roleRepository.findByName(name).isEmpty()) {
                roleRepository.save(new Role(name));
            }
        }

        Role customerRole = roleRepository.findByName(RoleName.ROLE_CUSTOMER).orElseThrow();
        Role vendorRole = roleRepository.findByName(RoleName.ROLE_VENDOR).orElseThrow();
        Role adminRole = roleRepository.findByName(RoleName.ROLE_ADMIN).orElseThrow();

        // 2. Cities
        List<City> cities = List.of(
                new City("Mumbai", "Maharashtra", "India"),
                new City("Delhi", "Delhi", "India"),
                new City("Bengaluru", "Karnataka", "India"),
                new City("Hyderabad", "Telangana", "India"),
                new City("Pune", "Maharashtra", "India"),
                new City("Jaipur", "Rajasthan", "India"),
                new City("Chennai", "Tamil Nadu", "India"),
                new City("Kolkata", "West Bengal", "India"));
        for (City city : cities) {
            if (cityRepository.findByNameIgnoreCaseAndStateIgnoreCase(city.getName(), city.getState()).isEmpty()) {
                cityRepository.save(city);
            }
        }

        City mumbai = cityRepository.findByNameIgnoreCaseAndStateIgnoreCase("Mumbai", "Maharashtra").orElse(cities.get(0));
        City jaipur = cityRepository.findByNameIgnoreCaseAndStateIgnoreCase("Jaipur", "Rajasthan").orElse(cities.get(5));
        City delhi = cityRepository.findByNameIgnoreCaseAndStateIgnoreCase("Delhi", "Delhi").orElse(cities.get(1));
        City bengaluru = cityRepository.findByNameIgnoreCaseAndStateIgnoreCase("Bengaluru", "Karnataka").orElse(cities.get(2));

        // 3. Vendor Categories
        List<VendorCategory> categories = List.of(
                new VendorCategory("Venue", "Event venues, resorts, and banquet halls", "bi-building"),
                new VendorCategory("Catering", "Gourmet catering and food services", "bi-cup-hot"),
                new VendorCategory("Photography", "Photography, drone, and cinema reels", "bi-camera"),
                new VendorCategory("Decoration", "Floral mandap and theme decor", "bi-flower1"),
                new VendorCategory("Music & DJ", "Live music, DJs, sound, and lighting", "bi-music-note-beamed"),
                new VendorCategory("Makeup", "Bridal makeup and styling artists", "bi-stars"),
                new VendorCategory("Transport", "Luxury cars and guest coach fleet", "bi-car-front"));
        for (VendorCategory category : categories) {
            if (categoryRepository.findByNameIgnoreCase(category.getName()).isEmpty()) {
                categoryRepository.save(category);
            }
        }

        VendorCategory catVenue = categoryRepository.findByNameIgnoreCase("Venue").orElseThrow();
        VendorCategory catCatering = categoryRepository.findByNameIgnoreCase("Catering").orElseThrow();
        VendorCategory catPhoto = categoryRepository.findByNameIgnoreCase("Photography").orElseThrow();
        VendorCategory catDecor = categoryRepository.findByNameIgnoreCase("Decoration").orElseThrow();
        VendorCategory catMusic = categoryRepository.findByNameIgnoreCase("Music & DJ").orElseThrow();
        VendorCategory catMakeup = categoryRepository.findByNameIgnoreCase("Makeup").orElseThrow();
        VendorCategory catTransport = categoryRepository.findByNameIgnoreCase("Transport").orElseThrow();

        // 4. Event Types
        List<EventType> eventTypes = List.of(
                new EventType("Wedding", "Grand wedding ceremonies and receptions", "bi-heart"),
                new EventType("Birthday", "Milestone birthdays and private parties", "bi-balloon"),
                new EventType("Corporate", "Corporate summits, galas, and conferences", "bi-briefcase"),
                new EventType("Engagement", "Engagement rings and sangeet celebrations", "bi-gem"),
                new EventType("Anniversary", "Anniversary banquets and milestones", "bi-calendar-heart"),
                new EventType("Other", "Special festive occasions and private dining", "bi-calendar-event"));
        for (EventType type : eventTypes) {
            if (eventTypeRepository.findByNameIgnoreCase(type.getName()).isEmpty()) {
                eventTypeRepository.save(type);
            }
        }

        EventType typeWedding = eventTypeRepository.findByNameIgnoreCase("Wedding").orElseThrow();
        EventType typeCorporate = eventTypeRepository.findByNameIgnoreCase("Corporate").orElseThrow();

        // 5. Admin User
        String effectiveAdminEmail = adminEmail.isBlank() ? "admin@eventsaathi.com" : adminEmail.trim().toLowerCase();
        String effectiveAdminPassword = adminPassword.isBlank() ? "Admin@123456" : adminPassword;
        if (!userRepository.existsByEmailIgnoreCase(effectiveAdminEmail)) {
            User admin = new User("EventSaathi Admin", effectiveAdminEmail,
                    passwordEncoder.encode(effectiveAdminPassword), "+91 99999 88888");
            admin.addRole(adminRole);
            admin.addRole(customerRole);
            admin.setCity(mumbai);
            userRepository.save(admin);
        }

        // 6. Demo Customer User
        String customerEmail = "customer@eventsaathi.com";
        User customer = userRepository.findByEmailIgnoreCase(customerEmail).orElseGet(() -> {
            User user = new User("Ananya Sharma", customerEmail,
                    passwordEncoder.encode("Customer@123456"), "+91 98765 43210");
            user.addRole(customerRole);
            user.setCity(mumbai);
            return userRepository.save(user);
        });

        // 7. Seed Vendors (if none present)
        if (vendorRepository.count() == 0) {
            // Vendor 1: Royal Grand Banquet & Lawns (Venue, Mumbai)
            User vUser1 = createVendorUser("Rajesh Malhotra", "vendor@eventsaathi.com", "+91 98111 22334", mumbai, vendorRole, customerRole);
            Vendor v1 = new Vendor(vUser1, "Royal Grand Banquet & Lawns", catVenue, mumbai);
            v1.setDescription("Premier 5-star banquet lawns with pillarless AC ballroom, chandeliers, and manicured open-air garden space.");
            v1.setAddress("Plot 42, Bandra Kurla Complex, Mumbai");
            v1.setContactEmail("events@royalgrandmumbai.com");
            v1.setContactPhone("+91 98111 22334");
            v1.setWebsiteUrl("https://royalgrandbanquets.example.com");
            v1.setBasePrice(new BigDecimal("75000"));
            v1.setRating(new BigDecimal("4.9"));
            v1.setTotalReviews(128);
            v1.setStatus(VendorStatus.APPROVED);
            v1.setApprovedAt(LocalDateTime.now().minusDays(30));
            v1.getImages().add(new VendorImage(v1, "https://images.unsplash.com/photo-1519167758481-83f550bb49b3?auto=format&fit=crop&w=1200&q=80", true, "Grand Ballroom"));
            v1.getImages().add(new VendorImage(v1, "https://images.unsplash.com/photo-1545232979-8bf68ee9b1af?auto=format&fit=crop&w=1200&q=80", false, "Open Lawn at Dusk"));
            VendorService s1_1 = new VendorService(v1, "Grand Imperial Ballroom", "Crystal chandeliers, mood lighting, and banquet seating for up to 500 guests.", new BigDecimal("120000"), 500, 8);
            VendorService s1_2 = new VendorService(v1, "Lakeside Open Lawn", "Stunning outdoor lawn setup with garden lighting and stage for up to 800 guests.", new BigDecimal("180000"), 800, 10);
            VendorService s1_3 = new VendorService(v1, "Sapphire Celebration Hall", "Cozy AC hall perfect for engagements, cocktail parties, and birthdays.", new BigDecimal("65000"), 150, 6);
            v1.getServices().addAll(List.of(s1_1, s1_2, s1_3));
            vendorRepository.save(v1);

            // Vendor 2: Saffron & Spice Gourmet Catering (Catering, Mumbai)
            User vUser2 = createVendorUser("Chef Sanjeev Mehra", "caterer@eventsaathi.com", "+91 98222 33445", mumbai, vendorRole, customerRole);
            Vendor v2 = new Vendor(vUser2, "Saffron & Spice Gourmet Catering", catCatering, mumbai);
            v2.setDescription("Award-winning wedding catering featuring royal Awadhi, Mughlai, Gujarati, and international live gourmet stations.");
            v2.setAddress("Unit 8, Lower Parel Mill District, Mumbai");
            v2.setContactEmail("contact@saffronspice.example.com");
            v2.setContactPhone("+91 98222 33445");
            v2.setBasePrice(new BigDecimal("850"));
            v2.setRating(new BigDecimal("4.8"));
            v2.setTotalReviews(94);
            v2.setStatus(VendorStatus.APPROVED);
            v2.setApprovedAt(LocalDateTime.now().minusDays(25));
            v2.getImages().add(new VendorImage(v2, "https://images.unsplash.com/photo-1555244162-803834f70033?auto=format&fit=crop&w=1200&q=80", true, "Gourmet Live Counter"));
            VendorService s2_1 = new VendorService(v2, "Royal Wedding Multi-Cuisine Feast", "Extensive 40+ item spread including live pasta, chaat, dim sums, royal entrees, and desserts.", new BigDecimal("1200"), 600, 5);
            VendorService s2_2 = new VendorService(v2, "Traditional Vedic Feast Thali", "Pure vegetarian satvik and traditional royal recipes served with authentic silver thalis.", new BigDecimal("850"), 400, 4);
            v2.getServices().addAll(List.of(s2_1, s2_2));
            vendorRepository.save(v2);

            // Vendor 3: Drishti Cinematic Studios (Photography, Mumbai)
            User vUser3 = createVendorUser("Karan Johar", "photo@eventsaathi.com", "+91 98333 44556", mumbai, vendorRole, customerRole);
            Vendor v3 = new Vendor(vUser3, "Drishti Cinematic Studios", catPhoto, mumbai);
            v3.setDescription("International award-winning wedding photographers specializing in candid storytelling, drone films, and Instagram cinematic reels.");
            v3.setAddress("702, Crystal Plaza, Andheri West, Mumbai");
            v3.setContactEmail("hello@drishtistudios.example.com");
            v3.setContactPhone("+91 98333 44556");
            v3.setBasePrice(new BigDecimal("45000"));
            v3.setRating(new BigDecimal("4.9"));
            v3.setTotalReviews(86);
            v3.setStatus(VendorStatus.APPROVED);
            v3.setApprovedAt(LocalDateTime.now().minusDays(20));
            v3.getImages().add(new VendorImage(v3, "https://images.unsplash.com/photo-1537633552985-df8429e8048b?auto=format&fit=crop&w=1200&q=80", true, "Bride & Groom Portrait"));
            VendorService s3_1 = new VendorService(v3, "Complete 4K Wedding Cinema & Drone", "3-day coverage with 2 candid photographers, 2 cinematographers, drone operator, and 4K feature film.", new BigDecimal("95000"), null, 12);
            VendorService s3_2 = new VendorService(v3, "Candid Photography & Pre-Wedding Shoot", "1 full day wedding photography plus a dreamy scenic pre-wedding couple shoot in Mumbai.", new BigDecimal("55000"), null, 8);
            v3.getServices().addAll(List.of(s3_1, s3_2));
            vendorRepository.save(v3);

            // Vendor 4: Shubh Aarambh Floral & Theme Decors (Decoration, Mumbai)
            User vUser4 = createVendorUser("Pooja Singhania", "decor@eventsaathi.com", "+91 98444 55667", mumbai, vendorRole, customerRole);
            Vendor v4 = new Vendor(vUser4, "Shubh Aarambh Floral & Theme Decors", catDecor, mumbai);
            v4.setDescription("Bespoke event decorators delivering fairy-tale floral mandaps, Moroccan sangeet lounges, and aesthetic pastel wedding designs.");
            v4.setAddress("Shop 14, Linking Road, Khar, Mumbai");
            v4.setContactEmail("pooja@shubhaarambh.example.com");
            v4.setContactPhone("+91 98444 55667");
            v4.setBasePrice(new BigDecimal("35000"));
            v4.setRating(new BigDecimal("4.7"));
            v4.setTotalReviews(62);
            v4.setStatus(VendorStatus.APPROVED);
            v4.setApprovedAt(LocalDateTime.now().minusDays(18));
            v4.getImages().add(new VendorImage(v4, "https://images.unsplash.com/photo-1511795409834-ef04bbd61622?auto=format&fit=crop&w=1200&q=80", true, "Floral Stage Backdrop"));
            VendorService s4_1 = new VendorService(v4, "Royal Palace Mandap & Entrance Arc", "Exotic fresh tuberose and marigold mandap with water fountains and royal entryway arches.", new BigDecimal("85000"), null, 10);
            VendorService s4_2 = new VendorService(v4, "Pastel Fairy Light & Boho Canopy", "Modern eucalyptus, pampas grass, fairy lights, and pastel drape arrangements for sangeet & cocktail.", new BigDecimal("45000"), null, 8);
            v4.getServices().addAll(List.of(s4_1, s4_2));
            vendorRepository.save(v4);

            // Vendor 5: BeatPulse DJ & Sound Production (Music & DJ, Mumbai)
            User vUser5 = createVendorUser("DJ Aryan Saxena", "music@eventsaathi.com", "+91 98555 66778", mumbai, vendorRole, customerRole);
            Vendor v5 = new Vendor(vUser5, "BeatPulse DJ & Sound Production", catMusic, mumbai);
            v5.setDescription("High-energy celebrity DJs with Line Array JBL concert sound, moving-head laser lights, cold pyros, and traditional Punjabi Dhol troupe.");
            v5.setAddress("Studio 3, Versova, Mumbai");
            v5.setContactEmail("bookings@beatpulse.example.com");
            v5.setContactPhone("+91 98555 66778");
            v5.setBasePrice(new BigDecimal("25000"));
            v5.setRating(new BigDecimal("4.8"));
            v5.setTotalReviews(51);
            v5.setStatus(VendorStatus.APPROVED);
            v5.setApprovedAt(LocalDateTime.now().minusDays(15));
            v5.getImages().add(new VendorImage(v5, "https://images.unsplash.com/photo-1470225620780-dba8ba36b745?auto=format&fit=crop&w=1200&q=80", true, "DJ Stage & Laser Show"));
            VendorService s5_1 = new VendorService(v5, "Full Concert Sangeet Sound & DJ Set", "Top Bollywood, Punjabi, and EDM set with 15k-watt Line Array sound and truss lighting.", new BigDecimal("45000"), 1000, 6);
            VendorService s5_2 = new VendorService(v5, "Royal Baraat Mobile Sound & Dhol", "Mobile power setup with 4 live Punjabi Dhol players and wireless mics for the groom's procession.", new BigDecimal("25000"), null, 4);
            v5.getServices().addAll(List.of(s5_1, s5_2));
            vendorRepository.save(v5);

            // Vendor 6: Glamour Glow Bridal Studio (Makeup, Mumbai)
            User vUser6 = createVendorUser("Neha Kapoor", "makeup@eventsaathi.com", "+91 98666 77889", mumbai, vendorRole, customerRole);
            Vendor v6 = new Vendor(vUser6, "Glamour Glow Bridal Studio", catMakeup, mumbai);
            v6.setDescription("Certified luxury bridal makeup artist using Dior, Huda Beauty, and Charlotte Tilbury for radiant HD airbrush finishes.");
            v6.setAddress("Boutique 5, Juhu Tara Road, Mumbai");
            v6.setContactEmail("neha@glamourglow.example.com");
            v6.setContactPhone("+91 98666 77889");
            v6.setBasePrice(new BigDecimal("18000"));
            v6.setRating(new BigDecimal("4.9"));
            v6.setTotalReviews(73);
            v6.setStatus(VendorStatus.APPROVED);
            v6.setApprovedAt(LocalDateTime.now().minusDays(10));
            v6.getImages().add(new VendorImage(v6, "https://images.unsplash.com/photo-1487412720507-e7ab37603c6f?auto=format&fit=crop&w=1200&q=80", true, "Bridal Glam"));
            VendorService s6_1 = new VendorService(v6, "HD Airbrush Bridal Makeup + Draping", "Includes premium airbrush makeup, hair extensions, jewelry setting, dupatta draping, and touchup kit.", new BigDecimal("22000"), 2, 4);
            v6.getServices().add(s6_1);
            vendorRepository.save(v6);

            // Vendor 7: The Heritage Palace & Fort (Venue, Jaipur)
            User vUser7 = createVendorUser("Rana Digvijay Singh", "jaipur.palace@eventsaathi.com", "+91 98777 88990", jaipur, vendorRole, customerRole);
            Vendor v7 = new Vendor(vUser7, "The Heritage Palace & Fort", catVenue, jaipur);
            v7.setDescription("Authentic 18th-century Rajputana heritage fortress with royal courtyards, elephant greetings, and torchlit ramparts for royal destination weddings.");
            v7.setAddress("Amer Road, Jaipur, Rajasthan");
            v7.setContactEmail("weddings@heritagepalace.example.com");
            v7.setContactPhone("+91 98777 88990");
            v7.setBasePrice(new BigDecimal("150000"));
            v7.setRating(new BigDecimal("5.0"));
            v7.setTotalReviews(115);
            v7.setStatus(VendorStatus.APPROVED);
            v7.setApprovedAt(LocalDateTime.now().minusDays(30));
            v7.getImages().add(new VendorImage(v7, "https://images.unsplash.com/photo-1566073771259-6a8506099945?auto=format&fit=crop&w=1200&q=80", true, "Fort Palace View"));
            VendorService s7_1 = new VendorService(v7, "Royal Courtyard & Fort Night Experience", "Full palace rental with candlelit courtyards, traditional folk dancers, and royal guest suite.", new BigDecimal("250000"), 800, 12);
            v7.getServices().add(s7_1);
            vendorRepository.save(v7);

            // Vendor 8: Elite Chauffeur & Vintage Fleets (Transport, Mumbai)
            User vUser8 = createVendorUser("Sameer Merchant", "transport@eventsaathi.com", "+91 98888 99001", mumbai, vendorRole, customerRole);
            Vendor v8 = new Vendor(vUser8, "Elite Chauffeur & Vintage Fleets", catTransport, mumbai);
            v8.setDescription("Showstopping vintage open convertibles for groom's grand entry plus premium Mercedes & luxury AC coaches for wedding family.");
            v8.setAddress("Colaba Causeway, Mumbai");
            v8.setContactEmail("rides@elitechauffeur.example.com");
            v8.setContactPhone("+91 98888 99001");
            v8.setBasePrice(new BigDecimal("15000"));
            v8.setRating(new BigDecimal("4.6"));
            v8.setTotalReviews(39);
            v8.setStatus(VendorStatus.APPROVED);
            v8.setApprovedAt(LocalDateTime.now().minusDays(8));
            v8.getImages().add(new VendorImage(v8, "https://images.unsplash.com/photo-1519641471654-76ce0107ad1b?auto=format&fit=crop&w=1200&q=80", true, "Vintage Groom Entry Car"));
            VendorService s8_1 = new VendorService(v8, "Vintage 1948 Convertible Groom Entry", "Immaculate classic vintage car with chauffeur in royal uniform and floral decor.", new BigDecimal("28000"), 4, 4);
            v8.getServices().add(s8_1);
            vendorRepository.save(v8);

            // Vendor 9: Apex Spark Events (Pending Admin Approval for demo purposes!)
            User vUser9 = createVendorUser("Rohit Verma", "pending.vendor@eventsaathi.com", "+91 98999 00112", delhi, vendorRole, customerRole);
            Vendor v9 = new Vendor(vUser9, "Apex Spark Events & Decors", catDecor, delhi);
            v9.setDescription("Contemporary LED neon decor, tunnel entrance installations, and holographic stage backdrops for modern weddings.");
            v9.setAddress("Sector 18, Noida / Delhi NCR");
            v9.setContactEmail("rohit@apexspark.example.com");
            v9.setContactPhone("+91 98999 00112");
            v9.setBasePrice(new BigDecimal("40000"));
            v9.setStatus(VendorStatus.PENDING_APPROVAL); // Demo pending approval!
            v9.getImages().add(new VendorImage(v9, "https://images.unsplash.com/photo-1464366400600-7168b8af9bc3?auto=format&fit=crop&w=1200&q=80", true, "Modern LED Event Decor"));
            VendorService s9_1 = new VendorService(v9, "Hologram Stage & Tunnel Entrance", "Futuristic neon arches, laser lights, and interactive digital photo booth.", new BigDecimal("55000"), null, 8);
            v9.getServices().add(s9_1);
            vendorRepository.save(v9);

            // 8. Seed Events for Demo Customer
            Event event1 = new Event(customer, "Aarav & Diya's Grand Wedding", LocalDate.now().plusMonths(1));
            event1.setEventType(typeWedding);
            event1.setCity(mumbai);
            event1.setGuestCount(350);
            event1.setTotalBudget(new BigDecimal("800000"));
            event1.setDescription("Traditional Hindu wedding ceremony followed by grand banquet reception and sangeet.");
            event1.setStatus(EventStatus.ACTIVE);
            event1 = eventRepository.save(event1);

            Event event2 = new Event(customer, "InnovateTech Annual Gala 2026", LocalDate.now().plusMonths(2));
            event2.setEventType(typeCorporate);
            event2.setCity(mumbai);
            event2.setGuestCount(200);
            event2.setTotalBudget(new BigDecimal("450000"));
            event2.setDescription("Corporate tech conference followed by awards ceremony and dinner.");
            event2.setStatus(EventStatus.DRAFT);
            eventRepository.save(event2);

            // 9. Seed Bookings for Event 1
            // Booking A: Confirmed with Royal Grand Banquets
            Booking booking1 = new Booking(event1, v1, customer, event1.getEventDate());
            booking1.setVendorService(s1_1);
            booking1.setStatus(BookingStatus.CONFIRMED);
            booking1.setTotalAmount(new BigDecimal("120000"));
            booking1.setNotes("Please arrange red carpet at main entryway.");
            booking1.setVendorNote("Confirmed! Welcome to Royal Grand Banquets. We look forward to hosting your celebration.");
            bookingRepository.save(booking1);

            // Booking B: Pending with Saffron & Spice Catering (Demonstrates vendor acceptance/rejection flow!)
            Booking booking2 = new Booking(event1, v2, customer, event1.getEventDate());
            booking2.setVendorService(s2_1);
            booking2.setStatus(BookingStatus.PENDING);
            booking2.setTotalAmount(new BigDecimal("85000"));
            booking2.setNotes("Require 50% Jain dietary counter and separate live chaat section.");
            bookingRepository.save(booking2);

            // Booking C: Completed with Drishti Cinematic Studios (Includes payment & review!)
            Booking booking3 = new Booking(event1, v3, customer, LocalDate.now().minusDays(10));
            booking3.setVendorService(s3_2);
            booking3.setStatus(BookingStatus.COMPLETED);
            booking3.setTotalAmount(new BigDecimal("55000"));
            booking3.setNotes("Pre-wedding shoot at Marine Drive & Gateway of India.");
            booking3.setVendorNote("Shooting completed and 4K album delivered.");
            booking3 = bookingRepository.save(booking3);

            Payment payment = new Payment(booking3, new BigDecimal("55000"), "UPI");
            payment.setStatus(PaymentStatus.COMPLETED);
            payment.setPaidAt(LocalDateTime.now().minusDays(5));
            payment.setTransactionId("TXN-" + UUID.randomUUID().toString().substring(0, 10).toUpperCase());
            paymentRepository.save(payment);

            Review review = new Review(booking3, customer, v3, 5,
                    "Absolutely breathtaking photography! Karan and the Drishti Studios team captured every smile and emotional tear with movie-like beauty. Highly recommended!");
            reviewRepository.save(review);

            // 10. Seed Notifications
            notificationRepository.save(new Notification(customer, "Welcome to EventSaathi!",
                    "Start planning your dream event with top verified vendors and our AI Assistant.", "SYSTEM"));
            notificationRepository.save(new Notification(customer, "Booking Confirmed 🎉",
                    "Royal Grand Banquet & Lawns has confirmed your booking for " + event1.getEventDate() + "!", "BOOKING_CONFIRMED"));
            notificationRepository.save(new Notification(vUser2, "New Booking Request",
                    "Ananya Sharma sent a booking request for 'Royal Wedding Multi-Cuisine Feast' on " + event1.getEventDate() + ".", "NEW_BOOKING"));
        }
    }

    private User createVendorUser(String name, String email, String phone, City city, Role vendorRole, Role customerRole) {
        return userRepository.findByEmailIgnoreCase(email).orElseGet(() -> {
            User user = new User(name, email, passwordEncoder.encode("Vendor@123456"), phone);
            user.addRole(vendorRole);
            user.addRole(customerRole);
            user.setCity(city);
            return userRepository.save(user);
        });
    }
}
