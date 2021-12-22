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

??? warning "Proper Field Selection"
    One, but not both, of `input` or `fluidInput` must be specified, or the recipe will not load. If `input` or `fluidInput` is set, then the corresponding output field must be set as well.

    If the ItemStack provided for `output` is not a BlockItem, but just an Item, then the recipe will fail to load. If `input`'s Ingredient, whether a tag or a single item, is set, the recipe will successfully load, but the recipe will be effectively worthless as the recipe is only compatible with BlockItems.

??? info "Additional Outputs"
	**1.18-1.3.2+ only:** You can add field `additionalOutputs` to your recipe. This is a JSON Array of JSON Objects. This allows for one recipe to output a selection of various options, based on a chance. This type is only functional with the block-replacing, and will not work with the `fluid` or `fluidOutput` fields. You can include as many additional outputs as you wish. The objects contained in the array must have the following fields:

	`output`: ItemStack, the block output of the additional output. Just like with the standard output field, the item returned here must be of type BlockItem, or the recipe will fail to load.  
	`chance`: Decimal between 0 and 1, this is the chance, represented as a percentage likelihood, that this additional output is chosen to be the true output.

	The Corrupting Basin or the Entropy Reactor will cycle through all additional outputs in increasing chance order. It will test the chance given, and if met, that will be the output of the recipe. If the required chance is not met, it will output the standard `output` from the main JSON Object as a fallback.

	For an example, please see below Example #3.

??? tip "Special Block Placement"
	**1.18-1.3.2+ only:** Certain blocks, which implement Java interface `ISpecialEntropyPlacement`, can have special placement behavior. For example, the Tall Chaosweed and Tall Blooming Chaosweed implement this interface, allowing replaced 1-block-tall Grass blocks to be double-height when placed in the world. In the base mod, these is the only two blocks that implement this interface, but if you are a mod developer, you can implement it, too.

## Examples

Below are examples of some World Corruption recipes. All examples are from real operations performable in the base mod. The first example is to create Corrupt Diamond Ore from Diamond Ore.

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

**1.18-1.3.2+ only:** Finally, the last example demonstrates the capability of the `additionalOutputs` field. This will select a random type of Chaosweed from one of four options in order to replace Tall Grass.

``` json
{
	"type": "assemblylinemachines:world_corruption",
	"input":{
		"item": "minecraft:grass"
	},
	"output":{
		"item": "assemblylinemachines:chaosweed"
	},
	"additionalOutputs":[
		{
			"output":{
				"item": "assemblylinemachines:blooming_chaosweed"
			},
			"chance": 0.15
		},
		{
			"output":{
				"item": "assemblylinemachines:tall_chaosweed"
			},
			"chance": 0.15
		},
		{
			"output":{
				"item": "assemblylinemachines:tall_blooming_chaosweed"
			},
			"chance": 0.05
		}
	]
}
```