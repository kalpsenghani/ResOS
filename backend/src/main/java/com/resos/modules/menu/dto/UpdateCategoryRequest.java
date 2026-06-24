package com.resos.modules.menu.dto;

public record UpdateCategoryRequest(String name, String description, Integer sortOrder, Boolean active) {}
