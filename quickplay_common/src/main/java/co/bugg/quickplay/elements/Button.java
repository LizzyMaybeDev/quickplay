package co.bugg.quickplay.elements;

import co.bugg.quickplay.Quickplay;
import co.bugg.quickplay.util.Location;

import java.util.Arrays;
import java.util.regex.Pattern;

public class Button extends Element {
    public final String[] availableOn;
    public final String[] actionKeys;
    public final String imageURL;
    public final String translationKey;
    public final boolean visible;
    public final boolean adminOnly;
    public final Location hypixelLocrawRegex;
    public final String hypixelRankRegex;
    public final String hypixelPackageRankRegex;
    public final boolean hypixelBuildTeamOnly;
    public final boolean hypixelBuildTeamAdminOnly;
    public final boolean visibleInPartyMode;
    public final String partyModeScopeTranslationKey;

    public Button(final String key, final String[] availableOn, final String[] actionKeys, final String imageURL,
                  final String translationKey, final boolean visible, final boolean adminOnly,
                  final Location hypixelLocrawRegex, final String hypixelRankRegex, final String hypixelPackageRankRegex,
                  final boolean hypixelBuildTeamOnly, final boolean hypixelBuildTeamAdminOnly, final boolean visibleInPartyMode,
                  final String partyModeScopeTranslationKey) {
        super(key, 2);
        this.availableOn = availableOn;
        this.actionKeys = actionKeys;
        this.imageURL = imageURL;
        this.translationKey = translationKey;
        this.visible = visible;
        this.adminOnly = adminOnly;
        this.hypixelLocrawRegex = hypixelLocrawRegex;
        this.hypixelRankRegex = hypixelRankRegex;
        this.hypixelPackageRankRegex = hypixelPackageRankRegex;
        this.hypixelBuildTeamOnly = hypixelBuildTeamOnly;
        this.hypixelBuildTeamAdminOnly = hypixelBuildTeamAdminOnly;
        this.visibleInPartyMode = visibleInPartyMode;
        this.partyModeScopeTranslationKey = partyModeScopeTranslationKey;
    }

    /**
     * Verify that this button passes specifically checks against the user's rank, and can be displayed to users with
     * the current rank. Doesn't check Hypixel rank requirements if the user isn't currently on Hypixel.
     * @return true if the users rank passes all the checks, false otherwise.
     */
    public boolean passesRankChecks() {
        // admin-only actions require admin permission.
        if(this.adminOnly && !Quickplay.INSTANCE.isAdminClient) {
            return false;
        }
        if(Quickplay.INSTANCE.isOnHypixel()) {
            // Hypixel build team-only requires that the user is a build team member.
            if(this.hypixelBuildTeamOnly && !Quickplay.INSTANCE.isHypixelBuildTeamMember) {
                return false;
            }
            // Hypixel build team admin-only requires that the user is a build team admin.
            if(this.hypixelBuildTeamAdminOnly && !Quickplay.INSTANCE.isHypixelBuildTeamAdmin) {
                return false;
            }

            // If there is a regular expression against Hypixel rank (and the user's rank is known), make sure it matches.
            if(this.hypixelRankRegex != null && Quickplay.INSTANCE.hypixelRank != null &&
                    !Pattern.compile(this.hypixelRankRegex).matcher(Quickplay.INSTANCE.hypixelRank).find()) {
                return false;
            }
            // If there is a regular expression against Hypixel package rank (and the user's package rank is known),
            // make sure it matches.
            if(this.hypixelPackageRankRegex != null && Quickplay.INSTANCE.hypixelPackageRank != null &&
                    !Pattern.compile(this.hypixelPackageRankRegex).matcher(Quickplay.INSTANCE.hypixelPackageRank).find()) {
                return false;
            }
        }
        return true;
    }

    /**
     * Verify that this button passes checks for what Minecraft server the user is currently on.
     * @return true if this button is available on the user's current server, false otherwise.
     */
    public boolean passesServerCheck() {
        // If this is only available on some servers, but the client isn't on any known servers, this never passes.
        if(Quickplay.INSTANCE.currentServers == null && this.availableOn.length > 0) {
            return false;
        }
        // Actions must be available on the current server. No availableOn values = available on all servers
        if(this.availableOn != null && this.availableOn.length > 0) {
            synchronized (Quickplay.INSTANCE.elementController.lock) {
                if (Arrays.stream(this.availableOn).noneMatch(str -> Quickplay.INSTANCE.currentServers.contains(str))) {
                    return false;
                }
            }
        }
        return true;
    }

    /**
     * Verify that this button passes all checks and can be displayed to/used by the user at the current moment.
     * @return true if all checks pass, false otherwise.
     */
    public boolean passesPermissionChecks() {
        // non-"visible" actions always fail permission checks.
        if(!this.visible) {
            return false;
        }

        if(!this.passesRankChecks()) {
            return false;
        }

        // Check to make sure all the location-specific requirements on Hypixel match.
        // Only perform these checks if the user is currently on Hypixel and the regular expression object to verify
        // against is not null.
        if(this.hypixelLocrawRegex != null && Quickplay.INSTANCE.isOnHypixel() &&
                Quickplay.INSTANCE.hypixelInstanceWatcher != null &&
                Quickplay.INSTANCE.hypixelInstanceWatcher.getCurrentLocation() != null) {
            // If there is a regular expression for the "server" field, the current "server" must match.
            if(this.hypixelLocrawRegex.server != null &&
                    !Pattern.compile(this.hypixelLocrawRegex.server)
                            .matcher(Quickplay.INSTANCE.hypixelInstanceWatcher.getCurrentLocation().server)
                            .find()) {
                return false;
            }
            // If there is a regular expression for the "map" field, the current "map" must match.
            if(this.hypixelLocrawRegex.map != null &&
                    !Pattern.compile(this.hypixelLocrawRegex.map)
                            .matcher(Quickplay.INSTANCE.hypixelInstanceWatcher.getCurrentLocation().map)
                            .find()) {
                return false;
            }
            // If there is a regular expression for the "mode" field, the current "mode" must match.
            if(this.hypixelLocrawRegex.mode != null &&
                    !Pattern.compile(this.hypixelLocrawRegex.mode)
                            .matcher(Quickplay.INSTANCE.hypixelInstanceWatcher.getCurrentLocation().mode)
                            .find()) {
                return false;
            }
            // If there is a regular expression for the "gametype" field, the current "gametype" must match.
            if(this.hypixelLocrawRegex.gametype != null &&
                    !Pattern.compile(this.hypixelLocrawRegex.gametype)
                            .matcher(Quickplay.INSTANCE.hypixelInstanceWatcher.getCurrentLocation().gametype)
                            .find()) {
                return false;
            }
        }

        return this.passesServerCheck();
    }

    public void run() {
        if(this.actionKeys == null) {
            Quickplay.LOGGER.warning("Action keys are null for button " + this.key);
            return;
        }
        for (String actionKey : this.actionKeys) {
            if (actionKey == null || actionKey.length() <= 0) {
                continue;
            }
            AliasedAction aliasedAction = Quickplay.INSTANCE.elementController.getAliasedAction(actionKey);
            if (aliasedAction == null) {
                Quickplay.LOGGER.warning("Aliased action " + actionKey + " is not found.");
                continue;
            }
            if (!aliasedAction.passesPermissionChecks()) {
                Quickplay.LOGGER.warning("Aliased action " + actionKey + " does not pass permission checks.");
                continue;
            }
            if (aliasedAction.action != null) {
                aliasedAction.action.run();
            }
        }
    }

}
