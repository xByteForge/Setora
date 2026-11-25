package com.byteforge.setorabackend.business.mapper;

import com.byteforge.setorabackend.business.dtos.BusinessDetailedResponseDto;
import com.byteforge.setorabackend.business.dtos.BusinessLocationDto;
import com.byteforge.setorabackend.business.dtos.BusinessResponseDto;
import com.byteforge.setorabackend.business.dtos.CategoryDto;
import com.byteforge.setorabackend.business.model.Business;
import com.byteforge.setorabackend.business.model.Category;
import com.byteforge.setorabackend.business.model.Location;
import com.byteforge.setorabackend.serviceItem.ServiceItem;
import com.byteforge.setorabackend.serviceItem.dtos.ServiceResponseDto;
import com.byteforge.setorabackend.serviceItem.mapper.ServiceItemMapper;
import java.math.BigDecimal;
import java.util.Arrays;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class BusinessMapper {

    private final ServiceItemMapper serviceItemMapper;

    public BusinessResponseDto toResponseDto(Business business) {
        BigDecimal defaultMinPrice = business.getServiceItems() == null
                ? BigDecimal.ZERO
                : business.getServiceItems().stream()
                        .filter(ServiceItem::isActive)
                        .map(ServiceItem::getPrice)
                        .min(BigDecimal::compareTo)
                        .orElse(BigDecimal.ZERO);

        return toResponseDto(business, defaultMinPrice);
    }

    public BusinessResponseDto toResponseDto(Business business, BigDecimal minPrice) {
        String categoryName =
                business.getCategory() != null ? business.getCategory().getName() : "";

        String city = business.getLocation() != null && business.getLocation().getLocationName() != null
                ? business.getLocation().getLocationName()
                : "";

        return new BusinessResponseDto(
                business.getId(),
                business.getOwner().getId(),
                categoryName,
                business.getName(),
                business.getSlug(),
                business.getDescription(),
                toLocationDto(business.getLocation()),
                city,
                business.getPhone(),
                business.getEmail(),
                business.getTimezone().getId(),
                business.getCreatedAt(),
                business.getPhotoUrls() != null ? List.of(business.getPhotoUrls()) : List.of(),
                business.getThumbnailUrl(),
                business.getTags() != null ? List.of(business.getTags()) : List.of(),
                minPrice != null ? minPrice : BigDecimal.ZERO);
    }

    public BusinessDetailedResponseDto toDetailedResponseDto(Business business) {
        List<ServiceResponseDto> activeServices = business.getServiceItems() == null
                ? List.of()
                : serviceItemMapper.toResponseDtoList(business.getServiceItems().stream()
                        .filter(ServiceItem::isActive)
                        .toList());

        return new BusinessDetailedResponseDto(
                business.getId(),
                business.getOwner().getId(),
                toCategoryDto(business.getCategory()),
                business.getName(),
                business.getSlug(),
                business.getDescription(),
                toLocationDto(business.getLocation()),
                business.getPhone(),
                business.getEmail(),
                business.getTimezone().getId(),
                business.getCreatedAt(),
                business.getPhotoUrls() != null ? List.of(business.getPhotoUrls()) : List.of(),
                business.getThumbnailUrl(),
                business.getTags() != null ? List.of(business.getTags()) : List.of(),
                activeServices);
    }

    private BusinessLocationDto toLocationDto(Location location) {
        if (location == null) {
            return new BusinessLocationDto(null, null, null, null);
        }
        return new BusinessLocationDto(
                location.getLocationName(), location.getAddress(), location.getLatitude(), location.getLongitude());
    }

    private CategoryDto toCategoryDto(Category category) {
        if (category == null) {
            return null;
        }
        return new CategoryDto(category.getId(), category.getName());
    }

    private List<String> toList(String[] array) {
        return array == null ? List.of() : Arrays.asList(array);
    }
}
