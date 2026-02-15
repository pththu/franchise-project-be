package com.franchiseproject.identityaccessservice.controller;
import com.franchiseproject.identityaccessservice.model.User;
import com.franchiseproject.identityaccessservice.service.UserService;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/users")
@AllArgsConstructor
@FieldDefaults(makeFinal = true, level = AccessLevel.PRIVATE)
public class UserController {
    UserService userService;

    @GetMapping
    public ResponseEntity<Map<String, Object>> getAll() {
        Map<String, Object> response = new HashMap<>();

        List<User> users = userService.getAll();
        response.put("message", "Get All");
        response.put("data", users);

        return ResponseEntity.ok(response);
    }
}



