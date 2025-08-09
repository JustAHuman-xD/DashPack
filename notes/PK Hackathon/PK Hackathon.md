Deadline: 08/18/2025

Stuff I've realized I should PR into PK from working on this project:
- Non-combo ability whose cooldown is displayed in the same way a combo is
- Make `CoreAbility#getAbility(Class)` use type variable
- When registering plugin abilities (`CoreAbility#registerPluginAbilities`), if its an addon ability, load it like one
- Make a set velocity method that doesnt restrict the magnitude of the velocity
- Right click aint valid for combos
- Combo help for addon combos that return null for ability info doesnt work
- BendingPlayer#addCooldown shorthand for ability name that gets the core ability instance
- Make RaiseEarthWall#getDegreeRoundedVector public / move to utility class
- Add passives to /pk help <ability>
- Ripple needs to reinitialize locations on attribute recalculation