# Greenhouse Crop Recipes

The Greenhouse Crop recipes are for seeds or other growables you wish to function inside of the Greenhouse. When provided with a Soil and Fertilizer, your crop will grow into a specified output.

!!! error "1.18.2+ Feature"
    This recipe type was made available in Assembly Line Machines 1.18.2-1.4, in conjunction with the addition of The Greenhouse.

## Recipe Specification

Recipe namespace: `assemblylinemachines:greenhouse`.

- `input`: Ingredient, required  
*The input of the recipe, typically a Seed, or Sapling, or other item which can grow.*  
- `output`: ItemStack, required  
*The result of the operation, including the base count which is output. The true count may be modified based on a number of factors.*  
- `time`: Positive Integer, required  
*The amount of cycles per output count, including additional calculated outputs, that is required to process the recipe. For example, `time` of 8 with 9 resulting outputs would take 72 cycles, or 58 seconds with no Speed Upgrades.*  
- `waterPerUnit`: Positive Integer, *optional*  
*The amount of water, in mB, that is consumed per single result item. Defaults to 250mB.*  
- `baseAdditionalChance`: Decimal between 0 and 1, *optional*  
*If set, this is the base chance without factoring in Extra Output Upgrades that the true count will be increased beyond what is set in `output`.*  
- `upgradeAdditionalChance`: Decimal between 0 and 1, *optional*  
*If set, this is the additional chance per Extra Output Upgrade that the true count will be increased beyond what is set in `output`.*  

??? tip "Chances"
    For example, if `baseAdditionalChance` is set at 0.1 (10%), and `upgradeAdditionalChance` is set at 0.2 (20%), that means that with 3 Extra Output upgrades within the machine, the total output increase chance is 70%. Or, put simply:

    `(upgradeAdditionalChance * Number of Upgrades) + baseAdditionalChance = True Chance.`
- `maxAdditional`: Positive Integer, *optional*  
*If set, this is the maximum amount of output that can be added to the base count. For example, if the base count set in `output` is 3 and this is 2, the maximum possible output per operation is 5.*  
- `additionalScaling`: String, *optional*  
*This specifies the calculation algorithm to determine the number of additional outputs beyond what is set in `output`, if the chance requirement is satisfied. Defaults to `PURE_RANDOM` and may be set to `UPGRADE`.*  

??? tip "Output Algorithms"
    As specified, there are two output algorithms which determine the scaling to which the additional output is applied. Note that in both instances, the algorithm to determine output is only triggered if the RNG satisfies the requirements of the Chances (see above).  

    `PURE_RANDOM`: This algorithm, if the required Chance is met, will simply select a random number of additional outputs between 0 and `maxAdditional`.  

    `UPGRADE`: This algorithm will scale outputs according to the number of Extra Output Upgrades that are in the machine, up to `maxAdditional`, with an additional 33% of the maximum per upgrade, up to 100%. This allows for a more predictable additional output curve as long as Chance is met, rounded to the nearest whole number. If Chance is met through `baseAdditionalChance` and there are no upgrades in the machine, the final additional output will always be 1.

- `soil`: String, *optional*  
*The type of soil which is required to process the recipe, with some types having special properties. The soil selected will show visually within the Greenhouse. Only accepts certain values, and defaults to `DIRT`.*  
- `sprout`: String, *optional*  
*The type of sprout attached to the recipe, which sets special properties about the plant and also sets which type of plant is visually rendered in the Greenhouse. Only accepts certain values, and defaults to `SPROUT`.*  

??? info "List of Soils"
    There are a number of soils accepted in the Greenhouse:  

    `DIRT`: Accepts Dirt, Grass Block, Coarse Dirt, or Rooted Dirt.  
    `MYCELIUM`: Accepts Mycelium.  
    `SAND`: Accepts contents of tag `minecraft:sand`.  
    `SOUL_SAND`: Accepts contents of tag `minecraft:soul_fire_base_blocks`.  
    `CORRUPT`: Accepts Corrupt Dirt and Corrupt Grass.  
    `END_STONE`: Accepts End Stone.  

    *Note that `SOUL_SAND`, `CORRUPT`, and `END_STONE` requires the Interdimensional Specialization upgrade to perform operations!*

??? info "List of Sprouts"
    There are a number of sprouts accepted in the Greenhouse, listed are the options and the requirements to plant them:  

    `CACTUS`: Requires sunlight.  
    `CORRUPT_SPROUT`: Requires darkness.  
    `NETHER_SPROUT`: Requires darkness.  
    `SAPLING`: Requires sunlight unless has Soul Sand soil, requires Arborist's Specialization.  
    `MUSHROOM`: Requires darkness.  
    `SPROUT`: Requires sunlight.  
    `SUGAR_CANE`: Requires sunlight.  
    `CHORUS`: Requires darkness.  
    `BRAIN_CACTUS`: Requires darkness.  
    `CHAOSBARK_SAPLING`: Requires darkness, requires Arborist's Specialization.  
    `FLOWER`: Requires sunlight unless has Corrupt soil, requires Florist's Specialization.

## Examples

Listed are three examples of a Greenhouse recipe...

This will multiply Brain Cacti. As it is a `brain_cactus` sprout and `corrupt` soil, it will require darkness as well as an Interdimensional Specialization upgrade to grow it. It will produce between 1 and 4 units, and scales directly with the number of upgrades for additional result chances.

``` json
{
	"type": "assemblylinemachines:greenhouse",
	"input":{
		"item": "assemblylinemachines:brain_cactus"
	},
	"output":{
		"item": "assemblylinemachines:brain_cactus"
	},
	"waterPerUnit": 100,
	"baseAdditionalChance": 0.25,
	"upgradeAdditionalChance": 0.25,
	"maxAdditional": 3,
	"additionalScaling": "upgrade",
	"time": 5,
	"soil": "corrupt",
	"sprout": "brain_cactus"
}
```

This will grow Melons from Melon Seeds. As the sprout and soil values are unset, it acts as a `sprout` sprout and `dirt` soil, will require sunlight, and does not need any special upgrades. It will produce between 1 and 2 units, and is completely random on how many it will produce as long as the chance-RNG requirement is met.

``` json
{
	"type": "assemblylinemachines:greenhouse",
	"input":{
		"item": "minecraft:melon_seeds"
	},
	"output":{
		"item": "minecraft:melon"
	},
	"waterPerUnit": 350,
	"baseAdditionalChance": 0.2,
	"upgradeAdditionalChance": 0.2,
	"maxAdditional": 1,
	"time": 20
}
```

Finally, this will multiply Mandelblooms. As it is a `flower` sprout and `corrupt` soil, it will require darkness and requires both an Interdimensional Specialization and Florist's Specialization upgrade to complete. It will produce between 1 and 3 units, and is completely random much like the Melon recipe. This recipe will never produce more than 1 if there is not Extra Output Upgrades in the machine, as it does not have a `baseAdditionalChance`.

``` json
{
	"type": "assemblylinemachines:greenhouse",
	"input":{
		"item": "assemblylinemachines:mandelbloom"
	},
	"output":{
		"item": "assemblylinemachines:mandelbloom"
	},
	"waterPerUnit": 85,
	"upgradeAdditionalChance": 0.32,
	"maxAdditional": 2,
	"time": 9,
	"soil": "corrupt",
	"sprout": "flower"
}
```