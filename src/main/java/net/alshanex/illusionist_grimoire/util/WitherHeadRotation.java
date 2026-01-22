package net.alshanex.illusionist_grimoire.util;

public interface WitherHeadRotation {
    float[] illusionist_grimoire$getXRotHeadsArray();
    float[] illusionist_grimoire$getYRotHeadsArray();
    float[] illusionist_grimoire$getXRotOHeadsArray();
    float[] illusionist_grimoire$getYRotOHeadsArray();

    // Helper methods
    default float illusionist_grimoire$getXRotHeads(int index) {
        return illusionist_grimoire$getXRotHeadsArray()[index];
    }

    default void illusionist_grimoire$setXRotHeads(int index, float value) {
        illusionist_grimoire$getXRotHeadsArray()[index] = value;
    }

    default float illusionist_grimoire$getYRotHeads(int index) {
        return illusionist_grimoire$getYRotHeadsArray()[index];
    }

    default void illusionist_grimoire$setYRotHeads(int index, float value) {
        illusionist_grimoire$getYRotHeadsArray()[index] = value;
    }

    default float illusionist_grimoire$getXRotOHeads(int index) {
        return illusionist_grimoire$getXRotOHeadsArray()[index];
    }

    default void illusionist_grimoire$setXRotOHeads(int index, float value) {
        illusionist_grimoire$getXRotOHeadsArray()[index] = value;
    }

    default float illusionist_grimoire$getYRotOHeads(int index) {
        return illusionist_grimoire$getYRotOHeadsArray()[index];
    }

    default void illusionist_grimoire$setYRotOHeads(int index, float value) {
        illusionist_grimoire$getYRotOHeadsArray()[index] = value;
    }
}
