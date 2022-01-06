#Generator Fluid Recipes

The Generator Fluid recipes define which fluids may be used in the various Fluid Generators, currently the Geothermal and Combusion Generator, either as a Fuel or as a Coolant, as well as defining the strengths of each.

!!! error "1.18+ Feature"
    This recipe type was made available in Assembly Line Machines 1.18-1.3.5. Prior to this version, Generator Fluids were set through the common configuration file, and was not a recipe.

## Recipe Specification

Recipe namespace: `assemblylinemachines:generator_fluid`

- `fluid`: Namespace, required  
*The namespace for the fluid that should be consumed. The amount will always be 1,000 mB per operation.*  
- `generator`: String, required  
*The type of generator that this is used in. Valid inputs are `GEOTHERMAL`, `COMBUSTION`, or `COOLANT`. If the generator type is `COOLANT`, it will be valid in all generators as a coolant. Otherwise, it will be a valid fuel for one generator only.*  
- `fe`: Positive Integer, *optional*  
*The amount of FE 1,000 mB of fuel should generate with no Speed Upgrades. The average amount per tick varies depending on the upgrades installed as well as the fluid.*  
- `coolantstrength`: Positive Decimal, *optional*  
*The multiplicative value of the coolant on the fuel. The total lasting time of the fuel will be multiplied by this value.*  

!!! warning
    If the value for `generator` is `COOLANT`, `coolantstrength` must be set or the recipe will fail to load, but you do not need to set the `fe` field, as it is not a fuel, but a coolant. Any other `generator` type, and vice versa: `fe` must be set but `coolantstrength` does not.

## Examples

Below are examples of a coolant as well as a fuel.

Diesel, when used in a Combustion Generator, will produce 1,050,000 FE over the lifecycle of the fuel burning.

``` json
{
	"type": "assemblylinemachines:generator_fluid",
	"generator": "COMBUSTION", 
	"fluid":"assemblylinemachines:diesel",
	"fe":1050000
}
```

Water provides a 2x bonus to all fuels when provided as a coolant.

``` json
{
	"type": "assemblylinemachines:generator_fluid",
	"generator": "COOLANT", 
	"fluid":"minecraft:water",
	"coolantstrength":2.0
}
```