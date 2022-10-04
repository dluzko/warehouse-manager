package com.luzko.warehouse.service;

import com.luzko.warehouse.dto.BlockingDto;
import com.luzko.warehouse.dto.BlockingRequestDto;
import com.luzko.warehouse.dto.ProductRequestDto;
import com.luzko.warehouse.dto.ProductResponseDto;
import com.luzko.warehouse.mapper.BlockingMapper;
import com.luzko.warehouse.mapper.ProductMapper;
import com.luzko.warehouse.model.Blocking;
import com.luzko.warehouse.model.Product;
import com.luzko.warehouse.repository.ProductRepository;
import com.luzko.warehouse.service.exception.ErrorCode;
import com.luzko.warehouse.service.exception.WarehouseManagerException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ProductService {
    private final ProductRepository productRepository;
    private final ProductMapper productMapper;
    private final BlockingMapper blockingMapper;

    public List<ProductResponseDto> getAllProducts() {
        List<Product> foundProducts = productRepository.findAll();
        return foundProducts.stream()
                .map(productMapper::toProductResponseDto)
                .toList();
    }

    public List<ProductResponseDto> getSelectedProducts(List<String> productCodes) {
        List<Product> foundProducts = productRepository.findAllByProductCodeIn(productCodes);
        return foundProducts.stream()
                .map(productMapper::toProductResponseDto)
                .toList();
    }

    public List<ProductResponseDto> placeProducts(List<ProductRequestDto> productRequestDtos) {
        List<String> productCodes = productRequestDtos.stream()
                .map(ProductRequestDto::getProductCode)
                .toList();
        List<Product> existingProducts = productRepository.findAllByProductCodeIn(productCodes);

        Map<String, Product> productsToBeSaved = existingProducts.stream()
                .collect(Collectors.toMap(Product::getProductCode, Function.identity()));

        productRequestDtos.forEach (productRequestDto -> {
            String productCode = productRequestDto.getProductCode();
            if (productsToBeSaved.containsKey(productCode)) {
                Product product = productsToBeSaved.get(productCode);
                product.setQuantity(product.getQuantity().add(productRequestDto.getQuantity()));
                productsToBeSaved.put(productCode, product);
            } else {
                productsToBeSaved.put(productCode, productMapper.toProduct(productRequestDto));
            }
        });
        List<Product> savedProducts = productRepository.saveAllAndFlush(productsToBeSaved.values());
        return savedProducts.stream()
                .map(productMapper::toProductResponseDto)
                .toList();
    }

    public List<ProductResponseDto> departProducts (List<BlockingRequestDto> blockingRequestDtos) {
        Set<Product> productsToBeUpdated = new HashSet<>();
        Set<Product> productsToBeDeleted = new HashSet<>();
        Map<String, Map<Product, List<BlockingDto>>> addressesToProductsToBlockingDtos =
                updateProductsBlockings(false, blockingRequestDtos);

        addressesToProductsToBlockingDtos.forEach((address, productsToBlockingDtos) -> {
            productsToBlockingDtos.forEach((product, blockingDtos) -> {
                BigDecimal requestedQuantity = blockingDtos.stream().map(BlockingDto::getBlockedQuantity)
                        .reduce(BigDecimal.ZERO, BigDecimal::add);
                product.setQuantity(product.getQuantity().subtract(requestedQuantity));
                if (product.getQuantity().equals(BigDecimal.ZERO)) {
                    productsToBeDeleted.add(product);
                }
            });
            productsToBeUpdated.addAll(
                    new HashSet<>(productsToBlockingDtos.keySet()));
        });
        productRepository.saveAllAndFlush(productsToBeUpdated);
        productRepository.deleteAll(productsToBeDeleted);
        return productsToBeUpdated.stream().map(productMapper::toProductResponseDto).toList();
    }

    public List<ProductResponseDto> updateProducts (boolean block,
                                                    List<BlockingRequestDto> blockingRequestDtos) {
        Set<Product> productsToBeUpdated = new HashSet<>();
        Map<String, Map<Product, List<BlockingDto>>> addressesToProductsToBlockingDtos =
                updateProductsBlockings(block, blockingRequestDtos);
        addressesToProductsToBlockingDtos.forEach((address, productsToBlockingDtos) ->
                productsToBeUpdated.addAll(new HashSet<>(productsToBlockingDtos.keySet())));
        productRepository.saveAllAndFlush(productsToBeUpdated);
        return productsToBeUpdated.stream().map(productMapper::toProductResponseDto).toList();
    }

    private Map<String, Map<Product, List<BlockingDto>>> updateProductsBlockings(boolean block,
                                                                                List<BlockingRequestDto>
                                                                                        blockingRequestDtos) {
        Map<String, Map<String, List<BlockingDto>>> addressesToProductCodesToBlockingDtos =
                blockingRequestDtos.stream().collect(Collectors.groupingBy(
                        BlockingRequestDto::getAddress, Collectors.groupingBy(
                                BlockingRequestDto::getProductCode, Collectors.mapping(
                                        BlockingRequestDto::getBlockingInfo, Collectors.toList()))));

        Set<Product> productsFoundByAddresses = productRepository.findAllByAddressIn(
                addressesToProductCodesToBlockingDtos.keySet());
        Map<String, Set<Product>> addressesToFoundProducts = productsFoundByAddresses.stream()
                .collect(Collectors.groupingBy(Product::getAddress, Collectors.toSet()));

        checkAddresses(addressesToProductCodesToBlockingDtos, addressesToFoundProducts);
        checkProductCodesAtAddresses(addressesToProductCodesToBlockingDtos, addressesToFoundProducts);

        Map<String, Map<String, Product>> addressesToProductCodesToProducts = new HashMap<>();
        productsFoundByAddresses.forEach(product -> {
            String address = product.getAddress();
            addressesToProductCodesToProducts.putIfAbsent(address, new HashMap<>());
            addressesToProductCodesToProducts.get(address).put(product.getProductCode(), product);
        });

        Map<String, Map<Product, List<BlockingDto>>> addressesToProductsToBlockingDtos = new HashMap<>();
        addressesToProductCodesToBlockingDtos.forEach((address, reqItems) -> {
            Map<String, Product> foundOnAddress = addressesToProductCodesToProducts.get(address);
            reqItems.forEach((productCode, blockingDtos) -> {
                Product product = foundOnAddress.get(productCode);
                addressesToProductsToBlockingDtos.putIfAbsent(address, new HashMap<>());
                addressesToProductsToBlockingDtos.get(address).put(product, blockingDtos);
            });
        });

        blockUnblockProducts(block, addressesToProductsToBlockingDtos);
        return addressesToProductsToBlockingDtos;
    }

    private void blockUnblockProducts (boolean block,
                                       Map<String, Map<Product, List<BlockingDto>>>
                                               addressesToProductsToBlockingDtos) {
        for (Map.Entry<String, Map<Product, List<BlockingDto>>> resultEntry :
                addressesToProductsToBlockingDtos.entrySet()) {
            String currentAddress = resultEntry.getKey();
            for (Map.Entry<Product, List<BlockingDto>> productToBlockingDtosEntry :
                    resultEntry.getValue().entrySet()) {
                if (block) {
                    blockProducts(productToBlockingDtosEntry, currentAddress);
                } else {
                    unblockProducts(productToBlockingDtosEntry);
                }
            }
        }
    }

    private void blockProducts(Map.Entry<Product, List<BlockingDto>> productToBlockingDtosEntry,
                               String currentAddress) {
        Product currentProduct = productToBlockingDtosEntry.getKey();
        BigDecimal currentProductAvailableQuantity = getAvailableQuantity(currentProduct);

        List<BlockingDto> blockingDtosForCurrentProduct =
                productToBlockingDtosEntry.getValue();
        for (BlockingDto blockingDto : blockingDtosForCurrentProduct) {
            if (blockingDto.getBlockedQuantity().compareTo(currentProductAvailableQuantity) > 0) {
                throw new WarehouseManagerException(
                        ErrorCode.NOT_ENOUGH_OF_PRODUCT, currentProduct.getProductCode(), currentAddress);
            }
            currentProductAvailableQuantity = currentProductAvailableQuantity
                    .subtract(blockingDto.getBlockedQuantity());
            currentProduct.getBlockings().stream()
                    .filter(blocking -> isBlockingTheSame(blocking, blockingDto))
                    .findFirst()
                    .ifPresentOrElse(blocking -> blocking.setBlockedQuantity(blocking.getBlockedQuantity()
                                    .add(blockingDto.getBlockedQuantity())),
                            () -> blockingMapper.toBlocking(blockingDto).attachToProduct(currentProduct));
        }
    }

    private void unblockProducts(Map.Entry<Product, List<BlockingDto>> productToBlockingDtosEntry) {
        Product currentProduct = productToBlockingDtosEntry.getKey();
        List<BlockingDto> blockingDtosForCurrentProduct =
                productToBlockingDtosEntry.getValue();
        for (BlockingDto blockingDto : blockingDtosForCurrentProduct) {
            Blocking existingBlocking = currentProduct.getBlockings().stream()
                    .filter(blocking -> isBlockingTheSame(blocking, blockingDto))
                    .findFirst().orElseThrow(() -> {
                        throw new WarehouseManagerException(
                                ErrorCode.BLOCKING_NOT_FOUND, currentProduct, blockingDto);
                    });

            BigDecimal requestedQuantity = blockingDto.getBlockedQuantity();
            BigDecimal currentQuantity = existingBlocking.getBlockedQuantity();
            if (currentQuantity.compareTo(requestedQuantity) < 0) {
                throw new WarehouseManagerException(ErrorCode.BLOCKED_QUANTITY_IS_NOT_ENOUGH_FOR_UNBLOCKING,
                        currentProduct, blockingDto);
            } else if (currentQuantity.compareTo(requestedQuantity) > 0) {
                existingBlocking.setBlockedQuantity(
                        existingBlocking.getBlockedQuantity().subtract(requestedQuantity));
            } else {
                currentProduct.getBlockings().remove(existingBlocking);
            }
        }
    }

    private void checkAddresses(Map<String, Map<String, List<BlockingDto>>>
                                                   addressesToProductCodesToBlockingDtosMap,
                                Map<String, Set<Product>> allProductsFoundByAddressesMap) {
        addressesToProductCodesToBlockingDtosMap.keySet().stream()
                .filter(address -> !allProductsFoundByAddressesMap.containsKey(address))
                .findFirst()
                .ifPresent(notFoundAddress -> {
                    throw new WarehouseManagerException(ErrorCode.ADDRESS_NOT_FOUND, notFoundAddress);
                });
    }

    private void checkProductCodesAtAddresses(Map<String, Map<String, List<BlockingDto>>>
                                                                 addressesToProductCodesToBlockingDtosMap,
                                              Map<String, Set<Product>> allProductsFoundByAddressesMap) {
        addressesToProductCodesToBlockingDtosMap.forEach((address, blockingInfosByProductCode) -> {
            Map<String, Product> existingProductsAtAddress = allProductsFoundByAddressesMap.get(address).stream()
                    .collect(Collectors.toMap(Product::getProductCode, Function.identity()));
            blockingInfosByProductCode.forEach((productCode, blockInfos) -> {
                if (!existingProductsAtAddress.containsKey(productCode)) {
                    throw new WarehouseManagerException(
                            ErrorCode.PRODUCT_CODE_NOT_FOUND_AT_ADDRESS, productCode, address);
                }
            });
        });
    }

    private boolean isBlockingTheSame(Blocking blocking, BlockingDto blockingDto) {
        return blocking.getBlockingReason().equals(blockingDto.getBlockingReason())
                && blocking.getBlockingToken().equals(blockingDto.getBlockingToken());
    }

    private BigDecimal getAvailableQuantity(Product product) {
        return product.getQuantity().subtract(
                product.getBlockings().stream()
                        .map(Blocking::getBlockedQuantity).reduce(BigDecimal.ZERO, BigDecimal::add));
    }
}
