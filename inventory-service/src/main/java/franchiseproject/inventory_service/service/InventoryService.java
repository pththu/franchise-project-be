package franchiseproject.inventory_service.service;

import franchiseproject.inventory_service.dto.ExportInventoryRequest;
import franchiseproject.inventory_service.dto.SetMinStockRequest;
import franchiseproject.inventory_service.model.FranchiseIngredient;
import franchiseproject.inventory_service.model.InventoryTransaction;
import franchiseproject.inventory_service.repository.FranchiseIngredientRepository;
import franchiseproject.inventory_service.repository.InventoryTransactionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class InventoryService {

    private final FranchiseIngredientRepository ingredientRepository;
    private final InventoryTransactionRepository transactionRepository;


    //Manage warehouse release forms
    //Export Inventory
    public InventoryTransaction exportInventory(ExportInventoryRequest request){

        FranchiseIngredient ingredient = ingredientRepository.findById(request.getFranchiseIngredientId())
                .orElseThrow(() -> new RuntimeException("Ingredient not found"));

        int beforeQuantity = ingredient.getQuantity();

        if(beforeQuantity < request.getQuantity()){
            throw new RuntimeException("Not enough inventory");
        }

        int afterQuantity = beforeQuantity - request.getQuantity();

        ingredient.setQuantity(afterQuantity);
        ingredientRepository.save(ingredient);

        InventoryTransaction transaction = InventoryTransaction.builder()
                .franchiseIngredient(ingredient)
                .quantity(request.getQuantity())
                .beforeQuantity(beforeQuantity)
                .afterQuantity(afterQuantity)
                .type("EXPORT")
                .staffId(request.getStaffId())
                .status("SUCCESS")
                .build();

        return transactionRepository.save(transaction);
    }

    //View Export Records
    public List<InventoryTransaction> getExportRecords(){
        return transactionRepository.findAll()
                .stream()
                .filter(t -> "EXPORT".equals(t.getType()) && "SUCCESS".equals(t.getStatus()))
                .toList();
    }

    //View Export Record Details
    public InventoryTransaction getExportRecordDetail(UUID id){

        InventoryTransaction transaction = transactionRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Export record not found"));

        if(!"EXPORT".equals(transaction.getType())){
            throw new RuntimeException("Record is not an export transaction");
        }

        return transaction;
    }

    //Edit Export Record
    public InventoryTransaction editExportRecord(UUID id, ExportInventoryRequest request){

        InventoryTransaction transaction = transactionRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Export record not found"));

        if(!"EXPORT".equals(transaction.getType())){
            throw new RuntimeException("Record is not export type");
        }

        FranchiseIngredient ingredient = transaction.getFranchiseIngredient();

        int oldQuantity = transaction.getQuantity();
        int newQuantity = request.getQuantity();

        int currentStock = ingredient.getQuantity();

        int diff = newQuantity - oldQuantity;

        if(diff > 0){
            if(currentStock < diff){
                throw new RuntimeException("Not enough inventory");
            }
            ingredient.setQuantity(currentStock - diff);
        }
        else if(diff < 0){
            ingredient.setQuantity(currentStock + Math.abs(diff));
        }

        ingredientRepository.save(ingredient);

        transaction.setBeforeQuantity(ingredient.getQuantity() + newQuantity);
        transaction.setQuantity(newQuantity);
        transaction.setAfterQuantity(ingredient.getQuantity());

        return transactionRepository.save(transaction);
    }

    //Delete Export Record
    public void deleteExportRecord(UUID id){

        InventoryTransaction transaction = transactionRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Export record not found"));

        if(!"EXPORT".equals(transaction.getType())){
            throw new RuntimeException("Record is not export type");
        }

        FranchiseIngredient ingredient = transaction.getFranchiseIngredient();

        ingredient.setQuantity(
                ingredient.getQuantity() + transaction.getQuantity()
        );

        ingredientRepository.save(ingredient);

        transactionRepository.delete(transaction);
    }

    //Inventory alert management
    //Set Min Stock
    public InventoryTransaction setMinStock(SetMinStockRequest request){

        FranchiseIngredient ingredient = ingredientRepository.findById(request.getFranchiseIngredientId())
                .orElseThrow(() -> new RuntimeException("Ingredient not found"));

        InventoryTransaction transaction = InventoryTransaction.builder()
                .franchiseIngredient(ingredient)
                .threshold(request.getThreshold())
                .type("SET_MIN_STOCK")
                .staffId(request.getStaffId())
                .status("SUCCESS")
                .build();

        return transactionRepository.save(transaction);
    }

    //View Stock Alerts
    public List<InventoryTransaction> getStockAlerts(){

        List<FranchiseIngredient> ingredients = ingredientRepository.findAll();

        List<InventoryTransaction> thresholds = transactionRepository.findAll()
                .stream()
                .filter(t -> "SET_MIN_STOCK".equals(t.getType()))
                .toList();

        Map<UUID, Integer> thresholdMap = thresholds.stream()
                .collect(Collectors.toMap(
                        t -> t.getFranchiseIngredient().getId(),
                        InventoryTransaction::getThreshold,
                        (oldVal,newVal) -> newVal
                ));

        List<InventoryTransaction> alerts = new ArrayList<>();

        for(FranchiseIngredient ingredient : ingredients){

            Integer threshold = thresholdMap.get(ingredient.getId());

            if(threshold != null && ingredient.getQuantity() < threshold){

                InventoryTransaction alert = InventoryTransaction.builder()
                        .franchiseIngredient(ingredient)
                        .beforeQuantity(ingredient.getQuantity())
                        .threshold(threshold)
                        .type("ALERT")
                        .status("LOW_STOCK")
                        .staffId(null)   // FIX tránh lỗi DB
                        .build();

                alerts.add(alert);
            }
        }

        return alerts;
    }

    //View Low Stock by Franchise
    public List<FranchiseIngredient> getLowStockByFranchise(UUID franchiseId){

        List<FranchiseIngredient> ingredients = ingredientRepository.findAll()
                .stream()
                .filter(i -> i.getFranchise().getId().equals(franchiseId))
                .toList();

        List<InventoryTransaction> thresholds = transactionRepository.findAll()
                .stream()
                .filter(t -> "SET_MIN_STOCK".equals(t.getType()))
                .toList();

        Map<UUID, Integer> thresholdMap = thresholds.stream()
                .collect(Collectors.toMap(
                        t -> t.getFranchiseIngredient().getId(),
                        InventoryTransaction::getThreshold,
                        (oldVal,newVal) -> newVal
                ));

        return ingredients.stream()
                .filter(i -> {
                    Integer threshold = thresholdMap.get(i.getId());
                    return threshold != null && i.getQuantity() < threshold;
                })
                .toList();
    }
}