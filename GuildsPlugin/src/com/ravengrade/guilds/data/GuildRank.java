package com.ravengrade.guilds.data;

public enum GuildRank {
    LEADER,
    OFFICER,
    VETERAN,
    MEMBER,
    RECRUIT;

    public boolean canInvite() {
        return this == LEADER || this == OFFICER || this == VETERAN;
    }

    public boolean canKick() {
        return this == LEADER || this == OFFICER;
    }

    public boolean canPromoteDemote() {
        return this == LEADER;
    }

    public boolean canClaim() {
        return this == LEADER || this == OFFICER;
    }

    public boolean canSetHome() {
        return this == LEADER || this == OFFICER;
    }
}
