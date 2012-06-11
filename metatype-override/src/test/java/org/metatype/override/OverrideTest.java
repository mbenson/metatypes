package org.metatype.override;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.math.BigDecimal;

import javax.annotation.Metatype;
import javax.validation.constraints.Digits;
import javax.validation.constraints.Pattern;

import org.junit.Assert;
import org.junit.Test;
import org.metatype.MetaAnnotatedClass;
import org.metatype.MetaAnnotatedField;

public class OverrideTest {
    @Metatype(extractUsing = Currency.Extractor.class)
    @Pattern(regexp = "\\d*")
    @Digits(integer = 0, fraction = 0)
    @Retention(RetentionPolicy.RUNTIME)
    public @interface Currency {

        public class Extractor extends OverridingMetatypeExtractor<Currency> {
            public Extractor() {
                super(new AnnotationOverrider<Digits, Currency>() {

                    @Override
                    protected void override(Digits result, Digits overridden,
                            Currency overriding) {
                        when(result.integer()).thenReturn(overriding.integer());
                        when(result.fraction()).thenReturn(
                                overriding.fraction());
                    }
                });
            }
        }

        int integer() default 7;

        int fraction() default 2;
    }

    @SuppressWarnings("unused")
    public static class Item {
        private String description;
        @Currency
        private BigDecimal price;
    }

    @SuppressWarnings("unused")
    public static class Item2 {
        private String description;
        @Currency(integer = 9, fraction = 0)
        private BigDecimal price;
    }

    @Test
    public void testItem() throws Exception {
        MetaAnnotatedClass<Item> metaItem = new MetaAnnotatedClass<Item>(
                Item.class);
        MetaAnnotatedField priceField = metaItem.getDeclaredField("price");
        Digits digits = priceField.getAnnotation(Digits.class);
        Assert.assertNotNull(digits);
        Assert.assertEquals(7, digits.integer());
        Assert.assertEquals(2, digits.fraction());
    }

    @Test
    public void testItem2() throws Exception {
        MetaAnnotatedClass<Item2> metaItem2 = new MetaAnnotatedClass<Item2>(
                Item2.class);
        MetaAnnotatedField priceField = metaItem2.getDeclaredField("price");
        Digits digits = priceField.getAnnotation(Digits.class);
        Assert.assertNotNull(digits);
        Assert.assertEquals(9, digits.integer());
        Assert.assertEquals(0, digits.fraction());
    }
}
