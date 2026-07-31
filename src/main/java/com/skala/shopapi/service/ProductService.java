package com.skala.shopapi.service;

import com.skala.shopapi.common.PagedList;
import com.skala.shopapi.common.Response;
import com.skala.shopapi.entity.Product;
import com.skala.shopapi.exception.Error;
import com.skala.shopapi.exception.ParameterException;
import com.skala.shopapi.exception.ResponseException;
import com.skala.shopapi.repository.ProductRepository;
import com.skala.shopapi.tools.StringUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ProductService {

    private final ProductRepository productRepository;

    public Response<PagedList<Product>> getAllProducts(int offset, int count) {
        Page<Product> page = productRepository.findAll(PageRequest.of(offset, count));
        return Response.success(PagedList.of(page, offset, count));
    }

    public Response<Product> getProductById(Long id) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new ResponseException(Error.DATA_NOT_FOUND));
        return Response.success(product);
    }

    public Response<Product> createProduct(Product product) {
        validateProduct(product);

        productRepository.findByProductName(product.getProductName())
                .ifPresent(p -> {
                    throw new ResponseException(Error.DATA_DUPLICATED);
                });

        product.setId(null);
        Product saved = productRepository.save(product);
        return Response.success(saved);
    }

    public Response<Product> updateProduct(Product product) {
        validateProduct(product);

        Product existing = productRepository.findById(product.getId())
                .orElseThrow(() -> new ResponseException(Error.DATA_NOT_FOUND));

        existing.setProductName(product.getProductName());
        existing.setProductPrice(product.getProductPrice());
        Product saved = productRepository.save(existing);
        return Response.success(saved);
    }

    public Response<Void> deleteProduct(Product product) {
        Product existing = productRepository.findById(product.getId())
                .orElseThrow(() -> new ResponseException(Error.DATA_NOT_FOUND));

        productRepository.delete(existing);
        return Response.success(null);
    }

    private void validateProduct(Product product) {
        if (StringUtil.isAnyEmpty(product.getProductName())
                || product.getProductPrice() == null
                || product.getProductPrice() <= 0) {
            throw new ParameterException("productName", "productPrice");
        }
    }
}
