package franchiseproject.promotion_service.service.impl;

import franchiseproject.promotion_service.dto.CreatePromotionScopeRequest;
import franchiseproject.promotion_service.entity.Promotion;
import franchiseproject.promotion_service.entity.PromotionScope;
import franchiseproject.promotion_service.exception.ResourceNotFoundException;
import franchiseproject.promotion_service.repository.PromotionRepository;
import franchiseproject.promotion_service.repository.PromotionScopeRepository;
import franchiseproject.promotion_service.service.PromotionScopeService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class PromotionScopeServiceImpl implements PromotionScopeService {

    private final PromotionScopeRepository scopeRepository;
    private final PromotionRepository promotionRepository;

    @Override
    public PromotionScope createScope(CreatePromotionScopeRequest request) {

        Promotion promotion = promotionRepository.findById(request.getPromotionId())
                .orElseThrow(() -> new ResourceNotFoundException("Promotion not found"));

        if (scopeRepository.existsByPromotionIdAndScopeTypeAndScopeValue(
                request.getPromotionId(),
                request.getScopeType(),
                request.getScopeValue())) {

            throw new RuntimeException("Scope already exists for this promotion");
        }

        PromotionScope scope = PromotionScope.builder()
                .promotion(promotion)
                .scopeType(request.getScopeType())
                .scopeValue(request.getScopeValue())
                .createdAt(Instant.now())
                .build();

        return scopeRepository.save(scope);
    }

    @Override
    public PromotionScope updateScope(UUID id, CreatePromotionScopeRequest request) {

        PromotionScope scope = scopeRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Promotion scope not found"));

        scope.setScopeType(request.getScopeType());
        scope.setScopeValue(request.getScopeValue());

        return scopeRepository.save(scope);
    }

    @Override
    public List<PromotionScope> getScopes(UUID promotionId) {

        return scopeRepository.findByPromotionId(promotionId);

    }
}