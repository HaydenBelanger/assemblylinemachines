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

There is a large number of other Item Tags specifically used for only one or two niche recipes. Therefore, they are not listed here, but can be viewed either by running the datagen for Assembly Line Machines, or you can also get an idea of what is added at [this GitHub page.](https://github.com/HaydenBelanger/assemblylinemachines/blob/bf25fc021755cc73774dda4610978f772b97bde5/src/main/java/me/haydenb/assemblylinemachines/registry/datagen/TagMaster.java#L59-L89) Note that this link is accurate as of commit `bf25fc0`, uploaded on January 6, 2022.