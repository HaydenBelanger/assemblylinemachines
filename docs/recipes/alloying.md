# Alloying Recipes

The Alloying recipe are for recipes you want to be performed in the Alloy Smelter. The Alloy Smelter will take in two inputs, process them, and output an ItemStack, typically an alloy or blend of some kind.

## Recipe Specification

Recipe namespace: `assemblylinemachines:alloying`.

- `part_a`: Ingredient, required  
*The first item in the recipe.*
- `part_b`: Ingredient, required  
*The second item in the recipe.*
- `output`: ItemStack, required  
*The result stack for the recipe.*
- `time`: Positive Integer, required  
*The time required to complete the operation. Every 12.5 in this value is equal to 1 second.  
For example, `time: 100` would be equal to 8 seconds of processing time.  
Every Speed Upgrade in the machine will cut the processing time in half.*

## Example

Below is an example of an Alloying recipe. This will take an Energized Gold and a Ground Netherite and output an Electrified Netherite Blend.
``` json
{
	"type": "assemblylinemachines:alloying",
	"part_a":{
		"tag": "forge:ingots/energized_gold"
	},
	"part_b":{
		"tag": "forge:dusts/netherite"
	},
	"output":{
		"item": "assemblylinemachines:electrified_netherite_blend"
	},
	"time": 350
}
```