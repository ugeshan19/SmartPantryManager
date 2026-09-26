package com.example.smartpantrymanager;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Unit tests for the strict-matching rule (assignment Section 2.3).
 * These run on the computer, no emulator needed.
 */
public class RecipeMatcherTest {

    // ---------- small helpers to keep the tests readable ----------

    private static PantryItem item(String name, double qty, String unit) {
        return new PantryItem(0, name, qty, unit, "");
    }

    private static RecipeIngredient need(String name, double qty, String unit) {
        return new RecipeIngredient(name, qty, unit);
    }

    private static Recipe recipe(String name, RecipeIngredient... ingredients) {
        return new Recipe(1, name, "steps", new ArrayList<>(Arrays.asList(ingredients)));
    }

    private static List<PantryItem> pantry(PantryItem... items) {
        return new ArrayList<>(Arrays.asList(items));
    }

    // ---------- the core rule from the brief ----------

    @Test
    public void recipeNeedingFiveIngredients_withOnlyFourInPantry_isNotSuggested() {
        Recipe r = recipe("Five Item Dish",
                need("egg", 2, "pieces"), need("milk", 200, "millilitres"),
                need("flour", 100, "grams"), need("butter", 1, "tablespoons"),
                need("sugar", 1, "tablespoons"));

        List<PantryItem> p = pantry(
                item("Egg", 2, "pieces"), item("Milk", 500, "millilitres"),
                item("Flour", 500, "grams"), item("Butter", 250, "grams"));
        // sugar is missing

        assertFalse(RecipeMatcher.canMake(r, p));
    }

    @Test
    public void addingTheMissingIngredient_makesTheRecipeAppear() {
        Recipe r = recipe("Five Item Dish",
                need("egg", 2, "pieces"), need("sugar", 1, "tablespoons"));

        List<PantryItem> p = pantry(item("Egg", 2, "pieces"));
        assertFalse(RecipeMatcher.canMake(r, p));

        p.add(item("Sugar", 1, "kilograms"));
        assertTrue(RecipeMatcher.canMake(r, p));
    }

    @Test
    public void quantityTooSmall_isNotSuggested() {
        Recipe r = recipe("Omelette", need("egg", 3, "pieces"));
        assertFalse(RecipeMatcher.canMake(r, pantry(item("Egg", 2, "pieces"))));
        assertTrue(RecipeMatcher.canMake(r, pantry(item("Egg", 3, "pieces"))));
    }

    @Test
    public void emptyPantry_matchesNothing() {
        Recipe r = recipe("Toast", need("bread", 2, "pieces"));
        List<Recipe> result = RecipeMatcher.findStrictMatches(
                Arrays.asList(r), new ArrayList<PantryItem>());
        assertEquals(0, result.size());
    }

    @Test
    public void recipeWithNoIngredients_isNeverSuggested() {
        assertFalse(RecipeMatcher.canMake(recipe("Empty"), pantry(item("Egg", 1, "pieces"))));
    }

    @Test
    public void findStrictMatches_returnsOnlyFullMatches() {
        Recipe full = recipe("Full", need("egg", 1, "pieces"));
        Recipe partial = recipe("Partial", need("egg", 1, "pieces"), need("cheese", 50, "grams"));

        List<Recipe> result = RecipeMatcher.findStrictMatches(
                Arrays.asList(full, partial), pantry(item("Egg", 2, "pieces")));

        assertEquals(1, result.size());
        assertEquals("Full", result.get(0).getName());
    }

    // ---------- robustness to real-world messiness ----------

    @Test
    public void singularAndPluralNamesMatch() {
        Recipe r = recipe("Sauce", need("tomato", 2, "pieces"));
        assertTrue(RecipeMatcher.canMake(r, pantry(item("Tomatoes", 4, "pieces"))));
    }

    @Test
    public void caseSpacesAndFillerWordsAreIgnored() {
        Recipe r = recipe("Sauce", need("tomato", 2, "pieces"));
        assertTrue(RecipeMatcher.canMake(r, pantry(item("  FRESH  Tomatoes ", 2, "pieces"))));
    }

    @Test
    public void normalizeName_handlesCommonPlurals() {
        assertEquals("tomato", RecipeMatcher.normalizeName("Tomatoes"));
        assertEquals("potato", RecipeMatcher.normalizeName("potatoes"));
        assertEquals("egg", RecipeMatcher.normalizeName("EGGS"));
        assertEquals("berry", RecipeMatcher.normalizeName("berries"));
        assertEquals("cheese", RecipeMatcher.normalizeName("cheese"));
    }

    @Test
    public void synonymsMatch() {
        Recipe r = recipe("Pap", need("maize meal", 250, "grams"));
        assertTrue(RecipeMatcher.canMake(r, pantry(item("Mealie meal", 1, "kilograms"))));
    }

    @Test
    public void kilogramsSatisfyGramsRequirement() {
        Recipe r = recipe("Cake", need("flour", 200, "grams"));
        assertTrue(RecipeMatcher.canMake(r, pantry(item("Flour", 1, "kilograms"))));
    }

    @Test
    public void litresSatisfyMillilitresRequirement() {
        Recipe r = recipe("Pancakes", need("milk", 300, "millilitres"));
        assertTrue(RecipeMatcher.canMake(r, pantry(item("Milk", 1, "litres"))));
        assertFalse(RecipeMatcher.canMake(r, pantry(item("Milk", 0.2, "litres"))));
    }

    @Test
    public void weightInPantrySatisfiesSpoonRequirement() {
        // Recipe wants 0.5 teaspoons of salt, pantry has a 500 g bag.
        Recipe r = recipe("Eggs", need("salt", 0.5, "teaspoons"));
        assertTrue(RecipeMatcher.canMake(r, pantry(item("Salt", 500, "grams"))));
    }

    @Test
    public void weightInPantrySatisfiesPieceRequirement() {
        Recipe r = recipe("Sauce", need("tomato", 4, "pieces"));
        assertTrue(RecipeMatcher.canMake(r, pantry(item("Tomato", 1, "kilograms"))));
        assertFalse(RecipeMatcher.canMake(r, pantry(item("Tomato", 100, "grams"))));
    }

    @Test
    public void quantitiesOfDuplicatePantryRowsAreAddedTogether() {
        Recipe r = recipe("Omelette", need("egg", 4, "pieces"));
        assertTrue(RecipeMatcher.canMake(r,
                pantry(item("Egg", 2, "pieces"), item("Eggs", 2, "pieces"))));
    }

    @Test
    public void unitsThatCannotBeCompared_areTreatedAsMissing() {
        // No density is known for "chicken", so grams cannot become millilitres.
        Recipe r = recipe("Stock", need("chicken", 100, "millilitres"));
        assertFalse(RecipeMatcher.canMake(r, pantry(item("Chicken", 500, "grams"))));
    }
}