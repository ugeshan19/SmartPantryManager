package com.example.smartpantrymanager;

/**
 * One ingredient line of a recipe, e.g. "tomato, 4, pieces".
 * Mirrors one row of the recipe_ingredients table.
 */
public class RecipeIngredient {

    private final String name;
    private final double quantity;
    private final String unit;

    public RecipeIngredient(String name, double quantity, String unit) {
        this.name = name;
        this.quantity = quantity;
        this.unit = unit;
    }

    public String getName() {
        return name;
    }

    public double getQuantity() {
        return quantity;
    }

    public String getUnit() {
        return unit;
    }

    /** Display text such as "200 grams" or "0.5 teaspoons". */
    public String getQuantityText() {
        return PantryItem.formatQuantity(quantity) + " " + unit;
    }
}