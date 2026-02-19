package top.szzz666.Dungeon.recipe;

import cn.nukkit.inventory.ShapedRecipe;
import cn.nukkit.item.Item;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import static top.szzz666.Dungeon.DungeonMain.nkServer;

public class Recipes {
    public static void MagicStone() {
        Map<Character, Item> ingredients = new HashMap<>();

        ingredients.put('A', Item.fromString("s_rpg:magic_stone_fragment"));
        String[] shape = new String[]{
                "AAA",
                "AAA",
                "AAA"
        };
        Item result = Item.fromString("s_rpg:magic_stone");
        result.count = 1;
        ShapedRecipe recipe = new ShapedRecipe(
                result,
                shape,
                ingredients,
                new ArrayList<>()
        );
//        Item result = Item.fromString("s_rpg:magic_stone");
//        List<Item> ingredients = new ArrayList<>();
//        for (int i = 0; i < 9; i++) {
//            ingredients.add(Item.fromString("s_rpg:magic_stone_fragment"));
//        }
//        ShapelessRecipe recipe1 = new ShapelessRecipe(
//                result,ingredients
//        );
//        nkServer.getCraftingManager().registerShapedRecipe(recipe);
        nkServer.getCraftingManager().registerRecipe(recipe);
        nkServer.getCraftingManager().registerRecipe(313,recipe);
        nkServer.getCraftingManager().registerRecipe(332,recipe);
        nkServer.getCraftingManager().registerRecipe(388,recipe);
        nkServer.getCraftingManager().registerRecipe(419,recipe);
        nkServer.getCraftingManager().registerRecipe(527,recipe);
    }
}
