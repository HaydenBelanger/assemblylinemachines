# Fluid Bath Recipes

The Fluid Bath recipes are for recipes you want to be performed in the Fluid Bath, the Basic Fluid Mixer, or the Electric Fluid Mixer. With some exceptions, all of the recipes in this designation are able to be used in all fluid-mixing-based machines.

## Recipe Specification

Recipe namespace: `assemblylinemachines:bath`.

- `input_a`: Ingredient, required  
*The first item in the recipe.*
- `input_b`: Ingredient, required  
*The second item in the recipe.*
- `output`: ItemStack, required  
*The result stack for the recipe.*
- `stirs`: Positive Integer, required  
*The general uses or operations required to complete the recipe. Varies between machine types.*

??? note "On the Processing Time of Mixing Recipes"
    The calculation for the processing time on mixing recipes varies wildly due to the intrinsic difference between the three machines that can process this type of recipe.  
    For the *Fluid Bath*, 1 is simply equal to 1 use in-world of a Stirring Stick held by a player.  
    For the *Simple Fluid Mixer*, 1 would be equal to a minimum of 2 seconds of processing time, depending on the regularity of a Crank/Gearbox use. For example, `stirs: 8` would last for 16 seconds, as long as the block is supplied with Crank power at least once a second.  
    For the *Electric Fluid Mixer*, 1 is equal to 2.88 seconds of processing time. So for example, `stirs: 8` would last for 23.04 seconds of processing. Every Speed Upgrade in the machine will cut the processing time in half.

- `fluid`: String, required  
*The fluid that is required to process the recipe. Options include `water`, `lava`, `oil`, `condensed_void`, or `naphtha`.*

!!! warning
    If the fluid is not `water` or `lava`, the only machine allowed for the recipe will be the Electric Fluid Mixer, as the other two early-game machines do not work with anything else.

- `mix_color`: String hex code, required  
*The color the liquid will show in the basin when crafted in the Fluid Bath. This option does not have an effect on the Simple or Electric Fluid Mixer, and can be set to 0 safely when using `mixer_type: MIXER_ONLY`.*

- `mixer_type`: String, *optional*  
*The type of machine that can perform this recipe. Options include `MIXER_ONLY` or `BASIN_ONLY`. If unset, all machines will allow processing.*

- `drain_percent`: String, *optional*  
*The amount of fluid that will be drained from the machine when an operation is concluded, in increments of full buckets/1000 mB. Options include `FULL`, `HALF`, or `QUARTER`. If unset, `FULL` will be used.

## Examples

Below are examples of some Fluid Bath recipes.

To demonstrate, both of the mod's Silt recipes will be shown to demonstrate the capability of the above options. Within the game, mixing Sand and Gravel together in Water will produce Silt, but in the Fluid Bath only one unit will be produced, while in the Simple and Electric Fluid Mixer, four will be made, allowing quicker generation later in the game. Below are the recipes listed respectively.

``` json
{
	"type": "assemblylinemachines:bath",
	"input_a":{
		"tag": "minecraft:sand"
	},
	"input_b":{
		"tag": "forge:gravel"
	},
	"output":{
		"item": "assemblylinemachines:silt"
	},
	"stirs": 4,
	"fluid": "water",
	"mix_color": "#b8b4ab",
	"mixer_type": "BASIN_ONLY",
	"drain_percent": "QUARTER"
}
```

``` json
{
	"type": "assemblylinemachines:bath",
	"input_a":{
		"tag": "minecraft:sand"
	},
	"input_b":{
		"tag": "forge:gravel"
	},
	"output":{
		"item": "assemblylinemachines:silt",
		"count": 4
	},
	"stirs": 4,
	"fluid": "water",
	"mix_color": "0",
	"mixer_type": "MIXER_ONLY",
	"drain_percent": "QUARTER"
}
```