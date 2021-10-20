# Experience Mill Recipes

The Experience Mill recipes are available for Book Mode enchanting while inside of the Experience Mill. All are to create a predefined Enchanted Book from a Book, any item, and Liquid Experience.

## Recipe Specification

Recipe namespace: `assemblylinemachines:enchantment_book`

- `input`: Ingredient, required  
*The item to be combined with the book to produce an enchantment.*
- `amount`: Positive Integer, required  
*The count of the `input` required for every 1 level of the enchantment.*
- `enchantment`: Namespace, required  
*The enchantment that will be applied to the Book.*

??? note "Enchantment Level Calculations"
    Depending on the number of Exp. Mill Level Upgrades there are inside of the machine, the level of the enchantment applied to the book will be directly impacted.  
    Running the recipe with **0 upgrades** will result in a Level 1 enchantment every time.  
    Running the recipe with **1 upgrade** will result in an enchantment one-third of the maximum level available for that enchantment, rounded up to the next nearest level. For example, `minecraft:sharpness` would produce a Sharpness II book, but `minecraft:fortune` would still only produce Fortune I.  
    Running the recipe with **2 upgrades** will result in an enchantment two-thirds of the maximum level available for that enchantment, rounded up to the next nearest level.  
    Running the recipe with **3 upgrades** will result in the maximum level enchantment available.


- `cost`: Positive Integer, required  
*The base 'cost' to apply the enchantment to the Book.*

??? note "Cost Calculation"
    The `cost` is used both to determine the length of the recipe as well as the required Liquid Experience to perform the recipe. 1 `cost` is equivalent to 0.5 seconds and 1 mB of Liquid Experience.  
    The actual result to the processing time and mB usage, in practice, is increased exponentially depending on a number of factors. If Speed Upgrades are present, the processing time is decreased by 25% and the mB usage is increased by 10% for each upgrade.  
    Finally, the processing time and mB usage is multiplied by the level of the enchantment being produced (See Enchantment Level Calculations above) to get the final resulting true cost to the player.

## Example

Below is an example of an Experience Mill recipe. The Experience Mill will use 1 Emerald to create an Enchanted Book containing Fortune I.

``` json
{
	"type": "assemblylinemachines:enchantment_book",
	"input":{
		"item": "minecraft:emerald"
	},
	"amount": 1,
	"enchantment": "minecraft:fortune",
	"cost": 125
}
```