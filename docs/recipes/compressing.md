# Compressing Recipes

The Compressing recipes are performed in the Pneumatic Compressor. Typically, this is used to create Rods, Plates, Gears, and Storage Blocks using Molds, but can be used for other purposes, too.

!!! error "1.18.2+ Feature"
    This recipe type was made available in Assembly Line Machines 1.18.2-1.4, in conjunction with the addition of the Pneumatic Compressor.

## Recipe Specification

Recipe namespace: `assemblylinemachines:pneumatic`

- `input`: Ingredient With Count, required  
*The item in the recipe.*  
- `output`: ItemStack With Tag, required  
*The result from performing the recipe.*  
- `mold`: String, *optional*  
*The Mold used to create this. Allows `ROD`, `GEAR`, `PLATE`, and `NONE` as the default.*  
- `moldItem`: Namespace, *optional*  
*The item used as the mold. This overrides `mold` when it is set, and can be any item.*  
- `time`: Positive Integer, required  
*The amount of cycles required to complete the recipe. Every Speed Upgrade in the machine will cut this in half.*

## Examples

These are examples of recipes for use in the Pneumatic Compressor.

The first recipe takes 4 Pure Steel Plates and converts it to 3 Pure Steel Gears, provided the Gear Mold is present.

``` json
{
  "type": "assemblylinemachines:pneumatic",
  "input": {
    "tag": "forge:plates/pure_steel",
    "count": 4
  },
  "output": {
    "tag": "forge:gears/pure_steel",
    "count": 3
  },
  "time": 9,
  "mold": "gear"
}
```

Second, this recipe will convert two Pure Steel Ingots into a Plate Mold, as long as a Pure Steel Plate is provided as a Mold.

``` json
{
  "type": "assemblylinemachines:pneumatic",
  "input": {
    "tag": "forge:ingots/pure_steel",
    "count": 2
  },
  "output": {
    "item": "assemblylinemachines:plate_mold"
  },
  "moldItem": "assemblylinemachines:pure_steel_plate",
  "time": 15
}
```