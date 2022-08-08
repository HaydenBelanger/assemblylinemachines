# Tool Configuration

The tool configuration is specified in `assemblylinemachines-experimental.toml` config located in the config directory. This allows more control over stats and features of various tools.

!!! error "Removed Feature"
    This feature was exclusively available in versions released for 1.18.2 including and after 1.18.2-1.4.3, and was removed in 1.19-1.4.5 due to a change with Forge not allowing configs to be loaded prior to the preinitialization phase.

    This feature is extremely volatile and may stop working as expected. Client-server config mismatch will cause all clients to be unable to join. It's recommended to only change these options for a modpack default config, allowing all clients to share the same values.
    
When first opening the configuration file, the array for `toolStats` will be empty. This is because there are no overrides. This configuration section acts on overrides, meaning that if unspecified, a tool will fall back to the default value for that stat.

## Declaration

To specify a tool override, you will need to provide sub-config sections in the `toolStats` array, containing a series of numbers, `data`, as well as the tool type that modification applies to, `type`. Additionally, every different type has a different expected length for data, depending on the stats relevant to that tool. If you provide too much or too few data-points, the section will be removed when the game is started as invalid. Additionally, some data-points prefer or allow a decimal whilst some do not.

## Stat Types

The stat types and abbreviations available are as follows:

- `ATK`: Attack modifier; The amount added to the damage dealt in half-hearts. This is spread across all tools with additional damage given to weapon-class tools (Sword and Axe).

- `SPD`: Harvest speed; The speed at which a block is broken. Generally, higher is better. View [breaking speed information](https://minecraft.fandom.com/wiki/Breaking#Speed) for a baseline on the harvest speed of vanilla tools.

- `ENC`: Enchantability; The relative efficacy of using this tool at an enchantment table. Higher is better, [Enchanting Table information](https://minecraft.fandom.com/wiki/Enchanting#Enchanting_table) is a good resource on this system and baseline information.

- `DURA`: Durability; The general use-life of tools and armor. Both are controlled here. For tools, the durability will be exactly what is entered, while for armor, it is as follows:
    - Helmet: 33% of entered value,  
    - Chestplate: 50% of entered value,  
    - Leggings: 40% of entered value,  
    - Boots: 30% of entered value.

- `KB`: Knockback resistance; This is a decimal value of the percentage-increase given to the player's knockback resistance while wearing the armor. For example, 0.2 would be equal to 20% knockback resistance increase.

- `PROT`: Damage protection; The relative strength of the armor in half-shields. Just like with durability, this is proportionally rounded based on the given value:
    - Helmet: Equal to the entered value,  
    - Chestplate: 150% of entered value,  
    - Leggings: 125% of entered value,  
    - Boots: 80% of entered value.

- `TOUGH`: Toughness; This is additional protection and can be a decimal. A higher value will reduce the amount of damage high-strength attacks will do. [Toughness information](https://minecraft.fandom.com/wiki/Armor#Armor_toughness) can help with baselines here.

- `PWR`: Energy; This is a mod-specific stat setting which controls how much of a secondary charge unit the tool in question can hold. For example, with Crank-Powered Tools, it would change the Cranks stored, and for Mystium Tools, it would alter the FE.

- `SPWR`: Special energy; This will change the maximum secondary charge unit on a special tool within the set. It is used in the following scenarios:
    - On `MYSTIUM`, it will control the energy storage of the Enhanced Mystium Chestplate.

## Stat Grid

Now that you know what you can control with this section, next up is the tools you can modify. Each row represents one tool type below. Stats are set from left-to-right when filling the `data` section, and names of sets are case-sensitive:

| Set Name | #1 | #2 | #3 | #4 | #5 | #6 | #7 | #8 | #9 |
| -----------: | ----------- | ----------- | ----------- | ----------- | ----------- | ----------- | ----------- | ----------- | ----------- |
| `TITANIUM` | ATK | SPD | ENC | DURA | KB | PROT | TOUGH |
| `STEEL` | ATK | SPD | ENC | DURA | KB | PROT | TOUGH |
| `CRANK` | ATK | SPD | ENC | DURA | PWR |
| `MYSTIUM` | ATK | SPD | ENC | DURA | KB | PROT | TOUGH | PWR | SPWR |
| `NOVASTEEL` | ATK | SPD | ENC | DURA | PWR |
| `CRG` | ENC | DURA | KB | PROT | TOUGH |

## Bringing it all Together

Lots of info, so let's combine everything you've learned into a config file. The comments are not necessary when you're doing it, just left for your learning information:

``` toml
#TOML subobjects work with double-square-brackets and then tab indentation.
[[toolStats]]
    #The type is set to TITANIUM for this section, so this will modify Titanium.
    type = "TITANIUM"
    #TITANIUM expects 7 values based on the stat grid:
    #ATK, SPD, ENC, DURA, KB, PROT, and TOUGH.
    #The data array MUST be ordered in this order.
    data = [12, 9, 15, 2750, 0.25, 4, 0.5]

#The amount of toolStats specified is not limited; just make another sub-object.
[[toolStats]]
    #This time, the type is CRG.
    type = "CRG"
    #CRG expects 5 values based on the stat grid:
    #ENC, DURA, KB, PROT, and TOUGH.
    #Since the CRG is just a Helmet, this will only apply to that.
    data = [15, 3, 0.3, 4, 0]
```