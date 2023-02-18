package me.txmc.gradlepluginbase.game.exception;

public class PlayerAlreadyInGameException extends Exception {

    public PlayerAlreadyInGameException(String errorMessage) {
        super(errorMessage);
    }
}
