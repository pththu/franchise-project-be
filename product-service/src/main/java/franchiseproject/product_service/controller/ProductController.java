package franchiseproject.product_service.controller;

import org.springframework.web.multipart.MultipartFile;
import java.util.UUID;
import franchiseproject.product_service.model.Product;
import franchiseproject.product_service.service.ProductService;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE , makeFinal = true)
@RequestMapping("/product")
public class ProductController {
  ProductService productService;

   @GetMapping("/getall")
   public List<Product> findAll() {
       return productService.getAll();
  }

    @PostMapping("/{id}/upload-image")
    public Product uploadImage(
            @PathVariable UUID id,
            @RequestParam("file") MultipartFile file
    ) {
        return productService.uploadImage(id, file);
    }

    @PutMapping("/{id}/update-image")
    public Product updateImage(
            @PathVariable UUID id,
            @RequestParam("file") MultipartFile file
    ) {
        return productService.updateImage(id, file);
    }
}
