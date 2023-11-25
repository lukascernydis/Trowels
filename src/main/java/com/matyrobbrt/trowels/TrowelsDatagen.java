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
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.function.Consumer;

@ParametersAreNonnullByDefault
public class TrowelsDatagen {
    @SubscribeEvent
    static void datagen(final GatherDataEvent event) {
        final DataGenerator gen = event.getGenerator();
        final ExistingFileHelper efh = event.getExistingFileHelper();

        gen.addProvider(event.includeClient(), new Lang(gen, "en_us", BuildEnglishLocale()));
        gen.addProvider(event.includeClient(), new Lang(gen, "ru_ru", BuildRussianLocale()));

        gen.addProvider(event.includeClient(), new ItemModels(gen, efh));

        gen.addProvider(event.includeServer(), new Recipes(gen.getPackOutput()));
    }

    private static Map<String, String> BuildEnglishLocale() {
        Map<String, String> map = new HashMap<>();

        map.put("item.trowels." + Trowels.TROWEL.get(), "Trowel");
        map.put("desc.trowels.trowel", "When right-clicked, the trowel will place a random block from your hotbar.");
        map.put("desc.trowels.trowel.upgrades",
                "Can be upgraded by combining it with an upgrade item in anvil.");
        map.put("desc.trowels.trowel.destroy_recent",
                "You can break blocks which have been recently (5 latest) placed by the trowel instantly, " +
                        "they are returned to your inventory.");

        map.put("item.trowels." + Trowels.REFILL_UPGRADE.get(), "Refill Upgrade");
        map.put("item.trowels." + Trowels.BREAK_UPGRADE.get(), "Break Upgrade");
        map.put("trowel_upgrade.refill", "Refill Upgrade");
        map.put("trowel_upgrade.break", "Break Upgrade");
        map.put("trowel_upgrade.refill.desc",
                "Combine with a Trowel in an anvil in order to make it refill " +
                        "the slot of the used block after placement, from your inventory.");
        map.put("trowel_upgrade.break.desc",
                "Combine with a Trowel in an anvil in order to give it an ability to " +
                        "instantly break blocks recently (5 latest) placed with the trowel.");

        map.put("tooltip.trowels.upgrades", "Upgrades:");

        map.put("creative_tab.trowels", "Trowels");

        return map;
    }

    private static Map<String, String> BuildRussianLocale() {
        Map<String, String> map = new HashMap<>();

        map.put("item.trowels." + Trowels.TROWEL.get(), "Мастерок");
        map.put("desc.trowels.trowel",
                "Мастерок размещает случайный блок из хотбара при использовании.");
        map.put("desc.trowels.trowel.upgrades",
                "Мастерок можно усовершенствовать, соединив с улучшением в наковальне.");
        map.put("desc.trowels.trowel.destroy_recent",
                "Удар по недавно установленному (5 последних) мастерком блоку с зажатым Shift " +
                        "позволит его моментально добыть.");

        map.put("item.trowels." + Trowels.REFILL_UPGRADE.get(), "Пополняющее улучшение");
        map.put("item.trowels." + Trowels.BREAK_UPGRADE.get(), "Добывающее улучшение");
        map.put("trowel_upgrade.refill", "Пополняющее улучшение");
        map.put("trowel_upgrade.break", "Добывающее улучшение");
        map.put("trowel_upgrade.refill.desc",
                "Соедините с мастерком в наковальне, чтобы пополнять заканчивающиеся в хотбаре стаки из инвентаря.");
        map.put("trowel_upgrade.break.desc",
                "Соедините с маестерком в наковальне, чтобы получить возможность моментально ломать недавно (5 последних)" +
                        " установленные мастерком блоки. Блок будет возвращён в инвентарь.");

        map.put("tooltip.trowels.upgrades", "Улучшения:");

        map.put("creative_tab.trowels", "Trowels");

        return map;
    }

    private static final class ItemModels extends ItemModelProvider {

        public ItemModels(DataGenerator generator, ExistingFileHelper existingFileHelper) {
            super(generator.getPackOutput(), Trowels.MOD_ID, existingFileHelper);
        }

        @Override
        protected void registerModels() {
            final ModelFile itemParent = new ModelFile.UncheckedModelFile("item/generated");

            getBuilder(Trowels.TROWEL.getId().toString())
                    .parent(itemParent)
                    .texture("layer0", new ResourceLocation("trowels", "item/trowel_base"))
                    .texture("layer1", new ResourceLocation("trowels", "item/trowel_handle"));

            basicItem(Trowels.REFILL_UPGRADE.get());
            basicItem(Trowels.BREAK_UPGRADE.get());
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

            ShapedRecipeBuilder.shaped(RecipeCategory.MISC, Trowels.BREAK_UPGRADE.get())
                    .pattern("H")
                    .pattern("I")
                    .pattern("S")
                    .define('H', Items.IRON_PICKAXE)
                    .define('I', Tags.Items.INGOTS_IRON)
                    .define('S', Tags.Items.RODS_WOODEN)
                    .unlockedBy("has_iron", has(Tags.Items.INGOTS_IRON))
                    .save(cons);
        }
    }

    private static final class Lang extends LanguageProvider {

        private final Map<String, String> locale;

        public Lang(DataGenerator gen, String langCode, Map<String, String> locale) {
            super(gen.getPackOutput(), Trowels.MOD_ID, langCode);
            this.locale = locale;
        }

        @Override
        protected void addTranslations() {
            for (Map.Entry<String, String> entry : locale.entrySet())
                add(entry.getKey(), entry.getValue());
        }
    }
}
