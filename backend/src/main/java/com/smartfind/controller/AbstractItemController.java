package com.smartfind.controller;

import com.smartfind.dto.request.ItemRequest;
import com.smartfind.dto.response.ApiResponse;
import com.smartfind.dto.response.ItemResponse;
import com.smartfind.entity.ItemType;
import com.smartfind.entity.User;
import com.smartfind.service.ItemService;
import com.smartfind.service.UserService;
import jakarta.validation.Valid;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

/** Shared endpoints for lost and found reports. Subclasses only choose the URL and the ItemType. */
public abstract class AbstractItemController {

    protected final ItemService itemService;
    protected final UserService userService;

    protected AbstractItemController(ItemService itemService, UserService userService) {
        this.itemService = itemService;
        this.userService = userService;
    }

    protected abstract ItemType type();

    @PostMapping
    public ResponseEntity<ApiResponse<ItemResponse>> create(@Valid @RequestBody ItemRequest req, Authentication auth) {
        User me = userService.fromAuth(auth);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.ok("Report submitted", itemService.create(type(), req, me)));
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<ItemResponse>>> list(
            @RequestParam(required = false) String name,
            @RequestParam(required = false) String category,
            @RequestParam(required = false) String color,
            @RequestParam(required = false) String location,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date,
            @RequestParam(required = false) String status,
            Authentication auth) {
        User me = userService.fromAuth(auth);
        return ResponseEntity.ok(ApiResponse.ok(
                itemService.list(type(), name, category, color, location, date, status, me)));
    }

    @GetMapping("/mine")
    public ResponseEntity<ApiResponse<List<ItemResponse>>> mine(Authentication auth) {
        return ResponseEntity.ok(ApiResponse.ok(itemService.mine(type(), userService.fromAuth(auth))));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<ItemResponse>> get(@PathVariable Long id, Authentication auth) {
        return ResponseEntity.ok(ApiResponse.ok(itemService.get(type(), id, userService.fromAuth(auth))));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<ItemResponse>> update(@PathVariable Long id,
                                                            @Valid @RequestBody ItemRequest req,
                                                            Authentication auth) {
        User me = userService.fromAuth(auth);
        return ResponseEntity.ok(ApiResponse.ok("Report updated", itemService.update(type(), id, req, me)));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> delete(@PathVariable Long id, Authentication auth) {
        itemService.delete(type(), id, userService.fromAuth(auth));
        return ResponseEntity.ok(ApiResponse.ok("Report deleted", null));
    }
}
