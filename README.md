Ember
=====

The Ember game engine. Intended to run a fairly open platformer/open world game on the PC (with XInput gamepad, PS3 controller, or generic USB) or Ouya (if market permits), at a solid 60 or 50 fps.

Intended feature set:
> Fun!
> No loading screens, stream from small files and over narrow pipes where necessary
> 3 render modes - High-end PC (deferred?), Ouya/Mid-range PC, low-range PC
> Antialiasing, alpha blending via second pass, LoD, water, pre-baked or dynamic lighting, multilights, particle effects
> Native Bullet on Android for speed, JBullet on PC
> Intelligent streaming of resources

Levels
> Jungle
> Caves

TODO
> Artist/scripting tools + in level objects (trigger area, boundary area, spawner, memory object, script object)
> Level editor/importer, prefabs. Tie to Maya or blender, or make own?
> Test level
> Lighting
> Forward renderer mode
> Culling/LoD
> Particles
> AI
> Streaming/multiple regions
> Animation
> Post-processor - compact materials, remove strings, possibly compute visibility/leaks, gen envmaps/lod? Do this and shadows clientside for bandwidth reduction?
> Accounts, achievements, etc
> Camera autocontrol, inc hints in env

WIP, workable as-is
> Audio system - needs different formats
> JBullet integration - needs performance tweaks esp collision resolution, also needs combat/collision/footstep callbacks
> Renderer

LOW PRI
> PS3/generic


Completed feature set
> XInput wrapper for xbox gamepads
