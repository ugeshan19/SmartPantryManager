package com.example.smartpantrymanager;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.DatabaseUtils;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class DatabaseHelper extends SQLiteOpenHelper {

    private static final String TAG = "SmartPantry";

    private static final String DATABASE_NAME = "SmartPantry.db";

    // Version 2: recipes are now seeded. Raising the version makes
    // onUpgrade() run once on phones that already have version 1.
    private static final int DATABASE_VERSION = 2;

    // Pantry table
    public static final String TABLE_PANTRY = "pantry_items";
    public static final String COL_PANTRY_ID = "id";
    public static final String COL_PANTRY_NAME = "name";
    public static final String COL_PANTRY_QUANTITY = "quantity";
    public static final String COL_PANTRY_UNIT = "unit";
    public static final String COL_PANTRY_EXPIRY = "expiry_date";

    // Recipe table
    public static final String TABLE_RECIPES = "recipes";
    public static final String COL_RECIPE_ID = "id";
    public static final String COL_RECIPE_NAME = "name";
    public static final String COL_RECIPE_STEPS = "steps";

    // Recipe ingredients table
    public static final String TABLE_RECIPE_INGREDIENTS = "recipe_ingredients";
    public static final String COL_RI_ID = "id";
    public static final String COL_RI_RECIPE_ID = "recipe_id";
    public static final String COL_RI_NAME = "ingredient_name";
    public static final String COL_RI_QUANTITY = "required_quantity";
    public static final String COL_RI_UNIT = "unit";

    public DatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {

        // Pantry table
        db.execSQL("CREATE TABLE " + TABLE_PANTRY + " (" +
                COL_PANTRY_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                COL_PANTRY_NAME + " TEXT NOT NULL, " +
                COL_PANTRY_QUANTITY + " REAL NOT NULL, " +
                COL_PANTRY_UNIT + " TEXT NOT NULL, " +
                COL_PANTRY_EXPIRY + " TEXT)");

        // Recipe tables, then fill them with the built-in recipes.
        createRecipeTables(db);
        seedRecipes(db);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // Keep the user's pantry. Only rebuild the recipe tables
        // so the seeded recipes are (re)loaded.
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_RECIPE_INGREDIENTS);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_RECIPES);
        createRecipeTables(db);
        seedRecipes(db);
    }

    private void createRecipeTables(SQLiteDatabase db) {

        // Recipes table
        db.execSQL("CREATE TABLE " + TABLE_RECIPES + " (" +
                COL_RECIPE_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                COL_RECIPE_NAME + " TEXT NOT NULL, " +
                COL_RECIPE_STEPS + " TEXT NOT NULL)");

        // Recipe ingredients table (many ingredients belong to one recipe)
        db.execSQL("CREATE TABLE " + TABLE_RECIPE_INGREDIENTS + " (" +
                COL_RI_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                COL_RI_RECIPE_ID + " INTEGER NOT NULL, " +
                COL_RI_NAME + " TEXT NOT NULL, " +
                COL_RI_QUANTITY + " REAL NOT NULL, " +
                COL_RI_UNIT + " TEXT NOT NULL, " +
                "FOREIGN KEY (" + COL_RI_RECIPE_ID + ") REFERENCES " +
                TABLE_RECIPES + "(" + COL_RECIPE_ID + "))");
    }

    // -------------------------
    // RECIPE SEED DATA
    // -------------------------

    /**
     * Inserts the built-in recipe collection. Runs only when the tables
     * are first created (or rebuilt after an upgrade).
     *
     * Ingredient format: "name|quantity|unit". Names are lowercase and
     * singular so they line up with the matching logic. Water is assumed
     * to always be available and is not tracked.
     */
    private void seedRecipes(SQLiteDatabase db) {

        insertRecipe(db, "Scrambled Eggs",
                "1. Crack the eggs into a bowl, add the milk and salt, and whisk.\n" +
                        "2. Melt the butter in a pan over low heat.\n" +
                        "3. Pour in the eggs and stir gently until just set.\n" +
                        "4. Serve immediately.",
                "egg|3|pieces", "butter|1|tablespoons",
                "milk|30|millilitres", "salt|0.5|teaspoons");

        insertRecipe(db, "Cheese Omelette",
                "1. Whisk the eggs with the salt.\n" +
                        "2. Melt the butter in a pan and pour in the eggs.\n" +
                        "3. When the base is set, sprinkle over the grated cheese.\n" +
                        "4. Fold the omelette in half and cook for one more minute.",
                "egg|3|pieces", "cheese|50|grams",
                "butter|1|tablespoons", "salt|0.5|teaspoons");

        insertRecipe(db, "Pancakes",
                "1. Whisk the flour, sugar and eggs together.\n" +
                        "2. Slowly add the milk until the batter is smooth.\n" +
                        "3. Melt a little butter in a pan over medium heat.\n" +
                        "4. Pour in a ladle of batter and cook until golden on both sides.",
                "flour|200|grams", "milk|300|millilitres",
                "egg|2|pieces", "sugar|1|tablespoons", "butter|1|tablespoons");

        insertRecipe(db, "French Toast",
                "1. Whisk the eggs, milk and sugar in a shallow dish.\n" +
                        "2. Dip each slice of bread so both sides are coated.\n" +
                        "3. Fry the slices in melted butter until golden on both sides.",
                "bread|4|pieces", "egg|2|pieces", "milk|100|millilitres",
                "butter|1|tablespoons", "sugar|1|tablespoons");

        insertRecipe(db, "Cheese Toasty",
                "1. Butter one side of each slice of bread.\n" +
                        "2. Place the cheese between the slices, butter side out.\n" +
                        "3. Toast in a pan or toaster until golden and the cheese melts.",
                "bread|4|pieces", "cheese|100|grams", "butter|1|tablespoons");

        insertRecipe(db, "Tomato Pasta",
                "1. Boil the pasta in salted water until soft, then drain.\n" +
                        "2. Fry the chopped onion and garlic in the oil until soft.\n" +
                        "3. Add the chopped tomatoes and simmer for 10 minutes.\n" +
                        "4. Mix the sauce through the pasta and serve.",
                "pasta|200|grams", "tomato|4|pieces", "onion|1|pieces",
                "garlic|2|pieces", "oil|2|tablespoons", "salt|1|teaspoons");

        insertRecipe(db, "Egg Fried Rice",
                "1. Cook the rice, then let it cool.\n" +
                        "2. Fry the chopped onion in the oil until soft.\n" +
                        "3. Push the onion aside, scramble the eggs in the pan.\n" +
                        "4. Add the rice and salt and stir-fry for 3 minutes.",
                "rice|200|grams", "egg|2|pieces", "onion|1|pieces",
                "oil|2|tablespoons", "salt|0.5|teaspoons");

        insertRecipe(db, "Mashed Potatoes",
                "1. Peel and chop the potatoes, then boil until soft.\n" +
                        "2. Drain well and return to the pot.\n" +
                        "3. Add the butter, milk and salt and mash until smooth.",
                "potato|500|grams", "butter|2|tablespoons",
                "milk|100|millilitres", "salt|0.5|teaspoons");

        insertRecipe(db, "Garlic Roast Potatoes",
                "1. Heat the oven to 200 degrees Celsius.\n" +
                        "2. Chop the potatoes and toss with the oil, salt and crushed garlic.\n" +
                        "3. Spread on a tray and roast for 40 minutes, turning once.",
                "potato|600|grams", "oil|3|tablespoons",
                "salt|1|teaspoons", "garlic|2|pieces");

        insertRecipe(db, "Banana Oat Porridge",
                "1. Heat the milk in a pot over medium heat.\n" +
                        "2. Stir in the oats and cook for 5 minutes until thick.\n" +
                        "3. Slice the banana on top and drizzle with the honey.",
                "oats|80|grams", "milk|300|millilitres",
                "banana|1|pieces", "honey|1|tablespoons");

        insertRecipe(db, "Banana Pancakes",
                "1. Mash the bananas in a bowl.\n" +
                        "2. Whisk in the eggs, then stir in the flour.\n" +
                        "3. Spoon small rounds into a hot pan and cook until golden on both sides.",
                "banana|2|pieces", "egg|2|pieces", "flour|100|grams");

        insertRecipe(db, "Peanut Butter Banana Toast",
                "1. Toast the bread.\n" +
                        "2. Spread the peanut butter over each slice.\n" +
                        "3. Slice the banana and arrange on top.",
                "bread|2|pieces", "peanut butter|2|tablespoons", "banana|1|pieces");

        insertRecipe(db, "Chicken and Rice",
                "1. Cut the chicken into pieces and season with salt.\n" +
                        "2. Fry the chopped onion in the oil, then brown the chicken.\n" +
                        "3. Add the rice and enough water to cover it.\n" +
                        "4. Cover and simmer for 20 minutes until the rice is cooked.",
                "chicken|400|grams", "rice|200|grams", "onion|1|pieces",
                "oil|2|tablespoons", "salt|1|teaspoons");

        insertRecipe(db, "Beef Bolognese",
                "1. Fry the onion and garlic in the oil until soft.\n" +
                        "2. Add the mince and brown it well.\n" +
                        "3. Add the chopped tomatoes and simmer for 20 minutes.\n" +
                        "4. Boil the pasta, drain, and serve with the sauce.",
                "beef mince|300|grams", "pasta|200|grams", "tomato|3|pieces",
                "onion|1|pieces", "garlic|2|pieces", "oil|1|tablespoons");

        insertRecipe(db, "Beans on Toast",
                "1. Heat the baked beans in a small pot.\n" +
                        "2. Toast and butter the bread.\n" +
                        "3. Pour the beans over the toast and serve.",
                "baked beans|400|grams", "bread|2|pieces", "butter|1|tablespoons");

        insertRecipe(db, "Vegetable Soup",
                "1. Chop the carrots, potatoes and onion.\n" +
                        "2. Fry the onion in the oil for 3 minutes.\n" +
                        "3. Add the other vegetables, salt and enough water to cover.\n" +
                        "4. Simmer for 25 minutes until soft.",
                "carrot|2|pieces", "potato|2|pieces", "onion|1|pieces",
                "salt|1|teaspoons", "oil|1|tablespoons");

        insertRecipe(db, "Pap and Tomato Relish",
                "1. Boil water with a pinch of salt and stir in the maize meal.\n" +
                        "2. Cover and cook on low heat for 20 minutes, stirring now and then.\n" +
                        "3. For the relish, fry the onion in the oil, add the chopped tomatoes and salt, and simmer for 10 minutes.\n" +
                        "4. Serve the pap with the relish.",
                "maize meal|250|grams", "tomato|3|pieces", "onion|1|pieces",
                "oil|2|tablespoons", "salt|1|teaspoons");

        insertRecipe(db, "Honey Lemon Tea",
                "1. Boil water and pour it into a mug.\n" +
                        "2. Squeeze in the juice of the lemon.\n" +
                        "3. Stir in the honey and drink while warm.",
                "lemon|1|pieces", "honey|2|tablespoons");

        insertRecipe(db, "Garlic Bread",
                "1. Heat the oven to 180 degrees Celsius.\n" +
                        "2. Mix the soft butter with the crushed garlic.\n" +
                        "3. Spread it on the bread and bake for 10 minutes until crisp.",
                "bread|4|pieces", "butter|3|tablespoons", "garlic|2|pieces");

        insertRecipe(db, "Spanish Omelette",
                "1. Slice the potatoes and onion thinly.\n" +
                        "2. Fry them in the oil for 15 minutes until soft.\n" +
                        "3. Mix with the beaten eggs and salt.\n" +
                        "4. Pour back into the pan and cook until set, flipping once.",
                "potato|300|grams", "egg|4|pieces", "onion|1|pieces",
                "oil|3|tablespoons", "salt|1|teaspoons");

        insertRecipe(db, "Rice Pudding",
                "1. Put the rice, milk and sugar in a pot.\n" +
                        "2. Simmer on low heat for 30 minutes, stirring often.\n" +
                        "3. Serve warm once thick and creamy.",
                "rice|100|grams", "milk|500|millilitres", "sugar|3|tablespoons");

        Log.d(TAG, "Recipes seeded: "
                + DatabaseUtils.queryNumEntries(db, TABLE_RECIPES));
    }

    /**
     * Inserts one recipe and all of its ingredient rows.
     *
     * @param ingredients lines in the format "name|quantity|unit"
     */
    private void insertRecipe(SQLiteDatabase db, String name, String steps,
                              String... ingredients) {

        ContentValues recipeValues = new ContentValues();
        recipeValues.put(COL_RECIPE_NAME, name);
        recipeValues.put(COL_RECIPE_STEPS, steps);
        long recipeId = db.insert(TABLE_RECIPES, null, recipeValues);

        for (String line : ingredients) {
            String[] parts = line.split("\\|");

            ContentValues ingredientValues = new ContentValues();
            ingredientValues.put(COL_RI_RECIPE_ID, recipeId);
            ingredientValues.put(COL_RI_NAME, parts[0].trim());
            ingredientValues.put(COL_RI_QUANTITY, Double.parseDouble(parts[1].trim()));
            ingredientValues.put(COL_RI_UNIT, parts[2].trim());
            db.insert(TABLE_RECIPE_INGREDIENTS, null, ingredientValues);
        }
    }

    // -------------------------
    // RECIPE READ METHODS
    // -------------------------

    /** Number of recipes stored in the database. */
    public int getRecipeCount() {
        SQLiteDatabase db = this.getReadableDatabase();
        return (int) DatabaseUtils.queryNumEntries(db, TABLE_RECIPES);
    }

    /**
     * Loads every recipe together with its ingredient list.
     * The strict-matching logic will run over this list.
     */
    public List<Recipe> getAllRecipes() {

        SQLiteDatabase db = this.getReadableDatabase();

        // Step 1: read all ingredient rows and group them by recipe id.
        Map<Integer, List<RecipeIngredient>> ingredientsByRecipe = new HashMap<>();

        Cursor ingredientCursor = db.query(
                TABLE_RECIPE_INGREDIENTS, null, null, null, null, null,
                COL_RI_ID + " ASC");

        while (ingredientCursor.moveToNext()) {
            int recipeId = ingredientCursor.getInt(
                    ingredientCursor.getColumnIndexOrThrow(COL_RI_RECIPE_ID));
            String name = ingredientCursor.getString(
                    ingredientCursor.getColumnIndexOrThrow(COL_RI_NAME));
            double quantity = ingredientCursor.getDouble(
                    ingredientCursor.getColumnIndexOrThrow(COL_RI_QUANTITY));
            String unit = ingredientCursor.getString(
                    ingredientCursor.getColumnIndexOrThrow(COL_RI_UNIT));

            List<RecipeIngredient> list = ingredientsByRecipe.get(recipeId);
            if (list == null) {
                list = new ArrayList<>();
                ingredientsByRecipe.put(recipeId, list);
            }
            list.add(new RecipeIngredient(name, quantity, unit));
        }
        ingredientCursor.close();

        // Step 2: read the recipes and attach each one's ingredient list.
        List<Recipe> recipes = new ArrayList<>();

        Cursor recipeCursor = db.query(
                TABLE_RECIPES, null, null, null, null, null,
                COL_RECIPE_NAME + " ASC");

        while (recipeCursor.moveToNext()) {
            int id = recipeCursor.getInt(
                    recipeCursor.getColumnIndexOrThrow(COL_RECIPE_ID));
            String name = recipeCursor.getString(
                    recipeCursor.getColumnIndexOrThrow(COL_RECIPE_NAME));
            String steps = recipeCursor.getString(
                    recipeCursor.getColumnIndexOrThrow(COL_RECIPE_STEPS));

            List<RecipeIngredient> list = ingredientsByRecipe.get(id);
            if (list == null) {
                list = new ArrayList<>();
            }
            recipes.add(new Recipe(id, name, steps, list));
        }
        recipeCursor.close();

        return recipes;
    }

    // -------------------------
    // PANTRY CRUD
    // -------------------------

    public long addPantryItem(String name, double quantity,
                              String unit, String expiryDate) {

        SQLiteDatabase db = this.getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put(COL_PANTRY_NAME, name);
        values.put(COL_PANTRY_QUANTITY, quantity);
        values.put(COL_PANTRY_UNIT, unit);
        values.put(COL_PANTRY_EXPIRY, expiryDate);

        return db.insert(TABLE_PANTRY, null, values);
    }

    public Cursor getAllPantryItems() {

        SQLiteDatabase db = this.getReadableDatabase();

        return db.query(
                TABLE_PANTRY,
                null,
                null,
                null,
                null,
                null,
                COL_PANTRY_NAME + " ASC"
        );
    }

    public int updatePantryItem(int id, String name,
                                double quantity,
                                String unit,
                                String expiryDate) {

        SQLiteDatabase db = this.getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put(COL_PANTRY_NAME, name);
        values.put(COL_PANTRY_QUANTITY, quantity);
        values.put(COL_PANTRY_UNIT, unit);
        values.put(COL_PANTRY_EXPIRY, expiryDate);

        return db.update(
                TABLE_PANTRY,
                values,
                COL_PANTRY_ID + "=?",
                new String[]{String.valueOf(id)}
        );
    }

    public int deletePantryItem(int id) {

        SQLiteDatabase db = this.getWritableDatabase();

        return db.delete(
                TABLE_PANTRY,
                COL_PANTRY_ID + "=?",
                new String[]{String.valueOf(id)}
        );
    }
}