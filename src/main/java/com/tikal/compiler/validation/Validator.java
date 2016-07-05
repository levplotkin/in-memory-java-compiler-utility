package com.tikal.compiler.validation;

import java.util.ArrayList;
import java.util.List;

/**
 * this class keeps list of validations, and invokes they all till one fails or all passes
 *
 * @author levp
 */
public class Validator {
    private List<Validation> validations = new ArrayList<Validation>();

    private Validator(List<Validation> validations) {
        this.validations = validations;
    }

    /**
     * @return true if all validations are successfully passed
     */
    public boolean isValid() {
        for (final Validation validation : validations) {
            if (!validation.isPassed())
                return false;
        }
        return true;
    }

    /**
     * Validator builder
     */
    public static class Builder {
        private final List<Validation> validations = new ArrayList<Validation>();

        /**
         * @param validation will be added to list of validations
         */
        public Builder addValidation(final Validation validation) {
            validations.add(validation);
            return this;
        }

        /**
         * @return new com.tikal.compiler.validation.Validator
         */
        public Validator build() {
            return new Validator(validations);
        }
    }
}