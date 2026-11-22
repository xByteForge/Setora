package com.byteforge.setorabackend.business.repository;

import com.byteforge.setorabackend.business.model.Category;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CategoryRepository extends JpaRepository<Category, Integer> {}