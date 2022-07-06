# Shaping Recipes

The Shaping recipes are for recipes you want to be performed in the Metal Shaper. Generally, this will process an Ingot of some kind and produce a Plate.

!!! error "Removed Feature"
	This recipe type was removed in Assembly Line Machines 1.18.2-1.4, with the addition of the Pneumatic Compressor, [Compressing Recipes](../recipes/compressing.md), and the removal of the Metal Shaper.
## Recipe Specification

Recipe namespace: `assemblylinemachines:metal`

- `input`: Ingredient, required  
*The item to process the recipe.*
- `output`: ItemStack, required  
*The result from the operation.*
- `time`: Positive Integer, required  
*The time required to process the recipe. 1 is equal to 0.8 seconds of processing time. Every Speed Upgrade in the machine will cut this processing time in half.*

## Example

Below is an example of a Shaping recipe. This will process one Attuned Titanium Ingot and produce an Attuned Titanium Plate.

``` json
{
	"type": "assemblylinemachines:metal",
	"input":{
		"tag": "forge:ingots/attuned_titanium"
	},
	"output":{
		"item": "assemblylinemachines:attuned_titanium_plate"
	},
	"time": 6
}
```