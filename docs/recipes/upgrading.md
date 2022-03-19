# Upgrading Recipes

The Upgrading recipes are for processes you want to happen when you use an Upgrade Kit in-world on a specified block. Using this process, you are able to specify slot transformations which relocate stored items into specific new slots.

!!! error "1.18.2+ Feature"
    This recipe type was made available in Assembly Line Machines 1.18.2-1.4, as a new mechanic.

## Recipe Specification

Recipe namespace: `assemblylinemachines:upgrade_kit`.

- `input_block`: Ingredient, required  
*The input block of the recipe.*  
- `upgrade_kit`: String, required  
*The type of Upgrade Kit used to upgrade the machine to the next level. Accepts `ELECTRIC` or `MKII`.*  
- `output_block`: Namespace, required  
*The resulting block that is created during the operation.*  
- `slot_copy`: Key-Value Set, *optional*  
*If set, this will specify slots from the origin machine and which equivalent slot should the stack contained be placed in within the destination machine. If a slot number is not set, or if `slot_copy` as a whole is not set, the Upgrade Kit will instead eject the item. For an example of formatting, view below.*

## Example

This is an example of an Upgrading recipe. In this example, this allows the upgrade of Simple Grinder to Electric Grinder using the Electric Upgrade Kit. Slot 1 is copied, as it is the input slot in both machines. Since the Electric Grinder does not need a Blade to operate, there is no slot to move the Blade from slot 0 in the Simple Grinder to, so the Blade is ejected when the upgrade kit is used.

``` json
{
	"type": "assemblylinemachines:upgrade_kit",
	"input_block":{
		"item": "assemblylinemachines:simple_grinder"
	},
	"upgrade_kit": "ELECTRIC",
	"output_block": "assemblylinemachines:electric_grinder",
	"slot_copy":{
		"1": 1
	}
}
```