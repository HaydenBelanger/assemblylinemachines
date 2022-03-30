# Data Types

Pages describing recipes or tags refer to different data types, depending on the type that the JSON value is looking for. See below for a list of data types.

## Basic Types

| Type Name | Description | JSON Example &nbsp; &nbsp; &nbsp; &nbsp; &nbsp; &nbsp; &nbsp; &nbsp; &nbsp; &nbsp; &nbsp; &nbsp; &nbsp; &nbsp; &nbsp; &nbsp; &nbsp; &nbsp; &nbsp; &nbsp; &nbsp; &nbsp; &nbsp; &nbsp; &nbsp; &nbsp; &nbsp; &nbsp; &nbsp; &nbsp; &nbsp; &nbsp; &nbsp; &nbsp; &nbsp; &nbsp; &nbsp; &nbsp; &nbsp; &nbsp; &nbsp; &nbsp; &nbsp; &nbsp; &nbsp; &nbsp; &nbsp; &nbsp; &nbsp; &nbsp; &nbsp; &nbsp; &nbsp; &nbsp; &nbsp; &nbsp; |
| -----------: | ----------- | ----------- |
| Integer | Any whole number. May be listed with a range to indicate valid numbers. | `"amount": 250` |
| Decimal | Any number including decimals. May be listed with a range to indicate valid numbers, for example "Chance between 0 and 1" or "Chance greater than 0". | `"chance": 0.75` |
| Boolean | A value of `true` or `false`. | `"enabled": true` |
| String | Any text value. Description of field may include valid values. | `"text": "ABC"` |
| Namespace | A string that is specifically a Minecraft `ResourceLocation`. Would be the same as when using `/give` or `/setblock`. Typically is used for providing a block or fluid NOT in the context of an input or output. | `"fluid":"minecraft:lava"` |

## Ingredient

The Ingredient type is used primarily for inputs on recipes. It allows the setting of an exact item or tag, and is always a single item.

!!! info inline end
    Tags are the preferred method, as other mods or datapacks would be able to dynamically add equivalent items to your recipe without any additional effort/recipes!

``` json
"input":{
	"item": "assemblylinemachines:ground_coal"
},
"input":{
	"tag": "forge:dusts/coal"
}
```

## Ingredient With Count

??? error "1.18.2+ Feature"
	This Data Type was made available in 1.18.2-1.4. Prior versions did not have this option and only supported regular Ingredients. Any field that specifies a Count Ingredient requires a traditional Ingredient in versions prior to 1.18.2-1.4.

The Count Ingredient is a special type which allows the addition of the specification of a `count` for an input, to change the amount consumed when a recipe is executed. Just like a traditional Ingredient, it supports tags and items. In any place you see a Count Ingredient, a traditional Ingredient will also work in its place by excluding the additional field.

``` json
"input":{
	"item": "minecraft:lapis_lazuli",
	"count": 15
},
"input":{
	"tag": "forge:gems/lapis",
	"count": 15
}
```

## ItemStack

The ItemStack is used as an output for most recipe types, and can always have a specific count specified. An ItemStack always uses a specific item, and cannot use a tag.

``` json
"output":{
	"item": "assemblylinemachines:pure_titanium_plate",
	"count": 5
}
```

## ItemStack With Tag

??? error "1.18.2+ Feature"
	This Data Type was made available in 1.18.2-1.4. Prior versions did not have this option and only supported regular ItemStacks. Any field that specifies a Tag ItemStack requires a traditional ItemStack in versions prior to 1.18.2-1.4.

The Tag ItemStack is a special type which allows specification of a tag to pull an item from instead of a static item for a recipe result. In a case where a tag has multiple items from different mods, you can specify a preferred Mod ID. For more information, see the configuration file. In any place you see a Tag ItemStack, a traditional ItemStack will also work in its place.

``` json
"output":{
	"tag":"forge:plates/pure_titanium",
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