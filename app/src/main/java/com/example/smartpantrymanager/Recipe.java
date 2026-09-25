package com.example.smartpantrymanager;

import java.util.List;

/**
 * Model class for one recipe: its name, its preparation steps and
 * the full list of ingredients it needs.
 */
public class Recipe {

    private final int id;
    private final String name;
    private final String steps;
    private final List<RecipeIngredient> ingredients;

    public Recipe(int id, String name, String steps,
                  List<RecipeIngredient> ingredients) {
        this.id = id;
        this.name = name;
        this.steps = steps;
        this.ingredients = ingredients;
    }

    public int getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getSteps() {
        return steps;
    }

    public List<RecipeIngredient> getIngredients() {
        return ingredients;
    }
}