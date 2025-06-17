package com.vserdiuk.casestudy.validator;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import jakarta.validation.Validator;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Set;

/**
 * A validator component responsible for validating Data Transfer Objects (DTOs)
 * using the Jakarta Bean Validation API.
 * <p>
 * This class uses a provided {@link Validator} instance to perform validation
 * on DTO objects and throws a {@link ConstraintViolationException} if any
 * validation constraints are violated.
 * </p>
 */
@Component
@RequiredArgsConstructor
public class ProductValidator {

    private final Validator validator;

    /**
     * Validates the provided DTO object against defined validation constraints.
     * <p>
     * This method uses the injected {@link Validator} to check the DTO for
     * constraint violations. If any violations are found, a
     * {@link ConstraintViolationException} is thrown containing the set of
     * violations.
     * </p>
     *
     * @param dto the Data Transfer Object to validate
     * @throws ConstraintViolationException if the DTO contains any validation constraint violations
     */
    public void validateDTO(Object dto) {
        Set<ConstraintViolation<Object>> violations = validator.validate(dto);
        if (!violations.isEmpty()) {
            throw new ConstraintViolationException(violations);
        }
    }
}