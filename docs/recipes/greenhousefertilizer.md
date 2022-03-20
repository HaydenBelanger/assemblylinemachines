# Greenhouse Fertilizer Recipes

The Greenhouse Fertilizer recipes are for types of Fertilizer you want to be valid within the Greenhouse. The base mod allows all types of Fertilizer in addition to Bone Meal to work within the Greenhouse.

!!! error "1.18.2+ Feature"
    This recipe type was made available in Assembly Line Machines 1.18.2-1.4, in conjunction with the addition of the Greenhouse.

## Recipe Specification

Recipe namespace: `assemblylinemachines:greenhouse_fertilizer`.

- `fertilizer`: Ingredient, required  
*The type of item which fulfills the requirements of the recipe.*  
- `outputMultiplication`: Positive Integer, *optional*  
*The amount the output of the Greenhouse is multiplied by, after factoring in all [chance-based-increases](../recipes/greenhousecrops.md#recipe-specification) from the Greenhouse recipe in question. Defaults to not multiply.*  
- `usesPerItem`: Positive Integer, *optional*  
*The amount of operations that one unit of this fertilizer will last. Defaults to once.*

## Examples

This is an example of a Fertilizer recipe. This will accept Enhanced Fertilizer, can be used 20 times per single unit of Enhanced Fertilizer, and will multiply the output by 2x.

``` json
{
	"type": "assemblylinemachines:greenhouse_fertilizer",
	"fertilizer":{
		"item": "assemblylinemachines:enhanced_fertilizer"
	},
	"usesPerItem": 20,
	"outputMultiplication": 2
}
```