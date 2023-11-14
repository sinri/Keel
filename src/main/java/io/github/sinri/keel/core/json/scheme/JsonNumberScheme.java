package io.github.sinri.keel.core.json.scheme;

import io.vertx.core.json.JsonObject;

import javax.annotation.Nonnull;
import java.math.BigDecimal;

/**
 * @since 2.7
 */
public class JsonNumberScheme extends JsonValueScheme<Number> {
    //private boolean withFractions;

    private boolean inclusiveMin;
    private Number min;
    private boolean inclusiveMax;
    private Number max;

    public JsonNumberScheme setMin(Number min, boolean inclusive) {
        this.min = min;
        this.inclusiveMin = inclusive;
        return this;
    }

    public JsonNumberScheme setMax(Number max, boolean inclusive) {
        this.max = max;
        this.inclusiveMax = inclusive;
        return this;
    }

    public Number getMax() {
        return max;
    }

    public Number getMin() {
        return min;
    }

    public boolean isInclusiveMin() {
        return inclusiveMin;
    }

    public boolean isInclusiveMax() {
        return inclusiveMax;
    }

    @Override
    public JsonElementSchemeType getJsonElementSchemeType() {
        return JsonElementSchemeType.JsonNumber;
    }

    @Override
    public @Nonnull JsonObject toJsonObject() {
        return super.toJsonObject()
                .put("scheme_type", getJsonElementSchemeType());
    }

    @Override
    public @Nonnull JsonElementScheme<Number> reloadDataFromJsonObject(@Nonnull JsonObject jsonObject) {
        super.reloadDataFromJsonObject(jsonObject);
        return this;
    }

//    @Override
//    public void validate(Object object) throws JsonSchemeMismatchException {
//        if (object == null) {
//            if(!isNullable()){
//                throw new JsonSchemeMismatchException(JsonSchemeMismatchException.RuleNullableNotAllowed);
//            }
//        }
//        if (object instanceof Number) {
//            if (object instanceof Double || object instanceof Float || object instanceof BigDecimal) {
//                double v = ((Number) object).doubleValue();
//                if (this.min != null) {
//                    if (this.min.doubleValue() > v) {
//                        throw new JsonSchemeMismatchException(JsonSchemeMismatchException.RuleValueNotExpected);
//                    }
//                    if (!this.inclusiveMin && this.min.doubleValue() == v) {
//                        throw new JsonSchemeMismatchException(JsonSchemeMismatchException.RuleValueNotExpected);
//                    }
//                }
//                if (this.max != null) {
//                    if (this.max.doubleValue() < v) {
//                        throw new JsonSchemeMismatchException(JsonSchemeMismatchException.RuleValueNotExpected);
//                    }
//                    if(!( this.inclusiveMax || this.max.doubleValue() != v)){
//                        throw new JsonSchemeMismatchException(JsonSchemeMismatchException.RuleValueNotExpected);
//                    }
//                }
//            } else {
//                long v = ((Number) object).longValue();
//                if (this.min != null) {
//                    if (this.min.longValue() > v) {
//                        throw new JsonSchemeMismatchException(JsonSchemeMismatchException.RuleValueNotExpected);
//                    }
//                    if (!this.inclusiveMin && this.min.longValue() == v) {
//                        throw new JsonSchemeMismatchException(JsonSchemeMismatchException.RuleValueNotExpected);
//                    }
//                }
//                if (this.max != null) {
//                    if (this.max.longValue() < v) {
//                        throw new JsonSchemeMismatchException(JsonSchemeMismatchException.RuleValueNotExpected);
//                    }
//                    if(!(this.inclusiveMax || this.max.longValue() != v)){
//                        throw new JsonSchemeMismatchException(JsonSchemeMismatchException.RuleValueNotExpected);
//                    }
//                }
//            }
//        }else {
////            System.out.println("actual: " + object.getClass().getName() + " ? " + (object instanceof Number));
//            throw new JsonSchemeMismatchException(JsonSchemeMismatchException.RuleValueTypeNotExpected);
//        }
//    }

    @Override
    public void digest(Number object) throws JsonSchemeMismatchException {
        if (object == null) {
            if (!isNullable()) {
                throw new JsonSchemeMismatchException(JsonSchemeMismatchException.RuleNullableNotAllowed);
            }
            return;
        }
//        if (object instanceof Number) {
        if (object instanceof Double || object instanceof Float || object instanceof BigDecimal) {
            double v = object.doubleValue();
            if (this.min != null) {
                if (this.min.doubleValue() > v) {
                    throw new JsonSchemeMismatchException(JsonSchemeMismatchException.RuleValueNotExpected);
                }
                if (!this.inclusiveMin && this.min.doubleValue() == v) {
                    throw new JsonSchemeMismatchException(JsonSchemeMismatchException.RuleValueNotExpected);
                }
            }
            if (this.max != null) {
                if (this.max.doubleValue() < v) {
                    throw new JsonSchemeMismatchException(JsonSchemeMismatchException.RuleValueNotExpected);
                }
                if (!(this.inclusiveMax || this.max.doubleValue() != v)) {
                    throw new JsonSchemeMismatchException(JsonSchemeMismatchException.RuleValueNotExpected);
                }
            }
        } else {
            long v = object.longValue();
            if (this.min != null) {
                if (this.min.longValue() > v) {
                    throw new JsonSchemeMismatchException(JsonSchemeMismatchException.RuleValueNotExpected);
                }
                if (!this.inclusiveMin && this.min.longValue() == v) {
                    throw new JsonSchemeMismatchException(JsonSchemeMismatchException.RuleValueNotExpected);
                }
            }
            if (this.max != null) {
                if (this.max.longValue() < v) {
                    throw new JsonSchemeMismatchException(JsonSchemeMismatchException.RuleValueNotExpected);
                }
                if (!(this.inclusiveMax || this.max.longValue() != v)) {
                    throw new JsonSchemeMismatchException(JsonSchemeMismatchException.RuleValueNotExpected);
                }
            }
        }
//        }else {
//            throw new JsonSchemeMismatchException(JsonSchemeMismatchException.RuleValueTypeNotExpected);
//        }
        this.digested = object;
    }

    @Override
    public Number getDigested() {
        return this.digested;
    }
}
