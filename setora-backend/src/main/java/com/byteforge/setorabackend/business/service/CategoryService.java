package com.byteforge.setorabackend.business.service;

import com.byteforge.setorabackend.business.dtos.CategoryDto;
import com.byteforge.setorabackend.business.repository.CategoryRepository;
import jakarta.persistence.EntityNotFoundException;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class CategoryService {

    private final CategoryRepository categoryRepository;

    @Transactional(readOnly = true)
    public List<CategoryDto> getAllCategories() {
        return categoryRepository.findAll(Sort.by(Sort.Direction.ASC, "name")).stream()
                .map(c -> new CategoryDto(c.getId(), c.getName()))
                .toList();
    }

    @Transactional(readOnly = true)
    public void existsById(Integer categoryId) {
        if (!categoryRepository.existsById(categoryId)) {
            throw new EntityNotFoundException("Category not found.");
        }
    }
}
