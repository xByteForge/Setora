package com.byteforge.setorabackend.business;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import com.byteforge.setorabackend.business.dtos.*;
import com.byteforge.setorabackend.business.mapper.BusinessMapper;
import com.byteforge.setorabackend.business.model.Business;
import com.byteforge.setorabackend.business.repository.BusinessRepository;
import com.byteforge.setorabackend.business.service.BusinessService;
import com.byteforge.setorabackend.common.PaginationParams;
import jakarta.persistence.EntityNotFoundException;
import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.EnumSource;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.ArgumentCaptor;
import org.mockito.ArgumentMatchers;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;

@ExtendWith(MockitoExtension.class)
class BusinessServiceTest {

    @Mock
    private BusinessRepository businessRepository;

    @Mock
    private BusinessMapper businessMapper;

    @InjectMocks
    private BusinessService businessService;

    // findById
    @Test
    void findById_whenExists_returnsBusiness() {
        Business business = createBusiness(1, "Studio Armonia");
        when(businessRepository.findById(1)).thenReturn(Optional.of(business));

        Business result = businessService.findById(1);

        assertThat(result).isSameAs(business);
        verify(businessRepository).findById(1);
    }

    @Test
    void findById_whenNotFound_throwsEntityNotFoundException() {
        when(businessRepository.findById(99)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> businessService.findById(99))
                .isInstanceOf(EntityNotFoundException.class)
                .hasMessage("Business not found");
    }

    // getBusinessById
    @Test
    void getBusinessById_whenExists_returnsMappedDto() {
        Business business = createBusiness(1, "Studio Armonia");
        BusinessDetailedResponseDto expectedDto = createDetailedDto(1, "Studio Armonia");

        when(businessRepository.findById(1)).thenReturn(Optional.of(business));
        when(businessMapper.toDetailedResponseDto(business)).thenReturn(expectedDto);

        BusinessDetailedResponseDto result = businessService.getBusinessById(1);

        assertThat(result).isEqualTo(expectedDto);
        verify(businessRepository).findById(1);
        verify(businessMapper).toDetailedResponseDto(business);
    }

    @Test
    void getBusinessById_whenNotFound_throwsEntityNotFoundExceptionAndSkipsMapper() {
        when(businessRepository.findById(99)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> businessService.getBusinessById(99))
                .isInstanceOf(EntityNotFoundException.class)
                .hasMessage("Business not found");

        verifyNoInteractions(businessMapper);
    }

    // getAllBusinesses
    @Test
    void getAllBusinesses_delegatesWithNullCriteriaAndDefaultNewestSort() {
        Business business = createBusiness(1, "Studio Armonia");
        BusinessResponseDto dto = createDto(1, "Studio Armonia");
        PaginationParams pageParams = new PaginationParams(0, 10);
        Page<Business> repoPage = new PageImpl<>(List.of(business), Pageable.ofSize(10), 1);

        when(businessRepository.findAll(ArgumentMatchers.<Specification<Business>>any(), any(Pageable.class)))
                .thenReturn(repoPage);
        when(businessMapper.toResponseDto(business)).thenReturn(dto);

        Page<BusinessResponseDto> result = businessService.getAllBusinesses(pageParams);

        assertThat(result.getContent()).containsExactly(dto);
        Pageable captured = capturePageable();
        assertThat(captured.getPageNumber()).isEqualTo(0);
        assertThat(captured.getPageSize()).isEqualTo(10);
        assertThat(captured.getSort()).containsExactly(
                Sort.Order.desc("createdAt"),
                Sort.Order.desc("id")
        );
    }

    @Test
    void getAllBusinesses_whenEmpty_returnsEmptyPage() {
        when(businessRepository.findAll(ArgumentMatchers.<Specification<Business>>any(), any(Pageable.class)))
                .thenReturn(Page.empty());

        Page<BusinessResponseDto> result = businessService.getAllBusinesses(new PaginationParams(0, 20));

        assertThat(result).isEmpty();
        verifyNoInteractions(businessMapper);
    }

    // getBusinesses
    @Test
    void getBusinesses_whenSortByIsNull_defaultsToNewestWithIdTieBreaker() {
        PaginationParams pageParams = new PaginationParams(0, 20);
        when(businessRepository.findAll(ArgumentMatchers.<Specification<Business>>any(), any(Pageable.class)))
                .thenReturn(Page.empty());

        businessService.getBusinesses(emptyCriteria(), null, pageParams);

        Pageable captured = capturePageable();
        assertThat(captured.getSort()).containsExactly(
                Sort.Order.desc("createdAt"),
                Sort.Order.desc("id")
        );
    }

    @ParameterizedTest
    @MethodSource("standardSortCases")
    void getBusinesses_standardSort_appliesExpectedOrderAndTieBreaker(
            BusinessSortBy sortBy,
            Sort.Order primaryOrder,
            Sort.Order tieBreakerOrder
    ) {
        PaginationParams pageParams = new PaginationParams(1, 10);
        when(businessRepository.findAll(ArgumentMatchers.<Specification<Business>>any(), any(Pageable.class)))
                .thenReturn(Page.empty());

        businessService.getBusinesses(emptyCriteria(), sortBy, pageParams);

        Pageable captured = capturePageable();
        assertThat(captured.getPageNumber()).isEqualTo(1);
        assertThat(captured.getPageSize()).isEqualTo(10);
        assertThat(captured.getSort()).containsExactly(primaryOrder, tieBreakerOrder);
    }

    static Stream<Arguments> standardSortCases() {
        return Stream.of(
                Arguments.of(BusinessSortBy.NAME_ASC, Sort.Order.asc("name"), Sort.Order.asc("id")),
                Arguments.of(BusinessSortBy.NAME_DESC, Sort.Order.desc("name"), Sort.Order.desc("id")),
                Arguments.of(BusinessSortBy.OLDEST, Sort.Order.asc("createdAt"), Sort.Order.asc("id")),
                Arguments.of(BusinessSortBy.NEWEST, Sort.Order.desc("createdAt"), Sort.Order.desc("id"))
        );
    }

    @ParameterizedTest
    @EnumSource(value = BusinessSortBy.class, names = {"PRICE_ASC", "PRICE_DESC"})
    void getBusinesses_priceSort_passesUnsortedPageableSoSpecificationOrderIsNotOverridden(
            BusinessSortBy priceSort
    ) {
        PaginationParams pageParams = new PaginationParams(2, 15);
        when(businessRepository.findAll(ArgumentMatchers.<Specification<Business>>any(), any(Pageable.class)))
                .thenReturn(Page.empty());

        businessService.getBusinesses(emptyCriteria(), priceSort, pageParams);

        Pageable captured = capturePageable();
        assertThat(captured.getPageNumber()).isEqualTo(2);
        assertThat(captured.getPageSize()).isEqualTo(15);
        assertThat(captured.getSort().isUnsorted()).isTrue();
    }

    @Test
    void getBusinesses_withFullDiscoveryCriteria_mapsResultsAndPreservesMetadata() {
        Business b1 = createBusiness(1, "Alpha Cuts");
        Business b2 = createBusiness(2, "Beta Spa");
        BusinessResponseDto dto1 = createDto(1, "Alpha Cuts");
        BusinessResponseDto dto2 = createDto(2, "Beta Spa");

        BusinessFilterCriteria criteria = new BusinessFilterCriteria(
                null,
                "cuts",
                "Cluj-Napoca",
                List.of("barbershop"),
                1,
                2,
                new BigDecimal("50.00"),
                new BigDecimal("150.00"),
                LocalDate.of(2026, 11, 25),
                LocalTime.of(6, 0),
                LocalTime.of(12, 0)
        );

        PaginationParams pageParams = new PaginationParams(0, 2);
        Page<Business> repoPage = new PageImpl<>(List.of(b1, b2), Pageable.ofSize(2), 5);

        when(businessRepository.findAll(ArgumentMatchers.<Specification<Business>>any(), any(Pageable.class)))
                .thenReturn(repoPage);
        when(businessMapper.toResponseDto(b1)).thenReturn(dto1);
        when(businessMapper.toResponseDto(b2)).thenReturn(dto2);

        Page<BusinessResponseDto> result = businessService.getBusinesses(
                criteria, BusinessSortBy.NAME_ASC, pageParams
        );

        assertThat(result.getContent()).containsExactly(dto1, dto2);
        assertThat(result.getTotalElements()).isEqualTo(5);
        assertThat(result.getTotalPages()).isEqualTo(3);
        assertThat(result.getNumber()).isEqualTo(0);
        verify(businessMapper).toResponseDto(b1);
        verify(businessMapper).toResponseDto(b2);
    }

    @Test
    void getBusinesses_whenNoMatches_returnsEmptyPageAndSkipsMapper() {
        when(businessRepository.findAll(ArgumentMatchers.<Specification<Business>>any(), any(Pageable.class)))
                .thenReturn(Page.empty());

        Page<BusinessResponseDto> result = businessService.getBusinesses(
                emptyCriteria(), BusinessSortBy.NEWEST, new PaginationParams(0, 20)
        );

        assertThat(result).isEmpty();
        verifyNoInteractions(businessMapper);
    }

    @Test
    void getBusinesses_whenCriteriaIsNull_doesNotThrowAndDelegatesToRepository() {
        when(businessRepository.findAll(ArgumentMatchers.<Specification<Business>>any(), any(Pageable.class)))
                .thenReturn(Page.empty());

        Page<BusinessResponseDto> result = businessService.getBusinesses(
                null, BusinessSortBy.NEWEST, new PaginationParams(null, null)
        );

        assertThat(result).isEmpty();
        Pageable captured = capturePageable();
        assertThat(captured.getPageNumber()).isEqualTo(0);
        assertThat(captured.getPageSize()).isEqualTo(20);
    }

    // BusinessFilterCriteria validation & helper methods
    @Test
    void filterCriteria_validatesPriceAndTimeRanges() {
        BusinessFilterCriteria valid = new BusinessFilterCriteria(
                null, null, null, null, null, null,
                new BigDecimal("60"), new BigDecimal("120"),
                null, LocalTime.of(12, 0), LocalTime.of(18, 0)
        );
        assertThat(valid.isPriceRangeValid()).isTrue();
        assertThat(valid.isTimeRangeValid()).isTrue();

        BusinessFilterCriteria equalBounds = new BusinessFilterCriteria(
                null, null, null, null, null, null,
                new BigDecimal("100"), new BigDecimal("100"),
                null, LocalTime.of(12, 0), LocalTime.of(12, 0)
        );
        assertThat(equalBounds.isPriceRangeValid()).isTrue();
        assertThat(equalBounds.isTimeRangeValid()).isTrue();

        BusinessFilterCriteria invalid = new BusinessFilterCriteria(
                null, null, null, null, null, null,
                new BigDecimal("200"), new BigDecimal("50"),
                null, LocalTime.of(18, 0), LocalTime.of(6, 0)
        );
        assertThat(invalid.isPriceRangeValid()).isFalse();
        assertThat(invalid.isTimeRangeValid()).isFalse();
    }

    @Test
    void filterCriteria_helperFlagsDetectActiveFilters() {
        assertThat(emptyCriteria().hasText()).isFalse();
        assertThat(emptyCriteria().hasService()).isFalse();
        assertThat(emptyCriteria().hasScheduleFilter()).isFalse();

        BusinessFilterCriteria active = new BusinessFilterCriteria(
                null, "  barber ", null, null, null, 3,
                null, null, null, LocalTime.of(18, 0), null
        );
        assertThat(active.hasText()).isTrue();
        assertThat(active.hasService()).isTrue();
        assertThat(active.hasScheduleFilter()).isTrue();
    }

    // helpers
    private Pageable capturePageable() {
        ArgumentCaptor<Pageable> captor = ArgumentCaptor.forClass(Pageable.class);
        verify(businessRepository).findAll(ArgumentMatchers.<Specification<Business>>any(), captor.capture());
        return captor.getValue();
    }

    private BusinessFilterCriteria emptyCriteria() {
        return new BusinessFilterCriteria(
                null, null, null, null, null, null, null, null, null, null, null
        );
    }

    private Business createBusiness(int id, String name) {
        Business business = new Business();
        business.setId(id);
        business.setName(name);
        return business;
    }

    private BusinessResponseDto createDto(int id, String name) {
        return new BusinessResponseDto(
                id,
                10,
                "Barbershop",
                name,
                name.toLowerCase().replace(" ", "-"),
                null,
                new BusinessLocationDto("Piezisa", "Piezisa nr. 4", 46.77, 23.59),
                "Cluj-Napoca",
                null,
                "contact@example.com",
                "Europe/Bucharest",
                Instant.parse("2026-11-01T10:00:00Z"),
                List.of(),
                null,
                List.of(),
                BigDecimal.ZERO
        );
    }

    private BusinessDetailedResponseDto createDetailedDto(int id, String name) {
        return new BusinessDetailedResponseDto(
                id,
                10,
                new CategoryDto(1, "Barbershop"),
                name,
                name.toLowerCase().replace(" ", "-"),
                null,
                new BusinessLocationDto("Piezisa", "Piezisa nr. 4", 46.77, 23.59),
                null,
                "contact@example.com",
                "Europe/Bucharest",
                Instant.parse("2026-11-01T10:00:00Z"),
                List.of(),
                null,
                List.of(),
                List.of()
        );
    }
}