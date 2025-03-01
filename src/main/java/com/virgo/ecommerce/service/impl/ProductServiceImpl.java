package com.virgo.ecommerce.service.impl;

import com.virgo.ecommerce.config.advisers.exception.NotFoundException;
import com.virgo.ecommerce.model.meta.Product;
import com.virgo.ecommerce.model.meta.ProductStock;
import com.virgo.ecommerce.model.meta.Store;
import com.virgo.ecommerce.model.meta.User;
import com.virgo.ecommerce.repo.ProductRepository;
import com.virgo.ecommerce.repo.ProductStockRepository;
import com.virgo.ecommerce.repo.StoreRepository;
import com.virgo.ecommerce.service.AuthenticationService;
import com.virgo.ecommerce.service.ProductService;
import com.virgo.ecommerce.utils.dto.ProductRequestDTO;
import com.virgo.ecommerce.utils.dto.ProductDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ProductServiceImpl implements ProductService {
    private final ProductRepository productRepository;
    private final ProductStockRepository productStockRepository;
    private final AuthenticationService authenticationService;
    private final StoreRepository storeRepository;

    @Override
    @CachePut(value = "products", key = "#result.id")
    public ProductDTO create(ProductRequestDTO req) {
        User user = authenticationService.getUserAuthenticated();
        Store store = storeRepository.findByUserId(user.getId()).orElseThrow(() -> new NotFoundException("User belum memiliki Store, Buat Store Terlebih Dahulu!"));
        Integer reqStock = req.getStock() != null ? req.getStock() : 0;

        //create product
        Product product = Product.builder()
                .store_id(store.getId())
                .product_name(req.getProduct_name())
                .product_description(req.getProduct_description())
                .price(req.getPrice())
                .build();
        ProductDTO res = productRepository.save(product);
        //create stock
        productStockRepository.save(
                ProductStock.builder()
                        .products_id(res.getId())
                        .quantity(reqStock)
                        .build()
        );
        res.setStock(reqStock);

        return res;
    }

    @Override
    @Cacheable(value = "products")
    public List<ProductDTO> getAll() {
        return productRepository.findAll();
    }

    @Override
    @Cacheable(value = "products", key = "#id")
    public ProductDTO getById(Integer id) {
        return productRepository.findById(id).orElseThrow(() -> new NotFoundException("Product not found"));
    }

    @Override
    @Cacheable(value = "products", key = "#storeId")
    public List<ProductDTO> getByStoreId(Integer storeId) {
        return productRepository.findByStoreId(storeId);
    }

    @Override
    @CacheEvict(value = "products", key = "#id")
    public void delete(Integer id) {
        if (productRepository.findById(id).isPresent()) {
            productRepository.deleteById(id);
        } else {
            throw new NotFoundException("Product with ID " + id + " not found");
        }
    }

    @Override
    @CachePut(value = "products", key = "#id")
    public ProductDTO updateById(Integer id, ProductRequestDTO req) {
        ProductDTO product = getById(id);
        product.setStore_id(product.getStore_id());
        product.setProduct_name(req.getProduct_name() != null ? req.getProduct_name() : product.getProduct_name());
        product.setProduct_description(req.getProduct_description() != null ? req.getProduct_description() : product.getProduct_description());
        product.setPrice(req.getPrice() != null ? req.getPrice() : product.getPrice());
        productRepository.update(productDTOToProduct(product));

        //update stock
        if (req.getStock() != null){
            ProductStock productStock= productStockRepository.findByProductId(product.getId()).orElseThrow(() -> new NotFoundException("product stock not found"));
            productStock.setQuantity(req.getStock());
            productStockRepository.update(productStock);
            product.setStock(productStock.getQuantity());
        }
        return product;
    }

    private Product productDTOToProduct(ProductDTO product){

        return Product.builder()
                .id(product.getId())
                .store_id(product.getStore_id())
                .product_name(product.getProduct_name())
                .product_description(product.getProduct_description())
                .price(product.getPrice())
                .build();
    }
}