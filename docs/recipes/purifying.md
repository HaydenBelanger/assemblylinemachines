# Purifying Recipes

The Purifying recipes are for recipes you want to be performed in the Electric Purifier. In the base mod, this is used to create Pure versions of Ingots, as well as a few other uses.

## Recipe Specification



Recipe namespace: `assemblylinemachines:purifier`

!!! warning inline end
    If `part_a` or `part_b` are not `minecraft:gravel` or `minecraft:sand`, or tags that contain them, the Electric Purifier will require the Advanced Filtering Upgrade in order to allow processing of the recipe.

- `part_a`: Ingredient, required  
*The first part of the 'filter ingredients'.*
- `part_b`: Ingredient, required  
*The second part of the 'filter ingredients'.*

!!! tip
    The Electric Purifier is affected by the Machine Conservation Upgrade, with every upgrade reducing the chance to consume `part_a` or `part_b` by 10%. This only applies to `minecraft:sand` or `minecraft:gravel`.

- `tobepurified`: Ingredient, required  
*The primary item that will be used for the recipe.*
- `output`: ItemStack, required  
*The result item from the recipe.*
- `time`: Positive Integer, required  
*The time required to process the recipe. 1 is equal to 0.08 seconds of processing time. Every Speed Upgrade in the machine will cut this processing time in half.*

## Examples

Below are some examples of Purfying recipes.

This will use Gravel and Sand to purify a Copper Ingot into a Pure Copper Ingot.

``` json
{
	"type": "assemblylinemachines:purifier",
	"part_a":{
		"tag": "forge:gravel"
	},
	"part_b":{
		"tag": "minecraft:sand"
	},
	"tobepurified":{
		"item": "minecraft:copper_ingot"
	},
	"output":{
		"item": "assemblylinemachines:pure_copper"
	},
	"time": 200
}
```

This one will use a Mystium Blend and an Ender Pearl to purfy a Pure Titanium Ingot into an Attuned Titanium Ingot. Of note here is that the `part_a` and `part_b` items are not Sand and Gravel, so the Advanced Filtering Upgrade will be required in the machine for processing.

``` json
{
	"type": "assemblylinemachines:purifier",
	"part_a":{
		"tag": "forge:dusts/mystium"
	},
	"part_b":{
		"tag": "forge:ender_pearls"
	},
	"tobepurified":{
		"tag": "forge:ingots/pure_titanium"
	},
	"output":{
		"item": "assemblylinemachines:attuned_titanium_ingot"
	},
	"time": 600
}
```