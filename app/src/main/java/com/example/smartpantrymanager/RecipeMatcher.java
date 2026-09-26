package com.example.smartpantrymanager;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

/**
 * The strict-matching engine (assignment Section 2.3).
 *
 * A recipe is only "suggested" when EVERY ingredient it needs is in the
 * pantry in at least the required quantity. One missing or too-small
 * ingredient means the recipe is excluded.
 *
 * To cope with real-world messiness the matcher:
 *  1. normalises names (case, spaces, plurals, filler words, synonyms)
 *  2. converts units (kg to g, litres to ml, cups/tbsp/tsp to ml)
 *  3. converts between weight, volume and piece counts for common foods
 *  4. adds up several pantry rows that are the same ingredient
 *
 * This class is plain Java (no Android classes) so it can be unit tested.
 */
public class RecipeMatcher {

    /** The three families of measurement. */
    private enum Kind { COUNT, MASS, VOLUME }

    // Tiny tolerance so floating point rounding never rejects an exact match.
    private static final double EPSILON = 1e-6;

    // Words that describe an ingredient without changing what it is.
    private static final Set<String> FILLER_WORDS = new HashSet<>(Arrays.asList(
            "fresh", "large", "small", "medium", "big", "ripe", "chopped",
            "grated", "sliced", "diced", "whole", "raw", "frozen", "tinned",
            "canned"));

    // Different names for the same ingredient (keys are already normalised).
    private static final Map<String, String> SYNONYMS = new HashMap<>();

    // Approximate grams per millilitre, used to compare weight with volume.
    private static final Map<String, Double> DENSITY = new HashMap<>();

    // Approximate grams for ONE piece, used to compare pieces with weight.
    private static final Map<String, Double> PIECE_WEIGHT = new HashMap<>();

    static {
        SYNONYMS.put("mealie meal", "maize meal");
        SYNONYMS.put("mince", "beef mince");
        SYNONYMS.put("minced beef", "beef mince");
        SYNONYMS.put("ground beef", "beef mince");
        SYNONYMS.put("cooking oil", "oil");
        SYNONYMS.put("sunflower oil", "oil");
        SYNONYMS.put("vegetable oil", "oil");
        SYNONYMS.put("olive oil", "oil");
        SYNONYMS.put("plain flour", "flour");
        SYNONYMS.put("cake flour", "flour");
        SYNONYMS.put("bread flour", "flour");
        SYNONYMS.put("white rice", "rice");
        SYNONYMS.put("brown rice", "rice");
        SYNONYMS.put("white sugar", "sugar");
        SYNONYMS.put("brown sugar", "sugar");
        SYNONYMS.put("caster sugar", "sugar");
        SYNONYMS.put("table salt", "salt");
        SYNONYMS.put("sea salt", "salt");
        SYNONYMS.put("cheddar cheese", "cheese");
        SYNONYMS.put("cheddar", "cheese");
        SYNONYMS.put("chicken breast", "chicken");
        SYNONYMS.put("chicken thigh", "chicken");
        SYNONYMS.put("spaghetti", "pasta");
        SYNONYMS.put("penne", "pasta");
        SYNONYMS.put("macaroni", "pasta");
        SYNONYMS.put("garlic clove", "garlic");
        SYNONYMS.put("white bread", "bread");
        SYNONYMS.put("brown bread", "bread");
        SYNONYMS.put("full cream milk", "milk");
        SYNONYMS.put("rolled oat", "oat");

        DENSITY.put("salt", 1.2);
        DENSITY.put("sugar", 0.85);
        DENSITY.put("butter", 0.95);
        DENSITY.put("honey", 1.4);
        DENSITY.put("peanut butter", 1.1);
        DENSITY.put("flour", 0.53);
        DENSITY.put("oil", 0.92);
        DENSITY.put("milk", 1.03);
        DENSITY.put("rice", 0.8);
        DENSITY.put("oat", 0.35);
        DENSITY.put("maize meal", 0.65);
        DENSITY.put("cheese", 0.45);
        DENSITY.put("baked bean", 1.0);

        PIECE_WEIGHT.put("egg", 50.0);
        PIECE_WEIGHT.put("tomato", 120.0);
        PIECE_WEIGHT.put("onion", 150.0);
        PIECE_WEIGHT.put("potato", 170.0);
        PIECE_WEIGHT.put("garlic", 5.0);
        PIECE_WEIGHT.put("banana", 120.0);
        PIECE_WEIGHT.put("carrot", 80.0);
        PIECE_WEIGHT.put("lemon", 100.0);
        PIECE_WEIGHT.put("bread", 30.0);
    }

    // Utility class: not meant to be instantiated.
    private RecipeMatcher() { }

    // ------------------------------------------------------------------
    // PUBLIC API
    // ------------------------------------------------------------------

    /**
     * Returns only the recipes the user can make right now.
     * Partial matches are never included.
     */
    public static List<Recipe> findStrictMatches(List<Recipe> recipes,
                                                 List<PantryItem> pantry) {
        List<Recipe> matches = new ArrayList<>();
        for (Recipe recipe : recipes) {
            if (canMake(recipe, pantry)) {
                matches.add(recipe);
            }
        }
        return matches;
    }

    /**
     * THE STRICT RULE: true only if every ingredient is available
     * in at least the required quantity.
     */
    public static boolean canMake(Recipe recipe, List<PantryItem> pantry) {

        // A recipe with no ingredients is never suggested.
        if (recipe.getIngredients().isEmpty()) {
            return false;
        }

        for (RecipeIngredient needed : recipe.getIngredients()) {
            if (!hasIngredient(needed, pantry)) {
                // One missing or too-small ingredient rules the recipe out.
                return false;
            }
        }
        return true;
    }

    /**
     * True if the pantry holds at least the quantity this recipe
     * ingredient requires.
     */
    public static boolean hasIngredient(RecipeIngredient needed,
                                        List<PantryItem> pantry) {

        Kind neededKind = kindOf(needed.getUnit());
        if (neededKind == null) {
            // Unknown unit: we cannot verify the quantity, so be strict.
            return false;
        }

        double required = toBase(needed.getQuantity(), needed.getUnit());
        double available = availableAmount(needed, pantry, neededKind);

        return available + EPSILON >= required;
    }

    /**
     * Turns a typed ingredient name into a comparable form:
     * "  Fresh Tomatoes " becomes "tomato".
     */
    public static String normalizeName(String raw) {
        if (raw == null) {
            return "";
        }

        // Lowercase and replace punctuation/digits with spaces.
        String cleaned = raw.toLowerCase(Locale.ROOT).replaceAll("[^a-z ]", " ");

        StringBuilder result = new StringBuilder();
        for (String word : cleaned.trim().split("\\s+")) {
            if (word.isEmpty() || FILLER_WORDS.contains(word)) {
                continue;
            }
            if (result.length() > 0) {
                result.append(' ');
            }
            result.append(singularize(word));
        }

        String name = result.toString();
        String canonical = SYNONYMS.get(name);
        return canonical != null ? canonical : name;
    }

    // ------------------------------------------------------------------
    // NAME HELPERS
    // ------------------------------------------------------------------

    /** Simple plural to singular rules (tomatoes, berries, eggs...). */
    private static String singularize(String word) {
        int length = word.length();

        if (length <= 3) {
            return word;
        }
        if (word.endsWith("ies")) {
            return word.substring(0, length - 3) + "y";      // berries -> berry
        }
        if (word.endsWith("oes")) {
            return word.substring(0, length - 2);            // tomatoes -> tomato
        }
        if (word.endsWith("ches") || word.endsWith("shes")
                || word.endsWith("sses") || word.endsWith("xes")) {
            return word.substring(0, length - 2);            // peaches -> peach
        }
        if (word.endsWith("ss") || word.endsWith("us") || word.endsWith("is")) {
            return word;                                     // hummus stays hummus
        }
        if (word.endsWith("s")) {
            return word.substring(0, length - 1);            // eggs -> egg
        }
        return word;
    }

    // ------------------------------------------------------------------
    // QUANTITY AND UNIT HELPERS
    // ------------------------------------------------------------------

    /**
     * Total amount of an ingredient in the pantry, expressed in the same
     * kind of measurement as the recipe needs. Several pantry rows for the
     * same ingredient are added together.
     */
    private static double availableAmount(RecipeIngredient needed,
                                          List<PantryItem> pantry,
                                          Kind neededKind) {

        String neededName = normalizeName(needed.getName());
        double total = 0;

        for (PantryItem item : pantry) {

            if (!normalizeName(item.getName()).equals(neededName)) {
                continue;
            }

            Kind haveKind = kindOf(item.getUnit());
            if (haveKind == null) {
                continue;
            }

            double haveBase = toBase(item.getQuantity(), item.getUnit());
            double converted = convertKind(haveBase, haveKind, neededKind, neededName);

            // NaN means "no way to compare these two units": ignore the row.
            if (!Double.isNaN(converted)) {
                total += converted;
            }
        }
        return total;
    }

    /** Which family of measurement a unit belongs to (null if unknown). */
    private static Kind kindOf(String unit) {
        String u = unit == null ? "" : unit.trim().toLowerCase(Locale.ROOT);
        switch (u) {
            case "pieces": case "piece": case "pcs": case "pc":
                return Kind.COUNT;
            case "grams": case "gram": case "g":
            case "kilograms": case "kilogram": case "kg":
                return Kind.MASS;
            case "millilitres": case "millilitre": case "ml":
            case "litres": case "litre": case "l":
            case "cups": case "cup":
            case "tablespoons": case "tablespoon": case "tbsp":
            case "teaspoons": case "teaspoon": case "tsp":
                return Kind.VOLUME;
            default:
                return null;
        }
    }

    /**
     * Converts a quantity into its base unit:
     * pieces for COUNT, grams for MASS, millilitres for VOLUME.
     */
    private static double toBase(double quantity, String unit) {
        String u = unit.trim().toLowerCase(Locale.ROOT);
        switch (u) {
            case "kilograms": case "kilogram": case "kg":
                return quantity * 1000;
            case "litres": case "litre": case "l":
                return quantity * 1000;
            case "cups": case "cup":
                return quantity * 240;
            case "tablespoons": case "tablespoon": case "tbsp":
                return quantity * 15;
            case "teaspoons": case "teaspoon": case "tsp":
                return quantity * 5;
            default:
                // pieces, grams and millilitres are already base units.
                return quantity;
        }
    }

    /**
     * Converts an amount (in base units) from one kind of measurement to
     * another using the density / piece-weight tables.
     * Returns NaN when there is not enough information to convert.
     */
    private static double convertKind(double amount, Kind from, Kind to,
                                      String ingredient) {
        if (from == to) {
            return amount;
        }

        // Step 1: express the amount in grams.
        double grams;
        if (from == Kind.MASS) {
            grams = amount;
        } else if (from == Kind.VOLUME) {
            Double density = DENSITY.get(ingredient);
            if (density == null) {
                return Double.NaN;
            }
            grams = amount * density;
        } else {
            Double pieceWeight = PIECE_WEIGHT.get(ingredient);
            if (pieceWeight == null) {
                return Double.NaN;
            }
            grams = amount * pieceWeight;
        }

        // Step 2: turn the grams into the kind we need.
        if (to == Kind.MASS) {
            return grams;
        } else if (to == Kind.VOLUME) {
            Double density = DENSITY.get(ingredient);
            if (density == null) {
                return Double.NaN;
            }
            return grams / density;
        } else {
            Double pieceWeight = PIECE_WEIGHT.get(ingredient);
            if (pieceWeight == null) {
                return Double.NaN;
            }
            return grams / pieceWeight;
        }
    }
}