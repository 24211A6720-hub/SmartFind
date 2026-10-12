package com.smartfind.config;

import com.smartfind.dto.request.ItemRequest;
import com.smartfind.entity.ItemType;
import com.smartfind.entity.Role;
import com.smartfind.entity.User;
import com.smartfind.repository.UserRepository;
import com.smartfind.service.ItemService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.time.LocalDate;

/**
 * DEVELOPMENT SAMPLE DATA. Runs only when the users table is empty.
 * The passwords below are for demonstration only - change or remove this class before any real use.
 */
@Component
@Profile("!test")
public class DataSeeder implements CommandLineRunner {

    private static final Logger log = LoggerFactory.getLogger(DataSeeder.class);

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final ItemService itemService;

    public DataSeeder(UserRepository userRepository, PasswordEncoder passwordEncoder, ItemService itemService) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.itemService = itemService;
    }

    @Override
    public void run(String... args) {
        if (userRepository.count() > 0) return;

        User admin = createUser("Admin", "admin@smartfind.com", "9000000000", "Admin@123", Role.ADMIN);
        User alice = createUser("Alice", "alice@smartfind.com", "9000000001", "User@1234", Role.USER);
        User bob = createUser("Bob", "bob@smartfind.com", "9000000002", "User@1234", Role.USER);
        User carol = createUser("Carol", "carol@smartfind.com", "9000000003", "User@1234", Role.USER);

        LocalDate today = LocalDate.now();

        // STRONG pair: Alice lost a wallet, Bob found one
        itemService.create(ItemType.LOST, new ItemRequest("Black Leather Wallet", "Wallet", "Black",
                "Anurag University Library", today.minusDays(5),
                "Black leather wallet with a student ID card inside.", "Has a small scratch near the zip",
                null, "alice@smartfind.com / 9000000001"), alice);
        itemService.create(ItemType.FOUND, new ItemRequest("Black Wallet", "Wallet", "Black",
                "College library near Anurag University", today.minusDays(4),
                "Black wallet found near the library with a student ID card.", "Student ID card inside",
                null, "bob@smartfind.com / 9000000002"), bob);

        // POSSIBLE pair: Carol lost a phone, Bob found a phone
        itemService.create(ItemType.LOST, new ItemRequest("Silver Samsung Phone", "Phone", "Silver",
                "Canteen", today.minusDays(3),
                "Samsung phone with a cracked screen guard.", null,
                null, "carol@smartfind.com / 9000000003"), carol);
        itemService.create(ItemType.FOUND, new ItemRequest("Grey Mobile Phone", "Phone", "Grey",
                "Cafeteria near main block", today.minusDays(2),
                "Mobile phone found on a table with a cracked screen protector.", null,
                null, "bob@smartfind.com / 9000000002"), bob);

        // WEAK pair: Alice lost a blue umbrella, Carol found a black one somewhere else
        itemService.create(ItemType.LOST, new ItemRequest("Blue Umbrella", "Umbrella", "Blue",
                "Bus Stop", today.minusDays(6),
                "Large blue umbrella with a wooden handle.", null,
                null, "alice@smartfind.com / 9000000001"), alice);
        itemService.create(ItemType.FOUND, new ItemRequest("Black Umbrella", "Umbrella", "Black",
                "Hostel Gate", today.minusDays(3),
                "Folding black umbrella with a metal handle.", null,
                null, "carol@smartfind.com / 9000000003"), carol);

        // NO MATCH: unrelated item
        itemService.create(ItemType.FOUND, new ItemRequest("Red Water Bottle", "Bottle", "Red",
                "Sports Ground", today.minusDays(1),
                "Red steel water bottle with a dent on the side.", null,
                null, "carol@smartfind.com / 9000000003"), carol);

        log.warn("=== SmartFind DEV sample data created. Admin: admin@smartfind.com / Admin@123 "
                + "| Users: alice|bob|carol@smartfind.com / User@1234 (CHANGE THESE LATER) ===");
    }

    private User createUser(String name, String email, String phone, String password, Role role) {
        User u = new User();
        u.setName(name);
        u.setEmail(email);
        u.setPhone(phone);
        u.setPasswordHash(passwordEncoder.encode(password));
        u.setRole(role);
        return userRepository.save(u);
    }
}
