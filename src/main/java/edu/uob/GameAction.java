package edu.uob;

import java.util.List;

public class GameAction {
    private final List<String> triggers;
    private final List<String> subjects;
    private final List<String> consumed;
    private final List<String> produced;
    private final String narration;


    public GameAction(List<String> triggers, List<String> subjects, List<String> consumed,
                      List<String> produced, String narration) {
        this.triggers = triggers;
        this.subjects = subjects;
        this.consumed = consumed;
        this.produced = produced;
        this.narration = narration;
    }

    public List<String> getTriggers() {
        return triggers;
    }

    public List<String> getSubjects() {
        return subjects;
    }

    public List<String> getConsumed() {
        return consumed;
    }

    public List<String> getProduced() {
        return produced;
    }

    public String getNarration() {
        return narration;
    }

}

