package com.skala.shopapi.service;

import com.skala.shopapi.common.PagedList;
import com.skala.shopapi.common.Response;
import com.skala.shopapi.entity.Category;
import com.skala.shopapi.entity.Product;
import com.skala.shopapi.exception.Error;
import com.skala.shopapi.exception.ParameterException;
import com.skala.shopapi.exception.ResponseException;
import com.skala.shopapi.repository.CategoryRepository;
import com.skala.shopapi.repository.ProductRepository;
import com.skala.shopapi.tools.StringUtil;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class CategoryService {

    private final CategoryRepository categoryRepository;
    private final ProductRepository productRepository;

    public Response<PagedList<Category>> getAllCategories(int offset, int count) {
        Page<Category> page = categoryRepository.findAll(PageRequest.of(offset, count));
        return Response.success(PagedList.of(page, offset, count));
    }

    public Response<Category> getCategoryById(Long id) {
        Category category = categoryRepository.findById(id)
                .orElseThrow(() -> new ResponseException(Error.DATA_NOT_FOUND));
        return Response.success(category);
    }

    public Response<Category> createCategory(Category category) {
        if (StringUtil.isAnyEmpty(category.getCategoryName())) {
            throw new ParameterException("categoryName");
        }

        category.setId(null);
        Category saved = categoryRepository.save(category);
        return Response.success(saved);
    }

    public Response<Category> updateCategory(Category category) {
        if (StringUtil.isAnyEmpty(category.getCategoryName())) {
            throw new ParameterException("categoryName");
        }

        Category existing = categoryRepository.findById(category.getId())
                .orElseThrow(() -> new ResponseException(Error.DATA_NOT_FOUND));

        existing.setCategoryName(category.getCategoryName());
        Category saved = categoryRepository.save(existing);
        return Response.success(saved);
    }

    @Transactional
    public Response<Void> deleteCategory(Category category) {
        Category existing = categoryRepository.findById(category.getId())
                .orElseThrow(() -> new ResponseException(Error.DATA_NOT_FOUND));

        List<Product> linkedProducts = productRepository.findByCategory_Id(existing.getId());
        linkedProducts.forEach(product -> product.setCategory(null));

        categoryRepository.delete(existing);
        return Response.success(null);
    }
}
