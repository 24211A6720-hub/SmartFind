package com.smartfind.service;

import com.smartfind.dto.response.*;
import com.smartfind.entity.*;
import com.smartfind.exception.BadRequestException;
import com.smartfind.exception.ConflictException;
import com.smartfind.exception.ResourceNotFoundException;
import com.smartfind.mapper.EntityMapper;
import com.smartfind.repository.*;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional
public class AdminService {

    private final UserRepository userRepo;
    private final LostItemRepository lostRepo;
    private final FoundItemRepository foundRepo;
    private final MatchResultRepository matchRepo;
    private final ClaimRepository claimRepo;
    private final UserService userService;
    private final MatchingService matchingService;
    private final NotificationService notificationService;

    public AdminService(UserRepository userRepo, LostItemRepository lostRepo, FoundItemRepository foundRepo,
                        MatchResultRepository matchRepo, ClaimRepository claimRepo, UserService userService,
                        MatchingService matchingService, NotificationService notificationService) {
        this.userRepo = userRepo;
        this.lostRepo = lostRepo;
        this.foundRepo = foundRepo;
        this.matchRepo = matchRepo;
        this.claimRepo = claimRepo;
        this.userService = userService;
        this.matchingService = matchingService;
        this.notificationService = notificationService;
    }

    @Transactional(readOnly = true)
    public StatsResponse stats() {
        long resolved = matchRepo.countByStatus(MatchStatus.CLAIMED) + matchRepo.countByStatus(MatchStatus.CLOSED);
        long strong = matchRepo.findAll().stream().filter(m -> m.getMatchLevel() == MatchLevel.STRONG).count();
        return new StatsResponse(userRepo.count(), lostRepo.count(), foundRepo.count(), matchRepo.count(), strong,
                claimRepo.countByStatus(ClaimStatus.PENDING), claimRepo.countByStatus(ClaimStatus.APPROVED),
                claimRepo.countByStatus(ClaimStatus.REJECTED), resolved);
    }

    public UserResponse setUserEnabled(Long id, boolean enabled, User admin) {
        if (admin.getId().equals(id)) throw new BadRequestException("You cannot disable your own account");
        User user = userService.find(id);
        user.setEnabled(enabled);
        return EntityMapper.toUser(userRepo.save(user));
    }

    public void deleteUser(Long id, User admin) {
        if (admin.getId().equals(id)) throw new BadRequestException("You cannot delete your own account");
        User user = userService.find(id);
        try {
            userRepo.delete(user);
            userRepo.flush();
        } catch (DataIntegrityViolationException ex) {
            throw new ConflictException("This user has reports or claims. Disable the account instead of deleting it.");
        }
    }

    @Transactional(readOnly = true)
    public ActivityResponse activity(Long id, User admin) {
        User user = userService.find(id);
        return new ActivityResponse(EntityMapper.toUser(user),
                lostRepo.findByUserIdOrderByCreatedAtDesc(id).stream().map(i -> EntityMapper.toItem(i, true)).toList(),
                foundRepo.findByUserIdOrderByCreatedAtDesc(id).stream().map(i -> EntityMapper.toItem(i, true)).toList(),
                claimRepo.findByClaimantIdOrderByCreatedAtDesc(id).stream().map(EntityMapper::toClaim).toList());
    }

    /** Final step of a case: the item has been handed over, so everything is CLOSED. */
    public MatchResponse closeCase(Long matchId, User admin) {
        MatchResult match = matchRepo.findById(matchId)
                .orElseThrow(() -> new ResourceNotFoundException("Match not found"));
        if (match.getStatus() != MatchStatus.CLAIMED) {
            throw new BadRequestException("Only a match with an approved claim can be closed");
        }
        match.setStatus(MatchStatus.CLOSED);
        match.getLostItem().setStatus(ItemStatus.CLOSED);
        match.getFoundItem().setStatus(ItemStatus.CLOSED);
        lostRepo.save(match.getLostItem());
        foundRepo.save(match.getFoundItem());
        matchRepo.save(match);
        notificationService.notify(match.getLostItem().getUser(),
                "Your case for \"" + match.getLostItem().getItemName() + "\" has been marked as resolved and closed.", match);
        notificationService.notify(match.getFoundItem().getUser(),
                "The case for \"" + match.getFoundItem().getItemName() + "\" has been marked as resolved. Thank you!", match);
        return matchingService.toResponse(match, admin);
    }

    public List<User> admins() {
        return userRepo.findByRole(Role.ADMIN);
    }
}
