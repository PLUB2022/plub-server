package plub.plubserver.domain.category.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import plub.plubserver.domain.category.dto.CategoryDto.*;
import plub.plubserver.domain.category.exception.CategoryError;
import plub.plubserver.domain.category.exception.CategoryException;
import plub.plubserver.domain.category.repository.CategoryRepository;
import plub.plubserver.domain.category.repository.CategorySubRepository;

import java.sql.Timestamp;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class CategoryService {
    private final CategoryRepository categoryRepository;
    private final CategorySubRepository categorySubRepository;

    public List<CategoryListResponse> getAllCategory() {
        return categoryRepository.findAll()
                .stream().map(CategoryListResponse::of)
                .collect(Collectors.toList());
    }

    public List<CategorySubListResponse> getAllCategorySub() {
        return categorySubRepository.findAll()
                .stream().map(CategorySubListResponse::of)
                .collect(Collectors.toList());
    }

    public CategoryVersionResponse getCategoryVersion() {
       Timestamp categoryLatestDate = categoryRepository.getLatestDate()
                .orElseThrow(()->new CategoryException(CategoryError.NOT_FOUND_CATEGORY));
       Timestamp categorySubLatestDate = categorySubRepository.getLatestDate()
                .orElseThrow(()->new CategoryException(CategoryError.NOT_FOUND_CATEGORY));

       if(categoryLatestDate.before(categorySubLatestDate))
           return CategoryVersionResponse.of(categorySubLatestDate.toString(), "categorySub");
       else
           return CategoryVersionResponse.of(categoryLatestDate.toString(), "category");
    }
}
