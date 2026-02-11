package franchiseproject.inventory_service.controller;

import franchiseproject.inventory_service.model.Franchise;
import franchiseproject.inventory_service.service.FranchiseService;
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
@RequestMapping
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class FranchiseController {
    FranchiseService franchiseService;
    @GetMapping
    public ResponseEntity<Map<String, Object>> getAllFranchise(){
        Map<String, Object> response = new HashMap<>();
        List<Franchise> franchiseList = franchiseService.getAll();
        response.put("message","get All Franchise");
        response.put("Data",franchiseList);
        return ResponseEntity.ok(response);
    }
}
