package org.anarchadia.quasar.api.setting;

import java.util.function.Predicate;

public class Setting<T> {
    private final String name;
    private final String description;
    private T value;
    private T minimum;
    private T maximum;
    private T increment;
    private Predicate<Setting<?>> visibilityCondition;

    public Setting(String name, String description, T value) {
        this.name = name;
        this.description = description;
        this.value = value;
    }

    public Setting(String name, String description, T value, T minimum, T maximum, T increment) {
        this.name = name;
        this.description = description;
        this.value = value;
        this.minimum = minimum;
        this.maximum = maximum;
        this.increment = increment;
    }

    public T getValue() {
        return value;
    }

    public Setting<T> setVisibilityCondition(Predicate<Setting<?>> condition) {
        this.visibilityCondition = condition;
        return this; // Return this to allow method chaining
    }

    public boolean isVisible() {
        return visibilityCondition == null || visibilityCondition.test(this);
    }

    public void setValue(Object newValue) {
        if (value instanceof Boolean) {
            this.value = (T) Boolean.valueOf(newValue instanceof Boolean ? (Boolean) newValue : Boolean.parseBoolean(newValue.toString()));
        } else if (value instanceof Number) {
            double doubleValue = newValue instanceof Number ? ((Number) newValue).doubleValue() : Double.parseDouble(newValue.toString());
            this.value = (T) createNumber(doubleValue, (Number) value);
        } else if (value instanceof Enum) {
            if (newValue instanceof String) {
                // Handle casting to correct enum type
                this.value = (T) Enum.valueOf(((Enum<?>) value).getDeclaringClass(), (String) newValue);
            } else if (newValue instanceof Integer) {
                int ordinal = (Integer) newValue;
                Enum<?>[] enumConstants = ((Enum<?>) value).getDeclaringClass().getEnumConstants();
                if (ordinal >= 0 && ordinal < enumConstants.length) {
                    this.value = (T) enumConstants[ordinal];
                } else {
                    throw new IllegalArgumentException("Invalid ordinal value");
                }
            } else if (newValue instanceof Enum<?>) {
                this.value = (T) newValue;
            } else {
                throw new ClassCastException("Cannot cast " + newValue + " to " + value.getClass().getSimpleName());
            }
        } else if (value instanceof String) {
            this.value = (T) newValue.toString();
        } else {
            throw new ClassCastException("Cannot cast " + newValue + " to " + value.getClass().getSimpleName());
        }
    }


    public int getInt() {
        if (value instanceof Number) {
            return ((Number) value).intValue();
        } else if (value instanceof Enum<?>) {
            return ((Enum<?>) value).ordinal();
        }
        throw new ClassCastException("Cannot cast value to Integer");
    }

    public double getDouble() {
        if (value instanceof Number) {
            return ((Number) value).doubleValue();
        } else if (value instanceof Enum<?>) {
            return (double) ((Enum<?>) value).ordinal();
        }
        throw new ClassCastException("Cannot cast value to Double");
    }

    @SuppressWarnings("unchecked")
    public <E extends Enum<E>> E getEnum(Class<E> enumClass) {
        if (value instanceof Number) {
            int ordinal = ((Number) value).intValue();
            E[] enumConstants = enumClass.getEnumConstants();
            if (ordinal >= 0 && ordinal < enumConstants.length) {
                return enumConstants[ordinal];
            }
            throw new IllegalArgumentException("Invalid ordinal value");
        } else if (value instanceof Enum<?>) {
            return (E) value;
        }
        throw new ClassCastException("Cannot cast value to Enum");
    }

    public String getString() {
        return value.toString();
    }

    public boolean isEnabled() {
        if (value instanceof Boolean) {
            return (Boolean) value;
        } else if (value instanceof Number) {
            return ((Number) value).doubleValue() != 0;
        } else if (value instanceof String) {
            return !((String) value).isEmpty();
        } else if (value instanceof Enum<?>) {
            return ((Enum<?>) value).ordinal() != 0;
        }
        return value != null;
    }

    @SuppressWarnings("unchecked")
    private T createNumber(double value, Number originalValue) {
        if (originalValue instanceof Double) {
            return (T) Double.valueOf(value);
        } else if (originalValue instanceof Float) {
            return (T) Float.valueOf((float) value);
        } else if (originalValue instanceof Integer) {
            return (T) Integer.valueOf((int) Math.round(value));
        } else if (originalValue instanceof Long) {
            return (T) Long.valueOf(Math.round(value));
        } else if (originalValue instanceof Short) {
            return (T) Short.valueOf((short) Math.round(value));
        } else if (originalValue instanceof Byte) {
            return (T) Byte.valueOf((byte) Math.round(value));
        } else {
            throw new UnsupportedOperationException("Number type not supported");
        }
    }

    public T getMinimum() {
        // If minimum is null, return a default value (e.g., 0 for numbers)
        return minimum != null ? minimum : (T) (Number.class.isAssignableFrom(value.getClass()) ? 0 : null);
    }

    public void setMinimum(T minimum) {
        this.minimum = minimum;
    }

    public T getMaximum() {
        return maximum != null ? maximum : (T) (Number.class.isAssignableFrom(value.getClass()) ? 0 : null);
    }

    public void setMaximum(T maximum) {
        this.maximum = maximum;
    }

    public T getIncrement() {
        return increment != null ? increment : (T) (Number.class.isAssignableFrom(value.getClass()) ? 1 : null);
    }

    public void setIncrement(T increment) {
        this.increment = increment;
    }

    public String getName() {
        return name;
    }

    public String getDescription() {
        return description;
    }
}