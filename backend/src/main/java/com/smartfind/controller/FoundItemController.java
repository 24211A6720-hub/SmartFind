package com.smartfind.controller;

import com.smartfind.entity.ItemType;
import com.smartfind.service.ItemService;
import com.smartfind.service.UserService;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/found-items")
public class FoundItemController extends AbstractItemController {

    public FoundItemController(ItemService itemService, UserService userService) {
        super(itemService, userService);
    }

    @Override
    protected ItemType type() {
        return ItemType.FOUND;
    }
}
