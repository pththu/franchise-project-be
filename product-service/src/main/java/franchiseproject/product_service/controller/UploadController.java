package franchiseproject.product_service.controller;

import franchiseproject.product_service.dto.ApiResponse;
import franchiseproject.product_service.service.CloudinaryService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.Arrays;
import java.util.List;

@RestController
@RequestMapping("/api/upload")
@RequiredArgsConstructor
public class UploadController {

    private final CloudinaryService cloudinaryService;

    @PostMapping
    public ApiResponse<List<String>> upload(@RequestParam("file") MultipartFile[] files) {

        List<String> urls = Arrays.stream(files)
                .map(cloudinaryService::uploadFile)
                .toList();

        return ApiResponse.<List<String>>builder()
                .statusCode(200)
                .message("Upload success")
                .data(urls)
                .build();
    }
}