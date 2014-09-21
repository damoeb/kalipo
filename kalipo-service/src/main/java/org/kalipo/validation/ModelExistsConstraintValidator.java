package org.kalipo.validation;

import org.kalipo.repository.CommentRepository;

import javax.inject.Inject;
import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;

/**
 * Created by damoeb on 21.09.14.
 */
public class ModelExistsConstraintValidator implements ConstraintValidator<ModelExistsConstraint, String> {

    @Inject
    private CommentRepository commentRepository;
    private Class<?> model;

    @Override
    public void initialize(ModelExistsConstraint constraintAnnotation) {
        this.model = constraintAnnotation.value();
    }

    @Override
    public boolean isValid(String id, ConstraintValidatorContext context) {
        return id != null && commentRepository.exists(id);
    }
}
