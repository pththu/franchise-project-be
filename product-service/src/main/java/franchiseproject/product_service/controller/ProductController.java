package franchiseproject.product_service.controller;


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
@RequestMapping("/api/products")
public class ProductController {
  ProductService productService;

   @GetMapping("/getall")
   public List<Product> findAll() {
       return productService.getAll();
  }

}
