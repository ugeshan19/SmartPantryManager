# Smart Pantry Manager

A Java Android app that helps reduce food waste. The user records the
ingredients they already have at home (their "pantry"), and the app suggests
only the recipes they can cook **right now**, with no shopping trip required.

Built for **Mobile App Development 700 - Practical Assignment** (Richfield).

**Author:** Ugeshan Naicker
Student No: 402313594
Requires Android Studio with the Android SDK installed
**Language:** Java (no Kotlin) | **IDE:** Android Studio | **Min SDK:** 24 (Android 7.0)

---

## Features

- **Pantry management (full CRUD):** add, view, edit and delete ingredients
  (name, quantity, unit, optional expiry date), with a delete confirmation dialog.
- **Pantry list:** a RecyclerView with a custom adapter, bound to the database.
- **21 pre-loaded recipes:** seeded into the database on first run.
- **Suggested Recipes:** runs the strict-matching rule and lists only the
  recipes the user can make with what they have.
- **Recipe Detail:** full ingredient list and step-by-step method.
- **Settings:** toggle expiring-soon alerts and choose a default unit
  (saved with SharedPreferences).
- **Expiry alerts:** expired items show in red and items expiring within
  3 days in orange, with a banner on the pantry list.
- **Empty state:** a friendly message when no recipes match the pantry.
- **Input validation:** required fields, numeric quantity greater than zero,
  and a real calendar date for the expiry (YYYY-MM-DD).

## The strict-matching rule

A recipe is suggested **only if every ingredient it needs is in the pantry, in
at least the required quantity**. If a recipe needs 5 ingredients and the
pantry has 4, it is not shown.

The logic is in `RecipeMatcher.java`. To cope with real-world messiness it:

1. normalises names (upper/lower case, extra spaces, plural forms such as
   "tomato" and "tomatoes", filler words such as "fresh", and synonyms),
2. converts units (kg to g, litres to ml, cups/tablespoons/teaspoons to ml),
3. converts between weight, volume and piece counts for common foods, and
4. adds up several pantry rows that are the same ingredient.

`RecipeMatcher` is plain Java with no Android classes, so it is covered by
16 JUnit tests in `RecipeMatcherTest.java`.

## Database choice: SQLite

The app uses **SQLite** through `SQLiteOpenHelper` (`DatabaseHelper.java`).

**Why SQLite:**
- A pantry is personal data that should work with **no internet and no account**,
  so a local on-device database fits better than a cloud service.
- The recipe matching runs on-device, so no backend or REST API is needed.
- It is the approach taught in the module's persistent-data chapter.
- Data persists after the app is closed and reopened.

**Tables** (database `SmartPantry.db`, version 2):

| Table | Purpose |
|---|---|
| `pantry_items` | The user's ingredients (name, quantity, unit, expiry date) |
| `recipes` | Recipe name and preparation steps |
| `recipe_ingredients` | The ingredients each recipe needs (linked to `recipes`) |

## Project structure

| File | Role |
|---|---|
| `MainActivity` | Pantry list, toolbar menu, expiry banner |
| `AddIngredientActivity` | Add / edit form with validation |
| `SuggestedRecipesActivity` | Runs the matcher and lists results |
| `RecipeDetailActivity` | Ingredients and method for one recipe |
| `SettingsActivity` / `AppSettings` | Settings screen and SharedPreferences helper |
| `DatabaseHelper` | Creates tables, seeds recipes, all CRUD queries |
| `RecipeMatcher` | Strict-matching logic |
| `PantryAdapter` / `RecipeAdapter` | RecyclerView adapters |
| `PantryItem` / `Recipe` / `RecipeIngredient` | Model classes |

Screens are connected with Intents; data such as the recipe id or the item
being edited is passed as Intent extras.

## Setup and run

**Requirements:** Android Studio (latest stable), JDK 17 or newer (the JDK
bundled with Android Studio works), and an emulator or Android device on
Android 7.0 (API 24) or newer.

1. Clone the repository:
```bash
   git clone https://github.com/ugeshan19/SmartPantryManager.git
```
2. Open Android Studio, choose **File > Open**, and select the cloned folder.
3. Wait for **Gradle sync** to finish (an internet connection is needed the
   first time to download dependencies).
4. Choose an emulator or connect a phone with USB debugging on.
5. Press **Run** (the green triangle).

The 21 recipes are added automatically the first time the app runs. No API
keys or accounts are needed.

**Run the unit tests:** right-click `RecipeMatcherTest` in Android Studio and
choose **Run**, or run `./gradlew test` in a terminal.

## Out of scope

As required by the brief, the app does **not** use Google Maps, any mapping
SDK, GPS or location services, payments, or the Play Store.