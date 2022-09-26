package com.luzko.warehouse.service;

import com.luzko.warehouse.dto.BlockProductDto;
import com.luzko.warehouse.dto.BlockProductRequestDto;
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

        for (ProductRequestDto productRequestDto : productRequestDtos) {
            String productCode = productRequestDto.getProductCode();
            if (productsToBeSaved.containsKey(productCode)) {
                Product product = productsToBeSaved.get(productCode);
                product.setQuantity(product.getQuantity().add(productRequestDto.getQuantity()));
                productsToBeSaved.put(productCode, product);
            } else {
                productsToBeSaved.put(productCode, productMapper.toProduct(productRequestDto));
            }
        }
        List<Product> savedProducts = productRepository.saveAllAndFlush(productsToBeSaved.values());
        return savedProducts.stream()
                .map(productMapper::toProductResponseDto)
                .toList();
    }

    public List<ProductResponseDto> departProducts (List<BlockProductRequestDto> blockProductRequestDtos) {
        Set<Product> productsToBeUpdated = new HashSet<>();
        Set<Product> productsToBeDeleted = new HashSet<>();
        Map<String, Map<Product, List<BlockProductDto>>> addressesToProductsToBlockProductDtos =
                updateProductsBlockings(false, blockProductRequestDtos);

        addressesToProductsToBlockProductDtos.forEach((address, productsToBlockProductDtos) -> {
            productsToBlockProductDtos.forEach((product, blockProductDtos) -> {
                BigDecimal requestedQuantity = blockProductDtos.stream().map(BlockProductDto::getBlockedQuantity)
                        .reduce(BigDecimal.ZERO, BigDecimal::add);
                product.setQuantity(product.getQuantity().subtract(requestedQuantity));
                if (product.getQuantity().equals(BigDecimal.ZERO)) {
                    productsToBeDeleted.add(product);
                }
            });
            productsToBeUpdated.addAll(
                    new HashSet<>(productsToBlockProductDtos.keySet()));
        });
        productRepository.saveAllAndFlush(productsToBeUpdated);
        productRepository.deleteAll(productsToBeDeleted);
        return productsToBeUpdated.stream().map(productMapper::toProductResponseDto).toList();
    }

    public List<ProductResponseDto> updateProducts (boolean block,
                                                    List<BlockProductRequestDto> blockProductRequestDtos) {
        Set<Product> productsToBeUpdated = new HashSet<>();
        Map<String, Map<Product, List<BlockProductDto>>> addressesToProductsToBlockProductDtos =
                updateProductsBlockings(block, blockProductRequestDtos);
        addressesToProductsToBlockProductDtos.forEach((address, productsToBlockProductDtos) ->
                productsToBeUpdated.addAll(new HashSet<>(productsToBlockProductDtos.keySet())));
        productRepository.saveAllAndFlush(productsToBeUpdated);
        return productsToBeUpdated.stream().map(productMapper::toProductResponseDto).toList();
    }

    public Map<String, Map<Product, List<BlockProductDto>>> updateProductsBlockings(boolean block,
                                                                                    List<BlockProductRequestDto>
                                                                                            blockProductRequestDtos) {
        Map<String, Map<String, List<BlockProductDto>>> addressesToProductCodesToBlockProductDtos =
                blockProductRequestDtos.stream().collect(Collectors.groupingBy(
                        BlockProductRequestDto::getAddress, Collectors.groupingBy(
                                BlockProductRequestDto::getProductCode, Collectors.mapping(
                                        BlockProductRequestDto::getBlockInfo, Collectors.toList()))));

        Set<Product> productsFoundByAddresses = productRepository.findAllByAddressIn(
                addressesToProductCodesToBlockProductDtos.keySet());
        Map<String, Set<Product>> addressesToFoundProducts = productsFoundByAddresses.stream()
                .collect(Collectors.groupingBy(Product::getAddress, Collectors.toSet()));

        checkAddresses(addressesToProductCodesToBlockProductDtos, addressesToFoundProducts);
        checkProductCodesAtAddresses(addressesToProductCodesToBlockProductDtos, addressesToFoundProducts);

        Map<String, Map<String, Product>> addressesToProductCodesToProducts = new HashMap<>();
        productsFoundByAddresses.forEach(product -> {
            String address = product.getAddress();
            addressesToProductCodesToProducts.putIfAbsent(address, new HashMap<>());
            addressesToProductCodesToProducts.get(address).put(product.getProductCode(), product);
        });

        Map<String, Map<Product, List<BlockProductDto>>> addressesToProductsToBlockProductDtos = new HashMap<>();
        addressesToProductCodesToBlockProductDtos.forEach((address, reqItems) -> {
            Map<String, Product> foundOnAddress = addressesToProductCodesToProducts.get(address);
            reqItems.forEach((productCode, blockProductDtos) -> {
                Product product = foundOnAddress.get(productCode);
                addressesToProductsToBlockProductDtos.putIfAbsent(address, new HashMap<>());
                addressesToProductsToBlockProductDtos.get(address).put(product, blockProductDtos);
            });
        });

        blockUnblockProducts(block, addressesToProductsToBlockProductDtos);
        return addressesToProductsToBlockProductDtos;
    }

    private void blockUnblockProducts (boolean block,
                                       Map<String, Map<Product, List<BlockProductDto>>>
                                               addressesToProductsToBlockProductDtos) {
        for (Map.Entry<String, Map<Product, List<BlockProductDto>>> resultEntry :
                addressesToProductsToBlockProductDtos.entrySet()) {
            String currentAddress = resultEntry.getKey();
            for (Map.Entry<Product, List<BlockProductDto>> productToBlockingsEntry :
                    resultEntry.getValue().entrySet()) {
                if (block) {
                    blockProducts(productToBlockingsEntry, currentAddress);
                } else {
                    unblockProducts(productToBlockingsEntry);
                }
            }
        }
    }

    private void blockProducts(Map.Entry<Product, List<BlockProductDto>> productToBlockingsEntry,
                               String currentAddress) {
        Product currentProduct = productToBlockingsEntry.getKey();
        BigDecimal currentProductAvailableQuantity = getAvailableQuantity(currentProduct);

        List<BlockProductDto> blockProductDtosForCurrentProduct =
                productToBlockingsEntry.getValue();
        for (BlockProductDto blockProductDto : blockProductDtosForCurrentProduct) {
            if (blockProductDto.getBlockedQuantity().compareTo(currentProductAvailableQuantity) > 0) {
                throw new WarehouseManagerException(
                        ErrorCode.NOT_ENOUGH_OF_PRODUCT, currentProduct.getProductCode(), currentAddress);
            }
            currentProductAvailableQuantity = currentProductAvailableQuantity
                    .subtract(blockProductDto.getBlockedQuantity());
            currentProduct.getBlockings().stream()
                    .filter(blocking -> isBlockingTheSame(blocking, blockProductDto))
                    .findFirst()
                    .ifPresentOrElse(blocking -> blocking.setBlockedQuantity(blocking.getBlockedQuantity()
                                    .add(blockProductDto.getBlockedQuantity())),
                            () -> blockingMapper.toBlocking(blockProductDto).attachToProduct(currentProduct));
        }
    }

    private void unblockProducts(Map.Entry<Product, List<BlockProductDto>> productToBlockingsEntry) {
        Product currentProduct = productToBlockingsEntry.getKey();
        List<BlockProductDto> blockProductDtosForCurrentProduct =
                productToBlockingsEntry.getValue();
        for (BlockProductDto blockProductDto : blockProductDtosForCurrentProduct) {
            Blocking existingBlocking = currentProduct.getBlockings().stream()
                    .filter(blocking -> isBlockingTheSame(blocking, blockProductDto))
                    .findFirst().orElseThrow(() -> {
                        throw new WarehouseManagerException(
                                ErrorCode.BLOCKING_NOT_FOUND, currentProduct, blockProductDto);
                    });

            BigDecimal requestedQuantity = blockProductDto.getBlockedQuantity();
            BigDecimal currentQuantity = existingBlocking.getBlockedQuantity();
            if (currentQuantity.compareTo(requestedQuantity) < 0) {
                throw new WarehouseManagerException(ErrorCode.BLOCKED_QUANTITY_IS_NOT_ENOUGH_FOR_UNBLOCKING,
                        currentProduct, blockProductDto);
            } else if (currentQuantity.compareTo(requestedQuantity) > 0) {
                existingBlocking.setBlockedQuantity(
                        existingBlocking.getBlockedQuantity().subtract(requestedQuantity));
            } else {
                currentProduct.getBlockings().remove(existingBlocking);
            }
        }
    }

    private void checkAddresses(Map<String, Map<String, List<BlockProductDto>>>
                                                   addressesToProductCodeToRequestBlockingsMap,
                                Map<String, Set<Product>> allProductsFoundByAddressesMap) {
        addressesToProductCodeToRequestBlockingsMap.keySet().stream()
                .filter(address -> !allProductsFoundByAddressesMap.containsKey(address))
                .findFirst()
                .ifPresent(notFoundAddress -> {
                    throw new WarehouseManagerException(ErrorCode.ADDRESS_NOT_FOUND, notFoundAddress);
                });
    }

    private void checkProductCodesAtAddresses(Map<String, Map<String, List<BlockProductDto>>>
                                                                 addressesToProductCodeToRequestBlockingsMap,
                                              Map<String, Set<Product>> allProductsFoundByAddressesMap) {
        addressesToProductCodeToRequestBlockingsMap.forEach((address, blockInfosByProductCode) -> {
            Map<String, Product> existingProductsAtAddress = allProductsFoundByAddressesMap.get(address).stream()
                    .collect(Collectors.toMap(Product::getProductCode, Function.identity()));
            blockInfosByProductCode.forEach((productCode, blockInfos) -> {
                if (!existingProductsAtAddress.containsKey(productCode)) {
                    throw new WarehouseManagerException(
                            ErrorCode.PRODUCT_CODE_NOT_FOUND_AT_ADDRESS, productCode, address);
                }
            });
        });
    }

    private boolean isBlockingTheSame(Blocking blocking, BlockProductDto blockProductDto) {
        return blocking.getBlockingReason().equals(blockProductDto.getBlockingReason())
                && blocking.getBlockingToken().equals(blockProductDto.getBlockingToken());
    }

    private BigDecimal getAvailableQuantity(Product product) {
        return product.getQuantity().subtract(
                product.getBlockings().stream()
                        .map(Blocking::getBlockedQuantity).reduce(BigDecimal.ZERO, BigDecimal::add));
    }
}
