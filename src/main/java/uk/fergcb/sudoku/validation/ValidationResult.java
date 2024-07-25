package uk.fergcb.sudoku.validation;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public abstract class ValidationResult {

    public static ValidationResult valid() {
        return new Valid();
    }

    public static ValidationResult invalid(List<ValidationError> errors) {
        return new Invalid(errors);
    }

    public abstract boolean isValid();

    public abstract List<ValidationError> getErrors();

    public ValidationResult merge(ValidationResult other) {
        if (this.isValid() && other.isValid()) return valid();
        if (!this.isValid() && other.isValid()) return invalid(this.getErrors());
        if (this.isValid() && !other.isValid()) return invalid(other.getErrors());
        final var errors = Stream.concat(this.getErrors().stream(), other.getErrors().stream())
                .collect(Collectors.toList());
        return invalid(errors);
    }

    private static class Valid extends ValidationResult {
        @Override
        public boolean isValid() {
            return true;
        }

        @Override
        public List<ValidationError> getErrors() {
            throw new IllegalStateException("Cannot get errors for valid solution.");
        }
    }

    private static class Invalid extends ValidationResult {

        private final List<ValidationError> errors;

        public Invalid(List<ValidationError> errors) {
            this.errors = errors;
        }

        @Override
        public boolean isValid() {
            return false;
        }

        @Override
        public List<ValidationError> getErrors() {
            return errors;
        }
    }
}
