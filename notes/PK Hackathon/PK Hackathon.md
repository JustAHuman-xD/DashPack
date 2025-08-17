Deadline: 08/18/2025

Stuff I've realized I should PR into PK from working on this project:
- New ability type for abilities that are not passives but are not actively bound abilities and have the cooldowns displayed how combos are
- Make `CoreAbility#getAbility(Class)` use type variable
- When registering plugin abilities (`CoreAbility#registerPluginAbilities`), if its an addon ability, load it like one
- Make a set velocity method that doesnt restrict the magnitude of the velocity
- Right click aint valid for combos
- Combo help for addon combos that return null for ability info doesnt work
- BendingPlayer#addCooldown shorthand for ability name that gets the core ability instance
- Make RaiseEarthWall#getDegreeRoundedVector public / move to utility class
- Add passives to /pk help <ability>
- Ripple needs to reinitialize locations on attribute recalculation
- ComboManager method for adding ability information & checking for a combo
- MovementHandlers can override each other
- Combo priority system & context aware validation
- WaterAbility#isWaterbendable NPE with null block (how is it even being hit, occured with IceSpike)
- WaterArms using only grapple uses go into negatives
- Make the max speed of waterspout & airspout configurable
- No way to detect shockwave started from a fall (never calls start), also means you can't affect its attributes directly