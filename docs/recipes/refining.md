# Refining Recipes

The Refining recipes are available for the Refinery processing. This can take in an item and/or a fluid and output an item and/or up to two fluids as a result.

## Recipe Specification

Recipe namespace: `assemblylinemachines:refining`

??? info "Ingredients, ItemStacks, and FluidStacks With Chances"
    Below, all input and output fields have additional optional keys:

    `upgrade_preserve_chance`, Decimal between 0-1, valid for `input_fluid` and `input_item` - If set, will allow for the input item or fluid to be preserved if there is the Machine Conservation Upgrade installed. The chance will scale up to 2x `upgrade_preserve_chance` if there are 3 Machine Conservation Upgrades installed.

    `upgrade_multiply_chance`, Decimal between 0-1, valid for `output_fluid_a`, `output_fluid_b`, and `output_item` - If set, will allow for the outputs to be multiplied by 1.5x the normal output amount if there is an Extra Output Machine Upgrade installed. The chance will scale up to 2x `upgrade_multiply_chance` if there are 3 Extra Output Machine Upgrades installed.

!!! warning inline end
    Any block may be used as an `attachment`, but only Blocks that extend `BlockRefineryAddon` will have the effects of the reactor, including particles and other client-side effects.

- `attachment`: Namespace, required  
*The block attachment that must be placed above the Refinery during the craft to allow function.*
- `input_fluid`: FluidStack with Chance, *optional*  
*The fluid required to start the recipe.*
- `input_item`: Ingredient with Chance, *optional*  
*The item required to start the recipe.*

!!! warning
    You may use both or one of `input_fluid` or `input_item`, but one must be present or the recipe will not load.

    If `input_fluid` is present and has property `FluidAttributes#isGaseous()` set, the Refinery will require the Gas Upgrade, and FE/t cost will be increased by 2.5x.

- `proc_time`: Positive Integer, required  
*The time required to process the recipe. 1 is equal to 0.8 seconds of processing time. Every Speed Upgrade in the machine will cut this processing time in half.*
- `output_item`: ItemStack with Chance, *optional*  
*The resulting item from the recipe.*
- `output_fluid_a`: FluidStack with Chance, *optional*  
*The resulting fluid from the recipe. Can also be entered as `output_fluid`.*
- `output_fluid_b`: FluidStack with Chance, *optional*  
*The second resulting fluid from the recipe.*

!!! warning
    You may use any or all of `output_item`, `output_fluid_a`, or `output_fluid_b`, but at least one must be present or the recipe will not load.

    If `output_fluid_b` is set, `output_fluid_a` must be set as well, or else the recipe will not load.

## Examples

Below are a number of examples of a Refining recipe.

- This will take Oil and turn it into Gasoline and Diesel at a 2.5:1 ratio, with a 50% chance to double the Diesel output, as long as the Separation Refinery Attachment is on the Refinery. There is a 15% chance to consume half the Oil.

``` json
{
	"type": "assemblylinemachines:refining",
	"attachment": "assemblylinemachines:refinery_attachment_separation",
	"input_fluid":{
		"fluid": "assemblylinemachines:oil",
		"amount": 1000,
		"upgrade_preserve_chance": 0.15
	},
	"proc_time": 12,
	"output_fluid_a":{
		"fluid": "assemblylinemachines:gasoline",
		"amount": 500
	},
	"output_fluid_b":{
		"fluid": "assemblylinemachines:diesel",
		"amount": 200,
		"upgrade_multiply_chance": 0.50
	}
}
```

- This will take Ethylene and Ground Charcoal and turn it into a Plastic Ball, with a 25% chance to double the output, as long as the Addition Refinery Attachment is on the Refinery. As Ethylene is a gas, the Refinery will require the Gas Upgrade.

``` json
{
	"type": "assemblylinemachines:refining",
	"attachment": "assemblylinemachines:refinery_attachment_addition",
	"input_fluid":{
		"fluid": "assemblylinemachines:ethylene",
		"amount": 1000
	},
	"input_item":{
		"tag": "forge:dusts/charcoal"
	},
	"proc_time": 18,
	"output_item":{
		"item": "assemblylinemachines:plastic_ball",
		"upgrade_multiply_chance": 0.25
	}
}
```

- Finally, this will take Oil and turn it into Propane and Ethane at a 1:2 ratio, with a 25% chance to double each output, as long as the Halogen Reactor Refinery Attachment is on the Refinery. This is an example of how 'same input' recipes can produce different outputs depending on the attachment.

``` json
{
	"type": "assemblylinemachines:refining",
	"attachment": "assemblylinemachines:refinery_attachment_halogen",
	"input_fluid":{
		"fluid": "assemblylinemachines:oil",
		"amount": 1000
	},
	"proc_time": 22,
	"output_fluid_a":{
		"fluid": "assemblylinemachines:propane",
		"amount": 250,
		"upgrade_multiply_chance": 0.25
	},
	"output_fluid_b":{
		"fluid": "assemblylinemachines:ethane",
		"amount": 500,
		"upgrade_multiply_chance": 0.25
	}
}
```