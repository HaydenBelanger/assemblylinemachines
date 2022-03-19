# Grinding Recipes

The Grinding recipes are for recipes you want to be performed in the Simple Grinder, Electric Grinder, and Manual Grinder. With some exceptions, all of the recipes in this designation are able to be used in all grinding-based machines.

## Recipe Specification

Recipe namespace: `assemblylinemachines:grinder`

- `input`: Ingredient, required  
*The item to be ground.*  
- `grinds`: Positive Integer, required  
*The general uses or operations required to complete the recipe. Varies between machine types.*

??? note "On the Processing Time of Grinding Recipes"
    The calculation for the processing time on grinding recipes varies wildly due to the intrinsic difference between the three machines that can process this type of recipe.  
    For the *Manual Grinder*, 1 is simply equal to 1 use in-world of a Manual Grinder with a blade installed.  
    For the *Simple Grinder*, 1 is equal to a minimum of 2 seconds of processing time, depending on the regularity of a Crank/Gearbox use. For example, `grinds: 8` would last for 16 seconds, as long as the block is supplied with Crank power at least once a second.  
    For the *Electric Grinder*, 1 is equal to 2.875 seconds of processing time. For example, `stirs: 8` would last for 23 seconds of processing. Every Speed Upgrade in the machine will cut the processing time in half.

- `bladetype`: String, required  
*The blade required to allow processing of the recipe. This, and all blades in tiers above this blade, will work. Options include `TITANIUM`, `PUREGOLD`, and `STEEL`, in ascending tier-order.*

!!! tip
    The Electric Grinder does not have a blade slot, so it will always pass the check for correct blade regardless of the recipe.

- `machine_required`: Boolean, *optional*  
*If this is set to true, the recipe will not work in the Manual Grinder, and will only work in the Simple or Electric Grinder.*  
- `chanceToDouble`: Decimal between 0 and 1, **1.18-1.3.3+ only,** *optional*  
*If set, this will be the percentage likelihood that the output is doubled when the operation is completed. If unset, the chance is 0%.*
- `output`: ItemStack, *optional*  
*The resulting output from the recipe. As of 1.18.2-1.4, this may be a [Tag ItemStack.](../miscdev/datatypes.md#tag-itemstack)*

## Examples

Below is an example of a Grinding recipe. This will take Chromium Ore and produce 2 Ground Chromium, and this recipe requires a Simple or Electric Grinder.

``` json
{
	"type": "assemblylinemachines:grinder",
	"input":{
		"tag": "forge:ores/chromium"
	},
	"output":{
		"item": "assemblylinemachines:ground_chromium",
		"count": 2
	},
	"grinds": 8,
	"bladetype": "TITANIUM",
	"machine_required": true
}
```

**1.18-1.3.3+ only:** This recipe is a demonstration of the `chanceToDouble` parameter. 15% of the time, the recipe will produce 8 Blaze Powder instead of 4.

``` json
{
	"type": "assemblylinemachines:grinder",
	"input":{
		"tag": "forge:rods/blaze"
	},
	"output":{
		"item": "minecraft:blaze_powder",
		"count": 4
	},
	"grinds": 7,
	"chanceToDouble": 0.15,
	"bladetype": "PUREGOLD"
}
```