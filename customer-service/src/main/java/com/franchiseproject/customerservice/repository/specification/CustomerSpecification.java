package com.franchiseproject.customerservice.repository.specification;

import com.franchiseproject.customerservice.enums.CustomerStatus;
import com.franchiseproject.customerservice.model.Customer;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.List;

public class CustomerSpecification {

    public static Specification<Customer> filterCustomers(String keyword, CustomerStatus status) {
        return (root, query, criteriaBuilder) -> {
            List<Predicate> predicates = new ArrayList<>();

            // 1. Tìm kiếm theo keyword (Name, Email, Phone)
            if (StringUtils.hasText(keyword)) {
                String likePattern = "%" + keyword.toLowerCase() + "%";
                Predicate nameLike = criteriaBuilder.like(criteriaBuilder.lower(root.get("fullName")), likePattern);
                Predicate emailLike = criteriaBuilder.like(criteriaBuilder.lower(root.get("email")), likePattern);
                Predicate phoneLike = criteriaBuilder.like(criteriaBuilder.lower(root.get("phone")), likePattern);

                // Dùng OR cho 3 trường này
                predicates.add(criteriaBuilder.or(nameLike, emailLike, phoneLike));
            }

            // 2. Lọc theo Status
            if (status != null) {
                predicates.add(criteriaBuilder.equal(root.get("status"), status.name()));
            }

            // Ghép tất cả bằng AND
            return criteriaBuilder.and(predicates.toArray(new Predicate[0]));
        };
    }
}