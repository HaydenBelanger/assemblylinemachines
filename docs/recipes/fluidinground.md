# Fluid-in-Ground Recipes

The Fluid-in-Ground recipes define which fluids can generate as reservoirs in world chunks, detectable with the Mystium Dowsing Rod and collectible with the Pump.

## Recipe Specification

Recipe namespace: `assemblylinemachines:fluid_in_ground`

- `fluid`: Namespace, required  
*The namespace for the fluid that should be generated.*
- `chance`: Positive Integer between 0-100, required  
*The chance a chunk interacted with by the Mystium Dowsing Rod will contain this fluid.*
- `min`: Positive Integer, required  
*Minimum amount of fluid to store in the chunk, in increments of 10,000 mB.*
- `max`: Positive Integer, required  
*Maximum amount of fluid to store in the chunk, in increments of 10,000 mB.*
- `criteria_set`: String, required  
*The required criteria that must be satisfied when attempting to generate this fluid.*

??? note "Criteria Sets"
    The options for `criteria_set` are as follows:  
    `OVERWORLD_ANY` will allow equal generation in any Overworld biome.  
    `OVERWORLD_PREFHOT` will generate in any biome in the Overworld, but is more likely to occur in hot biomes.  
    `OVERWORLD_PREFCOLD` will generate in any biome in the Overworld, but is more likely to occur in cold biomes.  
    `OVERWORLD_ONLYHOT` will generate in hot Overworld biomes.  
    `OVERWORLD_ONLYCOLD` will generate in cold Overworld biomes.  
    `NETHER` will generate in the Nether.  
    `END` will generate in The End.  
    In relation to the preferred hot and cold sets, both the chance to generate and the actual amount that generates in the chunk will be halved when not in the preferred climate.

## Examples

Below is an example of a Fluid-in-Ground recipe. The generator, when a Mystium Dowsing Rod is used on an undowsed chunk, will place Lava in majority hot climate chunks in the Overworld, 12% of the time, between 2,500,000 mB and 15,000,000 mB.

``` json
{
	"type": "assemblylinemachines:fluid_in_ground",
	"fluid": "minecraft:lava",
	"chance": 12,
	"min": 250,
	"max": 1500,
	"criteria_set": "OVERWORLD_PREFHOT"
}
```