# Commands

Assembly Line Machines adds a number of OP-only development commands that may aid in pack- or level-making, debugging, or more. All commands fall under `/assemblylinemachines`, with sub-commands determining the function:

!!!error "1.18.2+ Feature"
    Commands were added in 1.18.2-1.4.

???info "Command Notation"
    A quick note about command notation, if you are unfamiliar:  
    No braces around an argument indicate a required argument. Therein, there are no alternative arguments for the command in that position.  
    Chevrons `<>` indicate a required argument. The command must have this argument in order to be executed.  
    Brackets `[]` indicate an optional argument. The command will function without, but may perform differently with the exclusion of the argument.  

`/assemblylinemachines guide [targets]`:  
Give a copy of Assembly Lines & You to specified players.  
- `[targets]`: A group of players to give the book to. Leaving this unset will give a copy only to the sender.

`/assemblylinemachines makecreative <pos>`:  
Change the block at a position into a creative version, as long as it can become creative. For example, this can include a Battery Cell, a Tank, or a BSU.  
- `<pos>`: The block location you wish to perform this operation on.

`/assemblylinemachines chunkfluid set <fluid> [amount] [pos]`:  
Change or add a new [Fluid Reservoir](../miscdev/fluidreservoir.md) to a chunk.  
- `<fluid>`: The type of fluid to add to the chunk. Must not be a flowing fluid.  
- `[amount]`: The amount of fluid to add to a chunk. If unset, defaults to 1,000,000 mB.  
- `[pos]`: The block location of the chunk you wish to add this fluid to. Defaults to the current chunk of the sender.

`/assemblylinemachines chunkfluid get [pos]`:  
Checks the fluid contained within a Fluid Reservoir.  
- `[pos]`: The block location of the chunk you wish to check. Defaults to the current chunk of the sender.

`/assemblylinemachines chunkfluid remove [pos]`:  
Removes any set Fluid Reservoir from a chunk.  
- `[pos]`: The block location of the chunk you wish to remove a fluid from. Defaults to the current chunk of the sender.