package com.smartfind.service;

import com.smartfind.dto.request.ClaimRequest;
import com.smartfind.dto.request.ItemRequest;
import com.smartfind.dto.response.ClaimResponse;
import com.smartfind.dto.response.MatchResponse;
import com.smartfind.entity.*;
import com.smartfind.exception.ForbiddenException;
import com.smartfind.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/** End-to-end test of the demo flow at service level (uses H2 in-memory DB, rolled back after the test). */
@SpringBootTest
@ActiveProfiles("test")
@Transactional
class SmartFindFlowTest {

    @Autowired UserRepository userRepository;
    @Autowired ItemService itemService;
    @Autowired MatchingService matchingService;
    @Autowired ClaimService claimService;
    @Autowired NotificationService notificationService;
    @Autowired AdminService adminService;

    private User user(String email, Role role) {
        User u = new User();
        u.setName(email);
        u.setEmail(email);
        u.setPhone("9999999999");
        u.setPasswordHash("x");
        u.setRole(role);
        return userRepository.save(u);
    }

    private ItemRequest wallet(String name, String loc, LocalDate date, String desc) {
        return new ItemRequest(name, "Wallet", "Black", loc, date, desc, null, null, "contact");
    }

    @Test
    void fullLostAndFoundWorkflow() {
        User alice = user("a@test.com", Role.USER);
        User bob = user("b@test.com", Role.USER);
        User admin = user("admin@test.com", Role.ADMIN);
        LocalDate today = LocalDate.now();

        // 1-2. Alice reports a lost wallet, Bob reports a found wallet
        var lost = itemService.create(ItemType.LOST, wallet("Black Leather Wallet", "Anurag University Library",
                today.minusDays(2), "Black leather wallet with a student ID card inside."), alice);
        itemService.create(ItemType.FOUND, wallet("Black Wallet", "College library near Anurag University",
                today.minusDays(1), "Black wallet found near the library with a student ID card."), bob);

        // 3. matching ran automatically and Alice was notified
        List<MatchResponse> matches = matchingService.list(alice);
        assertEquals(1, matches.size());
        assertEquals(MatchLevel.STRONG, matches.get(0).matchLevel());
        assertTrue(notificationService.unreadCount(alice) >= 1);
        // privacy: Alice cannot see the finder's contact before approval
        assertNull(matches.get(0).foundItem().contactInfo());

        // 4. Bob (finder) cannot claim; Alice can
        Long matchId = matches.get(0).id();
        assertThrows(ForbiddenException.class,
                () -> claimService.create(new ClaimRequest(matchId, "I found it, please give me this"), bob));
        ClaimResponse claim = claimService.create(new ClaimRequest(matchId, "The wallet has my student ID inside."), alice);
        assertEquals(ClaimStatus.PENDING, claim.status());

        // 5. Admin approves
        ClaimResponse approved = claimService.approve(claim.id(), "Verified ID card", admin);
        assertEquals(ClaimStatus.APPROVED, approved.status());
        assertEquals(ItemStatus.CLAIMED, itemService.get(ItemType.LOST, lost.id(), alice).status());
        assertNotNull(matchingService.get(matchId, alice).foundItem().contactInfo());   // now revealed

        // 6. Admin closes the case
        adminService.closeCase(matchId, admin);
        assertEquals(ItemStatus.CLOSED, itemService.get(ItemType.LOST, lost.id(), alice).status());
        assertEquals(1, adminService.stats().resolvedCases());
    }

    @Test
    void unrelatedReportsDoNotCreateStrongMatch() {
        User alice = user("c@test.com", Role.USER);
        User bob = user("d@test.com", Role.USER);
        LocalDate today = LocalDate.now();

        itemService.create(ItemType.LOST, new ItemRequest("Blue Umbrella", "Umbrella", "Blue", "Bus Stop",
                today.minusDays(3), "Large blue umbrella", null, null, "c"), alice);
        itemService.create(ItemType.FOUND, new ItemRequest("Red Water Bottle", "Bottle", "Red", "Sports Ground",
                today.minusDays(1), "Red steel bottle with dent", null, null, "d"), bob);

        assertTrue(matchingService.list(alice).isEmpty());
    }
}
