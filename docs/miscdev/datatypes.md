# Data Types

Pages describing recipes or tags refer to different data types, depending on the type that the JSON value is looking for. See below for a list of data types.

## Basic Types

| Type Name | Description | JSON Example &nbsp; &nbsp; &nbsp; &nbsp; &nbsp; &nbsp; &nbsp; &nbsp; &nbsp; &nbsp; &nbsp; &nbsp; &nbsp; &nbsp; &nbsp; &nbsp; &nbsp; &nbsp; &nbsp; &nbsp; &nbsp; &nbsp; &nbsp; &nbsp; &nbsp; &nbsp; &nbsp; &nbsp; &nbsp; &nbsp; &nbsp; &nbsp; &nbsp; &nbsp; &nbsp; &nbsp; &nbsp; &nbsp; &nbsp; &nbsp; &nbsp; &nbsp; &nbsp; &nbsp; &nbsp; &nbsp; &nbsp; &nbsp; &nbsp; &nbsp; &nbsp; &nbsp; &nbsp; &nbsp; &nbsp; &nbsp; |
| -----------: | ----------- | ----------- |
| Integer | Any whole number. May be listed with a range to indicate valid numbers. | `"amount": 250` |
| Decimal | Any number including decimals. May be listed with a range to indicate valid numbers. | `"chance": 0.75` |
| Boolean | A value of `true` or `false`. | `"enabled": true` |
| String | Any text value. Description of field may include valid values. | `"text": "ABC"` |
| Namespace | A string that is specifically a Minecraft `ResourceLocation`. Would be the same as when using `/give` or `/setblock`. Typically is used for providing a block or fluid NOT in the context of an input or output. | `"fluid":"minecraft:lava"` |

## Ingredient

The Ingredient type is used primarily for inputs on recipes. It allows the setting of an exact item or tag, and is always a single item.

!!! info inline end
    Tags are the preferred method, as other mods or datapacks would be able to dynamically add equivalent items to your recipe without any additional effort/recipes!

``` json
"input_as_tag":{
	"tag": "forge:dusts/coal"
},

"input_as_exact_item":{
	"item": "assemblylinemachines:ground_coal"
}
```

## ItemStack

The ItemStack type is used primarily for outputs from recipes, and can always contain a count.

### Traditional ItemStack

The Traditional ItemStack uses an exact item for the specified output.

``` json
"output":{
	"item": "assemblylinemachines:titanium_plate",
	"count": 5
}
```

### Tag ItemStack

??? error "1.18.2+ Feature"
	This Data Type was made available in 1.18.2-1.4. Prior versions did not have this option and only supported regular ItemStacks.

The Tag ItemStack is a hybrid of an *Ingredient* and an *ItemStack* used and referenced in certain recipe types which support it. It allows you to specify a tag to pull from for a recipe result as opposed to a static single-item result that one would normally expect with an ItemStack. If the tag has multiple like-type results, set `preferredModid` in `assemblylinemachines-common.toml`. For more information, see comments in the configuration file.

``` json
"output":{
	"tag":"forge:plates/titanium",
	"count": 5
}
```

## FluidStack

The FluidStack type is used primarily for inputs or outputs of liquids for some recipes.

``` json
"input_fluid":{
    "fluid": "minecraft:water",
    "amount": 1000
}
```