# Milling Recipes

The Milling recipes are available for the Lumber Mill processing. Typically, this will be to create wood-based items, like Planks.

## Recipe Specification

Recipe namespace: `assemblylinemachines:lumber`

- `input`: Ingredient, required  
*The item or block to process.*
- `output`: ItemStack, required  
*The resulting primary item from the recipe.*
- `secondaryoutput`: ItemStack, *optional*  
*The optional secondary output produced as a byproduct of the recipe.*
- `opbchance`: Decimal between 0-1, *optional*  
*The chance to produce the secondary output, between 0% and 100%. If `secondaryoutput` is set, this must be too.*

!!! tip
    The Lumber Mill is influenced by the Extra Output Machine Upgrade. Depending on the number of upgrades, the chance to generate will be multiplied by either 1.5x, 2x, or 2.5x.

- `time`: Positive Integer, required  
*The time required to process the recipe. 1 is equal to 0.8 seconds of processing time. Every Speed Upgrade in the machine will cut this processing time in half.*

## Example

Below is an example of a Milling recipe. This will process a Warped Stem and produce 6 Warped Planks, with a chance to produce Warped Sawdust.

``` json
{
	"type": "assemblylinemachines:lumber",
	"input":{
		"tag": "minecraft:warped_stems"
	},
	"output":{
		"item": "minecraft:warped_planks",
		"count": 6
	},
	"secondaryoutput":{
		"item": "assemblylinemachines:warped_sawdust"
	},
	"opbchance": 0.10,
	"time": 6
}
```