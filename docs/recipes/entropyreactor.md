# Entropy Reactor Waste Recipes

The Entropy Reactor Waste recipes are available for the output from the Entropy Reactor, every time it finishes an operation.

## Recipe Specification

Recipe namespace: `assemblylinemachines:entropy_reactor`

- `output`: ItemStack, required  
*The waste produced by the Reactor after every successful operation.*
- `varietyReqd`: Decimal between 0-1, required  
*The minimum Variety required in the Reactor to produce this output, between 0% and 100%.*
- `odds`: Decimal between 0-1, required  
*The chance to generate one unit of this waste upon a successful operation, between 0% and 100%.*
- `max`: Positive Integer, required  
*The maximum amount of this waste the Reactor will attempt to generate after every successful generation of this waste.*

??? note "How the Waste is Picked"
    The Reactor will only generate one type of waste every successful operation, and the priority for generation is in decending order based on `varietyReqd`. At which point, additional 'passes' are attempted to generate up to the `max` value. For example, in the base mod, `assemblylinemachines:strange_matter` requires 80% Variety at an 80% chance, and has the highest `varietyReqd`. As long as the Variety is satisfied, the Reactor will produce this item 80% of the time. The remainder of the time, the processing will continue to the next-highest `varietyReqd` output.

## Example

Below is an example of an Entropy Reactor Waste recipe. The reactor will generate up to 3 Strange Matter 80% of the time, as long as the Variety rating is at 80% or higher.

``` json
{
	"type": "assemblylinemachines:entropy_reactor",
	"output":{
		"item": "assemblylinemachines:strange_matter"
	},
	"varietyReqd": 0.8,
	"odds": 0.8,
	"max": 3
}
```