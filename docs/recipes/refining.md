# Refining Recipes

The Refining recipes are available for the Refinery processing. This can take in an item and/or a fluid and output an item and/or up to two fluids as a result.

## Recipe Specification

Recipe namespace: `assemblylinemachines:refining`

??? info "Preservation/Multiplication Chance"
    All `input` and `output` fields below can have an additional property set that is exclusive to the Refinery: `upgrade_multiply_chance`. This is a decimal between 0-1 and acts as a percentage chance, and works in conjunction with upgrades to allow the following:

	- For inputs, the input may be preserved if there is a Machine Conservation Upgrade installed. This chance to preserve will scale up to 2x specified if there are multiple upgrades installed.  
	- For outputs, the output may be multiplied if there is an Extra Output Upgrade installed. This chance to multiply will scale up to 2x if there are multiple upgrades installed.

	Not specifying `upgrade_multiply_chance` will always make the chance 0%, regardless of the amount of upgrades installed. In both cases, the amount after conservation or multiplication is always 1.5x the regular amount.

!!! warning inline end
    Any block may be used as an `attachment`, but only Blocks that extend `BlockRefineryAddon` will have the effects of the reactor, including particles and other client-side effects.

- `attachment`: Namespace, required  
*The block attachment that must be placed above the Refinery during the craft to allow function.*
- `proc_time`: Positive Integer, required  
*The time required to process the recipe. 1 is equal to 0.8 seconds of processing time. Every Speed Upgrade in the machine will cut this processing time in half.*

!!! info inline end
	If `input_fluid` is present and has property `FluidAttributes#isGaseous()` set, the Refinery will require the Gas Upgrade, and FE/t cost will be increased by 2.5x.

- `input_fluid`: FluidStack, *optional*  
*A fluid required to process the recipe.*
- `input_item`: Ingredient, *optional*  
*An item required to process the recipe.*
- `output_item`: ItemStack, *optional*  
*A resulting item from the recipe.*
- `output_fluid_a`: FluidStack, *optional*  
*A resulting fluid from the recipe.*
- `output_fluid_b`: FluidStack, *optional*  
*Another resulting fluid from the recipe.*

!!! warning
    At least one `input` and one `output` must be set, or the recipe will fail to load.

??? warning "1.18.2+ Update Warning"
	If you created recipes prior to 1.18.2-1.4 and are upgrading, there are a number of changes with the format that will result in the recipe not working as intended:

	- The `output_fluid_a` field may **no longer** be entered as `output_fluid`. You must use the long-form version of the name.
	- The `upgrade_preserve_chance` field for inputs is **no longer utilized.** Both inputs and outputs now use `upgrade_multiply_chance`, and just serves to simplify the recipe creation process. This tag works identically to `upgrade_preserve_chance` in prior versions.
	- You no longer need `output_fluid_a` to be present in order to load a recipe with `output_fluid_b` set, if you wish to only set `output_fluid_b`.

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
		"upgrade_multiply_chance": 0.15
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