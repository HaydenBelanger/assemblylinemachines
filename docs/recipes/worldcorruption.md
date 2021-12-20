# World Corruption Recipes

The World Corruption recipes are for recipes you want to be performed both in the Corrupting Basin and as a result of high Entropy in the area surrounding the Entropy Reactor. Typically, in the base mod, these recipes are used for turning default Minecraft blocks into their "Corrupt" variants, like turning Grass into Corrupt Grass.

!!! error "1.18+ Feature"
    This recipe type was made available in Assembly Line Machines 1.18-1.3.1. Prior to this version, World Corruption data was hardcoded and not accessible.

## Recipe Specification

Recipe namespace: `assemblylinemachines:world_corruption`.

- `input`: Ingredient, *optional*  
*The input block of the recipe.*  
- `fluidInput`: Namespace, *optional*  
*The fluid ResourceLocation for the input fluid of the recipe, for example `minecraft:water`.*  
- `output`: ItemStack, *optional*  
*The output block of the recipe. Quantity has no effect, and this recipe will always result in a single output.*  
- `fluidOutput`: Namespace, *optional*  
*The fluid ResourceLocation for the output fluid of the recipe.*

!!! warning
    One, but not both, of `input` or `fluidInput` must be specified, or the recipe will not load. If `input` or `fluidInput` is set, then the corresponding output field must be set as well.

    If the ItemStack provided for `output` is not a BlockItem, but just an Item, then the recipe will fail to load. If `input`'s Ingredient, whether a tag or a single item, is set, the recipe will successfully load, but the recipe will be effectively worthless as the recipe is only compatible with BlockItems.

## Examples

Below are examples of some World Corruption recipes. Both examples are from real operations performable in the base mod. The first example is to create Corrupt Diamond Ore from Diamond Ore.

``` json
{
	"type": "assemblylinemachines:world_corruption",
	"input":{
		"tag": "forge:ores/diamond"
	},
	"output":{
		"item": "assemblylinemachines:corrupt_diamond_ore"
	}
}
```

And the second example, to demonstrate the fluid version of the recipe, is to convert Water into Dark Energy.

``` json
{
	"type": "assemblylinemachines:world_corruption",
	"fluidInput": "minecraft:water",
	"fluidOutput": "assemblylinemachines:dark_energy"
}
```