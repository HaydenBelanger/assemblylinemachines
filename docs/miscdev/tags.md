# Tags

Assembly Line Machines has a couple of tags that are important as datapack developers, that change function or add additional function to some items or blocks.

### Gas Flammable

Block Tag namespace: `assemblylinemachines:world/gas_flammable`

Blocks in this tag, when placed in the world next to a pool of Diesel or Gasoline, will cause an explosion.

### Mystium Axe Mineable

Block Tag namespace: `assemblylinemachines:world/mystium_axe_mineable`

Blocks in this tag can be mined treefeller-style (breaks all connected blocks in one action) when using Mystium Axe's Secondary Ability.

### Naphtha Fireproof

Block Tag namespace: `assemblylinemachines:world/naphtha_fireproof`

Naphtha Fire, spread from pools of Naphtha, can spread onto any block and will never burn out, unless that block is contained within this tag.

### Needs Mystium Tool

Block Tag namespace: `assemblylinemachines:needs_mystium_tool`

??? error "1.17+ Feature"
    This tag was made available in 1.17.1-1.3. Prior versions did not have a tag for Mystium Tools exposed, or Minecraft did not have the JSON mining system implemented.

Blocks in this tag will require a Mystium Tool or equivalent to be harvested successfully and to drop the resulting drops.

### Fluid Tags

Fluid tags are automatically generated for all fluids within the mod with the same name as the fluid, with the exception of gaseous fluids. For example, the tag for Liquid Experience will be placed at `assemblylinemachines:liquid_experience`.

### Other

*All tags below are niche and used primarily for specific recipes only.*

| Tag Name (In `assemblylinemachines:`) &nbsp; &nbsp; &nbsp; &nbsp; &nbsp; | Type | Description |
| -----------: | ----------- | ----------- |
| `chaosbark_logs` | Block & Item | For types of Chaosbark Logs. |
| `crafting/carbon_large` | Item | Allowed items in 900mB Liquid Carbon recipe. |
| `crafting/carbon_small` | Item | Allowed items in 100mB Liquid Carbon recipe. |
| `crafting/entropy_reactor_outputs` | Item | Used in some recipes as 'Matter' outputs from E. Reactor. |
| `crafting/hammers` | Item | Items allowed to create plates as the hammer. |
| `crafting/organics` | Item | Items that can be composted to make Sludge. |
| `crafting/prism_roses` | Item | Items that can make Prismatic Dust. |
| `crafting/sawdust` | Item | Items valid to make Fertilizer. |
| `crafting/gears/precious` | Item | 'Precious Metal' gears used in some recipes. |
| `crafting/gears/industrial` | Item | 'Industrial' gears used in some recipes. |
| `crafting/gears/all` | Item | A combination of all 'Precious' and 'Industrial' gears. |