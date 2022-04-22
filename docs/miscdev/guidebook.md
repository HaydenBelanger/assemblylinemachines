# Guide Book Capability

As part of the default configuration, when a new Player joins the server (or a single-player world), they will receive a copy of the mod's Patchouli guidebook, Assembly Lines & You, as long as Patchouli is installed. Assembly Line Machines exposes a custom capability for this system which allows rudimentary management of the distribution of the guide book on player join.

!!! error "1.18.2+ Feature"
    A capability has only been exposed as of version 1.18.2-1.4. Prior to this version, the giving of the guide is attached directly to the Player and not through a capability.

## Obtaining the Capability

The capability can be obtained by calling `Entity#getCapability`, but the capability will only return as present when requested on a Player, as it is not attached to other types of entities. The instance of the capability can be obtained by either accessing it via `CapabilityManager.get`, casting to `Capability<IBookDistroCapability>`, or by accessing the stored singleton at `CapabilityBooks.BOOK_DISTRO_CAPABILITY`. Sides are not used, so the side within obtaining the capability can be `null`. Finally, cast the `LazyOptional<IBookDistroCapability>` to `IBookDistroCapability` using your preferred method, and you have successfully obtained an instance of the Guide Book capability!

## Methods

There is only a couple of methods available through the capability.

`IBookDistroCapability#giveBook`:  
If the Player has not received the book, call this on the Server-side to give the Player a copy.

`IBookDistroCapability#bookReceived`:  
Returns a `boolean` with whether or not the Player has received a copy of the book.