package com.example.smartpantrymanager;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

/**
 * Adapter that displays the suggested recipes inside a RecyclerView.
 * Each row shows the recipe name and a short summary of its ingredients.
 */
public class RecipeAdapter
        extends RecyclerView.Adapter<RecipeAdapter.RecipeViewHolder> {

    /** Callback so the Activity decides what happens when a recipe is tapped. */
    public interface OnRecipeClickListener {
        void onRecipeClick(Recipe recipe);
    }

    private final List<Recipe> recipes;
    private final OnRecipeClickListener listener;

    public RecipeAdapter(List<Recipe> recipes, OnRecipeClickListener listener) {
        this.recipes = recipes;
        this.listener = listener;
    }

    @NonNull
    @Override
    public RecipeViewHolder onCreateViewHolder(@NonNull ViewGroup parent,
                                               int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_recipe, parent, false);
        return new RecipeViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull RecipeViewHolder holder, int position) {

        Recipe recipe = recipes.get(position);

        holder.tvRecipeName.setText(recipe.getName());

        // Build a summary such as "4 ingredients: egg, butter, milk, salt".
        StringBuilder summary = new StringBuilder();
        List<RecipeIngredient> ingredients = recipe.getIngredients();
        summary.append(ingredients.size()).append(" ingredients: ");
        for (int i = 0; i < ingredients.size(); i++) {
            if (i > 0) {
                summary.append(", ");
            }
            summary.append(ingredients.get(i).getName());
        }
        holder.tvRecipeSummary.setText(summary.toString());

        holder.itemView.setOnClickListener(v -> listener.onRecipeClick(recipe));
    }

    @Override
    public int getItemCount() {
        return recipes.size();
    }

    /** Holds the views for one recipe row. */
    public static class RecipeViewHolder extends RecyclerView.ViewHolder {

        TextView tvRecipeName;
        TextView tvRecipeSummary;

        public RecipeViewHolder(@NonNull View itemView) {
            super(itemView);
            tvRecipeName = itemView.findViewById(R.id.tvRecipeName);
            tvRecipeSummary = itemView.findViewById(R.id.tvRecipeSummary);
        }
    }
}