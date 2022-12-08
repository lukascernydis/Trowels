package com.matyrobbrt.trowels;

import net.minecraft.data.DataGenerator;
import net.minecraft.data.PackOutput;
import net.minecraft.data.recipes.FinishedRecipe;
import net.minecraft.data.recipes.RecipeCategory;
import net.minecraft.data.recipes.RecipeProvider;
import net.minecraft.data.recipes.ShapedRecipeBuilder;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Items;
import net.minecraftforge.client.model.generators.ItemModelProvider;
import net.minecraftforge.client.model.generators.ModelFile;
import net.minecraftforge.common.Tags;
import net.minecraftforge.common.data.ExistingFileHelper;
import net.minecraftforge.common.data.LanguageProvider;
import net.minecraftforge.data.event.GatherDataEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;

import javax.annotation.ParametersAreNonnullByDefault;
import java.util.function.Consumer;

@ParametersAreNonnullByDefault
public class TrowelsDatagen {
    @SubscribeEvent
    static void datagen(final GatherDataEvent event) {
        final DataGenerator gen = event.getGenerator();
        final ExistingFileHelper efh = event.getExistingFileHelper();

        gen.addProvider(event.includeClient(), new Lang(gen));
        gen.addProvider(event.includeClient(), new ItemModels(gen, efh));

        gen.addProvider(event.includeServer(), new Recipes(gen.getPackOutput()));
    }

    private static final class ItemModels extends ItemModelProvider {

        public ItemModels(DataGenerator generator, ExistingFileHelper existingFileHelper) {
            super(generator, Trowels.MOD_ID, existingFileHelper);
        }

        @Override
        protected void registerModels() {
            final ModelFile itemParent = new ModelFile.UncheckedModelFile("item/generated");

            getBuilder(Trowels.TROWEL.getId().toString())
                    .parent(itemParent)
                    .texture("layer0", new ResourceLocation("trowels", "item/trowel_base"))
                    .texture("layer1", new ResourceLocation("trowels", "item/trowel_handle"));

            basicItem(Trowels.REFILL_UPGRADE.get());
        }
    }

    private static final class Recipes extends RecipeProvider {

        public Recipes(PackOutput gen) {
            super(gen);
        }

        @Override
        protected void buildRecipes(Consumer<FinishedRecipe> cons) {
            ShapedRecipeBuilder.shaped(RecipeCategory.TOOLS, Trowels.TROWEL.get())
                    .pattern("  I")
                    .pattern(" I ")
                    .pattern("S  ")
                    .define('I', Tags.Items.INGOTS_IRON)
                    .define('S', Tags.Items.RODS_WOODEN)
                    .unlockedBy("has_iron", has(Tags.Items.INGOTS_IRON))
                    .save(cons);

            ShapedRecipeBuilder.shaped(RecipeCategory.MISC, Trowels.REFILL_UPGRADE.get())
                    .pattern("H")
                    .pattern("I")
                    .pattern("S")
                    .define('H', Items.HOPPER)
                    .define('I', Tags.Items.INGOTS_IRON)
                    .define('S', Tags.Items.RODS_WOODEN)
                    .unlockedBy("has_iron", has(Tags.Items.INGOTS_IRON))
                    .save(cons);
        }
    }

    private static final class Lang extends LanguageProvider {

        public Lang(DataGenerator gen) {
            super(gen, Trowels.MOD_ID, "en_us");
        }

        @Override
        protected void addTranslations() {
            add(Trowels.TROWEL.get(), "Trowel");
            add("desc.trowels.trowel", "When right-clicked, the trowel will place a random item from your hotbar.");
            add("desc.trowels.trowel.destroy_recent", "When clicking a block that has been recently (last 5 blocks) placed by the trowel, you can mine it with it.");

            add(Trowels.REFILL_UPGRADE.get(), "Refill Upgrade");
            add("trowel_upgrade.refill", "Refill Upgrade");
            add("trowel_upgrade.refill.desc", "Combine with a Trowel in an anvil in order to make it refill the slot of the used block after placement, from your inventory.");

            add("tooltip.trowels.upgrades", "Upgrades:");

            add("creative_tab.trowels", "Trowels");
        }
    }
}
