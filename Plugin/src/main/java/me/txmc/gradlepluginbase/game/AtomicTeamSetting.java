package me.txmc.gradlepluginbase.game;

import lombok.Getter;
import lombok.Setter;

public class AtomicTeamSetting {

    @Getter
    @Setter
    private MiniGame.TeamSetting value;

    public AtomicTeamSetting(MiniGame.TeamSetting initialValue) {
        this.value = initialValue;
    }
}
