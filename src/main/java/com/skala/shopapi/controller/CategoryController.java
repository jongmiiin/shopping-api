package com.skala.shopapi.controller;

import com.skala.shopapi.common.PagedList;
import com.skala.shopapi.common.Response;
import com.skala.shopapi.entity.Category;
import com.skala.shopapi.service.CategoryService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "Category", description = "상품 카테고리 API")
@RestController
@RequestMapping("/api/categories")
@RequiredArgsConstructor
public class CategoryController {

    private final CategoryService categoryService;

    @Operation(summary = "카테고리 목록 조회 (페이징)")
    @GetMapping("/list")
    public Response<PagedList<Category>> getAllCategories(
            @RequestParam(defaultValue = "0") Integer offset,
            @RequestParam(defaultValue = "10") Integer count) {
        return categoryService.getAllCategories(offset, count);
    }

    @Operation(summary = "카테고리 상세 조회")
    @GetMapping("/{id}")
    public Response<Category> getCategoryById(@PathVariable Long id) {
        return categoryService.getCategoryById(id);
    }

    @Operation(summary = "카테고리 등록")
    @PostMapping
    public Response<Category> createCategory(@RequestBody Category category) {
        return categoryService.createCategory(category);
    }

    @Operation(summary = "카테고리 수정")
    @PutMapping
    public Response<Category> updateCategory(@RequestBody Category category) {
        return categoryService.updateCategory(category);
    }

    @Operation(summary = "카테고리 삭제")
    @DeleteMapping
    public Response<Void> deleteCategory(@RequestBody Category category) {
        return categoryService.deleteCategory(category);
    }
}
