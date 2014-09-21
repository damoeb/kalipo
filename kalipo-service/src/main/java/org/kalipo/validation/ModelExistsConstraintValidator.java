package org.kalipo.validation;

import org.apache.commons.lang3.StringUtils;
import org.kalipo.domain.Comment;
import org.kalipo.repository.CommentRepository;
import org.kalipo.repository.ThreadRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;

/**
 * Created by damoeb on 21.09.14.
 */
public class ModelExistsConstraintValidator implements ConstraintValidator<ModelExistsConstraint, String> {

    private final Logger log = LoggerFactory.getLogger(ModelExistsConstraintValidator.class);

    @Inject
    private CommentRepository commentRepository;
    @Inject
    private ThreadRepository threadRepository;
    private Class<?> clazz;

    @Override
    public void initialize(ModelExistsConstraint constraintAnnotation) {
        this.clazz = constraintAnnotation.value();
    }

    @Override
    public boolean isValid(String id, ConstraintValidatorContext context) {
        if (StringUtils.isBlank(id)) {
            return false;
        }
        if (clazz == Comment.class) {
            return commentRepository.exists(id);
        }
        if (clazz == org.kalipo.domain.Thread.class) {
            return threadRepository.exists(id);
        }

        log.error(String.format("Class %s is not supported", clazz));
        return false;
    }
}
