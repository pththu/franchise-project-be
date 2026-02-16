package franchiseproject.promotion_service.service.impl;

import franchiseproject.promotion_service.entity.Promotion;
import franchiseproject.promotion_service.repository.PromotionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class PromotionServiceImpl {

    private final PromotionRepository promotionRepository;

    public List<Promotion> getAllPromotions() {
        return promotionRepository.findAll(Sort.by(Sort.Direction.DESC, "createdAt"));
        // nếu không muốn sort thì dùng:
        // return promotionRepository.findAll();
    }
}
